import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Parser for message from Smart-Cloud
 */
public class MsgParser {

	
	// JSON format
	private JSONObject Json_obj;
	private JSONParser parser;

	static int HEADER_MARKER_SIZE = 8;
	static int HEADER_TYPE_SIZE = 1;
	static int HEADER_ARGN_SIZE = 1;

	static String PACKTYPE = "5041434b54595045";		// PACKTYPE

	//rawHeaderParse(marker, HEADER_MARKER_SIZE, HEADER_TYPE_SIZE, HEADER_ARGN_SIZE);


	// Parser constructor
	public MsgParser() {
	}

	static boolean chkHeader(String raw){
		
		/******************* [Header] Type Check *********************/
		String header = raw.substring(0,16).toString();
		
		if(header.equals(PACKTYPE)){
			return true;
		}
		else{
			System.out.println("   "+PACKTYPE);
			System.out.println("   "+header);
			return false;
		}
/*		
		*//****************** [Header] Detect Type Check ********************//*
		String devType = raw.substring(16,18);
				
		*//****************** [Header] ARG_Count Check *********************//*
		String num = raw.substring(18,20);
		ARG_Count = Integer.parseInt(num);
		
*/		
	}
	/**
	 * Parsing format	: Hex
	 * Send to SC		: Yes
	 * Sending format	: Hex
	 */
	static String getDevType(String raw){
		return raw.substring(16,18);
	}
	
	/**
	 * Parsing format	: Hex
	 * Send to SC		: No
	 */
	static int getDevNum(String raw){
		return Integer.parseInt(raw.substring(18,20));
	}

	/**
	 * Parsing format	: Hex
	 * Send to SC		: No
	 */
	static String getCommand(String raw){
		return raw.substring(20,22);
	}
	
	/**
	 * Parsing format	: Hex
	 * Send to SC		: YES
	 * Sending format	: ASCII(Sensor), Decimal(Sensor value)
	 */
	static HashMap<String, Integer> getThreshold(String raw, int devNum){
		HashMap<String, Integer> data=new HashMap<String, Integer>();
		 StringBuilder sensor = new StringBuilder();
		String tmp,value;
		int index = 22;
		
		for(int i=0; i<devNum; i++){
			tmp = raw.substring(index, index+2);
			int decimal = Integer.parseInt(tmp, 16);
			sensor.append((char)decimal);									// Hex to ASCII
			tmp = raw.substring(index+2, index+4);
			decimal = Integer.parseInt(tmp, 16);
			sensor.append((char)decimal);									// Hex to ASCII
			
			// When the sensor is temperature & humidity
			if(sensor.toString().equals("TH")){
				value = raw.substring(index+4, index+6);
				data.put(sensor.toString(), Integer.parseInt( value, 16 ));	// Hex to Decimal
				value = raw.substring(index+6, index+8);
				data.put("HU", Integer.parseInt( value, 16 ));				// Hex to Decimal
			}
			else{
				value = raw.substring(index+4, index+8);
				data.put(sensor.toString(), Integer.parseInt( value, 16 ));
			}
			
			sensor.setLength(0);											// Initialize StringBuilder value
			index+=8;
		}
		
		return data;
	}
	public String getDevType()
	{
		return (String) Json_obj.get("Dev");
	}
	
	public String getUserId()
	{
		return (String) Json_obj.get("userid");
	}
	
	public String getTgId()
	{
		return (String) Json_obj.get("tgid");
	}
	
	
	/**
	 * Pre		: Received the json format data that has no data value
	 * Func		: Fill the data value of empty json format
	 * Post		: Send json format that is filled all key values
	 */
	public String setData(String data){
		String type = Json_obj.get("type").toString();
				
		StringBuilder sb = new StringBuilder();
		sb.append("{\"type\":\"");
		//sb.append(Json_obj.get("type").toString());
		sb.append(type);
		sb.append("\", \"userid\" : \"");
		sb.append(Json_obj.get("userid").toString());
		sb.append("\",\"tg_id\":\"");
		sb.append(SCListener.getInstance().GetTG_ID());
		sb.append("\",\"data\":\"");
		sb.append(data);
		sb.append("\"}");
		
		return sb.toString();
	}
	
	// Get SensorData formed JSONArray
	public JSONArray getThresholdData() {
		return (JSONArray) Json_obj.get("Threshold");

	}
	/////////////////////////////////////////////////////////
	

	public JSONObject getThreshold() {
		return (JSONObject) Json_obj.get("Threshold");
	}
	

	/**
	 * Pre		: Received json format message from subscribed topic
	 * Func		: Set the message for parsing as json format
	 * Post		: Provide functions using many kind of keys
	 */
	public void SetMsg(String msg) throws ParseException {
		this.parser = new JSONParser();
		Object temp = parser.parse(msg);
		if (temp instanceof JSONObject) {
			Json_obj = (JSONObject) temp;
		} else
			System.out.println("Parser Error : Instance failed");
	}

	public byte[] getJsonBinaryData() throws UnsupportedEncodingException {
		String temp = (String) Json_obj.get("data");

		//return temp == null ? null : Base64.decode(temp);
		return temp == null ? null : Base64.decodeBase64(temp);
	}
	
	// Get Data 
	public String getData(){
		return (String) Json_obj.get("data");
	}
	public String getdataType()
	{
		return (String) Json_obj.get("type");
	}

	// Get SensorData formed JSONArray
	public JSONArray getSensorsData() {
		return (JSONArray) Json_obj.get("Sensors_data");

	}

	// Get LackID
	public String getLackID() {
		return (String) Json_obj.get("Lack_id");
	}

	public byte[] getDatatest() {
		return (byte[]) Json_obj.get("data");
	}

	// Get Event_num
	public String get_Seed() throws ParseException {
		return (String) Json_obj.get("seed");
	}
	
	// Get Event_num
	public String getEvent_num() throws ParseException {
		return (String) Json_obj.get("event_num");
	}

	// Get Push_type
	public String getPushType() throws ParseException {
		return (String) Json_obj.get("push_type");
	}

	// check if it's Actuator msg
	public boolean isActuator() throws ParseException {
		String type = (String) Json_obj.get("type");
		if (type.equals("actuator"))
			return true;
		else
			return false;
	}

	// Get EPL
	public String getEPL() {
		return (String) Json_obj.get("event");
	}

	// Get ListenerType
	public String getListenerType() {
		return (String) Json_obj.get("listenerType");
	}

	// Get Act_id
	public String getAct_id() {
		return (String) Json_obj.get("actuator_id");
	}

	public int getNum1() {
		System.out.println(Json_obj.get("num1"));
		return (Integer) Json_obj.get("num1");
	}

	public int getNum2() {
		return (Integer) Json_obj.get("num2");
	}

	// Get Act_value
	public String getAct_value() {
		return (String) Json_obj.get("action");
	}

	// Get Key_value
	public String getKey_value() {
		return (String) Json_obj.get("Key");
	}

	public boolean getNotice() {
		String temp = (String) Json_obj.get("notice");
		System.out.println(" data : " + temp);
		if (temp.isEmpty())
			return false;
		else
			return true;
	}

	public String getTopic() {
		return (String) Json_obj.get("topic");
	}

	public int getSecureNum() {
		String temp = (String) Json_obj.get("secureKey");
		return Integer.parseInt(temp);
	}

}

