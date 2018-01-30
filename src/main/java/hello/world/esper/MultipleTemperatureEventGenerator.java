package hello.world.esper;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  

/**
 * Just a simple class to create a number of Random TemperatureEvents and pass them off to the
 * TemperatureEventHandler.
 */
 
public class MultipleTemperatureEventGenerator {

    /** Logger */
    private static Logger LOG = LoggerFactory.getLogger(MultipleTemperatureEventGenerator.class);

    /** The TemperatureEventHandler - wraps the Esper engine and processes the Events  */ 
    private TemperatureEventHandler temperatureEventHandler;

    public MultipleTemperatureEventGenerator(TemperatureEventHandler par1) {
		this.temperatureEventHandler = par1;
	}

    private int count = 0;
	/**
     * Creates simple random Temperature events and lets the implementation class handle them.
     */
    public void startSendingTemperatureReadings(final long noOfTemperatureEvents) {

        ExecutorService xrayExecutor = Executors.newFixedThreadPool(101);

        xrayExecutor.submit(new Runnable() {
        	final TemperatureEventHandler xxx = temperatureEventHandler ;
            public void run() {
                LOG.debug(getStartingMessage());
                
                
                while (getCount() < noOfTemperatureEvents) {
                	try {
	                    TemperatureEvent ve = new TemperatureEvent(new Random().nextInt(500), new Date());
	                    xxx.handle(ve);
	                    setCount(getCount() + 1);
                    
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        LOG.error("Thread Interrupted", e);
                    } catch (Throwable e) {
                        LOG.error("???", e);
                    }
                }

            }
        });
    }

    
    private String getStartingMessage(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n************************************************************");
        sb.append("\n* STARTING - ");
        sb.append("\n* PLEASE WAIT - TEMPERATURES ARE RANDOM SO MAY TAKE");
        sb.append("\n* A WHILE TO SEE WARNING AND CRITICAL EVENTS!");
        sb.append("\n************************************************************\n");
        return sb.toString();
    }


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}
}
