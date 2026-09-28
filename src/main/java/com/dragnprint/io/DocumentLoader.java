package com.dragnprint.io;

import com.dragnprint.model.DocumentModel;
import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class DocumentLoader {
    private final DocumentReaders readers;

    public DocumentLoader(DocumentReaders readers) {
        this.readers = Objects.requireNonNull(readers, "readers");
    }

    public record Result(List<DocumentModel> documents, List<String> notices, boolean cancelled) {
    }

    public Result load(List<File> files, BooleanSupplier cancelled) {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(cancelled, "cancelled");
        List<DocumentModel> loaded = new ArrayList<>();
        List<String> notices = new ArrayList<>();
        boolean wasCancelled = false;
        boolean handedOff = false;
        try {
            for (File file : files) {
                if (cancelled.getAsBoolean()) {
                    wasCancelled = true;
                    break;
                }
                if (file == null) {
                    continue;
                }
                if (file.isDirectory()) {
                    notices.add(file.getName() + " is a folder, so it was skipped.");
                    continue;
                }
                DocumentType type = DocumentType.classify(file.toPath());
                if (!type.isSupported()) {
                    notices.add("Unsupported file type: " + file.getName());
                    continue;
                }
                try {
                    Path path = file.toPath();
                    RenderedDocument rendered = readers.read(path);
                    try {
                        loaded.add(new DocumentModel(path, type, rendered,
                                type == DocumentType.DOCX ? WordDocumentFormat.read(path) : null));
                    } catch (Exception e) {
                        rendered.close();
                        throw e;
                    }
                } catch (OutOfMemoryError e) {
                    notices.add(file.getName() + " could not be opened: out of memory.");
                    break;
                } catch (Exception e) {
                    notices.add("Could not open " + file.getName() + ": " + e.getMessage());
                }
            }
            if (wasCancelled) {
                closeAll(loaded);
                loaded.clear();
            }
            Result result = new Result(List.copyOf(loaded), List.copyOf(notices), wasCancelled);
            handedOff = true;
            return result;
        } finally {
            if (!handedOff) {
                closeAll(loaded);
            }
        }
    }

    private void closeAll(List<DocumentModel> documents) {
        for (DocumentModel document : new ArrayList<>(documents)) {
            document.close();
        }
    }
}
