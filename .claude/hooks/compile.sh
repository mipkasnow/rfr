#!/bin/bash
root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
file=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty')
rel="${file#"$root"/}"
case "$rel" in
  rfr-auth/*.java) label=rfr-auth; cmd=(./gradlew -q :rfr-auth:compileJava) ;;
  rfr-api/*.java) label=rfr-api; cmd=(./gradlew -q :rfr-api:compileJava) ;;
  rfr-e2e/*.java) label=rfr-e2e; cmd=(./gradlew -q :rfr-e2e:compileTestJava) ;;
  rfr-gql-client/src/*.ts|rfr-gql-client/src/*.tsx) label=rfr-gql-client; cmd=(npx --prefix rfr-gql-client tsc --noEmit -p rfr-gql-client) ;;
  *) exit 0 ;;
esac
cd "$root" || exit 0
if output=$("${cmd[@]}" 2>&1); then
  jq -n --arg msg "Compilation of $label passed after editing $rel" \
    '{hookSpecificOutput: {hookEventName: "PostToolUse", additionalContext: $msg}}'
  exit 0
fi
echo "Compilation of $label failed after editing $rel:" >&2
echo "$output" | grep -v '^WARNING' | tail -60 >&2
exit 2
