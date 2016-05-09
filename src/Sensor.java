import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author This class can make Thin-Gateway translate JSON to String
 */
public class Sensor {

	private HashMap<String, Object> SensorDatas = null;
	private HashMap<String, Object> ThresholdData = null;
	private HashMap<String, String> TypeDefine = null;
	private LinkedHashMap<String, String> Threshold = null;							// Current threshold of compound sensor
	private HashMap<String, String> ARG = null;
	private HashMap<String, String> ARG_Count = null;							// Number of sensors according to type of compound sensor
	private HashMap<String, String> sensor_list;								// Hex format sensor names 
	private HashMap<String, Object> DefineEventTypeName = null;
	private HashMap<String, Double> AverageSensorData = new HashMap<String, Double>();
	private HashMap<String, Double> previousSensorAvgData = new HashMap<String, Double>();
	private JSONArray sensor_arry;
	private JSONArray threshold_arry;
	private String current_data = null;
	private String init_Threshold = null;
	
	private String DetectType = "21";
	private static int count = 0;
	private String strPACKET = "5041434B54595045";		// "PACKET"
	MsgParser parser = new MsgParser();
	TG_DB db = new TG_DB();
	
	public Sensor() {
		SensorDatas = new HashMap<String, Object>();
		ThresholdData = new HashMap<String, Object>();
		ARG_Count = new HashMap<String, String>();
		ARG = new HashMap<String, String>();
		sensor_list= new HashMap<String, String>();
	
		// Number of sensors according to type of compound sensor
		ARG.put("21", "111111111000");
		ARG.put("22", "000010000001");
		ARG.put("23", "110001100010");
		ARG.put("24", "110000000000");
		ARG.put("25", "010001100110");
		ARG.put("26", "001010000001");
		ARG.put("27", "000010001000");
		ARG.put("28", "000110010000");
		ARG.put("29", "001110000000");
		ARG.put("2A", "000000001001");
		ARG.put("2B", "001010000101");
		
		// Number of sensors according to type of compound sensor
		ARG_Count.put("21", "08");
		ARG_Count.put("22", "02");
		ARG_Count.put("23", "04");
		ARG_Count.put("24", "02");
		ARG_Count.put("25", "04");
		ARG_Count.put("26", "03");
		ARG_Count.put("27", "02");
		ARG_Count.put("28", "03");
		ARG_Count.put("29", "03");
		ARG_Count.put("2A", "02");
		ARG_Count.put("2B", "04");
		
		// Hex format sensor names 
		sensor_list.put("GA","4741");
		sensor_list.put("GB","4742");
		sensor_list.put("VB","5642");
		sensor_list.put("IR","4952");
		sensor_list.put("AC","4143");
		sensor_list.put("TH","5448");
		sensor_list.put("MA","4D41");
		sensor_list.put("DB","4442");
		sensor_list.put("CS","4353");
		sensor_list.put("FL","464C");
		sensor_list.put("SH","5348");		
		
		Threshold = new LinkedHashMap<String, String>();
		Threshold.put("GA","0");		//Threshold.put("GA","00ff");
		Threshold.put("GB","0");		//Threshold.put("GB","0003");
		Threshold.put("VB","0");		//Threshold.put("VB","ffff");
		Threshold.put("IR","0");		//Threshold.put("IR","c600");
		Threshold.put("AC","0");		//Threshold.put("AC","0032");
		Threshold.put("TH","0");		//Threshold.put("TH","2300");
		Threshold.put("HU","0");
		Threshold.put("MA","0");		//Threshold.put("MA","0000");
		Threshold.put("DB","0");		//Threshold.put("DB","c5a9");
		Threshold.put("CS","0");
		Threshold.put("FL","0");
		Threshold.put("SH","0");	
		
	}
	
	public String getDevType(){
		return DetectType;
	}
	public String getCurrentData(){
		return current_data;
	}
	public void setCurrentData(String current_data){
		this.current_data = current_data;
	}
	public HashMap<String, String> getThreshold(){
		return Threshold;
	}
	
