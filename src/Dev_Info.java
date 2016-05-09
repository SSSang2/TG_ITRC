import java.util.ArrayList;
import java.util.HashMap;

public class Dev_Info {
	private static Dev_Info singleton = new Dev_Info();
	private ArrayList<String> Dev_List;
	private HashMap<String, String> Init_Threshold;
	private HashMap<String, Sensor> Sensor_List;
	
	private Dev_Info(){
		Dev_List = new ArrayList<String>();
		Init_Threshold = new HashMap<String, String>();
		Sensor_List = new HashMap<String, Sensor>();
	}
	public static Dev_Info getInstance(){
		return singleton;
	}
	public void InsertDev(String type){
		if(!Dev_List.contains(type)){
			Dev_List.add(type);
			Init_Threshold.put(type, "");
			Sensor_List.put(type, null);
		}
	}
	public boolean isMember(String type){
		return Dev_List.contains(type);
	}
	
	public ArrayList<String> getList(){
		return Dev_List;
	}
	
	
	// Initial Threshold managing function
	
	public void InsertInitThreshold(String type, String value){
		if(Init_Threshold.get(type)=="")
			Init_Threshold.put(type, value);
		else
			Init_Threshold.replace(type, value);
	}
	
	public String getInitThreshold(String type){
		return Init_Threshold.get(type);
	}
	
	
	
	
	
	// Sensor List managing function
	
	public void InsertSensor(String type, Sensor sens){
		if(Sensor_List.get(type)== null)
			Sensor_List.put(type, sens);
	}
	public Sensor getSensor(String type){
		return Sensor_List.get(type);
	}
}
