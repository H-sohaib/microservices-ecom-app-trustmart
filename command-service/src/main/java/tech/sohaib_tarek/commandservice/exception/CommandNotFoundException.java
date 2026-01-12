package tech.sohaib_tarek.commandservice.exception;

public class CommandNotFoundException extends RuntimeException {

    public CommandNotFoundException(String message) {
        super(message);
    }
}

