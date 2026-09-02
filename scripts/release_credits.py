#!/usr/bin/env python3
"""Contributor credits for commits between the previous tag and the current ref."""

from __future__ import annotations

import re
import subprocess
import sys

BOT_RE = re.compile(
    r"(\[bot\]|github-actions|dependabot|renovate|copilot|codecov|"
    r"semantic-release-bot|renovate-bot)",
    re.I,
)

Contributor = tuple[str, str, str | None, int]


def git(*args: str) -> str:
    return subprocess.run(
        ["git", *args], capture_output=True, text=True, check=True
    ).stdout


def previous_tag(current: str) -> str | None:
    tags = [t for t in git("tag", "--sort=-v:refname").splitlines() if t.strip()]
    for i, tag in enumerate(tags):
        if tag == current and i + 1 < len(tags):
            return tags[i + 1]
    return None


def is_bot(name: str, email: str) -> bool:
    return bool(BOT_RE.search(f"{name} {email}"))


def github_handle(name: str, email: str) -> str | None:
    if "users.noreply.github.com" not in email.lower():
        return None
    local = email.split("@", 1)[0]
    if "+" in local:
        return local.split("+", 1)[1]
    if local.isdigit():
        return None
    return local


def contributors(from_ref: str, to_ref: str) -> list[Contributor]:
    counts: dict[str, tuple[str, str, int]] = {}
    out = git("log", f"{from_ref}..{to_ref}", "--format=%aN|%aE")
    for line in out.splitlines():
        if "|" not in line:
            continue
        name, email = line.split("|", 1)
        if is_bot(name, email):
            continue
        key = email.lower()
        if key in counts:
            prev_name, prev_email, count = counts[key]
            counts[key] = (prev_name, prev_email, count + 1)
        else:
            counts[key] = (name, email, 1)

    result: list[Contributor] = []
    for name, email, count in counts.values():
        result.append((name, email, github_handle(name, email), count))
    result.sort(key=lambda row: (-row[3], (row[2] or row[0]).lower()))
    return result


def format_credits(from_ref: str, to_ref: str, people: list[Contributor]) -> str:
    if not people:
        return ""
    tokens = [f"@{handle}" if handle else name for name, _, handle, _ in people]
    return f"\n---\n\n## Credits ({from_ref} → {to_ref})\n\n{' '.join(tokens)}\n"


def main() -> None:
    to_ref = sys.argv[1] if len(sys.argv) > 1 else "HEAD"
    from_ref = previous_tag(to_ref)
    if not from_ref:
        return
    print(format_credits(from_ref, to_ref, contributors(from_ref, to_ref)))


def self_check() -> None:
    assert is_bot("github-actions[bot]", "github-actions[bot]@users.noreply.github.com")
    assert not is_bot("Alice", "alice@example.com")
    assert github_handle("x", "123+alice@users.noreply.github.com") == "alice"
    assert github_handle("x", "alice@users.noreply.github.com") == "alice"
    assert github_handle("Alice", "alice@example.com") is None

    people: list[Contributor] = [
        ("Alice", "alice@users.noreply.github.com", "alice", 5),
        ("Bob", "bob@example.com", None, 2),
    ]
    body = format_credits("v0.1.0", "v0.2.0", people)
    assert "@alice Bob" in body
    assert body.index("@alice") < body.index("Bob")
    print("ok")


if __name__ == "__main__":
    if len(sys.argv) == 2 and sys.argv[1] == "--self-check":
        self_check()
    else:
        main()
