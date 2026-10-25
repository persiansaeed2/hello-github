#!/usr/bin/env python3
"""
Converts the vocabulary CSV export into app/src/main/assets/words.json.

The app reads JSON only (so it needs no CSV dependency on the device), therefore the
CSV is converted once, here, at build time.

Usage:
    python3 tools/csv_to_words_json.py reference/words.csv
    python3 tools/csv_to_words_json.py reference/words.csv --lesson 3
    python3 tools/csv_to_words_json.py reference/words.csv -o app/src/main/assets/words.json

Column names are matched case-insensitively, in German / English / Persian:
    word    : word, wort, term, deutsch, german, english, vokabel, text, لغت, کلمه
    meaning : meaning, translation, übersetzung, ubersetzung, bedeutung,
              persian, fa, farsi, معنی, ترجمه
    lesson  : lesson, lektion, kapitel, einheit, unit, chapter, dars, درس
    id      : id, key, code, nummer, nr

Missing words or meanings are skipped (and counted), not fatal — the same rule the
app's JSON parser follows.
"""

import argparse
import csv
import io
import json
import sys

TERM_KEYS = ["word", "wort", "term", "deutsch", "german", "english", "vokabel", "text",
             "لغت", "کلمه", "واژه"]
MEANING_KEYS = ["meaning", "translation", "übersetzung", "ubersetzung", "bedeutung",
                "persian", "fa", "farsi", "معنی", "ترجمه"]
LESSON_KEYS = ["lesson", "lektion", "kapitel", "einheit", "unit", "chapter", "dars", "درس"]
ID_KEYS = ["id", "key", "code", "nummer", "nr"]


def find_key(headers, candidates):
    lowered = {header.strip().lower(): header for header in headers if header}
    for candidate in candidates:
        if candidate in lowered:
            return lowered[candidate]
    return None


def sniff_delimiter(text: str) -> str:
    sample = text[:4096]
    try:
        return csv.Sniffer().sniff(sample, delimiters=",;\t|").delimiter
    except csv.Error:
        counts = {delimiter: sample.count(delimiter) for delimiter in (";", ",", "\t", "|")}
        return max(counts, key=counts.get) if any(counts.values()) else ","


def convert(path: str, default_lesson: int, output: str) -> int:
    with open(path, "rb") as handle:
        raw = handle.read()

    for encoding in ("utf-8-sig", "utf-8", "cp1252", "latin-1"):
        try:
            text = raw.decode(encoding)
            break
        except UnicodeDecodeError:
            continue
    else:
        print("could not decode the file", file=sys.stderr)
        return 1

    dialect = sniff_delimiter(text)
    reader = csv.DictReader(io.StringIO(text), delimiter=dialect)
    headers = reader.fieldnames or []

    term_key = find_key(headers, TERM_KEYS)
    meaning_key = find_key(headers, MEANING_KEYS)
    lesson_key = find_key(headers, LESSON_KEYS)
    id_key = find_key(headers, ID_KEYS)

    if not term_key or not meaning_key:
        print(f"could not find vocabulary columns. headers found: {headers}", file=sys.stderr)
        print(f"expected something like: {TERM_KEYS[:4]} / {MEANING_KEYS[:4]}", file=sys.stderr)
        return 2

    words = []
    skipped = 0
    for row in reader:
        term = (row.get(term_key) or "").strip()
        meaning = (row.get(meaning_key) or "").strip()
        if not term or not meaning:
            skipped += 1
            continue

        lesson_raw = (row.get(lesson_key) or "").strip() if lesson_key else ""
        lesson = int(lesson_raw) if lesson_raw.isdigit() else default_lesson

        entry = {"word": term, "meaning": meaning, "lesson": lesson}
        if id_key and (row.get(id_key) or "").strip():
            entry["id"] = row[id_key].strip()
        words.append(entry)

    if not words:
        print("no usable rows", file=sys.stderr)
        return 3

    words.sort(key=lambda item: (item["lesson"], item["word"]))
    with open(output, "w", encoding="utf-8") as handle:
        json.dump(words, handle, ensure_ascii=False, indent=2)
        handle.write("\n")

    lessons = sorted({item["lesson"] for item in words})
    print(f"{len(words)} words, {len(lessons)} lessons {lessons}, skipped {skipped} -> {output}")
    return 0


def main():
    parser = argparse.ArgumentParser(description="CSV vocabulary -> words.json")
    parser.add_argument("csv_path")
    parser.add_argument("-o", "--output", default="app/src/main/assets/words.json")
    parser.add_argument("--lesson", type=int, default=1, help="lesson number when the CSV has no lesson column")
    args = parser.parse_args()
    return convert(args.csv_path, args.lesson, args.output)


if __name__ == "__main__":
    raise SystemExit(main())
