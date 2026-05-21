#!/bin/bash
# .claude/hooks/preCommit.sh
# 1) git push 전: ktlintCheck 실행 — 위반 시 실패 (자동 커밋 없음)
# 2) git commit 전: 커밋 메시지 포맷 검사

if [[ "$TOOL_NAME" != "Bash" ]]; then
    exit 0
fi
COMMAND="$TOOL_PARAMS_COMMAND"

# ── git push 처리 ──────────────────────────────────────────────────
if [[ "$COMMAND" =~ git[[:space:]]+push ]]; then
    PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"
    if [[ -z "$PROJECT_ROOT" || ! -f "$PROJECT_ROOT/gradlew" ]]; then
        exit 0
    fi
    echo "[Hook] Running ktlintCheck..."
    cd "$PROJECT_ROOT" && ./gradlew ktlintCheck -q 2>&1
    if [[ $? -ne 0 ]]; then
        echo "[Hook] ✗ ktlint 위반이 있습니다. 아래 명령어로 포맷 적용 후 커밋하세요:"
        echo "       ./gradlew ktlintFormat"
        exit 2
    fi
    echo "[Hook] ✓ ktlint 검사 통과"
    exit 0
fi

# ── git commit 처리 ────────────────────────────────────────────────
if [[ ! "$COMMAND" =~ git[[:space:]]+commit ]]; then
    exit 0
fi
if [[ "$COMMAND" =~ \$\( || "$COMMAND" =~ "<<" ]]; then
    exit 0
fi

COMMIT_MSG=$(echo "$COMMAND" | sed -n -e 's/.*-m[[:space:]]*"\([^"]*\)".*/\1/p' -e "s/.*-m[[:space:]]*'\([^']*\)'.*/\1/p" | head -n 1)
if [[ -z "$COMMIT_MSG" ]]; then
    exit 0
fi
PATTERN="^(feat|refactor|fix|delete|docs|test|merge|init)\\(([^)]+)\\): .+"
if [[ ! "$COMMIT_MSG" =~ $PATTERN ]]; then
    echo "[Hook] ✗ Invalid commit message format"
    echo "Expected: type(scope): description"
    echo "Example: fix(auth): API 키 로테이션 시 트랜잭션 롤백 방지"
    echo ""
    echo "Types: feat/refactor/fix/delete/docs/test/merge/init"
    exit 2
fi
echo "[Hook] ✓ Commit message format valid"
