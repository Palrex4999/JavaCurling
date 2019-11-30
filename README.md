# JavaCurling  
Java Curling Game  
  
## クラスの構成  
より中の階層にいるのは子クラス  
- GameObject  
  - Circle  
    - Stone  
    - Border  
  - Rect  
  
GameObjectには、値をsetするメソッドや, getするメソッドがある  
速度,加速度に従って動くようになっている  
  
- GameModel  
基本的にmodelにゲーム内の変数群やオブジェクト群をぶち込む  
衝突処理も大体ここ  
controllerとかviewでここにいるやつらにアクセスしたり、  
変更したりするのでset、getメソッドが大量にある  
  
- GameViewPanel  
viewは、各オブジェクトのdrawを実行している感じ  
実際に絵を入れたりするときは各オブジェクトのdrawメソッドの部分に直接書くことになると思う
  
- GameFrame ( main )  
実行するときはこいつの名前を書く  
現時点でいじる必要はなさそう？  
タイトル・ゲーム中・クリアのシーン遷移にここを使いそう?  
  
- GameController  
プレイヤー操作を受けつけて、model内の変数を操作する  
model.getObject.setValue();みたいな。  
  
## 各クラスのメソッド
コードの中にコメントたくさん書いたから読んでくれれば多分分かる？  
  
## ダブルクリックで実行させたいときにやること
  
```
javac GameFrame.java
jar cvf Game.jar *.class
jar xvf Game.jar
```
  
までやったら新たに生成された META-INF/MANIFEST.MF のファイル内の空行に以下を追加する  
  
```
Main-Class: GameFrame
(空行)
```
  
そして以下を実行する  
  
```
jar cvfm Game.jar META-INF/MANIFEST.MF *.class
```
  
そして生成されたGame.jarをダブルクリックして実行できるかどうかを確認する  
  
