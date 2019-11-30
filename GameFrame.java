import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/* 描画した図形を記録する GameObject クラス (継承して利用する) */
class GameObject {
  
  /* 座標、幅、高さ */
  protected int x, y, width, height, r1, r2;
  
  /* 速度, 加速度*/
  protected float vx = 0.0f, vy=0.0f, ax=0.0f, ay=0.0f;

  /* 摩擦係数 */
  protected float k = 0.4f;

  /* 速度停止時のしきい値 */
  protected float FIXED = 0.3f;
  
  /* 色 */
  protected Color color;

  /* 楕円・ 四角形を描く場合 */
  public GameObject( int x, int y, int w, int h, Color c ) {
    this.x = x; this.y = y;  
    this.width = w; this.height = h;   
    this.color = c;     
  }
  
  /* 円を描く場合 */
  public GameObject( int x, int y, int r, Color c ) {
    this.x = x; this.y = y;
    this.r1 = r; color = c;
    this.r2 = ( 2 * r ) / 3;
  }

  /* gets */
  public int getX() { return x; }
  public int getY() { return y; }
  public int getW() { return width; }
  public int getH() { return height; }
  public int getR() { return r1; }
  public float getK(){ return k; }
  public float getVX() { return vx; }
  public float getVY() { return vy; }
  public float getAX() { return ax; }
  public float getAY() { return ay; }
  public Color getC() { return color; }

  /* sets */
  /* 速度をセットする関数 */
  public void setV( float vx, float vy ) {
    this.vx = vx; this.vy = vy;
  }
  /* こすった時の加速度をセットする関数(後で使うかも？) */
    public void setNewAY( float newK ) {
      this.k = newK;
      this.ay = k*-sgn(vy);
  }

  /* 加速度として摩擦係数をかけたものを実行する */
  /* ブラシをかける人も多分一緒に動くのでGameObjectクラスに置いておいた */
  public void startSliding() {
    this.ay = k*-sgn(vy);
  }

  /* 描画の指定方法が 1.widthとheight 2.rのみ など様々なのでdrawは各々のクラスでオーバーライドする */
  public void draw(Graphics g) { }
  
  /* 速度、加速度の概念に基づいたmove */
  public void move() {

     x += vx;
     y += vy;
    /* vyの制御 */
    if( sgn( vy ) != 0 ){
       if( vy * sgn( vy ) > FIXED )
        vy += ay;
       else
        vy = 0.0f;
    }else{
       vy = 0.0f;
    }
     /* vxの制御 */
    if( sgn( vx ) != 0 ) {

      if( vy * sgn( vx ) > FIXED )
       vx += ax;
      else
       vx = 0.0f;

    }else{
      vx = 0.0f;
    }

  }

  /* 符号関数 正なら1,負なら-1,0なら0を返す */
  private int sgn( float a ){
    if( a > 0 ) return 1;
    else if( a < 0 ) return -1;
    else return 0;
  }

}

class Circle extends GameObject {

  public Circle( int x, int y, int r, Color c ) {
    super( x, y, r, c );
  }

  /* 中心点と半径を指定して円を描くパターンを新しく追加 */
  public void drawCircle( Graphics g, int x, int y, int r ) {
    g.drawOval( x - r/2, y - r/2, r, r );
  }
  public void fillCircle( Graphics g, int x, int y, int r ) {
    g.fillOval( x - r/2, y - r/2, r, r );
  }

  public void draw( Graphics g ) {
    g.setColor( color );
    drawCircle( g, x, y, r1 );
  }

}

class Stone extends Circle {
  /* 
      width,height: stoneの最も外側の大きい半径
      r2: stoneの内側の円の半径
      color: １P,２Pを判別する
  */

  /* すでに衝突したのかどうかを判定する */
  protected boolean isCollided = false;

  public Stone( int x, int y, int r, Color c ) {
    super( x, y, r, c );      
  }

  public void draw( Graphics g ) {
    g.setColor( Color.darkGray );
    fillCircle( g, x, y, r1 );
    g.setColor( color );
    fillCircle( g, x, y, r2 );
  }
  
  /*get*/
  public boolean getCollided() { return isCollided; }
  
  /*set*/
  public void setCollided( boolean b ) { isCollided = b; } 

}

class Border extends Circle {
  public Border( int x, int y, int r, Color c ) { 
    super( x, y, r, c );
  }

