#!/bin/bash
input=$(cat)
command=$(printf '%s' "$input" | jq -r '.command // empty')

deny() {
  jq -n --arg msg "$1" '{permission: "deny", user_message: $msg, agent_message: $msg}'
  exit 0
}

if printf '%s\n' "$command" | grep -Eq '(^|[;&|][[:space:]]*)(command[[:space:]]+)?(sudo[[:space:]]+)?(/usr/bin/|/bin/)?git([[:space:]]+(-C|--git-dir|--work-tree|-c)([[:space:]]+|=)[^[:space:]]+)*[[:space:]]+push([[:space:]]|$)'; then
  deny "git push is denied"
fi

if printf '%s\n' "$command" | grep -Eq '(^|[;&|][[:space:]]*)(command[[:space:]]+)?(/usr/bin/|/bin/)?git[[:space:]]+rm([[:space:]]|$)'; then
  deny "git rm is denied"
fi

if printf '%s\n' "$command" | grep -Eq '(^|[;&|][[:space:]]*)(command[[:space:]]+)?(/usr/bin/|/bin/)?rm([[:space:]]|$)'; then
  deny "rm is denied"
fi

if printf '%s\n' "$command" | grep -Eq '(^|[;&|][[:space:]]*)(command[[:space:]]+)?(/usr/bin/)?unlink([[:space:]]|$)'; then
  deny "unlink is denied"
fi

if printf '%s\n' "$command" | grep -Eq '(^|[;&|])[[:space:]]*find[[:space:]].*-delete([[:space:]]|$)'; then
  deny "find -delete is denied"
fi

if printf '%s\n' "$command" | grep -Eq '(^|[;&|])[[:space:]]*find[[:space:]].*-exec[[:space:]]+(rm|unlink)([[:space:]]|$)'; then
  deny "find -exec rm is denied"
fi

echo '{ "permission": "allow" }'
exit 0
