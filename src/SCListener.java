import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.espertech.esper.client.EPStatement;

/**
 * @author Smart-cloud Listener This class can make Thin-Gateway get Msg from
 *         Smart-cloud using LAN
 */
public class SCListener implements Runnable {
	
	private static int i=0;

	//Dev_State state = Dev_State.getInstance();
	private PushListener ps = null;
	private static final String TG_ID = "TG_01";
	private static final String TOPIC_EPL = "EPL";
	private static final String TOPIC_ACTUATOR = "ACT";
	private static final String TOPIC_THRESHOLD = "TGThr";
	private static final String TOPIC_RequestThr = "TGThr";
	
	private static StringBuilder EPL = new StringBuilder();
	private static StringBuilder ACT = new StringBuilder();
	private static StringBuilder THRE = new StringBuilder();
	private static StringBuilder ReqTHRE = new StringBuilder();
	
	private static String Key_value = null;
	private static MsgParser msgParser = null;
	static SCListener SC = null;
	
	private static MQTT mqtt = null;
	private String Mqtt_ip = null;
	private static Security SecurityInstance = new Security();
	private static MqttClient mqttclnt = null;
	private static int testInt=0;
	private String Act_ip;
	private Dev_Info devInfo = Dev_Info.getInstance();					// Value for managing about dev_list
	Sensor sensor;
	String reqMsgFromSC = null;
	/********************** Rabbit MQ *****************************/
	RabbitMQ rabbitMq = RabbitMQ.getInstance();
	String msgFromSensor = null;
	
	
	private String userId = "USER_01";
	
	public SCListener() throws IOException {
		//ThreadPool.getInstance().runWorker("UDP_Manager", new UDP_Manager());
	}
	public SCListener(String act_ip) throws IOException {
		//ThreadPool.getInstance().runWorker("UDP_Manager", new UDP_Manager());
		Act_ip = act_ip;
		System.out.println("===================================");
		System.out.println("+ Actuator IP Address : " + Act_ip);
		System.out.println("===================================");
	}
	
	public SCListener(String act_ip, String mqtt_ip) throws IOException {
		System.out.println("----------------------1--------------------------");
		//ThreadPool.getInstance().runWorker("UDP_Manager", new UDP_Manager());
		System.out.println("----------------------2--------------------------");
		Act_ip = act_ip;
		Mqtt_ip = mqtt_ip;
		System.out.println("===================================");
		System.out.println("+ Actuator IP Address : " + Act_ip);
		System.out.println("+ MQTT IP Address : " + Mqtt_ip);
		System.out.println("===================================");
		
	}
	
	public static synchronized void setTest(int a){
		testInt = a;
	}

	public static SCListener getInstance() {
		if (SC == null) {
			try {
				msgParser = new MsgParser();
				SC = new SCListener();
				SecurityInstance = new Security();
			} catch (IOException e) {
				System.out.println("SCListener Error : "+e.getMessage());
			}
		}
		return SC;
	}

	public void SetCodedTopic(String topic) {
		Key_value = topic;
	}

	public String GetTG_ID() {
		return TG_ID;
	}
	

	public void run() {
		//mqtt = MQTT.getInstance(Mqtt_ip);
		mqtt = MQTT.getInstance();
		mqttclnt = mqtt.getMQTTclient();
	
		// Receive Key_value from SC
		Key_value = TG_DB.getInstance().GetKeyValue();
		
		if (Key_value == null) {
			System.out.println("Waiting for Topic......");
			try {
				Thread.sleep(1000);
				GetData(TG_ID, mqttclnt);
			} catch (Exception e) {
				System.out.println("SCListener Error1 : "+e.getMessage());
			}
			
			
			while (mqtt.getGotTopic()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					System.out.println("SCListener Error2 : "+e.getMessage());
				}
			}
			
			try {
				mqttclnt.unsubscribe(TG_ID);
			} catch (MqttException e1) {
				System.out.println("SCListener Error3 : "+e1.getMessage());
			}
			
			
		} else {
			mqtt.SetKeyValue(Key_value);
			SetTopic();
			mqtt.publishLog("Key value in DB is added ", "12");
		}
		
		//////////////////////////////////////////////////////
		////////////   After exchange topic   ////////////////
		//////////////////////////////////////////////////////
		sendInitInform();
		
