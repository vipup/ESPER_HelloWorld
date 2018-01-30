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

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.Random;

/**
 * Sample demonstrating a simple filtering use-case
 */
public class SiddhiManyQueriesSample {

	private static final int HIGHEST_PRICE = 1000000;
	private static final int MAX_SPREAD = 1000;

	private static SiddhiManager createSiddhiManager() {
	    // Create Siddhi Manager
//		conf.setDistributedProcessing(true);
//		conf.setAsyncProcessing(true);

		SiddhiManager siddhiManager = new SiddhiManager();

	    return siddhiManager;
    }
	

	public void testManyQueries(int maxQueries, int maxEvents) throws InterruptedException {
		
		System.out.println("Initializing CEP engine");
		SiddhiManager cepMgr = createSiddhiManager();
		String executionPlan="@plan:parallel " +
				"define stream stockEventStream ( symbol string, price float); " +
				"";

//		List<String> queryNames = new ArrayList<String>();
		Random rnd = new Random(System.currentTimeMillis());
		for(int i=0; i < maxQueries; ++i) {
			int minPrice = rnd.nextInt(HIGHEST_PRICE);
//			int maxPrice = minPrice + rnd.nextInt(HIGHEST_PRICE-minPrice+1);
			int maxPrice = minPrice + rnd.nextInt(MAX_SPREAD);
			
			String query = 
					"from  stockEventStream [ price >= "+minPrice+" and price <= "+maxPrice+" ] " +
					"select * " +
					"insert into StockQuote"+(i/100) +"; ";

			executionPlan +=query;
//			queryNames.add(queryName);
		}

		ExecutionPlanRuntime runttime = cepMgr.createExecutionPlanRuntime(executionPlan);
		InputHandler inputHandler = runttime.getInputHandler("stockEventStream");

		runttime.start();
		System.out.format("Initialized CEP engine with %d queries\n", maxQueries);
		
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
	        inputHandler.send(new Object[]{symbol, price});
	        if (j%1000==0)
	        	System.out.println("Sent:" + j);
		}
		long endTime = System.currentTimeMillis();
		System.out.format("Processed %d events in %d milliseconds\n", maxEvents, (endTime-startTime));
	}

    public static void main(String[] args) throws InterruptedException {
    	SiddhiManyQueriesSample test = new SiddhiManyQueriesSample();
    	test.testManyQueries(10000, 1000000);
    }
}