  public void draw( Graphics g ) {

    /* 線の幅を20に設定する */
    Graphics2D g2 = (Graphics2D) g;
    BasicStroke bs = new BasicStroke(20);
    g2.setStroke(bs);
    g2.setColor( color );
    drawCircle( g2, x, y, r1 );

  }
}

class Rect extends GameObject {
  public Rect( int x, int y, int w, int h, Color c ) { 
    super( x, y, w, h, c );
  }

  public void draw( Graphics g ) {
    g.setColor( color );
    g.fillRect( x, y, width, height );
  }
}
////////////////////////////////////////////////
// Model (M)

// modelは java.util.Observableを継承する．Viewに監視される．
class GameModel extends Observable implements ActionListener{

  /* GameObjectを格納するArrayList */
  protected ArrayList<GameObject> gameObjs;
  protected ArrayList<Stone> stones;

  /* 主体となって動かすターゲット・ストーン*/
  protected Stone targetStone;
  /* 現在の色 */
  protected Color currentColor;

  /* 1pのターンかどうか */
  protected boolean is_Player1_turn = true;
  /* ターン遷移OKかどうか */
  protected boolean canChangeTurn = false;

  /* ホイールを回転させてよいかどうか */
  /* 値を読み終わった後にもう1度読み込んで加速するのを防ぐ */
  protected boolean canRot = true;
  
  /* こすってよいかどうか */
  protected boolean canRub = false;

  /* 回転値 */
  protected float rotation = 0.0f;

  /* ウインドウのwidth, height */
  private int WINDOW_WIDTH = 1200;
  private int WINDOW_HEIGHT = 800;

  /* 時間を管理する */
  javax.swing.Timer timer;
  
  /* コンストラクタ */
  public GameModel() {
    gameObjs = new ArrayList<GameObject>();
    stones = new ArrayList<Stone>();
    timer = new javax.swing.Timer( 60, this );
    targetStone = null;
    currentColor = Color.red;
    timer.start();
  }

  /* gets */
  public int getW() { return WINDOW_WIDTH; }
  public int getH() { return WINDOW_HEIGHT; } 
  public boolean getCanRot() { return canRot; }
  public boolean getCanRub() { return canRub; }
  public boolean getCanChangeTurn() { return canChangeTurn; }
  public float getRotation() { return rotation; }
  
  public ArrayList<GameObject> getGameObjects() {
    return gameObjs;
  }
  
  public ArrayList<Stone> getStones() {
    return stones;
  }
  
  public GameObject getGameObject( int idx ) {
    /* ０未満、size以上のindexを指定したらnullを返す */
    if(gameObjs.size() <= idx || idx < 0 )
      return null;
    else
      return gameObjs.get( idx );
  }

  public Stone getStone( int idx ) {
    /* ０未満、size以上のindexを指定したらnullを返す */
    if(stones.size() <= idx || idx < 0 )
      return null;
    else
      return stones.get( idx );
  }
  
  public Stone getTargetStone() {
    return targetStone;
  }

  /* sets*/
  public void setTargetStone( Stone s ){
    targetStone = s;
  }

  public void setColor( Color c ) {
    currentColor = c;
  }

  public void setCanRot( boolean b ) { this.canRot = b; }
  public void setCanRub( boolean b ) { this.canRub = b; }
  public void setCanChangeTurn( boolean b ) { this.canChangeTurn = b; }
  public void setRotation( float rot ) { this.rotation = rot; }


  /* null check用の関数 */
  public boolean isNull( Object o){
    return ( o == null );
  }
  /* ストーンが外側にいるかどうか */
  public boolean isOut( Stone s ){
    /* 判定基準は上に突き抜けたかどうかのみ. のちに要検証 */
    return ( s.getY() < 0 );
  }

