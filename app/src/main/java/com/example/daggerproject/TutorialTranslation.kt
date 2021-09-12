package com.example.daggerproject

/*

Setup

    Daggerを使用するためのテンプレコードに関する説明。翻訳は省略。



Initial Dagger

    早速、CommandRouterのインスタンスを作るため、Daggerを使ってみましょう！
    @Componentインターフェースを作るところからはじましょう。

        @Component
        interface CommandRouterFactory {
          CommandRouter router();
        }

    CommandRouterFactoryはCommandRouterのための通常のFactoryです。
    このFactoryが実装されれば、Mainメソッドの代わりに、CommandRouter()インスタンス化を行うでしょう。

    そして、@Componentアノテーションをつけることにより、CommandRouterFactoryの実装を
    Daggerが代わりに自動で行ってくれます！名前はDaggerCommandRouterFactoryというように文頭にDaggerがつきます。

    ちなみに、静的メソッドであるCreate()メソッドが自動生成されたFactoryにつきます。
    これは私たちがインスタンス化し、使用できるようにするためです。

        class CommandLineAtm {
            public static void main(String[] args) {
                Scanner scanner = new Scanner(System.in);
                CommandRouterFactory commandRouterFactory =　DaggerCommandRouterFactory.create();
                CommandRouter commandRouter = commandRouterFactory.router();

                while (scanner.hasNextLine()) {
                    commandRouter.route(scanner.nextLine());
                }
             }
         }

     ただ、この情報だけではDaggerはCommandRouterを作れないので、作り方を教えてあげましょう。
     @InjectアノテーションをCommandRouterのコンストラクターにつけて、ここが作り方が書いてある場所だと教えてあげます。

        final class CommandRouter {
          　...
          　@Inject
          　CommandRouter() {}
          　...
        }

      @Injectアノテーションは、次のことを表しています。
      私たちがCommandRouterを必要とした時、DaggerがCommandRouterをインスタンス化するということ。

      まだ特別なことはやっていませんが、DaggerApplicationの素の部分はできました！
      ひとまずRunして、様子をみてみましょう！

    【個人的まとめ】
    → クラスそのもののインスタンス化ではなく、Factoryインターフェースの実装を行ってくれるのが、@Componentで、
      自動生成されたDaggerCommandRouterFactoryという実装クラスからインスタンスを作るための静的メソッドcreate()もつけてくれる。



First Command

    Runしてみたらわかると思いますが、どんなCommandに対しても反応がありません。
    まずは、Commandを実装してみましょう。


        final class HelloWorldCommand implements Command {

            @Inject
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

    次に、CommandRouterのコンストラクターに、上記で実装したCommandを追加してみましょう。

        final class CommandRouter {
            private final Map<String, Command> commands = new HashMap<>();

            @Inject
            CommandRouter(HelloWorldCommand helloWorldCommand) {
                commands.put(helloWorldCommand.key(), helloWorldCommand);
            }

        ...
        }

    この値(HelloWorldCommand)は、Daggerに下記のことを教えます。
    CommandRouterインスタンスが生成された時、DaggerはHelloWorldCommandインスタンスも生成し、コンストラクターに引き渡さないといけないこと。

    DaggerはHelloWorldCommandコンストラクターについたInjectアノテーションによって、HelloWorldCommandインスタンスの作り方を知っています。
    また、これはCommandRouterの作り方をDaggerが知った時と同じです。

    アプリをRunすれば、helloと打った後にworldと返ってくるのがわかるでしょう。ちょっと前身しましたね！

    ※ 上記の例のように、依存関係それ自体が、依存関係をもつ再帰的なもの。
    ※ Injectについて説明する際、インスタンスやオブジェクトを主語にすると混乱の元になるので、Daggerを主語にする。



Depending on Interfaces

    しかし待ってください、なぜCommandRouteのコンストラクターはHelloWorldCommandを特別に必要とするのでしょうか？
    どんなタイプのCommandでも良いのでは？早速そうしてみましょう。

        @Inject
        CommandRouter(Command command) {
          ...
        }

    しかし、現在のDaggerはCommandインスタンスの作り方を知りません。。もしコンパイルしたらDaggerはエラーを吐き出すでしょう。
    Commandはインターフェースで、インターフェースには@Injectアノテーションもついていません。
    私たちはDaggerに対して作り方を知るための情報をもっと与えなくてはなりません。

    それをするため、@Bindアノテーションがつけられたメソッドを書きましょう。

        @Module
        abstract class HelloWorldModule {
            @Binds
            abstract Command helloWorldCommand(HelloWorldCommand command);
        }

    この@Bindsアノテーションがついたメソッドは、何かがCommandに依存した時にHelloWorldCommandオブジェクトを提供するように、Daggerに伝えます。
    例えば、CommandRouterがCommandを依存関係に取り入れた時に、HelloWorldCommandオブジェクトを渡すということです。
    ちなみに、これによりDaggerは「いつ」「どうやって」Commandインスタンスを提供するかを知ることができました。

    Daggerに何をするのか伝えるのかだけで良いので、メソッドは抽象メソッドとなっています。
    Daggerは実際には、このメソッドを呼び出したり、実装を提供するわけではないのです。

    なお、上記の例のように、@Bindsメソッドは、@Moduleアノテーションがつけられた型の中で宣言されます。
    モジュールス(複数形)は、bindingメソッドのコレクションス(複数形)で、Daggerにインスタンス提供の指示書を渡します。
    (bindingメソッドとは、@Bindsアノテーションがつけられたメソッド、あるいは後述するいくつかのメソッドのことを指しています。)
    クラスのコンストラクターに直接設置される@Injectアノテーションとは違い、@Bindsメソッドは必ずモジュールの内側に設置されなければなりません。

    HelloWorldModuleにある@Bindsメソッドを探すようにDaggerに伝える場合、@Componentアノテーションに対して下記のように追記しなければなりません。

        @Component(modules = HelloWorldModule.class)
        interface CommandRouterFactory {
          CommandRouter router();
        }

    これで、CommandRouterクラスは1つのCommand実装クラスだけではなく、複数のCommand継承クラスを依存関係に含むことができるようになりました。


    備考：
        なぜ@Inejctアノテーションには、@Moduleアノテーションは必要ないのでしょうか？
        それは使用される型がすでにコンポーネント暑いは、モジュールの中に明示的に記載されているからです。
        つまり、型認識されてないオブジェクトを@Moduleアノテーションの中に入れているのです。



Abstraction for Output

    現在、HelloWorldCommandは出力のためSystem.out.println()メソッドを使っていますね？
    依存関係注入の精神に乗っ取り、System.outを直接使うのではなく、抽象化してみましょう。
    HelloWorldCommand実装クラスを変更せず、Textの操作ができる柔軟性を持つことを目標にして進めましょう！
    まず私たちがすることは、Textを操作するOutputter型を作ることです。

        interface Outputter {
            void output(String output);
        }

    そして、HelloWorldCommand内で下記のように使用します。

        private final Outputter outputter;

        @Inject
        HelloWorldCommand(Outputter outputter) {
            this.outputter = outputter;
        }

        @Override
        public Status handleInput(List<String> input) {
            outputter.output("world!");
            return Status.HANDLED;
        }

    Outputterはインターフェースです。前述の通り、以下の手順を踏めばこのインターフェースの実装クラスをDaggerに組み込めるでしょう。
        1.実装クラスのコンストラクターに@Injectアノテーションをつける。
        2.@Bindアノテーションを使い、Outputterインターフェースと実装クラスを繋げる。

    しかし、Outputterはとてもシンプルなので、ラムダ式やメソッド参照でも実装できるでしょう。
    なので、上の手順を踏まずに、Outputterインスタンス自身を生成・返却する静的メソッドを記述してみましょう！

        @Module
        abstract class SystemOutModule {
          @Provides
          static Outputter textOutputter() {
            return System.out::println;
          }
        }

    ここでもう一つの@Moduleを作りましたが、@Bindアノテーションの代わりに@Providesメソッドを用いています。
    @Providesメソッドとは、@Injectコンストラクターのように動きます。Outputterインスタンスが必要になった時に、
    DaggerにSystemOutModule.textOutputter()を呼ぶように伝えるのです。

    再度繰り返しますが、コンポーネントに、新しいモジュールを追加する必要があります。
    これによって、DaggerはこのModuleを私たちのApplicationに組み込むのです。

        class CommandLineAtm {
          ...
          @Component(modules = {HelloWorldModule.class, SystemOutModule.class})
          interface CommandRouterFactory {
            CommandRouter router();
          }
        }

    アプリケーションの振る舞いが変化したわけではありませんが、これによってより簡単にコードが書けるようになりましたね。



    @Component　インターフェースを自動実装！
    DaggerXxx　実装されたクラス！生成された実装クラスはApplicationオブジェクト！どっからでも呼び出せる！
    静的createメソッド　実装されたクラスをインスタンス化してくれる便利なやつ！
    @Inject　Daggerにインスタンスの作り方を教えている！
    @Binds　インターフェースを依存関係に使用した時、どの実装クラスを使うかをDaggerに教えてあげるアノテーション！
    @Module　Bindingメソッドを格納したクラスを示す！コンポーネントにこのモジュールの場所を伝えることで、DaggerXxxの自動生成に組み込んでもらう！
             名前の通りモジュールとしての振る舞いを持ち、アプリケーションやコンテクストの中で組み合わせて使用できる。
 　　@Provides　正直ちょっとまだわからないので、保留。

    直接的にInjectする場合は、@Injectアノテーション
    間接的(インターフェースを使って)にInjectする場合は、@Module & &Bindアノテーションを使う！

    ModuleとBindを使った場合でも注入元の実装クラスには、注入元をマークするinjectアノテーションを入れなければいけない。
    @Providesは実装クラスではなく、実装メソッドだけを渡す！


*/