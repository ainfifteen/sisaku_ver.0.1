package sisaku;


//マルチキャスト版チャットプログラムChat.java
//このプログラムは,マルチキャストを使ってチャット機能を提供します
//使い方java Chat [ポート番号]

//ライブラリの利用
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

//受信スレッドを作成・実行し,送信を担当
public class Udp1 {
	final byte TTL = 1 ;//同一セグメント内部のみ到達可能とします
	final String MULTICASTADDRESS = ("224.0.0.1") ;
				// マルチキャストアドレス224.0.0.1は,
				// ルータを超えない場合のアドレスです
	int port;
	byte[] buff = new byte[1024] ;//送信用バッファ
	String myname ="" ;// 利用者名
	int nameLength = 0 ;//利用者名の長さ
	MulticastSocket soc = null ; // マルチキャストソケット
	InetAddress chatgroup = null ; //チャット用アドレス

	// コンストラクタ利用者名などを設定します
	public Udp1(int portno){
	port = portno ; //ポート番号の設定
	BufferedReader lineread
	  = new BufferedReader(new InputStreamReader(System.in)) ;

	}

	// makeMulticastSocketメソッド
	//MULTICASTADDRESSに対してマルチキャストソケットを作成します
	public void makeMulticastSocket()
	{
		try{
			chatgroup
				= InetAddress.getByName(MULTICASTADDRESS) ;
			soc = new MulticastSocket(port) ;
			soc.joinGroup(chatgroup) ;
		}
		catch(Exception e){
			e.printStackTrace() ;
			System.exit(1);
		}
	}

	// startLintenerメソッド
	// スレッド用クラスListenPacketのオブジェクトを生成し,起動します
	ListenPacket lisner;
	public void startListener()
	{
		try{
			lisner =
				new ListenPacket(soc);
			Thread lisner_thread = new Thread(lisner);
			lisner_thread.start();//受信スレッドの開始
		}
		catch(Exception e){
			e.printStackTrace() ;
			System.exit(1);
		}
	}

	// sendMsgsメソッド
	// マルチキャストパケットの送信を担当します
	public void sendMsgs(int people, double x, double y, double battery, int port)
	{
		try{
				String str = String.valueOf(people) + " " +
								String.valueOf(x) + " " +
								String.valueOf(y) + " " + "バッテリー" +
								String.valueOf(battery);
				buff = str.getBytes();
				DatagramPacket dp
					= new DatagramPacket(
					buff,buff.length,chatgroup,port) ;
				soc.send(dp) ;
		}
		catch(Exception e){
			e.printStackTrace() ;
			System.exit(1);
		}
	}

	public void sendMsgFromServer(  int port)
	{
		try{
				String str = "";
				buff = str.getBytes();
				DatagramPacket dp
					= new DatagramPacket(
					buff,buff.length,chatgroup,port) ;
				soc.send(dp) ;
		}
		catch(Exception e){
			e.printStackTrace() ;
			System.exit(1);
		}
	}

	// quitGroupメソッド
	// 接続を終了します
	public void quitGroup()
	{
		try{
			// 接続の終了
			System.out.println("接続終了") ;
			soc.leaveGroup(chatgroup) ;
			System.exit(0) ;
		}
		catch(Exception e){
			e.printStackTrace() ;
			System.exit(1);
		}
	}

}

//ListenPacketクラス
//マルチキャストパケットを受信します
class ListenPacket implements Runnable {
	MulticastSocket s = null;
	// コンストラクタマルチキャストスレッドを受け取ります
	public ListenPacket(MulticastSocket soc){
		s = soc;
	}

	byte[] rcvData;

	public byte[] getData() { return rcvData; }
	public void resetData() { rcvData = null; }

	// 処理の本体
	public void run(){
		byte[] buff = new byte[1024] ;
		try{
			while(true){
				DatagramPacket recv
					= new DatagramPacket(buff,buff.length) ;
				s.receive(recv) ;
				if(recv.getLength() > 0){
					rcvData = buff;
					System.out.write(buff,0,recv.getLength()) ;
				}
			}
		}catch(Exception e){
			e.printStackTrace() ;
			System.exit(1) ;
		}
	}
}