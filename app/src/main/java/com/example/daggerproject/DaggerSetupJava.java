package com.example.daggerproject;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

    //private final Map<String, Command> commands = Collections.emptyMap();
    private final Map<String, Command> commands = new HashMap<>();

    /**CommandRouterを作成するために、当該クラスのコンストラクターにAnnotationをつける。
     *
     * 引数としてインスタンスを注入する場合、@Injectを２箇所に設置する。
     * 1つは、注入先のコンストクター(コンストラクターの引数に対象クラスを記載されているはず。)
     * 2つは、注入物として選択されたクラスのコンストラクター
     * この2つのインジェクトアノテーションにより、Daggerはどこに何を注入するのかを特定する。
     */

    @Inject
    CommandRouter(HelloWorldCommand helloWorldCommand) {//注入先のコンストラクター
        commands.put(helloWorldCommand.key(), helloWorldCommand);
    }

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


final class HelloWorldCommand implements Command {//Commandインターフェースを実装

    @Inject //注入物のコンストラクター
    HelloWorldCommand() {}

    @Override
    public String key() {
        return "hello";
    }

    @Override
    public Status handleInput(List<String> input) {
        if (!input.isEmpty()) {
            return Status.INVALID;
        }
        System.out.println("world!");
        return Status.HANDLED;
    }

}


/**
 * CONCEPTS
 *
 * @Component
 *  Daggerにインターフェースあるいは、抽象クラスを実装し、アプリケーションオブジェクト(DaggerXXX)を作成するように指示するアノテーション
 *  tells Dagger to implement an interface or abstract class that creates and returns one or more application objects.
 *  Dagger will generate a class that implements the component type. The generated type will be named DaggerYourType (or DaggerYourType_NestedType for nested types)
 *
 * @Inject
 *  コンストラクタークラスでDaggerにクラスの初期化方法を教えるアノテーション。
 *  on a constructor tells Dagger how to instantiate that class. We’ll see more shortly.
 *
 *
     * インジェクトコンストラクターでコンストラクターの中に入れる値は依存関係にあるクラスである。
     * Daggerはクラスを初期化するための依存関係を提供する。
     *
     * コマンドルーターがハローワールドコマンドをInjectするという言い方もできるが、この言い回しは混乱の元になるため、
     * Daggerのチュートリアルでは、DaggerがCommandRouterを初期化するため、@Injectコンストラクターを用いて、CommandRouterそれ自体にInjectするという明示的な言い方で統一する。
 *
 * */