package com.example.daggerproject;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Inject;

import dagger.Component;

//【疑問】DaggerのJavaとKotlinでは使い方が違う？

public class DaggerSetupJava {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        /**
         * CommandRouterFactoryインターフェース(Component)が自動実装されたDaggerCommandRouterFactoryからCommandRouterFactoryをインスタンス化。
         * さらに同Factoryより、CommandRouterをインスタンス化
         * */
        CommandRouterFactory  commandRouterFactory =
                DaggerCommandRouterFactory.create();
        CommandRouter commandRouter = commandRouterFactory.router();


        while (scanner.hasNextLine()) {
            commandRouter.route(scanner.nextLine());
        }
    }
}

final class CommandRouter {

    /**CommandRouterを作成するために、当該クラスのコンストラクターにAnnotationをつける。*/
    @Inject
    CommandRouter() { }


    private final Map<String, Command> commands = Collections.emptyMap();

    Status route(String input) {
        List<String> splitInput = split(input);
        if (splitInput.isEmpty()) {
            return invalidCommand(input);
        }

        String commandKey = splitInput.get(0);
        Command command = commands.get(commandKey);
        if (command == null) {
            return invalidCommand(input);
        }

        Status status =
                command.handleInput(splitInput.subList(1, splitInput.size()));
        if (status == Status.INVALID) {
            System.out.println(commandKey + ": invalid arguments");
        }
        return status;
    }

    private Status invalidCommand(String input) {
        System.out.println(
                String.format("couldn't understand \"%s\". please try again.", input));
        return Status.INVALID;
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


/**
 * CONCEPTS
 *
     * @Component
 *      Daggerにインターフェースあるいは、抽象クラスを実装し、アプリケーションオブジェクトを作成するように指示するアノテーション
     *  tells Dagger to implement an interface or abstract class that creates and returns one or more application objects.
     *  Dagger will generate a class that implements the component type. The generated type will be named DaggerYourType (or DaggerYourType_NestedType for nested types)
     *
     * @Inject
 *      コンストラクタークラスでDaggerにクラスの初期化方法を教えるアノテーション。
     *  on a constructor tells Dagger how to instantiate that class. We’ll see more shortly.
 *
 * */