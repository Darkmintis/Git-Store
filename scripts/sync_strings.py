#!/usr/bin/env python3
"""Build locale strings.xml from values/strings.xml + l10n-overrides.json."""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
BASE = RES / "values/strings.xml"
OVERRIDES = Path(__file__).with_name("l10n-overrides.json")

LOCALES = {
    "de": "de",
    "es": "es",
    "ja": "ja",
    "ru": "ru",
    "zh": "zh-CN",
}

SKIP_NAMES = {
    "app_name",
    "language_kotlin",
    "language_java",
    "language_javascript",
    "language_typescript",
    "language_python",
    "language_swift",
    "language_rust",
    "language_go",
    "language_csharp",
    "language_cpp",
    "language_c",
    "language_dart",
    "language_ruby",
    "language_php",
    "promo_app_blink",
}

BAD_MARKERS = ("MYMEMORY", "network interface", "PLEASE SELECT")

STRING_RE = re.compile(
    r'(?P<indent>[ \t]*)<string name="(?P<name>[^"]+)"(?P<attrs>[^>]*)>(?P<value>.*?)</string>',
    re.DOTALL,
)


def parse_strings(path: Path) -> dict[str, str]:
    return {m.group("name"): m.group("value").strip() for m in STRING_RE.finditer(path.read_text(encoding="utf-8"))}


def escape_xml(value: str) -> str:
    value = (
        value.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("\\'", "'")
    )
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "\\'")
        .replace('"', '\\"')
    )


def is_bad(value: str) -> bool:
    upper = value.upper()
    return any(marker in upper for marker in BAD_MARKERS)


def build_locale_file(base_text: str, translations: dict[str, str]) -> str:
    out: list[str] = []
    pos = 0
    for m in STRING_RE.finditer(base_text):
        out.append(base_text[pos : m.start()])
        name = m.group("name")
        value = translations.get(name, m.group("value").strip())
        indent = m.group("indent")
        attrs = m.group("attrs")
        if "\n" in value:
            out.append(f'{indent}<string name="{name}"{attrs}>\n{value}\n{indent}</string>')
        else:
            out.append(f'{indent}<string name="{name}"{attrs}>{escape_xml(value)}</string>')
        pos = m.end()
    out.append(base_text[pos:])
    return "".join(out)


TRANSLATION_UI = {
    "de": {
        "translation_language_title": "Sprache",
        "select_translation_language": "Sprache auswählen",
        "auto_translate_description": "Repository-Text automatisch übersetzen, wenn er nicht in Ihrer Sprache ist",
        "translate_with_language": "Übersetzen (%1$s)",
        "translated_show_original": "Übersetzt (%1$s) · Original",
        "show_translation": "Übersetzung anzeigen (%1$s)",
        "translation_unavailable": "Übersetzung derzeit nicht verfügbar",
    },
    "es": {
        "translation_language_title": "Idioma",
        "select_translation_language": "Seleccionar idioma",
        "auto_translate_description": "Traducir automáticamente el texto del repositorio que no esté en tu idioma",
        "translate_with_language": "Traducir (%1$s)",
        "translated_show_original": "Traducido (%1$s) · Original",
        "show_translation": "Mostrar traducción (%1$s)",
        "translation_unavailable": "Traducción no disponible ahora",
    },
    "ja": {
        "translation_language_title": "言語",
        "select_translation_language": "言語を選択",
        "auto_translate_description": "選択した言語以外のリポジトリテキストを自動翻訳します",
        "translate_with_language": "翻訳 (%1$s)",
        "translated_show_original": "翻訳 (%1$s) · 原文",
        "show_translation": "翻訳を表示 (%1$s)",
        "translation_unavailable": "現在翻訳できません",
    },
    "ru": {
        "translation_language_title": "Язык",
        "select_translation_language": "Выберите язык",
        "auto_translate_description": "Автоматически переводить текст репозитория, если он не на вашем языке",
        "translate_with_language": "Перевести (%1$s)",
        "translated_show_original": "Перевод (%1$s) · Оригинал",
        "show_translation": "Показать перевод (%1$s)",
        "translation_unavailable": "Перевод сейчас недоступен",
        "issues": "Задачи",
    },
    "zh": {
        "translation_language_title": "语言",
        "select_translation_language": "选择语言",
        "auto_translate_description": "自动翻译非所选语言的仓库文本",
        "translate_with_language": "翻译 (%1$s)",
        "translated_show_original": "已翻译 (%1$s) · 原文",
        "show_translation": "显示翻译 (%1$s)",
        "translation_unavailable": "暂时无法翻译",
    },
}


def sync_locale(code: str, overrides: dict[str, str]) -> None:
    base_text = BASE.read_text(encoding="utf-8")
    base = parse_strings(BASE)
    locale_path = RES / f"values-{code}/strings.xml"
    existing = parse_strings(locale_path) if locale_path.exists() else {}

    merged: dict[str, str] = {}
    for name, en_value in base.items():
        if name in overrides:
            merged[name] = overrides[name]
            continue
        if name in SKIP_NAMES:
            merged[name] = en_value
            continue
        existing_value = existing.get(name, "")
        if existing_value and existing_value != en_value and not is_bad(existing_value):
            merged[name] = existing_value
            continue
        merged[name] = en_value

    merged.update(TRANSLATION_UI.get(code, {}))

    locale_path.parent.mkdir(parents=True, exist_ok=True)
    locale_path.write_text(build_locale_file(base_text, merged), encoding="utf-8")
    print(f"{code}: wrote {locale_path}")


def main() -> None:
    import sys

    all_overrides = json.loads(OVERRIDES.read_text(encoding="utf-8"))

    targets = LOCALES
    if len(sys.argv) > 1:
        targets = {k: v for k, v in LOCALES.items() if k in sys.argv[1:]}

    for code in targets:
        sync_locale(code, all_overrides.get(code, {}))
    print("done")


if __name__ == "__main__":
    main()
