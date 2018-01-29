package hello.world.esper;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test; 
import com.espertech.esper.client.Configuration; 
import com.espertech.esper.client.EPServiceProviderManager; 
 
public class StartOnesTest {
 
	TemperatureEventHandler temperatureEventHandler;
  
	@Before
    public void setUp() throws Exception {
		temperatureEventHandler = new TemperatureEventHandler();

        Configuration configuration = new Configuration();
        configuration.addEventType("TemperatureEvent", TemperatureEvent.class.getName()); 

        temperatureEventHandler.epService = EPServiceProviderManager.getProvider("NucleaTemperatureHandling", configuration);

		
        temperatureEventHandler.monitorEventSubscriber = new MonitorEventSubscriber(); 
        String expressionText = temperatureEventHandler.monitorEventSubscriber.getStatement();
        temperatureEventHandler.monitorEventStatement = temperatureEventHandler.epService.getEPAdministrator().createEPL(expressionText);
        temperatureEventHandler.monitorEventStatement.setSubscriber(temperatureEventHandler.monitorEventSubscriber);
        
		
        temperatureEventHandler.criticalEventSubscriber = new CriticalEventSubscriber();
        String expressionText2 = temperatureEventHandler.criticalEventSubscriber.getStatement();
        temperatureEventHandler.criticalEventStatement = temperatureEventHandler.epService.getEPAdministrator().createEPL(expressionText2);
        temperatureEventHandler.criticalEventStatement.setSubscriber(temperatureEventHandler.criticalEventSubscriber);
		
		
		temperatureEventHandler.warningEventSubscriber = new WarningEventSubscriber();
        String expressionText3 = temperatureEventHandler.warningEventSubscriber.getStatement();
        temperatureEventHandler.warningEventStatement = temperatureEventHandler.epService.getEPAdministrator().createEPL(expressionText3);
        temperatureEventHandler.warningEventStatement.setSubscriber(temperatureEventHandler.warningEventSubscriber);
		        
        
        // To reduce logging noise and get max performance
        //epService.getEPRuntime().sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
    }

	 
	@Test
	public void test() throws Exception {
		//setUp() ;
        // Start Demo
        RandomTemperatureEventGenerator generator = new RandomTemperatureEventGenerator(temperatureEventHandler);
        
        long noOfTemperatureEvents =111;
		generator .startSendingTemperatureReadings(noOfTemperatureEvents );
        generator.startSendingTemperatureReadings(noOfTemperatureEvents);
        Thread.sleep(noOfTemperatureEvents *111);
        
        assertEquals(new Double(251.1), ((MonitorEventSubscriber)temperatureEventHandler.monitorEventSubscriber).avg,100);
	}

}
