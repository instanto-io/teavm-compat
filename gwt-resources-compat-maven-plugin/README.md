# GWT resources compatibility for TeaVM

`gwt-resources-compat-maven-plugin` prepares the CSS, JavaScript, fonts and images that
widgets need in the browser. It handles two parts of the build:

- **Building a widget library:** `generate` reads the library's GWT module files
  and ClientBundle declarations to find its browser files. It collects those
  files for packaging in the library JAR and generates Java code that loads the
  required stylesheets and scripts, telling the application when they are ready.
- **Building an application:** `stage-assets` copies those files from the library
  JARs into the folder your web server serves. Each library supplies a small
  configuration file that says where its files belong.

For example, the Bootstrap library build packages its stylesheets, scripts and
fonts once. An application that uses Bootstrap can then run `mvn package` to copy
them into `target/site/assets/bootstrap5/`, alongside its compiled application
and HTML page. You can serve the completed website folder locally or upload it
to your server.

If you are using a widget library, start with **Stage assets for an application**
below. If you maintain a library, see
[Include browser files in a library](#include-browser-files-in-a-library) and
[Generate resource loaders for a library](#generate-resource-loaders-for-a-library).

The Maven artifact is `io.instanto:gwt-resources-compat-maven-plugin:0.1.0-SNAPSHOT`.
Its project name is **GWT Resources Compatibility Maven Plugin**. It is built
and maintained in `teavm-compat`.

For a local build, use JDK 21 and run this from the `teavm-compat` root:

```sh
mvn -pl gwt-resources-compat-maven-plugin -am install
```

To use a published version from GitHub Packages, add this to the application's
POM and configure Maven credentials for `github-teavm-compat`:

```xml
<pluginRepositories>
  <pluginRepository>
    <id>github-teavm-compat</id>
    <url>https://maven.pkg.github.com/instanto-io/teavm-compat</url>
  </pluginRepository>
</pluginRepositories>
```

## Stage assets for an application

The `stage-assets` goal copies the files needed by your application's widget
libraries into the folder your web server serves. Each library includes a file
named `META-INF/teavm-assets.properties` telling the plugin which files to copy
and where to put them. For Bootstrap 5, it contains:

```properties
source=META-INF/bootstrap5-assets
target=assets/bootstrap5
```

When the widget library is built, Maven includes this file in its JAR alongside
the CSS, scripts and fonts. Later, when your application is built, `stage-assets`
reads it and copies the contents of `source` into `target` inside your website
folder. With the default settings, that means `target/site/assets/bootstrap5/`.
Application authors do not need to create this file.

The plugin checks the libraries Maven uses to build and run your application,
including their dependencies. It can read JARs and compiled library folders from
the same Maven build.

Add this to your application's POM:

```xml
<plugin>
  <groupId>io.instanto</groupId>
  <artifactId>gwt-resources-compat-maven-plugin</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <executions>
    <execution><goals><goal>stage-assets</goal></goals></execution>
  </executions>
  <configuration>
    <outputDirectory>${project.build.directory}/site</outputDirectory>
  </configuration>
</plugin>
```

Running `mvn package` copies the files during Maven's `prepare-package` step.
The default destination is `target/site`. Put the compiled application and its
HTML page in the same folder. The [standalone example](https://github.com/instanto-io/bootstrap-widgets/tree/main/examples/hello-bootstrap5)
uses one setting, `site.directory`, for all of these files.

To copy just the libraries' browser files, run this from your application:

```sh
mvn io.instanto:gwt-resources-compat-maven-plugin:0.1.0-SNAPSHOT:stage-assets \
  -Dteavm.assets.outputDirectory=/path/to/webroot
```

If your POM already sets `<outputDirectory>`, change that setting instead.
In the standalone example, use `-Dsite.directory=/path/to/webroot`.

The Bootstrap libraries put their files in these folders:

| Library | Folder inside the website |
| --- | --- |
| `teavm-bootstrap3` | `assets/bootstrap3/` |
| `teavm-bootstrap5` | `assets/bootstrap5/` |

Tell the widget library where the browser can find these files before starting
it. For Bootstrap 5, use
`Bootstrap5Resources.setAssetBase("assets/bootstrap5/")`. With this setting, an
application at `https://example.com/my-app/` loads its files from
`https://example.com/my-app/assets/bootstrap5/`. If you serve the files elsewhere,
use that address instead.

The goal prepares files on disk. Your deployment process uploads them or makes
the folder available through your web server. Maven's `deploy` command publishes
Java packages to a Maven repository; use `package` to prepare this website.

The plugin keeps the folder structure intact so stylesheets can still find their
fonts and images. It replaces existing copies of the library files and leaves
other application files alone. It does not remove old files left by a library
upgrade. Use `mvn clean package` for a fresh `target/site`, or an empty destination
folder when preparing a release elsewhere.

If two libraries supply different files for the same destination, the build
stops before copying. Identical copies are accepted. The build also stops if a
library lists a folder that is missing or empty, or if none of the libraries has
an asset configuration file. Paths must stay inside the destination folder;
symbolic links inside that folder are rejected.

## Include browser files in a library

Library authors keep the file at
`src/main/resources/META-INF/teavm-assets.properties`. Maven includes it in the
JAR when it builds the library. For example:

```properties
source=META-INF/example-assets
target=assets/example
```

`source` is the folder inside the JAR containing the browser files. `target` is
where those files should go inside the website. Both paths must name a folder
and be relative: they cannot start with `/` or contain `..`.

The plugin copies files from the named source folder. It skips libraries without
this configuration file. Once a library supplies the file, applications can use
`stage-assets` without writing their own copying instructions. Rebuild and install
older library JARs to include this file before using the goal.

## Generate resource loaders for a library

The `generate` goal reads the shared GWT sources, packages their browser files
into the TeaVM library JAR and generates Java code to load them.

- `<script>` and `<stylesheet>` entries name files to load.
- `<public>` entries name folders to copy, with rules for including or excluding
  files. Their folder structure is preserved.
- ClientBundle interfaces name files using `@Source`. The annotation can contain
  one filename or a list of filenames. The Java filename need not end in
  `ClientBundle`.
- JavaScript files from these bundles are added to the list of scripts to load.
  Other files are copied for the application to use.

The generated code uses `ScriptModule` to load scripts in order and tell the
application when they are ready. The optional `scriptPresence` setting names a
Java method that checks whether the page has already loaded a script.

The application needs `gwt-user-compat`, which supplies `ScriptModule`. Set the
plugin's `resourcesClass` to your library's Java class with static `cssBase()` and
`jsBase()` methods returning the browser addresses for its CSS and scripts.
This setting belongs to the library's build. The existing `idPrefix` setting
is still accepted for compatibility with older build configurations.

`assetRoot` sets the folder used during development. `packagedRoot` sets where
the same files go inside the library JAR. Applications can then copy them using
`stage-assets` and set the browser address as described above. TeaVMTestRunner
can also serve files directly from a JAR through `/resources/`.

Use `inlineTextBundles` for ClientBundle interfaces whose `TextResource` methods
must return text immediately. The plugin generates Java implementations and the
registration files used to find them. Each method must name a single file with
`@Source`; the file's contents are preserved.

Scripts in these bundles are packaged for the application to load when needed.
For example, an editor can load the language selected by the user. Large text
files are split into Java string chunks to fit Java's class-file limits.

The plugin does not rewrite CSS, combine images into sprites or implement GWT's
rules for choosing generated implementations. Applications must also choose
compatible versions when several libraries need the same browser script.
