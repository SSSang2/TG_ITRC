import java.util.HashMap;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;


public class EventManager {

	private static EventManager eventManger = null;
	private static Configuration Conf =null;
	private static EPServiceProvider ServiceProvider = null;
	private static EPAdministrator CepAdmin =null;
	private static EPRuntime CepRT = null;
	public EventManager(){}
	public static EventManager getInstance()
	{
		if(eventManger == null)
		{
			Conf = new Configuration();
			ServiceProvider = EPServiceProviderManager.getProvider("Thin-Gateway", Conf);
			CepAdmin = ServiceProvider.getEPAdministrator();
			CepRT = ServiceProvider.getEPRuntime();
			eventManger = new EventManager();
		}
		return eventManger;
	}
	
	//Add EventType for CEP
	public void AddEventType(String eventTypeName, HashMap<String, Object> Event)
	{
		CepAdmin.getConfiguration().addEventType(eventTypeName, Event);
	}
	
	
	public void SendEvent(HashMap<String, Object> Event, String eventTypeName)
	{
		CepRT.sendEvent(Event, eventTypeName);
	}
	
	public EPStatement getStatement(String statementNum)
	{
		return CepAdmin.getStatement(statementNum);
	}
	
	public EPStatement CreateNewEPL(String statement, String StatementNum)
	{
		return CepAdmin.createEPL(statement, StatementNum);
	}
}