	/**
	 *  Pre		: Temporarily define included sensors by each service scenario
	 *  Func	: Match included sensor consider each scenario
	 *  Post	: Send initial information to server 
	 */
	public HashMap<String, String> getDevList(String type){
		HashMap<String, String> tmp = new HashMap<String, String>();
		String sensor = ARG.get(type);
		Iterator<Entry<String, String>> temp;
		int index=0;

		temp = Threshold.entrySet().iterator();
		System.out.println("Consis Key");
		while (temp.hasNext()) {
			String Key = temp.next().getKey();
			// '1' means used sensor in specific service 
			if(sensor.charAt(index++) == '1'){
				System.out.println("Key : " + Key);
				tmp.put(Key, Threshold.get(Key));
			}
			//System.out.println(Key + ","+ tmp.get(Key));
		}
		System.out.println("-----------1.5------------");
		return tmp;
		
	}
	
	/**
	 *  Pre		: Received request message from SC for changing threshold value
	 *  Func	: Update local value for changing compound sensor data
	 *  Post	: Send request to compound sensor to change threshold 
	 */
	public void updateThreshold(HashMap<String, String> thresholdFromSC){
		Iterator<Entry<String, String>> temp;
		temp = thresholdFromSC.entrySet().iterator();
		while (temp.hasNext()) {
			String Key = temp.next().getKey();
			
			if(Key.equals("VB"))
				System.out.println(thresholdFromSC.get(Key));
			
			Threshold.replace(Key, thresholdFromSC.get(Key));
		}
		((UDP_Manager)ThreadPool.getInstance().getThread("UDP_Manager")).ThresholdRequest("b1");
	}
	
	/**
	 * Pre		: Need udp destination ip address and port number
	 * Func		: Make a format of sending data according to command type for sending to compound sensor( TH_GET_Req or TH_SET_Req)
	 * Post		: Return full format of sending data
	 */
	public String getTH_Req(String type, String Command){
		String command = strPACKET;							// 5041434B54595045
		command += type;									// 5041434B5459504521
		
		if(Command.equals("a1")){
			command += "00";								// 5041434B545950452100
			command += Command;								// 5041434B545950452100A1
		}
		else if(Command.equals("b1")){
			command += ARG_Count.get(type);					// 5041434B545950452108
			command += Command;								// 5041434B545950452100B1
			//int num = Integer.parseInt(ARG_Count.get(type));
			
			Iterator<Entry<String, String>> temp;
			temp = sensor_list.entrySet().iterator();
			while (temp.hasNext()) {
				String Key = temp.next().getKey();
				String threshold = null;
				
				threshold = Threshold.get(Key);
				if(threshold == null)
					continue;
				else{
					command += sensor_list.get(Key);
					command += threshold;					// DB瑜� BYTE濡� ���옣�븷吏�, 10吏꾩닔濡� ���옣�븷吏� �젙�빐�빞 �븳�떎.
				}
			}
		}
		System.out.println("# Request Message : " + command);
		return command;
	}
	
	
	/**
	 * Pre		: Sent data is exist
	 * Func		: Parsing the hex format data
	 * 			: If, Command is "d1"
	 * 				Publish sensor name and value that is over threshold
	 * 			: else if, Command is "a2"
	 * 				Update Local hashmap value that contain current threshold data
	 * 
	 * Post		: If it is just confirm data, return true ( have no sensor data )
	 * 			: else, it has value of sensor data, send to SC
	 */
	public boolean comfirmData(String raw){
		String dev_Type = "21";
		String rcData = "GA";
		
		SendDataToSC(dev_Type,rcData, raw);
		return true;
		
	}
	
	public void setSensor_arry(JSONArray sensorData) {
		sensor_arry = sensorData;
	}
	
	public void setThreshold_arry(JSONArray thresholdData) {
		threshold_arry = thresholdData;
	}
	
	public String typeToHex(String sensorType){
		String tmp = null;
		String hex;
		char  tmpChar;
		
		// Data type must be 2 character
		if(sensorType.length() != 2)
			return null;
		
		for(int i=0; i<sensorType.length(); i++){
			tmpChar = sensorType.charAt(i);
			hex = String.format("%04x", (int)tmpChar);
			tmp += hex+ " ";
		}
		
		return tmp;
	}
	
