# JavaCurling  
Java Curling Game  
  
## クラスの構成  
より中の階層にいるのは子クラス  
- GameObject  
  - Circle  
    - Stone  
    - Border  
  - Rect  
  
- GameModel  
基本的にmodelにゲーム内の変数群やオブジェクト群をぶち込む  
衝突処理も大体ここ  
controllerとかviewでここにいるやつらにアクセスしたり、  
変更したりするのでset関数、get関数が大量にある  
  
- GameViewPanel  
viewは、各オブジェクトのdrawを実行している感じ  
実際に絵を入れたりするときは各オブジェクトのdrawメソッドの部分に直接書くことになると思う
  
- GameFrame ( main )
現時点でいじる必要はなさそう？  
タイトル・ゲーム中・クリアのシーン遷移にここを使いそう?  
  
- GameController  
プレイヤー操作を受けつけて、model内の変数を操作する  
model.getObject.setValue();みたいな。  
  
## 各クラスのメソッド
 コードの中にコメントたくさん書いたから読んでくれれば多分分かる？  
 
