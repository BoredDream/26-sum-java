package com.training.system.selection.service;

public class SelectionBusinessException extends RuntimeException {
    private final int code;

    public SelectionBusinessException(String message) {
        this(400, message);
    }

    public SelectionBusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
