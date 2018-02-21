package hello.world.esper.timertest;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.bean.BeanEventBean; 

public class TimerFunction implements UpdateListener {
	private static long updateCounter = 0;
	public static long getUpdateCounter() {
		return updateCounter;
	}

	private static void setUpdateCounter(long updateCounter) {
		TimerFunction.updateCounter = updateCounter;
	}	

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		setUpdateCounter(getUpdateCounter() + 1);
		for (Object e:newEvents) {
			System.out.println(e);
			BeanEventBean eBean = (BeanEventBean)e; 
			long l = System.currentTimeMillis() - Long.parseLong( ""+eBean.get("timestamp"));
			long id =   Long.parseLong( ""+eBean.get("id"));
			System.out.println("TTTTTTTTTTTTTTT#"+id+"/"+getUpdateCounter()+":: deliveryTime:"+ l+" ms." );
		}
	}


}
