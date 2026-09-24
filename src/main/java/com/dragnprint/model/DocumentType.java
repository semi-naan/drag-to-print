package com.dragnprint.model;

import java.nio.file.Path;
import java.util.Locale;

public enum DocumentType {
    PDF("PDF document", false, true),
    IMAGE("Image", false, true),
    DOCX("Word document (.docx)", true, true),
    TEXT("Text document", true, true),
    DOC("Word 97-2003 (.doc, read-only)", false, true),
    ODT("OpenDocument Text (.odt, read-only)", false, true),
    UNSUPPORTED("Unsupported file", false, false);

    private final String description;
    private final boolean editable;
    private final boolean supported;

    DocumentType(String description, boolean editable, boolean supported) {
        this.description = description;
        this.editable = editable;
        this.supported = supported;
    }

    public String description() {
        return description;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isSupported() {
        return supported;
    }

    public static DocumentType classify(Path path) {
        if (path == null || path.getFileName() == null) {
            return classify((String) null, null);
        }
        return classify(path.getFileName().toString(), null);
    }

    public static DocumentType classify(String fileName) {
        return classify(fileName, null);
    }

    public static DocumentType classify(String fileName, String mimeType) {
        DocumentType byExtension = fromFileName(fileName);
        if (byExtension != null) {
            return byExtension;
        }
        DocumentType byMime = fromMimeType(mimeType);
        return byMime != null ? byMime : UNSUPPORTED;
    }

    private static DocumentType fromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "pdf" -> PDF;
            case "png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff" -> IMAGE;
            case "docx" -> DOCX;
            case "txt", "md", "markdown", "text" -> TEXT;
            case "doc" -> DOC;
            case "odt" -> ODT;
            default -> null;
        };
    }

    private static DocumentType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        String normalized = mimeType.toLowerCase(Locale.ROOT).trim();
        int semicolon = normalized.indexOf(';');
        if (semicolon >= 0) {
            normalized = normalized.substring(0, semicolon).trim();
        }
        if (normalized.startsWith("image/")) {
            return IMAGE;
        }
        return switch (normalized) {
            case "application/pdf" -> PDF;
            case "application/msword" -> DOC;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> DOCX;
            case "application/vnd.oasis.opendocument.text" -> ODT;
            case "text/plain", "text/markdown", "text/x-markdown" -> TEXT;
            default -> null;
        };
    }
}
