# Drag&Print

Drag&Print is a desktop drag-and-drop print utility. Drop a PDF, an image, or a
document into the window, preview it, optionally edit text documents, and send
it to a printer — all from one place. The **Print** button is always in the
upper-right corner.

Built with JavaFX 25 on Java 27, with Apache PDFBox for PDF rendering and
Apache POI / odfdom for office documents.

## Features

- **Drag and drop anywhere** — drop one or more local files onto the window.
  The document list fills in, and the last opened document is selected.
- **Preview before printing** — images are scaled to fit; PDFs render page by
  page with previous/next navigation; text documents show their content.
- **Print from the upper-right** — the blue **Print** button opens a print
  preview dialog where you choose a page range and number of copies, then
  confirm. Printing sends the pages you selected.
- **Edit documents in place** — `.docx` and text files are editable in the
  right-hand pane. Unsaved changes are tracked and marked in the list.
- **Save As / Overwrite** — saving an edited document asks whether to
  **Save As…** a new file, **Overwrite** the original, or **Cancel**. The same
  prompt appears when you switch documents or close the window with unsaved
  changes.
- **Non-blocking feedback** — status messages appear in the status bar and as
  transient toasts; errors still open a dialog.

## Supported formats

| Format | Extensions | Behaviour |
| --- | --- | --- |
| Word (modern) | `.docx` | Editable (text-level) and printable |
| Text | `.txt`, `.md`, `.markdown`, `.text` | Editable and printable |
| PDF | `.pdf` | Preview and print only (not editable) |
| Images | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tif`, `.tiff` | Preview and print only |
| Word 97-2003 | `.doc` | Read-only preview and print |
| OpenDocument Text | `.odt` | Read-only preview and print |

Any other extension, a file with no extension, a folder, or a URL/HTML drop
(for example a link dragged from a browser) is reported with a notice instead
of being opened. **Google Docs are not supported directly** — export them to a
local `.docx`, `.pdf`, or text file first, then drop that file in.

## Requirements

- **JDK 27 or newer** (`java` / `javac` on `PATH`). The project compiles with
  `--release 27`.
- The Maven wrapper is checked in, so a separate Maven install is not required.
- The first build downloads dependencies from Maven Central, so it needs
  network access.
- A graphical display is required to run the app (it is a JavaFX desktop app).
- A working printer is required to actually print; previewing works without one.

## Build and run

From the project root:

```bash
# Run the application (development launch; requires a display)
./mvnw javafx:run

# Build and test
./mvnw -q clean test

# Build the jar into target/
./mvnw -q clean package
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

> The jar is a thin jar and bundles neither JavaFX nor the other libraries, so
> `java -jar target/dragnprint-*.jar` will fail with a "JavaFX runtime
> components are missing" style error. Launch with `./mvnw javafx:run`.
>
> The JavaFX dependency is declared without a classifier; OpenJFX selects the
> native libraries for the current operating system at build time. **The build
> is therefore per-OS** — rebuild on each platform you target.

## How to use

1. **Open a document.** Drag one or more supported files onto the window, or
   drop them onto the list on the left. The file appears under
   **OPEN DOCUMENTS** and is selected automatically.
2. **Preview it.** The center pane shows the document. For PDFs, use the
   left/right arrow buttons to move between pages.
3. **Edit it (optional).** If the document is editable (`.docx`, `.txt`, `.md`),
   an editor opens on the right. Type your changes; the document is marked as
   modified in the list.
4. **Save your changes (optional).** Click **Save** in the top bar. Drag&Print
   asks whether to:
   - **Save As…** — pick a new file (the extension must match the document
     type, e.g. `.docx` for a Word document, `.txt`/`.md` for text), or
   - **Overwrite** — replace the original file, or
   - **Cancel** — keep editing.
5. **Print.** Click the blue **Print** button in the upper-right. The print
   preview opens, showing the pages and printer. Enter a page range (`all`, a
   single page like `2`, or a range like `1-3`) and the number of copies
   (1–99), then press **Print**. Press **Cancel** to close the dialog without
   printing.
6. **Close a document.** Select it and click **Close document** in the left
   panel. If it has unsaved changes you will be prompted to save first.

## Limitations

- PDFs and images are **preview and print only** — they cannot be edited in the
  app.
- Editing `.docx` is **text-level**: saving rewrites the text content and does
  not preserve the original formatting (fonts, styles, images, layout).
- `.doc` and `.odt` files can be previewed and printed but not edited.
- There is no Google Docs / cloud integration; only local files are supported.
- Printing is exercised in tests through a fake print service; it has not been
  integration-tested against a physical printer here.

## Project layout

```
src/main/java/com/dragnprint/
  Launcher.java, DragNPrintApp.java   entry points
  model/       document types and the open-document model
  io/          readers/writers and the background document loader
  preview/     preview pane and page providers (lazy PDF rendering)
  edit/        text editor and the save-conflict dialog
  print/       print services, options, and the print preview dialog
  ui/          main window, drag target, theme, icons, toasts
  util/        text pagination
src/main/resources/com/dragnprint/
  dragnprint.css                      application theme
src/test/java/com/dragnprint/         JUnit 5 tests (headless)
```

## Development

```bash
# Run all tests (headless; no display or printer required)
./mvnw -q clean test

# Run a single test class
./mvnw -q test -Dtest=DocumentTypeTest

# Package
./mvnw -q clean package
```

Notes for contributors:

- Do not add comments to Java source unless asked.
- Keep the Maven wrapper pinned (`./mvnw`, currently Maven 3.9.16).
- No lint or formatter is configured.
- See `AGENTS.md` for the fuller architecture and toolchain notes.

If you see `Log4j API could not find a logging provider` or a
`restricted method ... native-access` warning on Java 27, both are harmless and
do not affect the application.
