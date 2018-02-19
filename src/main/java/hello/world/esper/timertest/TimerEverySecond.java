package hello.world.esper.timertest;
 
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class TimerEverySecond {
	private static EPServiceProvider createCEPManager() {
		// Configure the engine, this is optional
		Configuration config = new Configuration();
		//config.configure("configuration.xml");	// load a configuration from file
		//config.set....(...);    // make additional configuration settings

		// Obtain an engine instance
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);

		// Optionally, use initialize if the same engine instance has been used before to start clean
		epService.initialize();

		// Optionally, make runtime configuration changes
		//epService.getEPAdministrator().getConfiguration().add...(...);

		// Destroy the engine instance when no longer needed, frees up resources
		//epService.destroy();
		return epService;
	}	
    public static void main(String[] args) throws InterruptedException {
		System.out.println("Initializing CEP engine");
		
		// Some settings for single threaded apps
		Configuration config = new Configuration();
		config.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);
		config.getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(false);
		
		EPServiceProvider cepMgr = createCEPManager();
		EPAdministrator admin = cepMgr.getEPAdministrator();
		ConfigurationOperations conf = admin.getConfiguration();
		
		// Define event types 
		conf.addEventType("TimerEvent", TimerEvent.class); 
			String queryTmp = // any second pattern 
					" insert into TimerEvent  " +
					" select  current_timestamp  from pattern " +
					" [ every timer:at(*, *, *, *, *, *) ]"
					; 
			
			EPStatement queryStmt = cepMgr.getEPAdministrator().createEPL( queryTmp );
			queryStmt.addListener(new TimerListener()); 
			Thread.sleep(11111);
			
			
 		
    }

}
