package hello.world.esper.timertest;

public class TimerEvent {
	static long objectCounter = 0;
	private final long id = ++objectCounter;
			
	private long timestamp;

	public TimerEvent (long timestamp) {
		this.setTimestamp(timestamp);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getId() {
		return id;
	}

}
