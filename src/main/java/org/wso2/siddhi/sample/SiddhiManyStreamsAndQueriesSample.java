///**
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.wso2.siddhi.sample;
//
//import org.wso2.siddhi.core.SiddhiManager;
//import org.wso2.siddhi.core.config.SiddhiConfiguration;
//import org.wso2.siddhi.core.event.Event;
//import org.wso2.siddhi.core.persistence.PersistenceStore;
//import org.wso2.siddhi.core.stream.input.InputHandler;
//import org.wso2.siddhi.core.stream.output.StreamCallback;
//
//import java.util.*;
//
//
///**
// * Sample demonstrating a simple filtering use-case
// */
//public class SiddhiManyStreamsAndQueriesSample {
//
//	private static final int HIGHEST_PRICE = 1000000;
//	private static final int MAX_SPREAD = 1000;
//
//	private static SiddhiManager createSiddhiManager(PersistenceStore persistenceStore) {
//	    // Create Siddhi Manager
//		SiddhiConfiguration conf = new SiddhiConfiguration();
////		conf.setDistributedProcessing(true);
////		conf.setAsyncProcessing(true);
//
//		SiddhiManager siddhiManager = new SiddhiManager(conf);
//		siddhiManager.setPersistStore(persistenceStore);
//
//	    return siddhiManager;
//    }
//
//	static class StreamStats {
//		int count;
//	}
//
//	Map<String, StreamStats> streamStats = new HashMap<String, StreamStats>();
//
//	/**
//	 * Callback to be called when new events are put into a stream
//	 *
//	 */
//	class StatsUpdateListener  extends StreamCallback{
//		private final String streamName;
//		public StatsUpdateListener(String streamName) {
//			this.streamName = streamName;
//		}
//        @Override
//        public void receive(Event[] events) {
//        	streamStats.get(streamName).count++;
//        }
//	}
//
//	public String addQuery(SiddhiManager cepMgr, String query) {
////		System.out.println("Query: " + query);
//		return cepMgr.addQuery(query);
//	}
//
//	public void testManyQueries(int maxQueries, int maxEvents, int maxInputStreams) throws InterruptedException {
//
//		System.out.println("Initializing CEP engine");
//		SiddhiManager cepMgr = createSiddhiManager(null);
//		InputHandler[] inputHandlers = new InputHandler[maxInputStreams];
//		StreamCallback[] updateListeners = new StreamCallback[100];
//
//		for(int s=0; s < maxInputStreams; s++) {
//			String streamName = "stockEventStream" + s;
//			cepMgr.defineStream("define stream " + streamName+" ( symbol string, price float)");
//			InputHandler inputHandler = cepMgr.getInputHandler(streamName);
//			inputHandlers[s] = inputHandler;
//		}
//
//		for(int os = 0; os < 100; os++) {
//			String outputStream = "StockQuote"+os;
//			updateListeners[os] = new StatsUpdateListener(outputStream);
//		}
//
//		List<String> queryNames = new ArrayList<String>();
//		Random rnd = new Random(System.currentTimeMillis());
//		for(int i=0; i < maxQueries; ++i) {
//			int minPrice = rnd.nextInt(HIGHEST_PRICE);
//			int maxPrice = minPrice + rnd.nextInt(MAX_SPREAD);
//			String outputStream = "StockQuote"+(i%100);
//			String query =
//					"from  stockEventStream"+(rnd.nextInt(maxInputStreams))+" [ price >= "+minPrice+" and price <= "+maxPrice+" ] " +
//					"select * " +
//					"insert into " + outputStream;
//
//			String queryName = addQuery(cepMgr, query);
//			//Query queryObj = cepMgr.getQuery(queryName);
//			queryNames.add(queryName);
//			if(i<100) {
//				cepMgr.addCallback(outputStream, updateListeners[i]);
//				streamStats.put(outputStream, new StreamStats());
//			}
//		}
//
//		System.out.format("Initialized CEP engine with %d queries\n", maxQueries);
//
//		// Now feed in some data
//		ArrayList<String> symbols = new ArrayList<String>() {{
//			add("IBM");
//			add("GOOG");
//			add("ORCL");
//			add("HZ");
//			add("APPLE");
//			add("DELL");
//			add("HP");
//			add("COMPAQ");
//			add("EXXON");
//			add("SHELL");
//			add("ARAL");
//			add("BAYER");
//			add("LG");
//			add("SAMSUNG");
//		}};
//
//		int symNum = symbols.size();
//
//		long startTime = System.currentTimeMillis();
//		for(int j=0; j<maxEvents; j++) {
//			String symbol = symbols.get(rnd.nextInt(symNum));
//			float price = rnd.nextFloat()*HIGHEST_PRICE;
//			int sender = rnd.nextInt(maxInputStreams);
//			Object[] theEvent = new Object[]{symbol, price};
//	        inputHandlers[sender].send(theEvent);
//	        if (j%10000==0)
//	        	System.out.println("Sent:" + j);
//		}
//		long endTime = System.currentTimeMillis();
//		System.out.format("Processed %d events in %d milliseconds. %f events/sec\n", maxEvents, (endTime-startTime), maxEvents /((endTime-startTime)/1000.0));
//		int overallMatchedEvents = 0;
//		for(String k: new TreeSet<String>(streamStats.keySet())) {
//			StreamStats stat = streamStats.get(k);
//			if(stat.count > 0) {
//				System.out.format("Stream %s: %d matching events\n", k, stat.count);
//				overallMatchedEvents += stat.count;
//			}
//		}
//		System.out.format("Overall matched events by all streams: %d\n", overallMatchedEvents);
//	}
//
//    public static void main(String[] args) throws InterruptedException {
//    	SiddhiManyStreamsAndQueriesSample test = new SiddhiManyStreamsAndQueriesSample();
//    	test.testManyQueries(10000, 10000000, 300);
//    }
//}