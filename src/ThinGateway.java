// 0630 TG log ¼öÁ¤ Áß  

import java.io.IOException;

//Thin-Gateway;
public class ThinGateway {
		
	public static void main(String[] args) throws IOException {
		MQTT mqtt = MQTT.getInstance(args[1]);
		ThreadPool.getInstance().runWorker("Dev_State",  new Dev_State());
		System.out.println("----------- Start to check device state -------------");
		//ThreadPool.getInstance().runWorker("BT_Listener", new BT_Manager());
		
		
		// args[0] : Actuator address
		// args[1] : MQTT Server address
		ThreadPool.getInstance().runWorker("SC_Listener", new SCListener(args[0], args[1]));
		System.out.println("----------- Start to communicate with MQTT -------------");
		
		RabbitMQ rabbit = RabbitMQ.getInstance();
		rabbit.Consume();
		rabbit.Produce("isConn");
		ThreadPool.getInstance().runWorker("Actuator",  new ActuatorList());
	}
}


