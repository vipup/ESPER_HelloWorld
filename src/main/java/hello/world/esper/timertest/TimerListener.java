package hello.world.esper.timertest;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.bean.BeanEventBean; 

public class TimerListener implements UpdateListener {
	static long updateCounter = 0;

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		updateCounter++;
		for (Object e:newEvents) {
			System.out.println(e);
			BeanEventBean eBean = (BeanEventBean)e; 
			long l = System.currentTimeMillis() - Long.parseLong( ""+eBean.get("timestamp"));
			long id =   Long.parseLong( ""+eBean.get("id"));
			System.out.println("TTTTTTTTTTTTTTT#"+id+"/"+updateCounter+":: deliveryTime:"+ l+" ms." );
		}
	}
}
