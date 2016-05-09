import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMQ {
  private static final String EXCHANGE_NAME = "BLE";
  ConnectionFactory factory;
  Connection connection;
  Channel channel;
  String queueName = null;
  Consumer consumer;
  String lastMsg = null;
  Queue msgQue;
  private static RabbitMQ singleton = new RabbitMQ();
  
  private RabbitMQ(){
	  	msgQue = new LinkedList();
	    factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    try {
	    	
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			
			queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, "");
			
		} catch (IOException  e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
    
  }
  public static String stringToHex(String s) {
	  StringBuilder output = new StringBuilder("");
	    for (int i = 0; i < s.length(); i += 2)
	    {
	        String str = s.substring(i, i + 2);
	        output.append((char) Integer.parseInt(str, 16));
	    }
	    return output.toString();
	  }
  public int getMsgQueSize(){
	  return msgQue.size();
  }
  public String getMsgQueData(){
	  return (String)msgQue.remove();
  }
  public String getMsg(){
	  return lastMsg;
  }
  
  public static RabbitMQ getInstance(){
	  return singleton;
  }
  
  public void Consume(){
	  consumer = new DefaultConsumer(channel) {
		  
		// Handler that process when rabbitMQ receive some message
	    @Override
	    public void handleDelivery(String consumerTag, Envelope envelope,
	                               AMQP.BasicProperties properties, byte[] body) throws IOException {
	      String message = new String(body, "UTF-8");
	     // System.out.println("@Rabbit : " + message.toString());
	      //if(!message.substring(0,5).equals("00000"))
	      
	      //if(!message.contains("000000")){
	    	  lastMsg = message;
	    	  try{
	    	  int val = Integer.parseInt(lastMsg);
	    	  
		      if(message.length() > 20 || val > 5){
		    	  System.out.println("&cmd : " + message.substring(20,22));
//		    	  String type=message.substring(20,22);
//			      if(type.equals("B2") || type.equals("b2") ||type.equals("a2") || type.equals("A2")){
			    	  msgQue.add(message);
			    	  System.out.println("& Put to the que");
//			      }
		      }
	    	  }catch(NumberFormatException nfe){
	    		  
	    	  }
	    	  
	      //}
	      //System.out.println("&Last Msg : " + lastMsg);
	      //System.out.println("&Que size : " + msgQue.size());
	    }
	  };
	  try {
		channel.basicConsume(queueName, true, consumer);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  
  public void Produce(String msg){
	  try {
		channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
}