		mqttclnt.setCallback(new MqttCallback(){
			// Overriding messageArrived
			public void messageArrived(String topic, MqttMessage msg)
					throws Exception {
				
				
				if (topic.equals(EPL.toString())) {
					inputEvent(msg.toString(),false);
				} else if (topic.equals(ACT.toString())) {
					doAction(msg.toString());
				} else if (topic.equals(THRE.toString())){
					requestThreshold(msg.toString());
//					msgParser.SetMsg(msg.toString());
//					String type = msgParser.getdataType();
//					if(type.equals("requestThr"))
//						requestThreshold(msg.toString());
//					else if(type.equals("changeThr"))
					//	changeThreshold(msg.toString());
				} else if (topic.equals(ReqTHRE.toString())){		// 없어도 될듯
					msgParser.SetMsg(msg.toString());
					String type = msgParser.getdataType();
					if(type.equals("requestThrReq"))
						requestThreshold(msg.toString());
					else if(type.equals("changeThr"))
						changeThreshold(msg.toString());
				} 
				
			}

			public void deliveryComplete(IMqttDeliveryToken arg0) {
			}

			public void connectionLost(Throwable arg0) {
			}
		});
		
		
		try {
			// Receive data from Topic : EPL and ACT
			mqttclnt.subscribe(EPL.toString());
			mqttclnt.subscribe(ACT.toString());
			mqttclnt.subscribe(THRE.toString());
			mqttclnt.subscribe(ReqTHRE.toString());
			
			System.out.println("==============================================");
			System.out.println("----------------------------------------------");
			System.out.println("#Subscribe : " + EPL.toString());
			System.out.println("#Subscribe : " + ACT.toString());
			System.out.println("#Subscribe : " + THRE.toString());
			System.out.println("#Subscribe : " + ReqTHRE.toString());
			System.out.println("----------------------------------------------");
			System.out.println("==============================================");
			
		} catch (MqttException e2) {
			System.out.println("SCListener Subscribe Error : "+e2.getMessage());
		}
		testInt++;
		while(true) {
			try {
				//System.out.println("Test integer = "+testInt);
				//testInt++;
				msgFromSensor = rabbitMq.getMsg();					// Message from ble sensor
				Thread.sleep(1000);
				System.out.println("==================================================================================");
				System.out.println("----------------------------------------------------------------------------------");
				System.out.println("#");
				System.out.println("# Message from sensor : " + msgFromSensor);
				System.out.println("# Size : " + msgFromSensor.length());
				System.out.println("----------------------------------------------------------------------------------");
				System.out.println("==================================================================================");				
				
				// If this condition, SCListener is stop.....I don't know why this is....
				if(msgFromSensor.length()< 20){
					publishToSC(msgFromSensor);
					continue;
				}
				
//				// Higher priority than 'd1' Message 
//				// There is 'a2' or 'b2' message in the message queue
//				// After process all messages in queue, process 'd1' message
//				if(rabbitMq.getMsgQueSize()>0){
//					System.out.println("& QueSize : " + rabbitMq.getMsgQueSize());
//					String sensorData;
//					while(rabbitMq.getMsgQueSize()>0){
//						sensorData = rabbitMq.getMsgQueData();
//						System.out.println("& QueMsg : " + sensorData);
//						try {
//							msgParser.SetMsg(reqMsgFromSC);
//						} catch (ParseException e) {
//							e.printStackTrace();
//						}	
//						String user_id = msgParser.getUserId();
//						StringBuilder topic = new StringBuilder();
//						topic.append(TOPIC_RequestThr);
//						topic.append("/");
//						topic.append(user_id);
//						
//						// Fill the value of 'data' key as getting sensor data
//						String publish_json = msgParser.setData(sensorData);
//						System.out.println("# Json msg : " + publish_json);
//						
//						// Publish whole threshold data
//						if(MQTT.getInstance().GetKeyValue() != null)
//							MQTT.getInstance().publishMsgToUser(publish_json, topic.toString());
//					}
//				}
//				else if(msgFromSensor.substring(20,22).equals("d1")){
//					publishToSC(msgFromSensor);
//				}
				
				if(testInt ==5){
				try {
					ThreadPool.getInstance().getWorker("SC_Listener").getInstance().setTest(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void sendInitInform(){
		
		System.out.println("=================================");
		System.out.println("---------------------------------");
		System.out.println("# Send Initial service information");

		ArrayList<String> tmp = new ArrayList<String>();
		HashMap<String, String> sensor_list = new HashMap<String, String>();
		// 'Topic : /SC/TGThr/USER_01' ( for Publish )
		StringBuilder topic = new StringBuilder();
		StringBuilder publish_json = new StringBuilder();
		topic.append("SERV");
			
			
		//Sensor sensor = devInfo.getSensor(type)
		//Sensor sensor;
/*		*//**
		 * 		SAMPLE DATA 
		 **//*
		devInfo.InsertDev("21");
		devInfo.InsertDev("22");
		devInfo.InsertDev("23");
		devInfo.InsertDev("24");
		devInfo.InsertDev("25");
		devInfo.InsertDev("26");
		devInfo.InsertDev("27");
		devInfo.InsertDev("28");
		devInfo.InsertDev("29");
		devInfo.InsertDev("2A");
		devInfo.InsertDev("2B");*/
		
		tmp = devInfo.getList();			// Dev_Type Array List
		
		System.out.println("+ Num of Service : " + tmp.size());
		for(int i=0; i<tmp.size(); i++){
			System.out.println("----------1--------------");
			
			//System.out.println("numOf sensor : " + devInfo.getSensor(type));
			sensor = devInfo.getSensor(tmp.get(i));
			System.out.println("type : " + sensor.getDevType());
			// sensor_list : Hashmap<Key, current Threshold>
			// Key : specifically mapped sensor key according to dev_type
			
			sensor_list = sensor.getDevList(tmp.get(i));			
			
			JSONObject obj = new JSONObject();
			JSONObject obj1 = new JSONObject();
			
			System.out.println("----------2--------------");
			obj.put("type", "responseService");
			obj.put("dev_type", tmp.get(i));
			obj.put("tgid", TG_ID);
			obj.put("userid", userId);
			
			System.out.println("----------3--------------");
			System.out.println("Size : " +sensor_list.size());
			Iterator<Entry<String, String>> temp;
			temp = sensor_list.entrySet().iterator();
			
			while(temp.hasNext()){
				String Key = temp.next().getKey();
				int dec;
				System.out.println("KEY : " + Key + "," + sensor_list.get(Key));
				if(Key.equals("TH"))
					dec = Integer.parseInt( sensor_list.get(Key).substring(0, 2), 16 );
				else
					dec = Integer.parseInt( sensor_list.get(Key), 16 );
				System.out.println(Key + "," + dec);
				obj1.put(Key, dec);
			}
			
			System.out.println("----------4--------------");
			obj.put("sensor", obj1);
			//obj.put("userid", user_id);
			obj.put("origin", devInfo.getInitThreshold(tmp.get(i)));
			System.out.println("+ Msg : " + obj.toString());
			// Publish each service and sensor initial threshold
			if(MQTT.getInstance().GetKeyValue() != null)
				MQTT.getInstance().publishMsg(obj.toString(), topic.toString());
			
			publish_json.setLength(0);
		}	
		System.out.println("---------------------------------");
		System.out.println("=================================");
		System.out.println("");
	}
	
	/**
	 * Pre		: Subscribed from specific topic('key/TGThr/tgid')
	 * Func		: Transmit threshold of whole sensor data to User via smart cloud mqtt server
	 * Post		: Publish whole threshold of sensor to specific topic('SC/TGThr/USER_01')
	 * @throws InterruptedException 
	 * 
	 */
	public void requestThreshold(String msg) throws InterruptedException{
		String user_id = null;
		String sensor_data = null;
		String publish_json = null;
		String type = null;
		reqMsgFromSC = msg;									// Save the request message for responding to SC after receive sensor data
/*		
 		//State LED
		state.TurnOn("GREEN");
		state.TurnOff("RED");
		System.out.println("*************** GREEN *****************");
*/
		
		System.out.println("=================================");
		System.out.println("---------------------------------");
		System.out.println("# Request Threshold");
		System.out.println("# msg : " + msg);
		
		// Ready to message parse
		try {
			msgParser.SetMsg(msg);									
		} catch (ParseException e) {
			System.out.println("#! Message Parsing error ");
			e.printStackTrace();
		}			
		
		
		// Check the parsed tg_id is same with my id(TG_01)
		// If then, 
		// 	Parse type, and user id
		if(msgParser.getTgId().equals(TG_ID)){
			System.out.println("# TG_ID Checked : " + msgParser.getTgId());
			user_id = msgParser.getUserId(); 
			type = msgParser.getdataType();
			System.out.println("# User ID : " + user_id);
		}
		else{
			System.out.println("#! Error : TG_id is different with my id");
		}
	
		// If the user id exist,
		//	Send request message to sensor for getting whole threshold data of sensor
		if(user_id != null){
			/*** Send message to complex sensor as 'a1' command for receive whole sensor data ****/
			//sensor_data = ((UDP_Manager)ThreadPool.getInstance().getThread("UDP_Manager")).ThresholdRequest("a1");
			String data = msgParser.getData();
			System.out.println("#Data from SC : " + data);
			System.out.println(data.equals(""));
			
			// When message include data : Change Threshold message  
			if(data.equals("")==false){
				//System.out.println("-----Here I am----");
				String head = data.substring(0,16);
				System.out.println("head : " + head);
				String dev_type = data.substring(16,18);
				System.out.println("dev_type : " + dev_type);
				int num = Integer.parseInt(data.substring(18,20));
				System.out.println("num : " + num);
				String cmd_type = data.substring(20,22);
				System.out.println("cmd_type : " + cmd_type);
				StringBuilder cmd = new StringBuilder();
				System.out.println("==============================================");
				System.out.println("----------------------------------------------");
				
				int index = 22;		// Starting point of data in message
				while(num>0){
					cmd.setLength(0);
					if(num>=2){
						cmd.append(head);
						cmd.append(dev_type);
						cmd.append("02");
						cmd.append(cmd_type);
						cmd.append(data.substring(index, index+16));
						index += 16;
						num -= 2;
					}
					else if(num==1){
						cmd.append(head);
						cmd.append(dev_type);
						cmd.append("01");
						cmd.append(cmd_type);
						cmd.append(data.substring(index, index+8));
						index += 8;
						num -= 1;
					}
					System.out.println("# B1 Command : " + cmd.toString());
					System.out.println("# Num : " + num);
					
					while(rabbitMq.getMsgQueSize() != 0){
						System.out.println("&Response from sensor " + rabbitMq.getMsgQueData());
					}
					rabbitMq.Produce(cmd.toString());
					Thread.sleep(10000);
					
					}
				}
			
			// When message dose not include data : Change threshold message
			else{
				rabbitMq.Produce("5041434B545950452100A1");
			}
		}
			
		System.out.println("----------------------------------------------");
		System.out.println("==============================================");
		
			
			
//			System.out.println("# Sensor : " + sensor_data);
//			
			// 'Topic : /SC/TGThr/USER_01' ( for Publish )
//			StringBuilder topic = new StringBuilder();
//			topic.append(TOPIC_RequestThr);
//			topic.append("/");
//			topic.append(user_id);
//			
//			// Fill the value of 'data' key as getting sensor data
//			publish_json = msgParser.setData(sensor_data);
//			System.out.println("# Json msg : " + publish_json);
//			
//			// Publish whole threshold data
//			if(MQTT.getInstance().GetKeyValue() != null)
//				MQTT.getInstance().publishMsgToUser(publish_json, topic.toString());
		
				
		System.out.println("---------------------------------");
		System.out.println("=================================");

		
	}
	
	// 아직 미사용
	public void changeThreshold(String msg){
		String user_id = null;
		String sensor_data = null;
		String publish_json = null;
		String type = null;
		String data = null;

		//State LED
		//state.TurnOn("GREEN");
		//state.TurnOff("RED");
		//System.out.println("*************** GREEN *****************");
		
		System.out.println("=================================");
		System.out.println("---------------------------------");
		System.out.println("# Request to change threshold");
		System.out.println("# msg : " + msg);
		
		// Ready to message parse
		try {
			msgParser.SetMsg(msg);									
		} catch (ParseException e) {
			System.out.println("#! Message Parsing error ");
			e.printStackTrace();
		}			
		
		
		// Check the parsed tg_id is same with my id(TG_01)
		// If then, 
		// 	Parse type, and user id
		if(msgParser.getTgId().equals(TG_ID)){
			System.out.println("# TG_ID Checked : " + msgParser.getTgId());
			user_id = msgParser.getUserId(); 
			type = msgParser.getdataType();
			data = msgParser.getData();
			
			System.out.println("# User ID : " + user_id);
			System.out.println("# Data : "+ data);
		}
		else{
			System.out.println("#! Error : TG_id is different with my id");
		}
	
		// If the user id exist,
		//	Send request message to sensor for getting whole threshold data of sensor
		if(user_id != null){
			/*** Send message to complex sensor as 'a1' command for receive whole sensor data ****/
			sensor_data = ((UDP_Manager)ThreadPool.getInstance().getThread("UDP_Manager")).updateRequest(data);
			System.out.println("# Sensor : " + sensor_data);
			
			// 'Topic : /SC/TGThr/USER_01' ( for Publish )
			StringBuilder topic = new StringBuilder();
			topic.append(TOPIC_THRESHOLD);
			topic.append("/");
			topic.append(user_id);
			
			// Fill the value of 'data' key as getting sensor data
			publish_json = msgParser.setData(sensor_data);
			System.out.println("# Json msg : " + publish_json);
			
		// Publish whole threshold data
		//	if(MQTT.getInstance().GetKeyValue() != null)
		//		MQTT.getInstance().publishMsgToUser(publish_json, topic.toString());
		}
				
		System.out.println("---------------------------------");
		System.out.println("=================================");
		
	}
	
	// 문자열을 헥사 스트링으로 변환하는 메서드
	public static String stringToHex(String s) {
	    String result = "";

	    for (int i = 0; i < s.length(); i++) {
	      result += String.format("%02X ", (int) s.charAt(i));
	    }

	    return result;
	  }
	  
	// Input the event based on event type
	public void inputEvent(String msg, boolean IsDB) {
		String epl = null;
		try {
			System.out.println("msg : " + msg);
			// set message in msgParser
			
			msgParser.SetMsg(msg);
		} catch (ParseException e1) {
			System.out.println("SCListener Parser Error! " + e1.toString());
		}
		System.out.println("Input EPL : " + msgParser.getEPL().toString());
		try { 
			// get event from JSON
			String event_num = msgParser.getEvent_num();
			// get EPL from JSON
			epl = msgParser.getEPL();
			String listenerType = msgParser.getListenerType();

			// get EPL named event_num
			EPStatement stateEPL = EventManager.getInstance().getStatement(
					event_num);
			// if EPL named event_num is already exist
			if (stateEPL != null) {
				System.out.println("delete an overlapped statement - "
						+ event_num);
				TG_DB.getInstance().RemoveEvent(event_num);
				// stop and delete previous EPL
				stateEPL.stop();
				stateEPL.destroy();
				stateEPL = null;
			} else
				System.out.println("- EPL is new");
			
			if(!IsDB)
			{
				TG_DB.getInstance().StoreEPL(event_num, msg);
				TG_DB.getInstance().CloseDB();
			}

			// renew EPL named event_num
			stateEPL = EventManager.getInstance().CreateNewEPL(epl, event_num);
			System.out.println("create the statement - " + event_num);

			
			// Check Event Type
			if (listenerType.equals("Push")) {
				String push_type = msgParser.getPushType();
				ps = new PushListener(push_type);
				stateEPL.addListener(ps);
				System.out.println("new Push event is registered");

				// send log to SC *** Error occur
				MQTT.getInstance().publishLog(
						"new push event ( " + epl + " ) is registered", "11");
				
			} else {
				String act_id = msgParser.getAct_id();
				String act_value = msgParser.getAct_value();
				StringBuilder sb = new StringBuilder();
				sb.append(act_id);
				sb.append(",");
				sb.append(act_value);
				stateEPL.addListener(new ActuatorListener(sb.toString()));
				System.out.println("new Actuator event is registered");

				// send log to SC *** Error occur
				MQTT.getInstance().publishLog(
						"new actuator event ( " + epl + " ) is registered",
						"11");
			}

		} catch (Exception e) {
			System.out.println("SCListener failed add EPL Error : "
					+ e.getLocalizedMessage());
		}
	}

	// Execute Actuator
	public void doAction(String msg) {
		
		MsgParser parser = new MsgParser();
		try {
			parser.SetMsg(msg);
		} catch (ParseException e) {
			System.out.println("SCListener parsing Error : "+e.getMessage());
		}

		// we are supposed to send this Msg to Actuator, but we haven't decided
		// which protocol use yet.
		// It't for just test
		StringBuilder sb = new StringBuilder();
		sb.append(parser.getAct_id());
		sb.append(",");
		sb.append(parser.getAct_value());
		System.out.println("Parse Msg : " + sb.toString());
		ActuatorList tmp=null;
		
		String act_id = parser.getAct_id();
		String action = parser.getAct_value();
		//tmp.sendMsg(parser.getAct_value());
		// If Actuator id is window
		if(act_id.equals("window")){
			if(action.equals("on"))
				tmp.sendMsg("open");
			else if(action.equals("off"))
				tmp.sendMsg("close");
		}
		// If Actuator id is fan
		else if(act_id.equals("fan")){
			tmp.sendMsg(parser.getAct_value());
		}
		
		//ActuatorList.sendMsg(parser.getAct_value());
		//ActuatorList.getInstance().sendActMsg(sb.toString(), Act_ip);
		
		System.out.println("Actvalue: " + parser.getAct_value());
		MQTT.getInstance().publishLog("Actuator is " + parser.getAct_value(), "21");
	}

	public void SetTopic() {
		EPL.append(Key_value);
		EPL.append("/");
		EPL.append(TOPIC_EPL);
		EPL.append("/");
		EPL.append(TG_ID);
		
		ACT.append(Key_value);
		ACT.append("/");
		ACT.append(TOPIC_ACTUATOR);
		ACT.append("/");
		ACT.append(TG_ID);
		
		THRE.append(Key_value);
		THRE.append("/");
		THRE.append(TOPIC_THRESHOLD);
		THRE.append("/");
		THRE.append(TG_ID);

		ReqTHRE.append(Key_value);
		ReqTHRE.append("/");
		ReqTHRE.append(TOPIC_RequestThr);
		ReqTHRE.append("/");
		ReqTHRE.append(TG_ID);		
		
	}
	
	public void publishToSC(String msg){
		if(sensor == null)
			sensor = new Sensor();
		sensor.comfirmData(msg);
	}
	

	// Get Topic(Key_value) from SC
	public void SetTopicFromJson(String msg)
	{
//		MsgParser parser = new MsgParser();
//		try {
//			parser.SetMsg(msg);
//		} catch (ParseException e) {
//			System.out.println("error - parsing");
//		}
		Key_value=msg;
		
		System.out.println("setting ..... new topic : "+Key_value);
		
		TG_DB.getInstance().StoreKeyValue(Key_value);
		TG_DB.getInstance().CloseDB();
		SetTopic();
		mqtt.SetKeyValue(Key_value);
		mqtt.setGotTopic();
		mqtt.publishLog("New_key(" +Key_value +")is added", "12");
	}
	
	
	public void GetData(final String newTopic, final MqttClient clnt)
			throws MqttException {
		clnt.setCallback(new MqttCallback() {
			public void messageArrived(String topic, MqttMessage msg)
					throws Exception {

				MsgParser msgparser = new MsgParser();
				msgparser.SetMsg(msg.toString());
				System.out.println("Topic : " +topic + " message : "+msg.toString() +"count : "+i++);
				byte[] bytes;
				bytes = msgparser.getJsonBinaryData();
				System.out.println(bytes);
				System.out.println("Type :" + msgparser.getdataType());
				//if datatype is 01 then It is about Topic
				if(msgparser.getdataType().equals("01"))
				{
					String topicJson = null;
					//get Decrypted msg from byte data
					topicJson = SecurityInstance.GetDecryptedMsg(bytes);
					
					//'topicJson' is Json which includes the Topic data.
					
				}
				else if(msgparser.getdataType().equals("00")){
					SetTopicFromJson(SecurityInstance.GetTopicFromSC(bytes));
				}else if(msgparser.getdataType().equals("03"))
				{
					System.out.println("ignoring...");
				}
			}

						
			public void deliveryComplete(IMqttDeliveryToken arg0) {
			}

			public void connectionLost(Throwable arg0) {
				arg0.printStackTrace();
				System.out.println("Connection is Lost");
			}
		});
		clnt.subscribe(newTopic);
	}
	
}
