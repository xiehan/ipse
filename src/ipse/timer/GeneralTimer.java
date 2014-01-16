package ipse.timer;

import ipse.app.Ipse;
import madparker.gametools.util.StepTimer;

public class GeneralTimer extends StepTimer
{
	private Ipse parent;

	public GeneralTimer(Ipse parent, long interval) 
	{
		this(parent, interval, Long.MAX_VALUE);
	}

	public GeneralTimer(Ipse parent, long interval, long length) 
	{
		super(interval, length);
		this.parent = parent;
	}

	protected synchronized void executeStep()
	{
		parent.update();
	}
}
