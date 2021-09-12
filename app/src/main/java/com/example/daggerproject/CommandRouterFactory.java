package com.example.daggerproject;

import dagger.Component;

/**
 * Daggerにインターフェースあるいは、抽象クラスを実装し、アプリケーションオブジェクト(DaggerCommandRouterFactory)を作成するように指示するアノテーション
 * Moduleの中に実装処理を追加する。
 * **/
@Component(modules = {HelloWorldModule.class, SystemOutModule.class})
interface CommandRouterFactory {
    CommandRouter router();
}