  /* ストーンを生成する関数*/
  public void createStone( int x, int y, int r ) {
    Stone newStone = new Stone( x, y, r, currentColor );
    stones.add( newStone );
    targetStone = newStone;
    setChanged();
    notifyObservers();
  }
  /* クラス名を指定してオブジェクトを生成する関数, 半径を指定するバージョン */
  public void createObject( String className, int x, int y, int r, Color c ) {
    GameObject newObj;
    switch( className ){
      case "Border":
            newObj = new Border( x, y, r, c );
            break;
       /* 他のオブジェクトはここに追加 */
      default:
            return;
    }
    gameObjs.add( newObj );
    setChanged();
    notifyObservers();
  }
  /* クラス名を指定してオブジェクトを生成する関数, 幅と高さを指定するバージョン */
  public void createObject( String className, int x, int y, int w, int h, Color c ) {
    GameObject newObj;
    switch( className ){
      case "Rect":
            newObj = new Rect( x, y, w, h, c );
            break;
      /* 他のオブジェクトはここに追加 */
      default:
            return;
    }
    gameObjs.add( newObj );
    setChanged();
    notifyObservers();
  }

  /* timerが動くたびにactionPerformedは勝手に呼び出される 1/60秒に1回 */
  public void actionPerformed ( ActionEvent e ) {
    /* 初めのストーンのY座標 */
    int stone_initY = 700;

    /* 
    ターン遷移してよいかチェックする
    <summary>
    ターン遷移は全てのストーンの速度が０のときに起こす、としているが
    初期状態もすべてのストーンの速度が０となってしまうので、初期座標にいるときはチェックしないことにしている
    </summary>
    */
    if( !isNull( this.getTargetStone() ) ){
      if( this.getTargetStone().getY() != stone_initY )
        canChangeTurn = turnCheck();
    
    /* 衝突判定 */
    collisionCheck();
    /* 外側のストーン除去 */
    outerStoneRemove();
      
    /* ターン遷移して良い状態になったら */
    if( getCanChangeTurn() ){
      changeTurn();
      this.createStone( this.getW()/2 , stone_initY, 50 );
      setCanRot( true );
      setCanChangeTurn( false );
    }

    /* ここで observer(すなわちview) の update関数を呼ぶ */
    setChanged();
    notifyObservers();
  }
  public void collisionCheck(){
    /*
      int size 
      衝突処理途中でストーンが外側に行ってArrayListのサイズが減ってしまって,
      処理しなくなるものが出てくるので、先に保持しておく
    */
    int size = this.getStones().size();
    for ( int i = 0; i < size; i++ ) {
      if ( isNull( this.getStone( i ) ) )
        continue;

      for ( int j = i + 1; j < size; j++ ){
        if( isNull( this.getStone( j ) ) )
          continue;
        
        twoStoneCollision( this.getStone( i ), this.getStone( j ) );

      }
    }
  }
  /* 「 ２つの円の距離 ー ２つの円の半径 が 0以下 かどうか 」によって接しているかどうかを導出する*/
  public boolean isTouched(int x1, int y1, int x2, int y2, int r1, int r2 ) {
    return ( Math.sqrt( Math.pow( x1 - x2, 2 ) + Math.pow( y1 - y2, 2 ) ) - r1/2 - r2/2 <= 0.0f );
  }

  public void twoStoneCollision( Stone s1, Stone s2 ) {
    /* ２つのストーンが接したとき */
    if( isTouched(s1.getX(), s1.getY(), s2.getX(), s2.getY(), s1.getR(), s2.getR() ) ){
      /*
      <summary> 
      その２つのストーンが「どちらも既に衝突済み」だったら飛ばす
      速度が速すぎてストーン同士が埋もれるような状態になったとき常に衝突判定してしまう。
      それを避けるのための例外処理
      </summary>
      */
      if( !s1.getCollided() || !s2.getCollided () ){
        /* 互いの速度を入れ替える */
        float tmpVX = s1.getVX();
        float tmpVY = s1.getVY();
        s1.setV( s2.getVX(), s2.getVY() );
        s2.setV( tmpVX, tmpVY );
        /* 「衝突済み」にする */
        s1.setCollided( true );
        s2.setCollided( true );
      }
    }
  }
  /* ターン遷移OKならtrue, さもなくばfalseを返す関数 */
  public boolean turnCheck(){
    boolean isOK = true;
    for(Stone stone : this.getStones() ) {
      /* 一応現時点ではすべてのストーンの速度が０になっている時にターン遷移OKとしている */
      if( stone.getVX() != 0.0f || stone.getVY() != 0.0f ){
        isOK = false;
      }
    }
    return isOK;
  }
  /* ターン遷移する関数 */
  public void changeTurn() {

    /* こするの禁止！ */
    setCanRub( false );

    /* ターゲットストーン を null に */
    setTargetStone( null );

    /* ターン切り替え */
    is_Player1_turn = !is_Player1_turn;

    /* 色変更 */
    currentColor = (is_Player1_turn) ? Color.red : Color.yellow;

    /* すべてのストーンを「衝突済みでない」状態にする */
    for( Stone stone : this.getStones() )
      stone.setCollided( false );

  }

  /* 外側のストーンを削除する関数 */
  public void outerStoneRemove(){
    int size = this.getStones().size();
    for( int i = 0; i < size; i++ )
      if( !isNull( this.getStone( i ) ) )
        if( isOut( this.getStone( i ) ) )
          this.getStones().remove( i ); /* 外側にいるストーンを取り除く */
  }

}

////////////////////////////////////////////////
// View (V)

// Viewは，Observerをimplementsする．Modelを監視して，
// モデルが更新されたupdateする．実際には，Modelから
// update が呼び出される．
class GameViewPanel extends JPanel implements Observer {
  protected GameModel model;
  public GameViewPanel( GameModel m, GameController c ) {
    this.setBackground( Color.white );
    this.addMouseListener( c );
    this.addMouseMotionListener( c );
    this.setFocusable( true );
    this.addKeyListener( c );
    model = m;
    model.addObserver( this );
  }
  public void paintComponent( Graphics g ) {
    super.paintComponent( g );
    
    for( GameObject gameObj : model.getGameObjects() ) {
      gameObj.draw( g );
    }
    for( Stone stone : model.getStones() ) {
      stone.draw( g );
    }
  }
  /* 1/60秒で呼ばれる */
  public void update( Observable o, Object arg ) {

    /* 全オブジェクトを物理演算の対象にする,これはmodelで動かしてもいいかも */
    for(GameObject gameObj : model.getGameObjects() ) {
      gameObj.move();
    }
    for(Stone stone : model.getStones() ) {
      stone.move();
    }
    repaint();
  }
}

//////////////////////////////////////////////////
// main class

class GameFrame extends JFrame {
  GameModel model;
  GameViewPanel view1;
  GameController cont;
   public GameFrame() {
      model=new GameModel();
      cont =new GameController( model );
      view1 = new GameViewPanel( model, cont );
      this.setBackground( Color.black );
      this.setTitle( "JAVA CURLING GAME" );
      this.setSize( model.getW(), model.getH() );
      this.add( view1 );
      this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      this.setVisible( true );
    }
    public static void main( String argv[] ) {
      new GameFrame();
   }
}

////////////////////////////////////////////////
// Controller (C)

class GameController implements MouseListener,MouseMotionListener,KeyListener {
  protected GameModel model;
  public GameController( GameModel a ) {
    model = a;
    /* ボーダーラインの生成 */
    model.createObject( "Border", model.getW()/2, 100, 100, Color.red);
    model.createObject( "Border", model.getW()/2, 100, 300, Color.blue);
    /* 一番最初のストーンの生成 */
    model.createStone( model.getW()/2, 700, 50 );
  }

