package ipse.timer;

import ipse.app.Ipse;
import madparker.gametools.util.StepTimer;


public class MultiplayerTimer extends StepTimer
{
	private Ipse parent;
	
	public static long defaultInterval = 3 * 1000;
	

	public MultiplayerTimer(Ipse parent) 
	{
		this(parent, defaultInterval, Long.MAX_VALUE);
	}
	
	public MultiplayerTimer(Ipse parent, long interval) 
	{
		this(parent, interval, Long.MAX_VALUE);
	}

	public MultiplayerTimer(Ipse parent, long interval, long length) 
	{
		super(interval, length);
		this.parent = parent;
	}

	protected synchronized void executeStep()
	{
		if (!parent.amazonOffline)
			parent.multiplayerUpdate();
		else
			deactivate();
	}
}
