---
name: planning
argument-hint: "[instructions]"
description: Conduct an in-depth structured interview with the user to uncover non-obvious requirements, tradeoffs, and constraints, then produce a detailed implementation spec file.
allowed-tools: AskUserQuestion, Write
---

Interview the user relentlessly about every aspect of the plan until you reach a shared understanding. Walk down each branch of the design tree, resolving dependencies between decisions one-by-one. After the user responds to each question, provide your evaluation and recommended answer.

Ask the questions one at a time.

If a question can be answered by exploring the codebase, explore the codebase instead.
