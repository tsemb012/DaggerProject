package com.example.daggerproject;

import dagger.Component;

/**
 * Daggerにインターフェースあるいは、抽象クラスを実装し、アプリケーションオブジェクトを作成するように指示するアノテーション
 * CommandRouterFactoryに@Componentをつけて、CommandRouterFactoryのインスタンス化をするよう教える。
 * Moduleの中に実装処理を追加する。
 * **/
@Component
interface CommandRouterFactory {
    CommandRouter router();
}