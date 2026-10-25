#!/usr/bin/env python3
"""
Minimal, dependency-free PDF text extractor.

This sandbox has no pdftotext / pypdf and cannot reach PyPI, so the script walks the
PDF objects itself: it inflates FlateDecode streams, applies each font's ToUnicode
CMap when present, and decodes the Tj / TJ / ' / " text operators.

Usage:
    python3 tools/pdf_text.py book.pdf              # print text
    python3 tools/pdf_text.py book.pdf -o out.txt   # write to file
"""

import re
import sys
import zlib


# ---------------------------------------------------------------- pdf objects

def read_objects(data: bytes):
    """Map object number -> object body (bytes between `obj` and `endobj`)."""
    objects = {}
    for match in re.finditer(rb"(\d+)\s+(\d+)\s+obj(.*?)endobj", data, re.S):
        objects[int(match.group(1))] = match.group(3)
    return objects


def stream_of(body: bytes):
    """Return the decoded stream payload of an object body (or None)."""
    match = re.search(rb"stream\r?\n(.*?)\r?\nendstream", body, re.S)
    if not match:
        return None
    raw = match.group(1)
    if b"FlateDecode" in body:
        for attempt in (raw, raw.rstrip(b"\r\n")):
            try:
                return zlib.decompress(attempt)
            except zlib.error:
                continue
        try:
            return zlib.decompressobj().decompress(raw)
        except zlib.error:
            return None
    return raw


# ---------------------------------------------------------------- cmaps

def decode_utf16(hex_bytes: bytes) -> str:
    try:
        return bytes.fromhex(hex_bytes.decode()).decode("utf-16-be")
    except Exception:
        return ""


def parse_cmap(data: bytes):
    """Parse a ToUnicode CMap into {code:int -> str}."""
    mapping = {}
    for block in re.findall(rb"beginbfchar(.*?)endbfchar", data, re.S):
        for src, dst in re.findall(rb"<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>", block):
            mapping[int(src, 16)] = decode_utf16(dst)

    for block in re.findall(rb"beginbfrange(.*?)endbfrange", data, re.S):
        for lo, hi, dst in re.findall(
            rb"<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>", block
        ):
            start, end = int(lo, 16), int(hi, 16)
            base = int(dst, 16)
            for offset in range(min(end - start + 1, 65536)):
                mapping[start + offset] = chr(base + offset)
        for lo, _hi, arr in re.findall(
            rb"<([0-9A-Fa-f]+)>\s*<([0-9A-Fa-f]+)>\s*\[(.*?)\]", block, re.S
        ):
            start = int(lo, 16)
            for offset, item in enumerate(re.findall(rb"<([0-9A-Fa-f]+)>", arr)):
                mapping[start + offset] = decode_utf16(item)
    return mapping


def font_cmaps(objects):
    """resource-name -> cmap for every font that ships a ToUnicode map."""
    result = {}
    for body in objects.values():
        for font_name, ref in re.findall(rb"/(\w+)\s+(\d+)\s+0\s+R", body):
            font_body = objects.get(int(ref))
            if not font_body or b"/Font" not in font_body:
                continue
            cmap_ref = re.search(rb"/ToUnicode\s+(\d+)\s+0\s+R", font_body)
            if not cmap_ref:
                continue
            cmap_stream = stream_of(objects.get(int(cmap_ref.group(1)), b""))
            if cmap_stream:
                result[font_name.decode()] = parse_cmap(cmap_stream)

    if not result:  # no per-font resources: fall back to the only CMap in the file
        for body in objects.values():
            stream = stream_of(body)
            if stream and (b"beginbfchar" in stream or b"beginbfrange" in stream):
                result["*"] = parse_cmap(stream)
                break
    return result


# ---------------------------------------------------------------- content

def unescape_pdf_string(raw: bytes) -> bytes:
    out = bytearray()
    i = 0
    while i < len(raw):
        char = raw[i]
        if char == 0x5C and i + 1 < len(raw):  # backslash escape
            nxt = raw[i + 1]
            escapes = {0x6E: 10, 0x72: 13, 0x74: 9, 0x62: 8, 0x66: 12,
                       0x28: 40, 0x29: 41, 0x5C: 92}
            if nxt in escapes:
                out.append(escapes[nxt])
                i += 2
                continue
            if 0x30 <= nxt <= 0x37:  # octal
                digits = raw[i + 1:i + 4]
                octal = 0
                used = 0
                for digit in digits:
                    if 0x30 <= digit <= 0x37:
                        octal = octal * 8 + (digit - 0x30)
                        used += 1
                    else:
                        break
                out.append(octal & 0xFF)
                i += 1 + used
                continue
            out.append(nxt)
            i += 2
            continue
        out.append(char)
        i += 1
    return bytes(out)


