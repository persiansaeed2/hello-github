#!/usr/bin/env python3
"""
Turns the book export (reference/uni_sicher_wortschatz.json) into the app's
app/src/main/assets/words.json.

Mapping decisions:
  * one app "lesson" per book unit («1.1 Schule und Bildung», «1.2 Studium», …),
    numbered 1..N in numeric order (so 4.10 comes after 4.9, not before 4.2),
  * the headword is the `front` form, which already carries the article
    («die Schulpflicht») — good for the Persian learner and for TTS,
  * the Persian meaning is the `fa` field,
  * `id` stays stable («b<id>») so progress survives re-imports,
  * duplicates (same headword twice) are collapsed, extra metadata is kept for
    later features but ignored by the app.

Usage:
    python3 tools/import_book.py                       # writes assets/words.json
    python3 tools/import_book.py --include-related     # also import «→ ...» forms
"""

import argparse
import json
import re
from collections import Counter


def unit_sort_key(unit: str):
    match = re.match(r"\s*(\d+)(?:\.(\d+))?", unit or "")
    if not match:
        return (999, 999, unit or "")
    return (int(match.group(1)), int(match.group(2) or 0), unit)


def normalize(text: str) -> str:
    return re.sub(r"\s+", " ", (text or "").strip().lower())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", default="reference/uni_sicher_wortschatz.json")
    parser.add_argument("--output", default="app/src/main/assets/words.json")
    parser.add_argument("--include-related", action="store_true")
    args = parser.parse_args()

    book = json.load(open(args.source, encoding="utf-8"))
    raw_words = book["words"]

    units = sorted({word.get("unit", "") for word in raw_words}, key=unit_sort_key)
    unit_to_lesson = {unit: index for index, unit in enumerate(units, start=1)}

    entries = []
    seen = set()
    duplicates = 0

    def add(entry_id, term, meaning, unit, **extra):
        nonlocal duplicates
        term = (term or "").strip()
        meaning = (meaning or "").strip()
        if not term or not meaning:
            return
        key = normalize(term)
        if key in seen:
            duplicates += 1
            return
        seen.add(key)
        entry = {
            "id": entry_id,
            "word": term,
            "meaning": meaning,
            "lesson": unit_to_lesson.get(unit, 1),
            "unit": unit,
        }
        entry.update({k: v for k, v in extra.items() if v not in (None, "", False, [])})
        entries.append(entry)

    for word in raw_words:
        unit = word.get("unit", "")
        term = word.get("front") or " ".join(
            part for part in (word.get("art", ""), word.get("de", "")) if part
        )
        add(
            entry_id=f"b{word.get('id')}",
            term=term,
            meaning=word.get("fa", ""),
            unit=unit,
            pos=word.get("pos"),
            plural=word.get("plural"),
            page=word.get("page"),
            check=word.get("check"),
        )
        if args.include_related:
            for index, rel in enumerate(word.get("related") or []):
                add(
                    entry_id=f"b{word.get('id')}-r{index}",
                    term=rel.get("front") or " ".join(
                        part for part in (rel.get("art", ""), rel.get("de", "")) if part
                    ),
                    meaning=rel.get("fa", ""),
                    unit=unit,
                    pos=rel.get("pos"),
                    plural=rel.get("plural"),
                )

    payload = {
        "book": book.get("book", ""),
        "units": [
            {"lesson": unit_to_lesson[unit], "unit": unit} for unit in units
        ],
        "count": len(entries),
        "words": entries,
    }
    with open(args.output, "w", encoding="utf-8") as handle:
        json.dump(payload, handle, ensure_ascii=False, indent=1)
        handle.write("\n")

    per_lesson = Counter(entry["lesson"] for entry in entries)
    print(f"{len(entries)} words, {len(units)} lessons, duplicates skipped: {duplicates}")
    print("words per lesson:", dict(sorted(per_lesson.items())))
    print("first lesson:", units[0], "| last:", units[-1])
    print("wrote", args.output)


if __name__ == "__main__":
    main()