	public void setDetectType(String detectType){
		DetectType = detectType;
	}

	// Define EventType;
	// Save sensor_arry(package of (just) sensor type) to HashMap
	public HashMap<String, Object> GetDefinedEventType() {
		DefineEventTypeName = new HashMap<String, Object>();
		TypeDefine = new HashMap<String, String>();
		// Check how many data include in that array
		Iterator<JSONObject> iter = sensor_arry.iterator();
		while (iter.hasNext()) {
			JSONObject obj = (JSONObject) iter.next();
			String SensorName = obj.toJSONString().split("\"")[1];
			DefineEventTypeName.put(SensorName, Double.class);
			TypeDefine.put(SensorName, typeToHex(SensorName));
		}
		return DefineEventTypeName;
	}

	// Check if it's a new EventType
	public boolean IsNewRackType() {
		if (DefineEventTypeName == null)
			return true;
		HashMap<String, Object> temp = new HashMap<String, Object>();
		
		// Check how many data include in that array
		Iterator<JSONObject> iter = sensor_arry.iterator();
		while (iter.hasNext()) {
			JSONObject obj = (JSONObject) iter.next();
			String SensorName = obj.toJSONString().split("\"")[1];
			temp.put(SensorName, Double.class);
		}
		if (temp.equals(DefineEventTypeName))
			return false;
		else
			return true;
	}

	// Get Average SensorData
	public void CalculateSensorDataAvg() {
		// Send AvgData to SC and initialize AverageSensorData Map, count
		Double SensorData;
		String SensorName;
		if (count == 10 ) {
			SaveAverage();
			SendDataToSC();
			AverageSensorData.clear();// Initialize HashMap
			count = 0;
			System.out.println("Send data to SC");
		}
		
		Iterator<JSONObject> iter = sensor_arry.iterator();
		// Insert SensorData into HashMap
		
		while (iter.hasNext()) {
			// obj has array of SensorDatas
			JSONObject obj = (JSONObject) iter.next();
			// Get Key
			SensorName = obj.toJSONString().split("\"")[1];

			// if it's new key
			if (AverageSensorData.isEmpty()
					|| !AverageSensorData.containsKey(SensorName)) {
				SensorData = (Double.parseDouble((String) obj.get(SensorName)));
				AverageSensorData.put(SensorName, SensorData / 10.0);
			} else if (AverageSensorData.containsKey(SensorName)) {
				Double temp = AverageSensorData.get(SensorName);
				SensorData = temp
						+ (Double.parseDouble((String) obj.get(SensorName)) / 10.0);
				AverageSensorData.replace(SensorName, temp, SensorData);
				// update value;
			}
		}
		count++;
	}
	

