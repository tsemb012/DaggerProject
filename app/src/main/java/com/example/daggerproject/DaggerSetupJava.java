package com.example.daggerproject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import dagger.Component;

public class DaggerSetupJava {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandRouter commandRouter = new CommandRouter();

        while (scanner.hasNextLine()) {
            commandRouter.route(scanner.nextLine());
        }
    }
}


final class CommandRouter {
    private final Map<String, Command> commands = Collections.emptyMap();

    Command.Status route(String input) {
        List<String> splitInput = split(input);
        if (splitInput.isEmpty()) {
            return invalidCommand(input);
        }

        String commandKey = splitInput.get(0);
        Command command = commands.get(commandKey);
        if (command == null) {
            return invalidCommand(input);
        }

        Command.Status status =
                command.handleInput(splitInput.subList(1, splitInput.size()));
        if (status == Command.Status.INVALID) {
            System.out.println(commandKey + ": invalid arguments");
        }
        return status;
    }

    private Command.Status invalidCommand(String input) {
        System.out.println(
                String.format("couldn't understand \"%s\". please try again.", input));
        return Command.Status.INVALID;
    }

    // Split on whitespace
    private static List<String> split(String string) {  ...  }
}

interface Command {

    String key();
    Status handleInput(List<String> input);

    enum Status {
        INVALID,
        HANDLED
    }
}