def decode_show(raw: bytes, cmap):
    if cmap:
        pieces = []
        for index in range(0, len(raw) - 1, 2):
            code = (raw[index] << 8) | raw[index + 1]
            pieces.append(cmap.get(code, ""))
        if any(pieces):
            return "".join(pieces)
    return raw.decode("cp1252", errors="replace")


TOKEN = re.compile(
    rb"(\((?:\\.|[^\\()])*\))|(<[0-9A-Fa-f\s]*>)|(\[)|(\])|(/\w+)|(-?\d+\.?\d*)|([A-Za-z*'\"]+)",
    re.S,
)


def extract_text(content: bytes, cmaps) -> str:
    out = []
    stack = []
    array_mode = False
    array_items = []
    font_cmap = None
    last_line_y = None

    for match in TOKEN.finditer(content):
        literal, hexstring, open_bracket, close_bracket, name, number, operator = match.groups()

        if open_bracket:
            array_mode, array_items = True, []
            continue
        if close_bracket:
            array_mode = False
            continue

        if literal:
            value = unescape_pdf_string(literal[1:-1])
        elif hexstring:
            digits = re.sub(rb"\s", b"", hexstring[1:-1])
            if len(digits) % 2:
                digits += b"0"
            try:
                value = bytes.fromhex(digits.decode())
            except ValueError:
                value = b""
        else:
            value = None

        if name:
            stack.append(("name", name.decode().lstrip("/")))
            continue
        if number:
            stack.append(("num", float(number)))
            continue
        if value is not None:
            if array_mode:
                array_items.append(value)
            else:
                stack.append(("str", value))
            continue
        if operator is None:
            continue

        op = operator.decode(errors="replace")

        if op == "Tf":
            names = [entry[1] for entry in stack if entry[0] == "name"]
            if names:
                font_cmap = cmaps.get(names[-1]) or cmaps.get("*")
        elif op in ("Tj", "'", '"'):
            strings = [entry[1] for entry in stack if entry[0] == "str"]
            if strings:
                if op != "Tj":
                    out.append("\n")
                out.append(decode_show(strings[-1], font_cmap))
        elif op == "TJ":
            for raw in array_items:
                out.append(decode_show(raw, font_cmap))
        elif op in ("Td", "TD"):
            numbers = [entry[1] for entry in stack if entry[0] == "num"]
            if len(numbers) >= 2:
                out.append("\n" if numbers[-1] != 0 else " ")
        elif op == "T*":
            out.append("\n")
        elif op == "Tm":
            numbers = [entry[1] for entry in stack if entry[0] == "num"]
            if len(numbers) >= 6:
                y = numbers[-1]
                if last_line_y is not None and abs(y - last_line_y) > 0.5:
                    out.append("\n")
                else:
                    out.append(" ")
                last_line_y = y
        elif op == "ET":
            out.append("\n")

        stack = []

    text = "".join(out)
    text = re.sub(r"[ \t]+\n", "\n", text)
    return re.sub(r"\n{3,}", "\n\n", text)


def extract(path: str) -> str:
    with open(path, "rb") as handle:
        data = handle.read()

    objects = read_objects(data)
    cmaps = font_cmaps(objects)

    chunks = []
    for body in objects.values():
        if not re.search(rb"/Type\s*/Page[^s]", body):
            continue
        contents = re.search(rb"/Contents\s+(\d+)\s+0\s+R", body)
        if not contents:
            continue
        content = stream_of(objects.get(int(contents.group(1)), b""))
        if content:
            chunks.append(extract_text(content, cmaps))
    return "\n".join(chunks)


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    text = extract(argv[1])
    if "-o" in argv:
        output = argv[argv.index("-o") + 1]
        with open(output, "w", encoding="utf-8") as handle:
            handle.write(text)
        print(f"wrote {output} ({len(text)} chars)")
    else:
        sys.stdout.write(text)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
