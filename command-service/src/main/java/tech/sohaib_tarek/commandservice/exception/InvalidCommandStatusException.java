package tech.sohaib_tarek.commandservice.exception;

public class InvalidCommandStatusException extends RuntimeException {

    public InvalidCommandStatusException(String message) {
        super(message);
    }
}

