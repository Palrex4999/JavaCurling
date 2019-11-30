# JavaCurling  
Java Curling Game  
  
# クラスの構成  
より中の階層にいるのは子クラス  
- GameObject  
  - Circle  
    - Stone  
    - Border  
  - Rect  
  
- GameModel  
基本的にmodelにゲーム内の変数群やオブジェクト群をぶち込む  
衝突処理も大体ここ  
  
- GameViewPanel  
viewは、各オブジェクトのdrawを実行している感じ  
実際に絵を入れたりするときは各オブジェクトのdrawメソッドの部分に直接書くことになると思う
  
- GameFrame ( main )
現時点でいじる必要はなさそう？  
タイトル・ゲーム中・クリアのシーン遷移にここを使いそう?  
  
- GameController  
プレイヤー操作を受けつけて、model内の変数を操作する  
model.object.setValue();みたいな。  
  
# 各クラスのメソッド
 コメントたくさん書いたから読んで  
 
