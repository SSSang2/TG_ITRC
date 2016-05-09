import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

//Singleton 
public class MQTT {
	private static MQTT mqtt;

	private MQTT() {
	}

	private static MqttClient client = null;
	private static String MQTT_ADDRESS = "";
	private static String MQTT_ID = "";
	private static String MQTT_TEST_ADDRESS = "tcp://163.180.117.229:1883";
	private static String MQTT_TEST_ID = "TG";
	private static String Key_value = null;
	private static String SC_Key_value = "SC";						//  ¼öÁ¤ ¿ä¸Á
	private static boolean gotTopic = true;
	private static String TG_ID = "TG_01";

	public void setGotTopic()
	{
		gotTopic = false;
	}
	
	public boolean getGotTopic()
	{
		return gotTopic;
	}
	// Make only one instance
	public static MQTT getInstance() {
		// if there is no MQTT instance
		if (mqtt == null) {
			if (MQTT_ADDRESS.length() > 0) {
				mqtt = new MQTT();
			} else {
				MQTT_ADDRESS = MQTT_TEST_ADDRESS;
				MQTT_ID = MQTT_TEST_ID;
				mqtt = new MQTT();// make instance using basic info
			}
		}
		return mqtt;
	}
	// Make only one instance
	public static MQTT getInstance(String mqttIp) {
		// if there is no MQTT instance
		if (mqtt == null) {
			if (MQTT_ADDRESS.length() > 0) {
				mqtt = new MQTT();
			} else {
				MQTT_ADDRESS = "tcp://";
				MQTT_ADDRESS += mqttIp;
				MQTT_ID = MQTT_TEST_ID;
				mqtt = new MQTT();// make instance using basic info
			}
		}
		return mqtt;
	}
	
	// Set Address, id
	public void initMQTT(String addr, String id) {
		MQTT_ADDRESS = addr;
		MQTT_ID = id;
	}

