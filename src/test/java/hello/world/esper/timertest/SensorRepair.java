package hello.world.esper.timertest;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class SensorRepair implements UpdateListener {

	private static long updateCounter = 0;
	public static long getUpdateCounter() {
		return updateCounter;
	}

	private static void setUpdateCounter(long updateCounter) {
		SensorRepair.updateCounter = updateCounter;
	}
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		System.out.println("SensorRepair job...");
		setUpdateCounter(getUpdateCounter()+1); 
	}
}
