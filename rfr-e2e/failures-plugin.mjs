import { readdir, readFile, writeFile } from "node:fs/promises";
import { join, relative, sep } from "node:path";

const FAILED = new Set(["failed", "broken"]);
const UNCATEGORIZED = "Без категории";

export default class FailuresPlugin {
  #results;

  constructor(options = {}) {
    if (!options.results) {
      throw new Error("Failures plugin needs the results option with the allure-results directory");
    }
    this.#results = options.results;
  }

  async done(context, store) {
    const files = await readdir(this.#results);
    const containers = await fixtures(this.#results, files);
    const tests = await store.allTestResults();
    const failures = tests.filter((test) => FAILED.has(test.status));
    const entries = await Promise.all(failures.map(async (test) => ({
      test,
      category: category(context.categories ?? [], test),
      message: headline(test.error?.message),
      retries: await store.retriesByTr(test),
    })));
    const link = (file) => `[${file}](${relative(context.output, join(this.#results, file)).split(sep).join("/")})`;
    const order = [...(context.categories ?? []).map((definition) => definition.name), UNCATEGORIZED];
    const lines = [
      `# Падения: ${failures.length} из ${tests.length}`,
      "",
      `Результаты: \`${relative(context.output, this.#results).split(sep).join("/")}\`, ссылки ведут на JSON Allure относительно этого файла.`,
      ...order.flatMap((name) => section(name, entries.filter((entry) => entry.category === name), containers, link)),
      ...globals(await errors(this.#results, files), link),
    ];
    await writeFile(join(context.output, "failures.md"), `${lines.join("\n")}\n`);
  }
}

const section = (name, entries, containers, link) => {
  if (entries.length === 0) {
    return [];
  }
  const groups = Map.groupBy(entries, (entry) => entry.message);
  return [
    "",
    `## ${name} — ${entries.length}`,
    ...[...groups.entries()]
      .sort(([, left], [, right]) => right.length - left.length)
      .flatMap(([message, group]) => [
        "",
        `### ${message} — ${group.length}`,
        "",
        ...group
          .sort((left, right) => (left.test.fullName ?? "").localeCompare(right.test.fullName ?? ""))
          .flatMap((entry) => test(entry, containers, link)),
      ]),
  ];
};

const test = ({ test, retries }, containers, link) => {
  const file = test.sourceMetadata?.metadata?.originalFileName;
  return [
    `- **${test.status}** \`${test.fullName ?? test.name}\` — ${test.name}`,
    ...(file ? [`  - результат: ${link(file)}`] : []),
    ...(containers.get(uuid(file)) ?? []).map((container) => `  - фикстуры: ${link(container.file)} — ${container.fixtures}`),
    ...retries
      .filter((retry) => retry.sourceMetadata?.metadata?.originalFileName)
      .map((retry) => `  - ретрай ${retry.status}: ${link(retry.sourceMetadata.metadata.originalFileName)}`),
  ];
};

const globals = (errors, link) => errors.length === 0
  ? []
  : ["", `## Глобальные ошибки — ${errors.length}`, "", ...errors.map((error) => `- ${headline(error.message)} — ${link(error.file)}`)];

const category = (definitions, test) => {
  const data = {
    status: test.status,
    labels: (test.labels ?? []).map((label) => ({ name: label.name, value: label.value ?? "" })),
    message: test.error?.message,
    trace: test.error?.trace,
    flaky: test.flaky,
    duration: test.duration,
    transition: test.transition,
    environment: test.environment,
  };
  return definitions.find((definition) => definition.matchers.some((matcher) => matches(matcher, data)))?.name ?? UNCATEGORIZED;
};

const matches = (matcher, data) => {
  if (typeof matcher === "function") {
    return matcher(data);
  }
  const pattern = (value) => (value instanceof RegExp ? value : new RegExp(value));
  return (!matcher.statuses || matcher.statuses.includes(data.status))
    && (matcher.flaky === undefined || matcher.flaky === data.flaky)
    && Object.entries(matcher.labels ?? {}).every(([name, expected]) => data.labels
      .filter((label) => label.name === name)
      .some((label) => pattern(expected).test(label.value)))
    && (matcher.message === undefined || pattern(matcher.message).test(data.message ?? ""))
    && (matcher.trace === undefined || pattern(matcher.trace).test(data.trace ?? ""))
    && (!matcher.transitions || matcher.transitions.includes(data.transition))
    && (!matcher.environments || matcher.environments.includes(data.environment ?? "<Empty>"));
};

const HEADLINE = 120;

const headline = (message) => {
  const lines = (message ?? "").split("\n").map((line) => line.trim());
  const line = lines.map((text) => text.match(/^message='(.+)$/)?.[1]).find(Boolean) ?? lines.find(Boolean);
  return line ? shortened(line.replace(/(?<=^|\s)(?:\/[^\s/:]+)+/g, (path) => `…/${path.split("/").slice(-2).join("/")}`))
    : "Без сообщения";
};

const shortened = (text) => {
  if (text.length <= HEADLINE) {
    return text;
  }
  const cut = text.slice(0, HEADLINE);
  return `${cut.slice(0, Math.max(cut.lastIndexOf(" "), HEADLINE / 2)).trimEnd()} …`;
};

const uuid = (file) => file?.replace(/-result\.json$/, "");

const json = async (directory, file) => JSON.parse(await readFile(join(directory, file), "utf8"));

const fixtures = async (directory, files) => {
  const index = new Map();
  for (const file of files.filter((name) => name.endsWith("-container.json"))) {
    const container = await json(directory, file);
    const described = [...(container.befores ?? []), ...(container.afters ?? [])]
      .map((fixture) => `«${fixture.name}» ${fixture.status}`);
    if (described.length > 0) {
      (container.children ?? []).forEach((child) => index.set(child, [
        ...(index.get(child) ?? []),
        { file, fixtures: described.join(", ") },
      ]));
    }
  }
  return index;
};

const errors = async (directory, files) => (await Promise.all(files
  .filter((name) => name.endsWith("-globals.json"))
  .map(async (file) => ((await json(directory, file)).errors ?? []).map((error) => ({ ...error, file })))))
  .flat();
