# Browser coverage

`browser-coverage` is an opt-in Maven profile enabled by CI for the existing
`gwt-user-compat-tests` suite. It uses TeaCoCo 0.1.0-SNAPSHOT, JaCoCo 0.8.15 and
TeaVMTestRunner. No application source changes, test annotations or compiler fork
are required. TeaCoCo is not yet published to Maven Central; until it is, install
`io.instanto:teacoco-maven-plugin:0.1.0-SNAPSHOT` locally from a TeaCoCo clone.
The plugin supplies the `teacoco` runtime; no separate test dependency is needed.

Binding the plugin's `prepare` and `report` goals enables coverage in its default
JaCoCo mode. It configures Surefire's coverage properties, runtime classpath and
prepared classes automatically. This module only specifies which library and
sources to measure, plus the saved JVM coverage to merge:

```xml
<plugin>
  <groupId>io.instanto</groupId>
  <artifactId>teacoco-maven-plugin</artifactId>
  <version>${teacoco.version}</version>
  <configuration>
    <sourceRoots><sourceRoot>${project.basedir}/../gwt-user-compat/src/main/java</sourceRoot></sourceRoots>
    <dependencyIncludes><dependencyInclude>io.instanto:gwt-user-compat</dependencyInclude></dependencyIncludes>
    <executionData><file>${project.build.directory}/jvm-baseline.exec</file></executionData>
  </configuration>
  <executions><execution><goals><goal>prepare</goal><goal>report</goal></goals></execution></executions>
</plugin>
```

Here `executionData` is the JVM baseline produced by the profile's JaCoCo merge
step. TeaCoCo collects the current browser run automatically.

## Local run

Use JDK 21 and the project's normal browser configuration:

```sh
mvn -B -ntp clean install
npm ci --prefix scripts/browser-coverage --ignore-scripts
npm test --prefix scripts/browser-coverage
scripts/browser-coverage/node_modules/.bin/playwright install chromium
npm run collect --prefix scripts/browser-coverage -- --exclude-module gwt-user-compat
mvn -B -ntp -Pbrowser-coverage -pl gwt-user-compat-tests verify
mvn -B -ntp -pl gwt-user-compat org.jacoco:jacoco-maven-plugin:0.8.15:report \
  -Djacoco.dataFile="$PWD/gwt-user-compat-tests/target/jvm-baseline.exec"
python3 scripts/check-gwt-coverage.py
```

The baseline build must precede coverage and use the same sources/classes. The
profile merges the reactor's `*/target/jacoco.exec` files into the test module's
`target/jvm-baseline.exec`, then prepares test-only copies of the resolved
`gwt-user-compat` artifact. Original packaged classes are unchanged. Do not clean
between the baseline and coverage commands. TeaCoCo rejects mismatched JVM/class
identities rather than combining incompatible measurements.

TeaCoCo removes the JVM JaCoCo agent from the selected Surefire browser execution:
these class copies already contain probes. No `jacoco.skip` override is needed.
Ordinary JVM builds remain unchanged. The normal
verification workflow still runs its regular browser and production checks before
the extra instrumented pass. If browser tests fail, Maven stops before reporting;
run `mvn -Pbrowser-coverage -pl gwt-user-compat-tests io.instanto:teacoco-maven-plugin:0.1.0-SNAPSHOT:report` separately to
inspect available evidence while preserving the original test failure. Do not run
`prepare` again before that collection.

## Report ownership

| Scope | Authoritative report |
| --- | --- |
| `gwt-user-compat` | `gwt-user-compat-tests/target/teavm-coverage.xml`: one JaCoCo XML union of JVM and browser probes |
| Other modules' browser lines | `target/browser-coverage.xml`: source-map collector excluding `gwt-user-compat` |
| Other modules' JVM coverage | Their existing JaCoCo reports |

Run the existing Sonar command with `-Pbrowser-coverage`. The runtime module's
profile sets `sonar.coverage.jacoco.xmlReportPaths` to the combined report, replacing
its JVM-only default. The generic report must use `--exclude-module gwt-user-compat`;
this only assigns coverage ownership and is **not** a Sonar source exclusion.
`check-gwt-coverage.py` verifies real hits, matching original line/branch totals,
raw execution output, compiler diagnostics and no generic overlap before CI scans.

The profile generates `teavm-coverage.xml.exec` for the raw union and
`compiler-evidence.json` for reachability/source-location diagnostics. Missing
compiler locations do not prove exact branch elimination; omitted methods remain
missed in the standard report. Branches use JaCoCo's C1 definition, not MC/DC or
all-path coverage. The existing source-map collector remains available without
exclusions when running without TeaCoCo, but do not import both for this module.

CI diagnostics are archived to Instanto's private package store with per-run/attempt names
and SHA-256 download verification. The job summary links the archive. The uploader
and its tests are copied from instanto-poms revision `8e1edc2`; update them together.
Quality gates, thresholds and source exclusions are unchanged.
