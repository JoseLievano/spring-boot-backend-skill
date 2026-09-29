package com.agentForgeBackend.exceptions;

public class InvalidQueryRequestException extends Exception {

    public InvalidQueryRequestException(String message) {
        super(message);
    }

    public InvalidQueryRequestException() {
        super("Invalid query request");
    }
}