	// Get MqttClient
	public MqttClient getMQTTclient() {
		if (client == null) {
			try {
				client = new MqttClient(MQTT_ADDRESS, MQTT_ID);
				System.out.println("++++++++++++++++++++++++++++++");
				System.out.println("++ MQTT Connect to ");
				System.out.println("++ Address : " + MQTT_ADDRESS);
				System.out.println("++ Id : " + MQTT_ID);
				System.out.println("++++++++++++++++++++++++++++++");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		// if not connected
		if (!client.isConnected()) {
			try {
				System.out.println("++++++++++++++++++++++++++++++");
				System.out.println("++++++++  Connecting... +++++++++");
				client.connect();
				if (client.isConnected()){
					System.out.println("++ Connection Success !!");
				} else{
					System.out.println("++ Connection Failed... ");
				}
				System.out.println("++++++++++++++++++++++++++++++");
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		return client;
	}

	// It is used when TG send sensorData or log to SC and
	// log01 = DB , log02 = SC , log03 = EPL....
	/**
	 * Pre		: Ready to send string data by json, must have key value
	 * Func		: Publish json data to SC
	 * @param data
	 */
	public void publishMsg(String data, String category) {
		
		//State LED
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOff("RED");;
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOn("GREEN");;
		//System.out.println("*************** GREEN *****************");		
		
		if(client == null)
			client = getMQTTclient();
		MqttMessage msg = new MqttMessage(data.getBytes());
		Key_value = GetKeyValue();
		
		if (Key_value == null)
			System.out.println(" Mqtt Error : Need Key_value");
		else {
			try {
				
				System.out.println("+++++++++++ Message is Sent to SC ++++++++++++");
				System.out.println("==============================================");
				System.out.println("----------------------------------------------");
				System.out.println();

				MqttTopic topic = client.getTopic(Key_value + "/" + category + "/"+TG_ID);
				System.out.println("# Topic : " + topic.toString());
				System.out.println("# Data  : " + data);
				MqttDeliveryToken token = topic.publish(msg);
				token.waitForCompletion(1000);
			} catch (Exception e) {
				System.out.println(" Mqtt Error : " + e.getMessage());
			}
				System.out.println();
				System.out.println("----------------------------------------------");
				System.out.println("==============================================");
				System.out.println();
				System.out.println();
		}
	}
	
	
	public void publishMsgToUser(String data, String category) {
		
		//State LED
		//state.TurnOn("GREEN");
		//state.TurnOff("RED");
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOff("RED");;
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOn("GREEN");;
		System.out.println("*************** GREEN *****************");
		
		if(client == null)
			client = getMQTTclient();
		MqttMessage msg = new MqttMessage(data.getBytes());
		Key_value = GetKeyValue();
		
		if (Key_value == null)
			System.out.println(" Mqtt Error : Need Key_value");
		else {
			try {
				System.out.println("+++++++++++ Message is Sent to SC ++++++++++++");
				System.out.println("==============================================");
				System.out.println("----------------------------------------------");
				System.out.println();
				
				MqttTopic topic = client.getTopic(SC_Key_value + "/" + category);
				System.out.println("# Topic : " + topic.toString());
				System.out.println("# Data  : " + data);
				MqttDeliveryToken token = topic.publish(msg);
				token.waitForCompletion(1000);
			} catch (Exception e) {
				System.out.println("#!! Mqtt Error : " + e.getMessage());
			}
			System.out.println();
			System.out.println("----------------------------------------------");
			System.out.println("==============================================");
			System.out.println();
			System.out.println();
			
		}
	}
	
	public void publishConfig(String data) {
		if(client == null)
			client = getMQTTclient();
		MqttMessage msg = new MqttMessage(data.getBytes());
		Key_value = GetKeyValue();
		
		if (Key_value == null)
			System.out.println("Mqtt Error : Need Key_value");
		else {
			try {
				System.out.println("+++++++++++ Message is Sent to SC ++++++++++++");
				System.out.println("==============================================");
				System.out.println("----------------------------------------------");
				System.out.println();
				
				MqttTopic topic = client.getTopic(Key_value);
				MqttDeliveryToken token = topic.publish(msg);
				token.waitForCompletion(1000);
			} catch (Exception e) {
				System.out.println("Mqtt Error : " + e.getMessage());
			}
			System.out.println();
			System.out.println("----------------------------------------------");
			System.out.println("==============================================");
			System.out.println();
			System.out.println();
		}
	}
	
	public void publishLog(String data, String LogNum) {
		if(client == null)
			client = getMQTTclient();
		MqttMessage msg = new MqttMessage(data.getBytes());
		Key_value = GetKeyValue();
		
		if (Key_value == null)
			System.out.println("Mqtt Error : Need Key_value");
		else {
			try {
					System.out.println();
					System.out.println("++++++++++++++++++++++++++++++++++++++++++ LOG DATA ++++++++++++++++++++++++++++++++++++++++++");
					StringBuilder sb = new StringBuilder();
					sb.append("{\"type\":\"log\",\"tg_id\":\"");
					sb.append(SCListener.getInstance().GetTG_ID());
					sb.append("\",\"lognum\":\"");
					sb.append(LogNum);
					sb.append("\",\"data\":\"");
					sb.append(data);
					sb.append("\"}");
					System.out.println(sb.toString());
					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					System.out.println();
					msg = new MqttMessage(sb.toString().getBytes());
				//syncronization
					
				MqttTopic topic = client.getTopic(Key_value + "/Log/"+TG_ID);
				MqttDeliveryToken token = topic.publish(msg);
				token.waitForCompletion(1000);
			} catch (Exception e) {
				System.out.println("Mqtt Error : " + e.getMessage());
			}
			System.out.println("+++++ Message is Sent to SC +++++");
		}
	}
	
	

	public MqttClient getNewMQTTclient() {
		MqttClient mClient = null;
		try {
			mClient = new MqttClient(MQTT_ADDRESS, MQTT_ID);
			System.out.println("++++++++++++++++++++++++++++++");
			System.out.println("++ MQTT Connect to ");
			System.out.println("++ Address : " + MQTT_ADDRESS);
			System.out.println("++ Id : " + MQTT_ID);
			System.out.println("++++++++++++++++++++++++++++++");
			System.out.println("++++++++  Connecting... +++++++++");
			mClient.connect();
			if (mClient.isConnected()) {
				System.out.println("++ Connection Success !! ++");
			} else {
				System.out.println("Mqtt Error : Connection Failed ");
			}
			System.out.println("++++++++++++++++++++++++++++++");
		} catch (MqttException e) {
			System.out.println("Mqtt Error : "+e.getMessage());
		}

		return mClient;
	}

	public String GetKeyValue() {
		return Key_value;
	}

	public void SetKeyValue(String key) {
		Key_value = key;
	}

}
