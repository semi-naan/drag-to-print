# AGENTS.md

## Status

Implemented JavaFX desktop application with Maven build, JUnit 5 tests, and a
Maven wrapper. Source lives under `src/main/java/com/dragnprint` and tests under
`src/test/java/com/dragnprint`.

## Project

Drag&Print — a drag-and-drop print utility written in Java. The display name is
`Drag&Print`; Java identifiers keep `DragNPrint`/`dragnprint` because `&` is
illegal in packages and class names.

- GUI: JavaFX 25.0.4 (`javafx-controls`, `javafx-swing`); entrypoint
  `com.dragnprint.Launcher` (jar) / `com.dragnprint.DragNPrintApp` (JavaFX).
- UI theme: `src/main/resources/com/dragnprint/dragnprint.css`, applied via
  `ui/Theme`; helpers `ui/Icons` (CSS-shaped icon regions) and `ui/Toast`
  (transient notifications).
- PDF rendering: Apache PDFBox 3.0.8 (`Loader.loadPDF`, `PDFRenderer`).
  Pages are rendered lazily via `io/LazyPdfPageProvider` with a bounded
  (current ± 1) cache, so large PDFs are not fully rasterized into memory.
- Documents are read off the JavaFX Application Thread via a background
  `javafx.concurrent.Task`; the UI shows a busy indicator. Parsing is done by
  `io/DocumentLoader`, which closes any documents produced by a cancelled or
  failed load so file handles are not leaked.
- Printing: `javafx.print.PrinterJob` behind `PrintService`; `FakePrintService`
  is used by headless tests.
- The JavaFX dependency is declared without a classifier; the OS-specific
  classifier comes from OpenJFX's OS-activated Maven profiles. The build is
  therefore per-OS (do not hardcode a classifier).

### Supported formats

- Editable (text-level): `.docx` (Apache POI `poi`/`poi-ooxml`),
  `.txt` / `.md` / `.markdown`.
- Preview + print only: `.pdf`; images `.png` / `.jpg` / `.jpeg` / `.gif` /
  `.bmp` / `.tif` / `.tiff`; `.doc` (Apache POI `poi-scratchpad`);
  `.odt` (odfdom 0.12.0).
- Unsupported: any other extension (or none) is skipped with a user-visible
  notice; URL/HTML drops that are not local files are reported too.

## Toolchain (verified on this machine)

- Java: OpenJDK 27 (`java` / `javac` on PATH; Maven reports `java 27`, Arch build).
- Build: **Apache Maven 3.9.16** is installed (`mvn`, `MAVEN_HOME=/usr/share/java/maven`).
  Gradle 9.7.1 is also present, but prefer Maven for this project.
- The Maven wrapper is checked in (`mvnw`, `.mvn/wrapper`), pinned to Maven
  3.9.16; regenerate it with `mvn -N wrapper:wrapper -Dmaven=3.9.16`.

## Commands

Use the Maven wrapper (`./mvnw`, pinned to Maven 3.9.16). Verified on this
machine:

- build: `./mvnw -q clean package` (succeeds; jar in `target/`)
- run: `./mvnw javafx:run` (dev launch; requires a display)
- test: `./mvnw -q clean test` (66 tests pass)
- test (single test): `./mvnw -q test -Dtest=DocumentTypeTest`
- lint / format: none configured

## Conventions

- Keep this file in sync when build tooling, entrypoints, or package layout change.
- Do not add comments to Java source unless asked.
- Commit only when explicitly requested.
