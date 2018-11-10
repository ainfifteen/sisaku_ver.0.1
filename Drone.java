package sisaku;

public class Drone {

	final static int EDGE_SERVER = 100;

	final static int WAIT = 0;//初期
	final static int GO = 1;//移動
	final static int SENSING = 2;//通常状態
	final static int GATHERING = 3;//招集
	final static int BACK = 4;//帰還
	final static int END = 5;//終了

	final static short NULL = 0;
	final static short NORTH = 1;
	final static short EAST = 2;
	final static short SOUTH = 3;
	final static short WEST = 4;

	final static double CONSUMPTION = 0.06;//1秒間の電池消費

	int id;//ドローンのID
	double x,y,z;
	double battery;//バッテリー
	int state;//ドローンの状態
	//int area;
	int initX, initY;;//初期値
	int oneBlock;//1区画
	int discover;//データの格納

	short direction;
	double speed;//スピード
	double firstMove;//初期移動
	double arrivalTime;//到着時間
	double lapseTime;//経過時間

	Udp1 udp;


	Drone(int id, int initX, int initY){//コンストラクタ
		this.id = id;
		this.initX = initX;
		this.initY = initY;
		//this.area = area;
		x = 0.0;
		y = 0.0;
		battery = 100.0;
		state = WAIT;
		direction = NULL;
		speed = 10;
		firstMove = Math.sqrt(Math.pow(initX - x, 2) + Math.pow(initY - y, 2));
		arrivalTime = firstMove / speed;
		lapseTime = 0.0;
		oneBlock = 30;

		udp = new Udp1(id);
		udp.makeMulticastSocket() ;//ソケット生成
		udp.startListener() ;//受信
	}

	void move(double simTime) {//移動メソッド

		battery -= CONSUMPTION * simTime;

		lapseTime += simTime;

		if(battery < 10.0) state = BACK;//10%以下で帰還

		switch(state) {
		case WAIT:
			 state = GO;
			 break;
		case GO:
			double goTheta = Math.atan2(initY, initX);//角度
			double goDistance = speed * simTime;
			x += goDistance * Math.cos(goTheta);
			y += goDistance * Math.sin(goTheta);

			if(lapseTime >= arrivalTime){
				x = initX;
				y = initY;
				lapseTime = 0.0;
				state = SENSING;
				direction = SOUTH;
			}
			break;

		case SENSING:
			if(lapseTime >= 3.0) {
				switch(direction) {
				case NORTH://上へ
					y += 30;
					if(y >= initY) direction = EAST;
					break;
				case EAST://右へ
					x += 30;
					if(y >= initY) direction = SOUTH;
					else direction = NORTH;
					break;
				case SOUTH://下へ
					y -= 30;
					if(y <= initY - 240) direction = EAST;
					break;

				case WEST: break;//左へ
				default: break;

				}
				lapseTime = 0.0;//経過時間
			}
			if(x >= initX + 240 && y >= initY) {
				state = BACK;
				direction = NULL;
			}

			break;

		case GATHERING:

			break;
		case BACK:
			double backTheta = Math.atan2(y, x);
			//lapseTime = 0;
			double backDistance = speed * simTime;
			x -= backDistance * Math.cos(backTheta);
			y -= backDistance * Math.sin(backTheta);
			if(x <= 0 && y <= 0) {
				x = 0;
				y = 0;
				state = END;
				direction = NULL;
			}
			break;
		default:
			break;
		}

	 }



	void dataGet(int[][] area){//データ収集メソッド
		if(state == SENSING || state == GATHERING) {//通常状態もしくは招集状態
			if(x < 30 && y < 30) {

			}
			else if(x < 30 && y >= 30) {
				discover = area[(int)(x / 30) ][(int)(y / 30) - 1];//データ抽出
			}
			else if(x >= 30 && y < 30) {
				discover = area[(int)(x / 30) - 1][(int)(y / 30) ];//データ抽出
			}
			else {
				discover = area[(int)(x / 30) - 1][(int)(y / 30) - 1];//データ抽出
			}
			udp.sendMsgs(discover, x, y, battery, EDGE_SERVER);//エッジに送信
			byte[] rcvData = udp.lisner.getData();//受信データ

			if(rcvData != null) {//受信データが空でないのなら
				String str = new String(rcvData);//byte型を文字に変換
				if(str.equals("0")) state = GATHERING;//招集状態へ
				udp.lisner.resetData();//データのリセット
			}
		}
	}


}
