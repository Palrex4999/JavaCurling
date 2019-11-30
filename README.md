# JavaCurling  
Java Curling Game  
  
# クラスの構成  
- GameObject  
-- Circle  
--- Stone  
--- Border  
-- Rect  
  
- GameModel  
基本的にmodelにゲーム内の変数をぶち込む  
処理も大体ここ  
  
- GameViewPanel  
drawを書く感じだが、実際に絵をぶち込む場合はたぶん上に書いたStoneとかRectのdrawの中にぶち込む感じ  
  
- GameFrame ( main )
現時点でいじる必要はなさそう？  
タイトル・ゲーム中・クリアのシーン遷移にここを使いそう?  
  
- GameController  
プレイヤー操作を受けつけて、model内の変数を操作する  
  
