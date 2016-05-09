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
	   static final int port = 3000; //상수값으로 port 번호 설정

	   //입력용 Stream
	   InputStream is;
	   ObjectInputStream ois;

	   //출력용 Stream
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
	         server = new ServerSocket(port); //port 번호: 3000으로 ServerSocket 생성
	         serverList.add(server);
	         System.out.println("*****  Server Program이 Clinet 접속을 기다립니다. *****");

	         //*** Clinet 접속이 있을 때까지 대기: 접속하는 순간 Socket을 반환 ***//
	         client = server.accept();
	         clientList.add(client);
	         //*** 접속이 되면 Clinet로부터 IP 정보를 얻어 출력 ***//
	         System.out.println(client.getInetAddress()+"로부터 연결요청");

	         //*** Clinet로 부터 수신받은 message를 읽기 위한 입력 Stream ***//
	         is = client.getInputStream();
	         ois = new ObjectInputStream(is);

	         //Clinet로 부터 수신받은 message를 다시 보내기 위한 출력 Stream ***//
	         os = client.getOutputStream();
	         oos = new ObjectOutputStream(os);

	         //*** Clinet가 보내온 message를 Server가 읽은 후 다시 Clinet에게 전송함 ***//

//	         is.close();
//	         ois.close();
//	         os.close();
//	         oos.close();
	      }
	      catch (Exception e)
	      {
	         System.out.println("통신 Error !!");
	         System.exit(0);
	      }
		
	}
}

