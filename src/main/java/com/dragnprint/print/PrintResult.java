package com.dragnprint.print;

public record PrintResult(boolean success, String message, int pagesPrinted) {
    public static PrintResult success(int pagesPrinted) {
        return new PrintResult(true, "Sent " + pagesPrinted + " page(s) to the printer", pagesPrinted);
    }

    public static PrintResult partial(int pagesPrinted, int failed) {
        return new PrintResult(false, "Printed " + pagesPrinted + " page(s); " + failed + " failed", pagesPrinted);
    }

    public static PrintResult failure(String message) {
        return new PrintResult(false, message, 0);
    }
}
