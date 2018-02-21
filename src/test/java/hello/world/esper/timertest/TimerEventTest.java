package hello.world.esper.timertest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class TimerEventTest {
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
	
	EPServiceProvider cepMgr;
	EPAdministrator admin;
	@Before
	public void setup() {
		System.out.println("Initializing CEP engine");
		
		// Some settings for single threaded apps
		Configuration config = new Configuration();
		config.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);
		config.getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(false);
		
		cepMgr = createCEPManager();
		admin = cepMgr.getEPAdministrator();
		ConfigurationOperations conf = admin.getConfiguration();
		
		// Define event types 
		conf.addEventType("TimerEvent", TimerEvent.class); 
		conf.addEventType("TemperatureEvent", TemperatureEvent.class); 
		String queryTmp = // any second pattern 
					" insert into TimerEvent  " +
					" select  current_timestamp  from pattern " +
					" [ every timer:at(*, *, *, *, *, *) ]"
					; 
			
		EPStatement queryStmt = cepMgr.getEPAdministrator().createEPL( queryTmp );
		queryStmt.addListener(new TimerFunction());
		
		//select * from pattern[every StockTickEvent(symbol='GE')]
		String sensorControllerQ = 
				"select * from pattern["
				+ "every (timer:interval(10 sec) and not TemperatureEvent)"
				+ " ]";
		EPStatement sensorCTRL = cepMgr.getEPAdministrator().createEPL( sensorControllerQ );
		
		sensorCTRL.addListener(new SensorBreakListener());

		//select * from pattern[every StockTickEvent(symbol='GE')]
		String sensorRepairQ = 
				"select * from pattern["
				+ "every [3] (timer:interval(7 sec) and not TemperatureEvent)"
				+ " ]";
		EPStatement repairCTRL = cepMgr.getEPAdministrator().createEPL( sensorRepairQ );
		
		repairCTRL.addListener(new SensorRepair());
	
	}

	@Test
	public void test() throws InterruptedException {
		Thread.sleep(11111);
		assertEquals( "something wrong withing 11 sec! ", 11, TimerFunction.getUpdateCounter() ); 
//	}
//
//	@Test
//	public void testIsSensorBrocken() throws InterruptedException {
		// sensor is brocken
		assertEquals( "something wrong withing 11 sec! ", 1, SensorBreakListener.getUpdateCounter() );
        // prevent firing sensor-checker 
		TemperatureEvent event = new TemperatureEvent();
		cepMgr.getEPRuntime().sendEvent(event);
		Thread.sleep(5111);		
		// sensor gives data in last 10 sec
		assertEquals( "something wrong withing 11+5 sec! ", 1, SensorBreakListener.getUpdateCounter() );
		Thread.sleep(5111);
		// brocken again...
		assertEquals( "something wrong withing 11+5+5 sec! ", 2, SensorBreakListener.getUpdateCounter() );
//	}
//	
//	@Test
//	public void testIsSensorHasToBerepaired() throws InterruptedException {
		Thread.sleep(5111);
		assertEquals( "something wrong with Repair 5+5+5 < 3*7 sec! ", 0, SensorRepair.getUpdateCounter() );
		Thread.sleep(5111);
		assertEquals( "something wrong with Repair 5+5+5+5 < 3*7 sec! ", 0, SensorRepair.getUpdateCounter() );
		Thread.sleep(5111);
		assertEquals( "something wrong with Repair 5+5+5+5+5 > 3*7 sec! ", 1, SensorRepair.getUpdateCounter() );
		Thread.sleep(5111);
		assertEquals( "something wrong with Repair 5+5+5+5+5+5  > 3*7 sec! ", 1, SensorRepair.getUpdateCounter() );
	}
}
