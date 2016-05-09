import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDP_Manager implements Runnable{
	BufferedReader inFromUser;
	DatagramSocket clientSocket ;
	InetAddress IPAddress ;
	
	/**
	 *  Information of Compound Sensor Rack
	 */
	String dstAddr = "192.168.0.155";		// IP address of compound sensor device
	int port = 3000;
	byte[] sendData ;
	byte[] receiveData;
	Sensor sensor = new Sensor();
	String Dev_type = "21";				// Sensor device type ( One of practical scenario )
	Dev_Info devInfo = Dev_Info.getInstance();
	
	/**
	 * Contributor of UDP_Manager class
	 * Func		: Assign memory of values
	 * 			: , Connection test with sensor
	 */
	public UDP_Manager() throws SocketException, UnknownHostException {
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		clientSocket = new DatagramSocket();
		IPAddress = InetAddress.getByName(dstAddr);
		receiveData = new byte[64];
		sendData = new byte[64];
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOn("YELLOW");;
		// Initially verify and set threshold
		ThresholdRequest("a1");
		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		devInfo.InsertSensor(Dev_type, sensor);
		//ThresholdRequest("b1");
	}
	
	public UDP_Manager(String type) throws SocketException, UnknownHostException {
		Dev_type = type;
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
		clientSocket = new DatagramSocket();
		IPAddress = InetAddress.getByName(dstAddr);
		receiveData = new byte[64];
		sendData = new byte[64];
		((Dev_State)ThreadPool.getInstance().getThread("Dev_State")).TurnOn("YELLOW");;
		// Initially verify and set threshold
		ThresholdRequest("a1");
		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		devInfo.InsertSensor(Dev_type, sensor);
		//ThresholdRequest("b1");
	}
	
	/**
	 * @return Sensor instance that is matched by this UDP_Manager Threadpool
	 */
	public Sensor getSensor(){
		return sensor;
	}
	
	
	public void SendMessageToSensor(String command){
		byte[] command_byte = command.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(command_byte, command_byte.length, IPAddress, port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try {
			clientSocket.send(sendPacket);					// Send data to compound sensor
			clientSocket.receive(receivePacket);			// Receive data from compound sensor
		} catch (IOException e) {
			e.printStackTrace();
		}
		ThresholdRequest("a1");
	}
	/**
	 * Pre		: Already had ip address and port number of compound sensor device
	 * Func		: Send command(A1 or B1) to check current threshold or configure new threshold
	 * Post		: Receive hex format string from sensor according to command
	 * 
	 * @param Command	: 'A1' : request current threshold,
	 *  				  'B1' : request configure new threshold
	 * @return
	 */
	public String ThresholdRequest(String Command){							 
		String TH_GET_Req = sensor.getTH_Req(Dev_type, Command);	
		String received_commnad;
		int arg_count = 0;
		int string_length = 22;
		
		sendData = new java.math.BigInteger(TH_GET_Req, 16).toByteArray();	 								// Change the data format
		
		// Set the UDP packet
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try {
			clientSocket.send(sendPacket);					// Send data to compound sensor
			clientSocket.receive(receivePacket);			// Receive data from compound sensor
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		String receivedData = new java.math.BigInteger(receivePacket.getData()).toString(16);				// Get data by Hex format
		
		// Service type define
		if(Dev_type.equals(null))
			Dev_type = receivedData.substring(16,18);
		
		received_commnad = receivedData.substring(20, 22);
		System.out.println("# Received Data : " + receivedData);
		System.out.println("# Received_command : " + received_commnad);
		if(!sensor.comfirmData(receivedData)){
			System.out.println("Data parsing error");
		}
		// Command : Configure threshold data --------> return current threshold data
		if(Command.equals("b1"))
			ThresholdRequest("a1");
		else if(received_commnad.equals("a2")){
			System.out.println("# Data : " + receivedData);
			System.out.println("# arg_count(String): " + receivedData.substring(18, 20));
			arg_count = Integer.parseInt(receivedData.substring(18, 20));									// Number of sensor
			System.out.println("# arg_count(Integer) : " + arg_count);
			string_length += arg_count *8;																	// Default data size(22) + Number of sensor * 8 ( Size of each sensor)
			System.out.println("# string_length : " + string_length);
			receivedData = receivedData.substring(0, string_length);										// Cut the string as a data size
			sensor.setCurrentData(receivedData);															// Save the current data (String type)
			
	
		}
		return sensor.getCurrentData();
	}
	
	
	public String updateRequest(String Command){
		String TH_GET_Req = Command;
		String received_commnad;
		int arg_count = 0;
		int string_length = 22;
		
		sendData = new java.math.BigInteger(TH_GET_Req, 16).toByteArray();	 								// Change the data format
		
		// Set the UDP packet
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try {	
			clientSocket.send(sendPacket);					// Send data to compound sensor
			clientSocket.receive(receivePacket);			// Receive data from compound sensor
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		String receivedData = new java.math.BigInteger(receivePacket.getData()).toString(16);				// Get data by Hex format
		received_commnad = receivedData.substring(20, 22);
		
		if(!sensor.comfirmData(receivedData)){
			System.out.println("Data parsing error");
		}
		
		System.out.println("# Data : " + receivedData);
		System.out.println("# arg_count(String): " + receivedData.substring(18, 20));
		arg_count = Integer.parseInt(receivedData.substring(18, 20));									// Number of sensor
		System.out.println("# arg_count(Integer) : " + arg_count);
		string_length += arg_count *8 *2;																	// Default data size(22) + Number of sensor * 8 ( Size of each sensor)
		System.out.println("# string_length : " + string_length);
		receivedData = receivedData.substring(0, string_length);										// Cut the string as a data size
		System.out.println("# New Data : " + receivedData);
		sensor.setCurrentData(receivedData);					
		
		// Command : Configure threshold data --------> return current threshold data
		return sensor.getCurrentData();

		//return sensor.getCurrentData();
	}
	
	public void run(){
		while(true){
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);			// Receive ascii format data form sensor
				 Thread.sleep(1000);
				 
			} catch (IOException e) {
				System.out.println("UDP Socket error");
				clientSocket.close();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Change recieved data to hex
			String receivedData = new java.math.BigInteger(receivePacket.getData()).toString(16);
/*			System.out.println();
			System.out.println();
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("+" + receivedData);
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println();
			System.out.println();*/
			// confirm the received data
			if(!sensor.comfirmData(receivedData))
				System.out.println("Data parsing error");
			
		}		
	}
}
