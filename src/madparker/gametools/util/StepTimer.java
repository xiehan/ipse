package madparker.gametools.util;

public class StepTimer extends Timer
{
	protected long end;
	
//	protected long baseBegin;
//	protected long baseEnd;
	
	protected long interval;
	protected long lastInterval;
	
	
	public StepTimer(long interval, long length) 
	{
		this.currentTime = pApplet.millis();
		this.interval = interval;
		this.end = currentTime+Math.min(length, Long.MAX_VALUE-currentTime);
	}
		
	public void trigger() {
		active = true;
		begin = pApplet.millis();
		currentTime = begin;
		lastCheck = currentTime;
//		end = baseEnd + begin;
		executeBegin();
	}
	
	public void update() {
		currentTime = pApplet.millis();
		
		if (currentTime > end){
			active = false;
			executeEnd();
		}
		
		if (active && (currentTime > begin) && (currentTime >= lastInterval + interval)) {
			executeStep();
			lastInterval = currentTime;
		}
		lastCheck = currentTime;
	}
	
	public long getElapsed() {
		return lastCheck - begin;
	}
	
	public long getRemaining() {
		return end - lastCheck;
	}
	
	protected void executeBegin() {
	}
	
	protected synchronized void executeStep() {
	}
	
	protected void executeEnd() {
	}
	
	public long getBegin() {
		return begin;
	}
	public long getEnd() {
		return end;
	}
	public long getInterval() {
		return interval;
	}
	public void setInterval(long interval) {
		this.interval = interval;
	}
	public boolean isActive() {
		return active;
	}
	
	public void deactivate() {
		executeEnd();
		active = false;
	}
	
	public boolean isEnd()
	{
		if (currentTime > end) return true;
		return false;
	}
	
	public void step() { executeStep(); }
}
