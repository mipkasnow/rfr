#!/bin/bash
# Blocks the end of a turn while a frontend or backend module differs from the point where the branch left
# the main branch without a raised minor version, so a pull request raises each changed module once.
root="${CLAUDE_PROJECT_DIR:-$(pwd)}"
cd "$root" || exit 0
main=$(git symbolic-ref -q --short refs/remotes/origin/HEAD || echo master)
base=$(git merge-base HEAD "$main" 2>/dev/null) || exit 0

gradle_version() {
  sed -nE "s/^version[[:space:]]*=[[:space:]]*['\"]([^'\"]+)['\"].*/\1/p" | head -1
}

json_version() {
  jq -r '.version // empty'
}

version_of() {
  local file=$1 source=$2
  local parse=gradle_version
  [[ $file == *.json ]] && parse=json_version
  if [[ $source == base ]]; then
    git show "$base:$file" 2>/dev/null | $parse
  else
    $parse < "$file"
  fi
}

problems=()
for entry in rfr-gql-client:package.json rfr-api:build.gradle rfr-auth:build.gradle; do
  module=${entry%%:*}
  file="$module/${entry#*:}"
  if git diff --quiet "$base" -- "$module" && [[ -z $(git ls-files --others --exclude-standard -- "$module") ]]; then
    continue
  fi
  before=$(version_of "$file" base)
  after=$(version_of "$file" work)
  [[ -n $before ]] || continue
  IFS=. read -r before_major before_minor _ <<< "$before"
  IFS=. read -r after_major after_minor _ <<< "$after"
  if [[ $after_major != "$before_major" || ! $after_minor =~ ^[0-9]+$ ]] || (( after_minor <= before_minor )); then
    problems+=("$module changed since it left $main at ${base:0:8} but its version in $file is ${after:-missing} instead of $before_major.$((before_minor + 1)).0 or a later minor")
  fi
done

(( ${#problems[@]} == 0 )) && exit 0
reason=$(printf '%s\n' "${problems[@]}")
jq -n --arg reason "Raise the minor version of every changed module before finishing: $reason" \
  '{decision: "block", reason: $reason}'
exit 0
