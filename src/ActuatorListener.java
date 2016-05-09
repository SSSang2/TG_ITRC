
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * Actuator Listener if Actuator event occurs, It generate actuator
 */
public class ActuatorListener implements UpdateListener {

	 private String Act_info;

	public ActuatorListener(String act_value) {
		 this.Act_info = act_value;
	}

	// should send log to SC
	public void update(EventBean[] newData, EventBean[] oldData) {
		//ActuatorList.getInstance().sendActMsg(this.Act_info);

	}
}