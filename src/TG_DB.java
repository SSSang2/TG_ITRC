/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class TG_DB {

	static TG_DB db_temp;
	static Connection conn = null;
	static Statement stmt;
	static ResultSet rs;
	static String sql = null;
	private static boolean hasEPL = false;
	private HashMap<String, String> Threshold;
	public TG_DB() {
	}

	// singleton
	public static TG_DB getInstance() {
		if (db_temp == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:test.db");
				stmt = conn.createStatement();

				// Create EPL table in DB
				sql = "CREATE TABLE IF NOT EXISTS EPL"
						+ "(Event_num TEXT PRIMARY KEY NOT NULL,"
						+ "Event TEXT NOT NULL)";
				stmt.executeUpdate(sql);

				// Create Topic table in DB
				sql = "CREATE TABLE IF NOT EXISTS TOPIC"
						+ "(Key_value TEXT NOT NULL)";

				stmt.executeUpdate(sql);
			
		*//*********************************************************************************//*
				
				sql = "DROP TABLE IF EXISTS DEV";
				stmt.executeUpdate(sql);
				
				// Create Sensor table in DB
				sql = "CREATE TABLE IF NOT EXISTS DEV"
						+"(id INTEGER PRIMARY KEY,"
						+ "DEV_NAME TEXT FORIEGN KEY NOT NULL,"
						+ "DEV_TYPE TEXT NOT NULL)";
				stmt.executeUpdate(sql);
				

				
		*//*********************************************************************************//*
				sql = "DROP TABLE IF EXISTS THRESHOLD";
				stmt.executeUpdate(sql);
				
				
				// Create Sensor table in DB
				sql = "CREATE TABLE IF NOT EXISTS THRESHOLD"
						+"(DEV_NAME TEXT PRIMARY KEY NOT NULL,"
						+ "GA TEXT,"
						+ "GB TEXT,"
						+ "VB TEXT,"
						+ "IR TEXT,"
						+ "AC TEXT,"
						+ "TH TEXT,"
						+ "MA TEXT,"
						+ "DB TEXT,"
						+ "CS TEXT,"
						+ "FL TEXT,"
						+ "SH TEXT)";
				stmt.executeUpdate(sql);
				
				sql = "DROP TABLE IF EXISTS SENSOR";
				stmt.executeUpdate(sql);
				
				// Create Sensor table in DB
				sql = "CREATE TABLE IF NOT EXISTS SENSOR"
						+"(Sensor_name TEXT PRIMARY KEY NOT NULL,"
						+ "Sensor_id TEXT NOT NULL,"
						+ "Threshold INTEGER,"
						+ "data INTEGER)";
				stmt.executeUpdate(sql);

				
		*//*********************************************************************************//*
				
				db_temp = new TG_DB();
	//			db_temp.initSensor();				// Initailize threshold value
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Data Base Instance Error : " + e.toString());
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Data Base Instance Connection Error : " + e.toString());
			}
		}
		return db_temp;
	}

	public boolean hasEPL() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			hasEPL = rs.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasEPL;
	}

	public void initSensor(){
		Threshold = new HashMap<String, String>();
		Threshold.put("GA","00ff");
		Threshold.put("GB","0003");
		Threshold.put("VB","ffff");
		Threshold.put("IR","c600");
		Threshold.put("AC","0032");
		Threshold.put("TH","2300");
		Threshold.put("MA","0000");
		Threshold.put("DB","c5a9");
		Threshold.put("CS",null);
		Threshold.put("FL",null);
		Threshold.put("SH",null);	
		StoreData("21",Threshold);
		System.out.println("++++++++++++ Complete to save initial sensor models ++++++++++++");
		PrintSensor();
	}
	
	public void updateSensorThreshold(String req, int threshold){
		sql = "UPDATE SENSOR SET Threshold = "+threshold+"WHERpi"
				+ "E Sensor_id="+req;
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateSensorValue(String req, int data){
		sql = "UPDATE SENSOR SET data = "+data+"WHERE Sensor_id="+req;
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void StoreDev(int id, String name, String type){
		sql = "INSERT INTO DEV_NAME (id, DEV_NAME, DEV_TYPE)" + "VALUES ('" + id
				+ "','" + name +"','" + type +  "');";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void StoreData(String name, HashMap<String, String> sensor){
		Set key = sensor.keySet();
		String keyName;
		String valueName;
		StringBuilder query_key = new StringBuilder();
		query_key.append("DEV_NAME");
		StringBuilder query_value = new StringBuilder();
		query_value.append("'"+name+"'");
		
		try {
			  for (Iterator iterator = key.iterator(); iterator.hasNext();) {
			  
                keyName = (String) iterator.next();
                valueName = (String) sensor.get(keyName);
                query_key.append(", " + keyName);
                query_value.append(", "+"'"+valueName+"'");
			}
			//  	System.out.println(query_key.toString());
			//  	System.out.println(query_value.toString());
				sql = "INSERT INTO THRESHOLD ("+query_key.toString()+")" + "VALUES ("+query_value.toString()+");";
				stmt.executeUpdate(sql);
				
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Store EPL in DB
	public void StoreEPL(String event_num, String event) {
		
		System.out.println("Add epl in DataBase : " + event_num);
		
		sql = "INSERT INTO EPL (Event_num,Event) " + "VALUES ('" + event_num
				+ "','" + event + "');";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("DataBase Error : EPL update failed");
			e.printStackTrace();
		}
		PrintEPL();

	}

	// Store TopicKeyValue in DB
	public void StoreKeyValue(String key) {
		sql = "INSERT INTO TOPIC (Key_value) " + "VALUES ('" + key + "');";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("DataBase Error : Insert Key Value failed" );
		}
	}

	// Print Sensor Table in DB
	public void PrintSensor() {
		try {
			rs = stmt.executeQuery("SELECT * FROM SENSOR;");
			while (rs.next()) {
				System.out.println(rs.getString("Sensor_name") + " | " + rs.getString("Sensor_id")  + " | "
						+ rs.getString("Threshold") + " | "+rs.getString("data"));
			}
		} catch (SQLException e) {
			System.out.println("DataBase Print Error : No Event in DB");
		}
	}
	
	public void PrintEPL() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			while (rs.next()) {
				System.out.println("Event_num : " + rs.getString("Event_num"));
				String epl = rs.getString("Event");
				System.out.println("Event : " + epl);
				System.out.println("EPL JSON : " + epl);
			}
		} catch (SQLException e) {
			System.out.println("DataBase Print Error : No Event in DB");
		}
	}

	// Register EPL for EventManager
	public void RegisterEvent() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			while (rs.next()) {
				System.out.println("Event_num : " + rs.getString("Event_num"));
				String epl = rs.getString("Event");
				System.out.println("Event : " + epl);
				SCListener.getInstance().inputEvent(epl, true);
				
				//publish Log Data
				MQTT.getInstance().publishLog("New_event(" + epl + ")is added",
						"11");
			}
		} catch (SQLException e) {
			System.out.println("DataBase Register Error : No Event in DB");
		}
	}

	// return TopicKeyValue
	public String GetKeyValue() {
		try {
			rs = stmt.executeQuery("SELECT Key_value FROM TOPIC");
			return rs.getString("Key_value");
		} catch (SQLException e) {
			System.out.println("DataBase KeyValue Error : Doesn't have Key_value");
			return null;
		}
	}
	
	// return TopicKeyValue
	public String GetThreshold(String DEV_NAME, String sensor) {
		try {
			rs = stmt.executeQuery("SELECT "+sensor+" FROM THRESHOLD WHERE DEV_NAME = \'"+DEV_NAME+"\';");
			return rs.getString(sensor);
		} catch (SQLException e) {
			System.out.println("DataBase KeyValue Error : Doesn't have Key_value");
			return null;
		}
	}

	// Remove selected EPL in DB
	public void RemoveEvent(String event_num) {
		sql = "DELETE FROM EPL" + "WHERE Event_num ='" + event_num + "'";
	}

	// close DB
	public void CloseDB() {
		try {
			db_temp = null;
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("DataBase Close DB Error : " + e.toString());
		}
	}

}
*/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TG_DB {

	static TG_DB db_temp;
	static Connection conn = null;
	static Statement stmt;
	static ResultSet rs;
	static String sql = null;
	private static boolean hasEPL = false;

	public TG_DB() {
	}

	// singleton
	public static TG_DB getInstance() {
		if (db_temp == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:test.db");
				stmt = conn.createStatement();

				// Create EPL table in DB
				sql = "CREATE TABLE IF NOT EXISTS EPL"
						+ "(Event_num TEXT PRIMARY KEY NOT NULL,"
						+ "Event TEXT NOT NULL)";
				stmt.executeUpdate(sql);

				// Create Topic table in DB
				sql = "CREATE TABLE IF NOT EXISTS TOPIC"
						+ "(Key_value TEXT NOT NULL)";

				stmt.executeUpdate(sql);
				db_temp = new TG_DB();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Data Base Instance Error : " + e.toString());
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Data Base Instance Connection Error : " + e.toString());
			}
		}
		return db_temp;
	}

	public boolean hasEPL() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			hasEPL = rs.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasEPL;
	}
	
	// Store EPL in DB
	public void StoreEPL(String event_num, String event) {
		
		System.out.println("Add epl in DataBase : " + event_num);
		
		sql = "INSERT INTO EPL (Event_num,Event) " + "VALUES ('" + event_num
				+ "','" + event + "');";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("DataBase Error : EPL update failed");
			e.printStackTrace();
		}
		PrintEPL();

	}

	// Store TopicKeyValue in DB
	public void StoreKeyValue(String key) {
		sql = "INSERT INTO TOPIC (Key_value) " + "VALUES ('" + key + "');";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("DataBase Error : Insert Key Value failed" );
		}
	}

	public void PrintEPL() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			while (rs.next()) {
				System.out.println("Event_num : " + rs.getString("Event_num"));
				String epl = rs.getString("Event");
				System.out.println("Event : " + epl);
				System.out.println("EPL JSON : " + epl);
			}
		} catch (SQLException e) {
			System.out.println("DataBase Print Error : No Event in DB");
		}
	}

	// Register EPL for EventManager
	public void RegisterEvent() {
		try {
			rs = stmt.executeQuery("SELECT * FROM EPL;");
			while (rs.next()) {
				System.out.println("Event_num : " + rs.getString("Event_num"));
				String epl = rs.getString("Event");
				System.out.println("Event : " + epl);
				SCListener.getInstance().inputEvent(epl, true);
				
				//publish Log Data
				MQTT.getInstance().publishLog("New_event(" + epl + ")is added","11");
			}
		} catch (SQLException e) {
			System.out.println("DataBase Register Error : No Event in DB");
		}
	}

	// return TopicKeyValue
	public String GetKeyValue() {
		try {
			rs = stmt.executeQuery("SELECT Key_value FROM TOPIC");
			return rs.getString("Key_value");
		} catch (SQLException e) {
			System.out.println("DataBase KeyValue Error : Doesn't have Key_value");
			return null;
		}
	}

	// Remove selected EPL in DB
	public void RemoveEvent(String event_num) {
		sql = "DELETE FROM EPL" + "WHERE Event_num ='" + event_num + "'";
	}

	// close DB
	public void CloseDB() {
		try {
			db_temp = null;
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("DataBase Close DB Error : " + e.toString());
		}
	}

}
