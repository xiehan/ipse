package ipse.timer;

import ipse.app.Ipse;
import madparker.gametools.util.StepTimer;


public class AmazonTimer extends StepTimer
{
	private Ipse parent;
	
	public static long defaultInterval = 3 * 60 * 1000;
	

	public AmazonTimer(Ipse parent) 
	{
		this(parent, defaultInterval, Long.MAX_VALUE);
	}
	
	public AmazonTimer(Ipse parent, long interval) 
	{
		this(parent, interval, Long.MAX_VALUE);
	}

	public AmazonTimer(Ipse parent, long interval, long length) 
	{
		super(interval, length);
		this.parent = parent;
	}

	protected synchronized void executeStep()
	{
		if (parent.amazonOffline)
			parent.attemptAmazonConnect();
		else
			deactivate();
	}
}