  /* マウス操作 */
  public void mouseClicked( MouseEvent e ) { }
  public void mousePressed( MouseEvent e ) {
    /* 実際にはマウスホイールの時にこの動作を行なうが、いまはとりあえず動作確認ということでmousePressedに入れている */
    /* ホイールが回転開始したら速度を与えるようにしたい */
    if( model.getTargetStone() != null && model.getCanRot() ) {
      /* 実際には、ホイールの値を読み終わった後に以下を実行するので、もうちょい条件が必要 */

      /* 実際にはホイールの値をrotationに代入する */
      model.setRotation(-20.0f);

      /* ホイールの回転具合によって速度を設定する */
      model.getTargetStone().setV( 0.0f, model.getRotation() );

      /* 摩擦係数固定の状態で加速度を設定する */
      model.getTargetStone().startSliding();

      model.setCanRot(false); model.setCanRub(true);
    }
  }
  public void mouseDragged(MouseEvent e) {
    if( model.getCanRub() ) {
        /* この辺で摩擦係数をいじる操作を追加 */
        /* model.getTargetStone().setNewAY(0.4f); とか */
    }
  }
  public void mouseReleased(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseMoved(MouseEvent e) { }

  /* キー操作が必要になったらこの辺に処理を追加 */
  public void keyPressed(KeyEvent e) { }
  public void keyTyped(KeyEvent e) { }
  public void keyReleased(KeyEvent e) { }
 
}

