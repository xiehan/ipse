package ipse.timer;

import madparker.gametools.util.StepTimer;
import ipse.avatar.Avatar;


public class HurtTimer extends StepTimer 
{
	private Avatar parent;
	
	
	public HurtTimer(Avatar parent, long interval)
	{
		this(parent, interval, Long.MAX_VALUE);
	}
	
	public HurtTimer(Avatar parent, long interval, long length)
	{
		super(interval, length);
		this.parent = parent;
	}

	@Override
	protected synchronized void executeStep()
	{
		parent.blink();
	}
	
	@Override
	public void executeEnd()
	{
		parent.immune = false;
		parent.blinked = false;
	}
}
