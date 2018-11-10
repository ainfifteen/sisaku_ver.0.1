package sisaku;

public class EdgeServer {
	final static int ID = 100;;//エッジサーバのID

	Udp1 udp;

	EdgeServer(){
		udp = new Udp1(ID);//UDPインスタンスにID付与
		udp.makeMulticastSocket();//ソケット生成
		udp.startListener();//受信
	}

	void receiveData() {//受信メソッド
		byte[] rcvData = udp.lisner.getData();//受信データ
		if(rcvData != null) {

			String str = new String(rcvData);//byte型から文字に変換




			udp.lisner.resetData();//バッファの中のデータをリセット
		}
	}

}