	  public static boolean isStringDouble(String s) {
	    try {
	        Double.parseDouble(s);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	  }
	/**
	 * Pre		: Get 'd1' type command from sensor
	 * Func		: Make json format according to received data
	 * Post		: Publish the received data to SC
	 * @param Dev_Type
	 * @param rcData
	 */
	public void SendDataToSC(String Dev_Type, String rcData, String raw) {
		// Convert HashMapData to Json
		raw = raw.replace("\n", "");
		String tmp = raw;

		if(!isStringDouble(tmp))
			return ;
		
		double temp = Double.parseDouble(tmp) / 10;
		tmp = Double.toString(temp);
		
		StringBuilder data = new StringBuilder();
		data.append("{\"type\":\"sensordata\"");
		//data.append("{\"type\":\"threshold\"");
		data.append(",\"dev_type\":\"");
		data.append(Dev_Type);
		data.append("\",\"tg_id\":\"");
		data.append(SCListener.getInstance().GetTG_ID());
		data.append("\",\"sensors_data\":[");
		
		// until there is no new SensorData info

		String Key = rcData;
		data.append("{\"");
		data.append(Key);
		data.append("\":\"");
		data.append(tmp);
		data.append("\"}");

		
		data.append("], \"origin\" : \"");
		data.append(raw);
		data.append("\"}");
		//System.out.println("Message : " + data.toString());
		if(MQTT.getInstance().GetKeyValue() != null)
			MQTT.getInstance().publishMsg(data.toString(), "TGdata");
		
	}
	
	public void SendDataToSC() {
		// Convert HashMapData to Json
	
		StringBuilder data = new StringBuilder();
		data.append("{\"type\":\"sensordata\"");
		data.append(",\"tg_id\":\"");
		data.append(SCListener.getInstance().GetTG_ID());
		data.append("\",\"sensors_data\":[");
		Iterator<Entry<String, Double>> temp;
		temp = AverageSensorData.entrySet().iterator();

		// until there is no new SensorData info
		while (temp.hasNext()) {
			String Key = temp.next().getKey();
			data.append("{\"");
			data.append(Key);
			data.append("\":\"");
			data.append(AverageSensorData.get(Key));
			data.append("\"}");
			if (temp.hasNext())
				data.append(","); 
		}
		data.append("]}");
		if(MQTT.getInstance().GetKeyValue() != null)
			MQTT.getInstance().publishMsg(data.toString(), "TGdata");
		//System.out.println("Message : " + data.toString());
	}

	
	// Get SensorData in HashMap
	public HashMap<String, Object> GetSensorData() {
		Iterator<JSONObject> iter = sensor_arry.iterator();
		// Check how many data include in that array
		while (iter.hasNext()) {
			JSONObject obj = (JSONObject) iter.next();
			String SensorName = obj.toJSONString().split("\"")[1];
			Double SensorData = Double
					.parseDouble((String) obj.get(SensorName));
			SensorDatas.put(SensorName, SensorData);
		}
		return SensorDatas;
	}

	// Get SensorData in HashMap
	public HashMap<String, Object> GetThresholdData() {
		Iterator<JSONObject> iter = threshold_arry.iterator();
		// Check how many data include in that array
		while (iter.hasNext()) {
			JSONObject obj = (JSONObject) iter.next();
			String SensorName = obj.toJSONString().split("\"")[1];
			Double Threshold = Double
					.parseDouble((String) obj.get(SensorName));
			ThresholdData.put(SensorName, Threshold);
		}
		return ThresholdData;
	}
	
	//Compare with avgData, if it is unusual data
	public void CompareWithAvg() {
		Iterator<JSONObject> iter = sensor_arry.iterator();
		if (!previousSensorAvgData.isEmpty()) {
			while (iter.hasNext()) {
				JSONObject obj = (JSONObject) iter.next();
				String SensorName = obj.toJSONString().split("\"")[1];
				Double SensorData = Double.parseDouble((String) obj
						.get(SensorName));
				Double AvgSensorData = previousSensorAvgData.get(SensorName);
				// Check if it is unusual data
				
				
				// 1.5 is temporary value , unusual alarm
				if (SensorData > (AvgSensorData * 1.2)
						|| SensorData < (AvgSensorData-(AvgSensorData*1.2))) {
					//log 
					MQTT.getInstance().publishLog(
							SensorName +(SensorData-AvgSensorData), "20");
				}
			}
		} else
			System.out.println("SensorAvgData doesn't exist");
	}

	public void SaveAverage() {
		previousSensorAvgData.putAll(AverageSensorData);
	}
	
//	public String NewRackLog()
//	{
//		HashMap<String, Object> temp = new HashMap<String, Object>();
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("\"Rack_List\":[{\"Rack_ID\":\"Rack01\",\"Sensor_List\":[");
//		Iterator<JSONObject> iter = sensor_arry.iterator();
//		while (iter.hasNext()) {
//			JSONObject obj = (JSONObject) iter.next();
//			String SensorName = obj.toJSONString().split("\"")[1];
//			sb.append("\"");
//			sb.append(SensorName);
//			if(iter.hasNext())
//				sb.append("\",");
//			else
//				sb.append("\"");
//		}
//		sb.append("]}]");
//		
//		sb.append(",\"Act_List\":[{\"Act_ID\":\"Act03\",\"Status\":\"on\"},"
//				+ "{\"Act_ID\":\"Act02\",\"Status\":\"on\"}]");
//		System.out.println("++++++++++++++++++++++++++++++++++++++++++ LOG DATA ++++++++++++++++++++++++++++++++++++++++++");
//		System.out.println(sb.toString());
//		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//		return sb.toString();
//	}

}
