package com.dragnprint.io;

import java.io.IOException;

public class UnsupportedDocumentException extends IOException {
    public UnsupportedDocumentException(String message) {
        super(message);
    }

    public UnsupportedDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
