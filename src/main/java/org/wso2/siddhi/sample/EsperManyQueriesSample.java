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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventSender;


/**
 * Sample demonstrating a simple filtering use-case
 */
public class EsperManyQueriesSample {

	private static final int HIGHEST_PRICE = 1000000;
	private static final int MAX_SPREAD = 1000;

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
	
	public void testManyQueries(int maxQueries, int maxEvents) throws InterruptedException {
		
		System.out.println("Initializing CEP engine");
		
		// Some settings for single threaded apps
		Configuration config = new Configuration();
		config.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);
		config.getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(false);
		
		EPServiceProvider cepMgr = createCEPManager();
		EPAdministrator admin = cepMgr.getEPAdministrator();
		ConfigurationOperations conf = admin.getConfiguration();
		
		
		// Define event types
		String[] propertyNames = new String[] {"symbol", "price"};
		Object[] propertyTypes = new Object[] {String.class, Float.class};
		conf.addEventType("stockEventStream", propertyNames, propertyTypes);
		
        //admin.defineStream("define stream stockEventStream ( symbol string, price float)");
		List<EPStatement> queries = new ArrayList<EPStatement>();
		Random rnd = new Random(System.currentTimeMillis());
		for(int i=0; i < maxQueries; ++i) {
			int minPrice = rnd.nextInt(HIGHEST_PRICE);
			int maxPrice = minPrice + rnd.nextInt(MAX_SPREAD);
			
			String query = 
					"insert into StockQuote"+(i/100) + " " +
					"select * " +
//					"from  stockEventStream ( price >= "+minPrice+" and price <= "+maxPrice+" ).win:length(1) "
					"from  stockEventStream ( price in ["+minPrice+" : " +maxPrice+" ]).win:length(1) "
					; 
			
			EPStatement queryStmt = addQuery(cepMgr, query);
			queries.add(queryStmt);
		}

		System.out.format("Initialized CEP engine with %d queries\n", maxQueries);
		EPRuntime runtime = cepMgr.getEPRuntime();
		EventSender sender = cepMgr.getEPRuntime().getEventSender("stockEventStream");
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
			Object[] theEvent = new Object[]{symbol, price};
	        //runtime.sendEvent(, "");
			sender.sendEvent(theEvent);
	        if (j%1000==0)
	        	System.out.println("Sent:" + j);
		}
		long endTime = System.currentTimeMillis();
		System.out.format("Processed %d events in %d milliseconds\n", maxEvents, (endTime-startTime));
	}

    public static void main(String[] args) throws InterruptedException {
    	EsperManyQueriesSample test = new EsperManyQueriesSample();
    	test.testManyQueries(10000, 1000000);
    }
}
