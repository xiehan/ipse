package madparker.gametools.util;

import java.util.List;

@SuppressWarnings("unused")
public abstract class Timer extends GameElement{

	protected long begin;
	
	protected long lastCheck;
	
	public boolean active = false;
	
	protected long currentTime;
	
	protected long frameMillis;
	
	public void trigger() {
		active = true;
		begin = pApplet.millis();
		currentTime = begin;
		lastCheck = currentTime;
		frameMillis = 0;
		executeBegin();
	}
	
	public void update() {
		currentTime = pApplet.millis();
		
		frameMillis = (currentTime - lastCheck);
		
		if (isEnd()){
			active = false;
			executeEnd();
		}else {//if (active && (currentTime > begin)){
			step();
		}
		lastCheck = currentTime;
	}
	
	public long getElapsed() {
		return lastCheck - begin;
	}
	
	protected abstract void executeBegin();
	
	protected abstract void step();
	
	protected abstract void executeEnd();
	
	protected abstract boolean isEnd();
	
	public long getBegin() {
		return begin;
	}

	public boolean isActive() {
		return active;
	}
	
	public void deactivate() {
		executeEnd();
		active = false;
	}
	
}