#!/bin/bash
root="${CLAUDE_PROJECT_DIR:-${CURSOR_PROJECT_DIR:-$(pwd)}}"
input=$(cat)
file=$(printf '%s' "$input" | jq -r '.tool_input.file_path // .tool_input.path // .file_path // .tool_response.filePath // empty')
rel="${file#"$root"/}"

report() {
  local code="$1"
  local msg="$2"
  printf '%s\n' "$msg" >&2
  jq -n --arg msg "$msg" '{
    additional_context: $msg,
    hookSpecificOutput: {hookEventName: "PostToolUse", additionalContext: $msg}
  }'
  exit "$code"
}

case "$rel" in
  rfr-auth/*.java) label=rfr-auth; kind=java; gradle=(./gradlew -q :rfr-auth:compileJava) ;;
  rfr-api/*.java) label=rfr-api; kind=java; gradle=(./gradlew -q :rfr-api:compileJava) ;;
  rfr-e2e/*.java) label=rfr-e2e; kind=java; gradle=(./gradlew -q :rfr-e2e:compileTestJava) ;;
  rfr-gql-client/src/*.ts|rfr-gql-client/src/*.tsx) label=rfr-gql-client; kind=front ;;
  *) exit 0 ;;
esac

cd "$root" || exit 0

if [[ "$kind" == java ]]; then
  if output=$("${gradle[@]}" 2>&1); then
    report 0 "Compilation of $label passed after editing $rel"
  fi
  filtered=$(printf '%s\n' "$output" | grep -v '^WARNING' | tail -60)
  report 2 "Compilation of $label failed after editing $rel:
$filtered"
fi

rel_front="${rel#rfr-gql-client/}"
if ! output=$(npx --prefix rfr-gql-client tsc --noEmit -p rfr-gql-client 2>&1); then
  filtered=$(printf '%s\n' "$output" | grep -v '^WARNING' | tail -60)
  report 2 "Compilation of $label failed after editing $rel:
$filtered"
fi
if ! output=$(cd rfr-gql-client && npx eslint --max-warnings 0 "$rel_front" 2>&1); then
  filtered=$(printf '%s\n' "$output" | tail -60)
  report 2 "ESLint of $label failed after editing $rel:
$filtered"
fi
report 0 "Compilation and ESLint of $label passed after editing $rel"
