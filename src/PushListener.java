import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * @author if Thin-Gateway find push event, its event will be sent to
 *         Smart-cloud
 */
class PushListener implements UpdateListener {

	private String PushType;

	public PushListener(String PushType) {
		this.PushType = PushType;
	}

	public void update(EventBean[] newData, EventBean[] oldData) {
		// TODO Auto-generated method stub
		if (this.PushType.equals("emergency")) {
			System.out
					.println("######################### Emergency Message is sent##########################");
//			MQTT.getInstance().publishMsg("Push_event(Emergency)",true);
			System.out.println(newData[0].getUnderlying());
		} else
			System.out
					.println("######################### Other Message is sent  ##########################");

	}
}