#!/usr/bin/env python3
"""Builds two tiny PDFs used to test tools/pdf_text.py (no third-party libs)."""

import zlib

HEADER = b"%PDF-1.4\n"


def build(path, content: bytes, font_body: bytes, cmap: bytes = None):
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
        b"/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        font_body,
    ]

    compressed = zlib.compress(content)
    objects.append(
        b"<< /Length %d /Filter /FlateDecode >>\nstream\n" % len(compressed)
        + compressed
        + b"\nendstream"
    )

    if cmap:
        compressed_cmap = zlib.compress(cmap)
        objects.append(
            b"<< /Length %d /Filter /FlateDecode >>\nstream\n" % len(compressed_cmap)
            + compressed_cmap
            + b"\nendstream"
        )

    out = bytearray(HEADER)
    offsets = []
    for index, body in enumerate(objects, start=1):
        offsets.append(len(out))
        out += b"%d 0 obj\n" % index + body + b"\nendobj\n"

    xref_at = len(out)
    out += b"xref\n0 %d\n" % (len(objects) + 1)
    out += b"0000000000 65535 f \n"
    for offset in offsets:
        out += b"%010d 00000 n \n" % offset
    out += b"trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n" % (
        len(objects) + 1,
        xref_at,
    )
    with open(path, "wb") as handle:
        handle.write(out)
    print("wrote", path, len(out), "bytes")


# 1) plain Helvetica text with German umlauts (WinAnsi / cp1252)
text = (
    b"BT /F1 14 Tf 50 800 Td (Lektion 1) Tj\n"
    b"0 -26 Td (der Apfel \\334) Tj\n"
    b"0 -26 Td (die Gr\\374\\337e) Tj\n"
    b"0 -26 Td (das Wasser) Tj ET"
)
build("/tmp/test_winansi.pdf", text, b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>")

# 2) font with a ToUnicode CMap (this is how Persian text in a real book is stored)
cmap = (
    b"/CIDInit /ProcSet findresource begin 12 dict begin begincmap\n"
    b"1 begincodespacerange <0000> <FFFF> endcodespacerange\n"
    b"3 beginbfchar\n<0001> <0041>\n<0002> <0627>\n<0003> <0633>\nendbfchar\n"
    b"1 beginbfrange\n<0010> <0012> <0628>\nendbfrange\n"
    b"endcmap end end"
)
text2 = b"BT /F1 14 Tf 50 800 Td <000100020003> Tj 0 -26 Td <001000110012> Tj ET"
build(
    "/tmp/test_cmap.pdf",
    text2,
    b"<< /Type /Font /Subtype /Type0 /BaseFont /TestFont /Encoding /Identity-H /ToUnicode 6 0 R >>",
    cmap,
)
