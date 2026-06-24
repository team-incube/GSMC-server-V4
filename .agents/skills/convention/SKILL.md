---
name: convention
description: Review and apply project code conventions for naming, DTO, Entity, Repository, Service, Controller, transaction placement, and formatting.
---

Read `.claude/rules/convention.md` and `CLAUDE.md`, then apply the conventions to the current code.

Priority when rules conflict: `CLAUDE.md` > `.claude/rules/convention.md`.

Check changed Kotlin files first. Do not edit test files unless the user explicitly asks.

After code edits, run ktlint formatting when Kotlin files changed.
