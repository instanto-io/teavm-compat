# Extraction validation — 8 September 2026

The public repository and Maven artifacts are published in
[instanto-io/teavm-compat](https://github.com/instanto-io/teavm-compat).
[Linux verification](https://github.com/instanto-io/teavm-compat/actions/runs/34198934343)
and [production publication](https://github.com/instanto-io/teavm-compat/actions/runs/34198965215)
both passed.

A standalone application imported the published BOM and all four runtime adapters
from GitHub Packages using a new, empty Maven cache. It compiled with advanced
optimization and passed its native API tests in Chromium, Firefox and WebKit.

## Independent compatibility build

- `mvn clean install`: passed on JDK 21.
- Production build and deployment to a temporary local Maven repository: passed.
- Nine JVM tests cover declaration adaptation, immutable input hashes and BOM isolation.
- Nineteen TeaVMTestRunner contracts passed in Chrome, including mixed GWT client and
  Elemental2 document access and Java string conversion.
- Three Java Playwright integration tests passed in Chromium, Firefox and WebKit,
  covering maps, callbacks, dates, locales, storage, promises, Blob fetch, FileReader,
  SVG, history and the independently published console logger. Both build modes passed.
- SpotBugs completed without analyzer errors or untriaged high-priority findings.
- All four compatibility runtime JARs contain 2,025 unique classes. None packages
  private copies of the official JsInterop annotations.

The additional TeaVMTestRunner Firefox invocation could not start on this Mac
because its expected `/Applications/Firefox.app` installation is absent. Firefox
native API tests passed through Java Playwright's bundled browser. TeaVMTestRunner Firefox verification subsequently passed on the Linux runner.

## Widget consumers

Both repositories were exported into independent directories and built with separate,
initially empty Maven caches. Only the compatibility package repository was mirrored
to the temporary local staging repository; other dependencies were downloaded using
their declared repositories. Maven's repository records confirm the new compatibility
artifacts came from staging, not a sibling checkout or pre-existing local cache.

Domino's optimized build passed. Its final generated widget hashes match the tested
independent build. All 222 maintained browser scenarios passed in each build mode,
covering Chromium, Firefox and WebKit. Native scrolling checks passed seven tests;
two unsupported touch-engine cases are explicitly skipped. The maintained port and
showcase are TeaVM-only, with upstream comparison links.

Bootstrap's complete 27-module build passed from the empty cache. Its GWT reference
and TeaVM widget contract suites passed against the extracted artifacts in both the
main checkout and the independent fresh-cache build. Existing declared skipped scenarios remain visible in the test reports.

## Automatic formatting

The isolated `sarto-poms` change passed the POM suite. A child POM inherited the
formatter, corrected deliberately unformatted maintained Java during `validate`,
and left generated Java untouched. The fixture passed on JDK 21 and JDK 25. The
existing dirty parent checkout was preserved.

The widget consumer changes have been pushed to their origins.
Domino's [development and production CI](https://github.com/cstainton/domino-widgets/actions/runs/34201414158)
and [Maven publication](https://github.com/cstainton/domino-widgets/actions/runs/34201890404)
passed, including a new empty-cache external consumer and its browser tests.
Its [TeaVM showcase](https://cstainton.github.io/domino-widgets/teavm/) is deployed;
live rendering and native scrolling passed in Chromium, Firefox and WebKit.

Bootstrap's [Maven publication](https://github.com/cstainton/bootstrap-widgets/actions/runs/34200755888)
and [full CI](https://github.com/cstainton/bootstrap-widgets/actions/runs/34200723061)
passed, and its [showcases](https://cstainton.github.io/bootstrap-widgets/) are deployed.
A subsequent run passed all 127 mobile cases but failed during temporary Chrome
profile deletion. The browser harness now waits for theme application before the
next tap, requests graceful Chrome shutdown and allows bounded cleanup retries.
The revised theme cases and cleanup passed locally; Linux confirmation of the final
cleanup adjustment is pending.

The base-POM change is also pushed; its remote build still encounters the same
missing Sarto BOM dependencies seen before the formatter change. This does not
affect the standalone compatibility build.
