import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ActuatorList implements Runnable {
	static ArrayList<ServerSocket> serverList = new ArrayList<ServerSocket>();
	static ArrayList<Socket> clientList = new ArrayList<Socket>();
	
	ServerSocket server = null;
	   Socket client = null;
	   static final int port = 3000; //��������� port ��ȣ ����

	   //�Է¿� Stream
	   InputStream is;
	   ObjectInputStream ois;

	   //��¿� Stream
	   OutputStream os;
	   ObjectOutputStream oos;

	   String receiveData;

	   ActuatorList()
	   {	
		   try {
			server = new ServerSocket();
			client = new Socket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		   if(serverList.size()==0){
			   run();
		   }
//		   else{
//			   for(int i=0; i<clientList.size(); i++){
//				   Socket temp = clientList.get(i);
//				   try {
//					os = temp.getOutputStream();
//					oos = new ObjectOutputStream(os);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			       
//			   }
//			   
//		   }
	   }
	 static void sendMsg(String msg){
		  System.out.println("================================");
		  System.out.println("================================");
		  System.out.println("================================");

	   //for(int i=0; i<clientList.size(); i++){
		  for(int i=0; i<1; i++){
		   Socket temp = clientList.get(i);
		   try {
			OutputStream os = temp.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeUTF(msg);
			System.out.println("Sent data to Actuator");

		} catch (IOException  e) {
			e.printStackTrace();
		}
	   }
          
	   }
	@Override
	public void run() {
		try
	      {
	         server = new ServerSocket(port); //port ��ȣ: 3000���� ServerSocket ����
	         serverList.add(server);
	         System.out.println("*****  Server Program�� Clinet ������ ��ٸ��ϴ�. *****");

	         //*** Clinet ������ ���� ������ ���: �����ϴ� ���� Socket�� ��ȯ ***//
	         client = server.accept();
	         clientList.add(client);
	         //*** ������ �Ǹ� Clinet�κ��� IP ������ ��� ��� ***//
	         System.out.println(client.getInetAddress()+"�κ��� �����û");

	         //*** Clinet�� ���� ���Ź��� message�� �б� ���� �Է� Stream ***//
	         is = client.getInputStream();
	         ois = new ObjectInputStream(is);

	         //Clinet�� ���� ���Ź��� message�� �ٽ� ������ ���� ��� Stream ***//
	         os = client.getOutputStream();
	         oos = new ObjectOutputStream(os);

	         //*** Clinet�� ������ message�� Server�� ���� �� �ٽ� Clinet���� ������ ***//

//	         is.close();
//	         ois.close();
//	         os.close();
//	         oos.close();
	      }
	      catch (Exception e)
	      {
	         System.out.println("��� Error !!");
	         System.exit(0);
	      }
		
	}
}

