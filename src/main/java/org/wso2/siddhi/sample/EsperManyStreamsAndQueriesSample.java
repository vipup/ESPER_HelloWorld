/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.sample;

import com.espertech.esper.client.*;

import java.util.*;


/**
 * Sample demonstrating a simple filtering use-case
 */
public class EsperManyStreamsAndQueriesSample {

	private static final int HIGHEST_PRICE = 1000000;
	private static final int MAX_SPREAD = 1000;

	static class StreamStats {
		int count;
	}
	
	Map<String, StreamStats> streamStats = new HashMap<String, StreamStats>();
	
	/***
	 * This is a listener class for receiving updates, when 
	 * a rule triggers.
	 *
	 */
	class StatsUpdateListener implements UpdateListener {
		private final String streamName;
		public StatsUpdateListener(String streamName) {
			this.streamName = streamName;
		}
		
		void update(String symbol, float price) {
			streamStats.get(streamName).count++;
		}

		public void update(EventBean[] newEvents, EventBean[] oldEvents) {
			streamStats.get(streamName).count++;	        
        }
	}

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
	
	public EPStatement addQuery(EPServiceProvider cepMgr, String query) {
		return cepMgr.getEPAdministrator().createEPL(query);
	}
	
	public void testManyQueries(int maxQueries, int maxEvents, int maxInputStreams) throws InterruptedException {
		
		System.out.println("Initializing CEP engine");
		
		// Some settings for single threaded apps
		Configuration config = new Configuration();
		//config.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);
		//config.getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(false);
		
		EPServiceProvider cepMgr = createCEPManager();
		EPAdministrator admin = cepMgr.getEPAdministrator();
		ConfigurationOperations conf = admin.getConfiguration();
		
		
		// Define event types
		for(int s=0; s < maxInputStreams; s++) {
			String[] propertyNames = new String[] {"symbol", "price"};
			Object[] propertyTypes = new Object[] {String.class, Float.class};
			conf.addEventType("stockEventStream"+s, propertyNames, propertyTypes);
		}
		
        //admin.defineStream("define stream stockEventStream ( symbol string, price float)");
		List<EPStatement> queries = new ArrayList<EPStatement>();
		Random rnd = new Random(System.currentTimeMillis());
		for(int i=0; i < maxQueries; ++i) {
			int minPrice = rnd.nextInt(HIGHEST_PRICE);
			int maxPrice = minPrice + rnd.nextInt(MAX_SPREAD);
			String outputStream = "StockQuote"+(i%100);
			
			String query = 
					"insert into " + outputStream + " " +
					"select symbol, price " +
//					"from  stockEventStream ( price >= "+minPrice+" and price <= "+maxPrice+" ).win:length(1) "
					"from  stockEventStream"+(rnd.nextInt(maxInputStreams))+" ( price in ["+minPrice+" : " +maxPrice+" ]).win:length(1) "
					; 
			
			EPStatement queryStmt = addQuery(cepMgr, query);
			if (i<100) {
				// Define a dedicated listener for this output stream
				UpdateListener myListener = new StatsUpdateListener(outputStream);
				queryStmt.addListener(myListener);
				streamStats.put(outputStream, new StreamStats());
			}
			queries.add(queryStmt);
		}

		System.out.format("Initialized CEP engine with %d queries\n", maxQueries);
		EPRuntime runtime = cepMgr.getEPRuntime();
		EventSender[] senders = new EventSender[maxInputStreams];
		for(int s=0; s < maxInputStreams; s++)
			senders[s] = cepMgr.getEPRuntime().getEventSender("stockEventStream"+s);
		
		// Now feed in some data
		ArrayList<String> symbols = new ArrayList<String>() {{
			add("IBM");
			add("GOOG");
			add("ORCL");
			add("HZ");
			add("APPLE");
			add("DELL");
			add("HP");
			add("COMPAQ");
			add("EXXON");
			add("SHELL");
			add("ARAL");
			add("BAYER");
			add("LG");
			add("SAMSUNG");
		}};
		
		int symNum = symbols.size();
		
		long startTime = System.currentTimeMillis();
		
		for(int j=0; j<maxEvents; j++) {
			String symbol = symbols.get(rnd.nextInt(symNum));
			float price = rnd.nextFloat()*HIGHEST_PRICE;
			int sender = rnd.nextInt(maxInputStreams);
			Object[] theEvent = new Object[]{symbol, price};
			senders[sender].sendEvent(theEvent);
	        if (j%10000==0)
	        	System.out.println("Sent:" + j);
		}
		long endTime = System.currentTimeMillis();
		System.out.format("Processed %d events in %d milliseconds. %f events/sec\n", maxEvents, (endTime-startTime), maxEvents /((endTime-startTime)/1000.0));
		int overallMatchedEvents = 0;
		for(String k: new TreeSet<String>(streamStats.keySet())) {
			StreamStats stat = streamStats.get(k);
			if(stat.count > 0) {
				System.out.format("Stream %s: %d matching events\n", k, stat.count);
				overallMatchedEvents += stat.count;
			}
		}
		System.out.format("Overall matched events by all streams: %d\n", overallMatchedEvents);
	}

    public static void main(String[] args) throws InterruptedException {
    	EsperManyStreamsAndQueriesSample test = new EsperManyStreamsAndQueriesSample();
    	test.testManyQueries(10000, 10000000, 300);
    }
}