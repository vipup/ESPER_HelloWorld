package hello.world.esper;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test; 
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager; 
 
public class StartOnesTest {
 
	TemperatureEventHandler temperatureEventHandler;
  
	@Before
    public void setUp() throws Exception { 
		
	    Configuration configuration = new Configuration();
	    configuration.addEventType("TemperatureEvent", TemperatureEvent.class.getName()); 
	    //configuration.addEventTypeAutoName("com.cor.cep.event");
	    EPServiceProvider provider = EPServiceProviderManager.getProvider("NucleaTemperatureHandling", configuration);
	    
		temperatureEventHandler = new TemperatureEventHandler(provider);

		temperatureEventHandler.subscribe(new WarningEventSubscriber());		
        temperatureEventHandler.subscribe(new MonitorEventSubscriber());
		temperatureEventHandler.subscribe(new CriticalEventSubscriber());
		      
    }

	 
	@Test
	public void test() throws Exception { 
		// Start Demo
        RandomTemperatureEventGenerator generator = new RandomTemperatureEventGenerator(temperatureEventHandler);
        
        long noOfTemperatureEvents =111;
		generator .startSendingTemperatureReadings(noOfTemperatureEvents ); 
        Thread.sleep(noOfTemperatureEvents *111);
        
        assertEquals(new Double(251.1), ((MonitorEventSubscriber) temperatureEventHandler.getSubscriberByClass((MonitorEventSubscriber.class))).avg,100);
	}

}
