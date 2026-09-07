#!/bin/bash
# .claude/hooks/secretGuard.sh
# Block Write/Edit calls whose content looks like a hardcoded secret or credential.

if [[ "$TOOL_NAME" != "Write" ]] && [[ "$TOOL_NAME" != "Edit" ]]; then
    exit 0
fi

FILE_PATH="$TOOL_PARAMS_FILE_PATH"

# .env* files are expected to hold real local secrets — never committed, so skip.
if [[ "$(basename "$FILE_PATH")" == .env* ]]; then
    exit 0
fi

if [[ "$TOOL_NAME" == "Write" ]]; then
    CONTENT="$TOOL_PARAMS_CONTENT"
else
    CONTENT="$TOOL_PARAMS_NEW_STRING"
fi

PATTERNS=(
    "AKIA[0-9A-Z]{16}"                                     # AWS access key
    "ghp_[A-Za-z0-9]{36}"                                  # GitHub personal access token
    "ghs_[A-Za-z0-9]{36}"                                  # GitHub server token
    "github_pat_[A-Za-z0-9_]{82}"                          # GitHub fine-grained PAT
    "sk-[A-Za-z0-9]{48}"                                   # OpenAI-style API key
    "sk-proj-[A-Za-z0-9_-]{50,}"                           # OpenAI project key
    "xox[baprs]-[A-Za-z0-9-]+"                             # Slack token
    "-----BEGIN[[:space:]]*(RSA|EC|OPENSSH)?[[:space:]]*PRIVATE KEY-----"
    "(password|secret|apiKey|clientSecret|client_secret)[[:space:]]*[:=][[:space:]]*[\"'][^\"'$][^\"']{7,}[\"']"
)

for pattern in "${PATTERNS[@]}"; do
    if echo "$CONTENT" | grep -qEi -- "$pattern"; then
        echo "[Hook] ✗ Potential secret detected in $(basename "$FILE_PATH") (pattern: $pattern)" >&2
        echo "하드코딩된 시크릿으로 의심되는 값이 있습니다. 환경 변수(\${VAR})로 치환하거나, 실제 비밀이 아니라면 무시하고 다시 시도해주세요." >&2
        exit 2
    fi
done

exit 0
