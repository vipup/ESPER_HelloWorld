package hello.world.esper;

import java.util.Date;
import java.util.Random;

import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class StockTest {
    public static class CEPListener implements UpdateListener { 
   	 
        public void update(EventBean[] newData, EventBean[] oldData) { 
            //SystemUtil.println("Event received, old: %s, new: %s.", ObjToStringUtil.objToString(oldData), ObjToStringUtil.objToString(newData));
        	//System.out.println("."+ ObjUtils.objToString(oldData) + " /// "+ ObjUtils.objToString(newData));
        } 
    }

    public static class MyFilteredListener implements UpdateListener { 
   	 
        private long start;

		public MyFilteredListener(long start) {
			this.start = start;
		}

		public void update(EventBean[] newData, EventBean[] oldData) { 
            //SystemUtil.println("Event received, old: %s, new: %s.", ObjToStringUtil.objToString(oldData), ObjToStringUtil.objToString(newData));
			long sinceStart = System.currentTimeMillis() - start;
        	System.out.println("+"+sinceStart+" ms::"+ ObjUtils.objToString(oldData) + " /// "+ ObjUtils.objToString(newData));
        } 
    }
    
    public static class Tick { 
        String symbol; 
        Double price; 
        Date timeStamp; 
 
        public Tick(String s, double p, long t) { 
            symbol = s; 
            price = p; 
            timeStamp = new Date(t); 
        } 
 
        public double getPrice() { 
            return price; 
        } 
 
        public String getSymbol() { 
            return symbol; 
        } 
 
        public Date getTimeStamp() { 
            return timeStamp; 
        } 
 
        @Override 
        public String toString() { 
            return "Price: " + price.toString().substring(0, 5) + " time: " + DateTimeUtil_formatDate2TimeStr(timeStamp); 
        }

		private String DateTimeUtil_formatDate2TimeStr(Date timeStamp2) {
			
			return timeStamp2.toString();
		} 
    } 	
    private static Random generator = new Random(); 
    
    public static void generateRandomTick(EPRuntime cepRT) { 
        double price = generator.nextDouble() * 100; 
        long timeStamp = System.currentTimeMillis(); 
        String [] symbols = {"MSFT","IBM","GOOG","AAPL","AAPL","AAPL","AAPL","YAHO"};
		String symbol = symbols [(int) (price % symbols.length)]; 
        Tick tick = new Tick(symbol, price, timeStamp); 
        //System.out.println("Sending tick:" + tick); 
        cepRT.sendEvent(tick); 
    } 
    
    @Test
	public void test() {
	    // The Configuration is meant only as an initialization-time object.
	    Configuration cepConfig = new Configuration();
	    cepConfig.addEventType("StockTick", Tick.class.getName());
	    EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);
	    EPRuntime cepRT = cep.getEPRuntime();

	    EPAdministrator cepAdm = cep.getEPAdministrator();
// Any 10 times	    
//	    EPStatement cepStatement = cepAdm.createEPL("select symbol,price,avg(price) from " + "StockTick(symbol='AAPL').win:length(10) having avg(price) > 60.0");
// Any 10 sec	    
//	    EPStatement cepStatement = cepAdm.createEPL("select symbol,price,avg(price) from " + "StockTick(symbol='AAPL').win:time_batch(10 sec) ");
// Average on 10 sec	    
//	    EPStatement cepStatement = cepAdm.createEPL("select symbol,price,avg(price) from " + "StockTick(symbol='AAPL')#time(110 sec) ");
// ++	    insert into ThroughputPerFeed
// step 1 : filter	    'AAPL'
	    EPStatement cepStatement = cepAdm.createEPL(""
	    		+ "insert into Average100SecStock  select symbol,price,avg(price) from " + "StockTick(symbol='AAPL').win:time(110 sec) ");	    
	    cepStatement.addListener(new CEPListener());
// step 2 :  split / agregate by 10 sec	    
	    EPStatement cepStatement10sec = cepAdm.createEPL("select avg(price), count(price), min(price), max(price)  from Average100SecStock.win:time_batch(3 sec) ");
	    long start = System.currentTimeMillis();
	    cepStatement10sec.addListener(new MyFilteredListener(start));
	    
	    // We generate a few ticks...
	    for (int i = 0; i < 1000000; i++) {
	        generateRandomTick(cepRT);
	    }	
	    System.out.println("generating is done in "+ (System.currentTimeMillis()-start )+" miliseconds");
        try {
        	Thread.sleep(15110);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	    // We generate a few ticks...
//	    for (int i = 0; i < 1000000; i++) {
//	        generateRandomTick(cepRT);
//	    }	
//	    System.out.println("generating is done in "+ (System.currentTimeMillis()-start )+" miliseconds");
//        try {
//        	Thread.sleep(25110);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
