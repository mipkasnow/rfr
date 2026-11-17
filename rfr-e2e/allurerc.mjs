import { fileURLToPath } from "node:url";

export default {
  output: fileURLToPath(new URL("build/reports/allure-report/allureReport", import.meta.url)),
  plugins: {
    awesome: {
      options: {
        singleFile: false,
      },
    },
    failures: {
      import: fileURLToPath(new URL("failures-plugin.mjs", import.meta.url)),
      options: {
        results: fileURLToPath(new URL("build/allure-results", import.meta.url)),
      },
    },
  },
  categories: [
    {
      name: "visual",
      matchers: {
        statuses: ["failed"],
        trace: /io\.student\.rangiffler\.visual\.ScreenshotMismatchError/,
      },
    },
    {
      name: "stand",
      matchers: {
        statuses: ["failed", "broken"],
        trace: /net::ERR_|ECONNREFUSED|Connection refused|CommunicationsException|CannotGetJdbcConnectionException/,
      },
    },
  ],
};
