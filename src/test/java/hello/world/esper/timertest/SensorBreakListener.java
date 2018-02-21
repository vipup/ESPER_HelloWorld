package hello.world.esper.timertest;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class SensorBreakListener implements UpdateListener {
	private static long updateCounter = 0;
	public static long getUpdateCounter() {
		return updateCounter;
	}

	private static void setUpdateCounter(long updateCounter) {
		SensorBreakListener.updateCounter = updateCounter;
	}
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		setUpdateCounter(getUpdateCounter()+1); 
	}

}
