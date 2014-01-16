package ipse.timer;

import madparker.gametools.util.StepTimer;
import ipse.avatar.MultiplayerAvatar;
import ipse.avatar.SoundWave;

public class ToneTimer extends StepTimer 
{
	private MultiplayerAvatar parent;
	
	public ToneTimer(MultiplayerAvatar parent, long interval)
	{
		this(parent, interval, Long.MAX_VALUE);
	}
	
	public ToneTimer(MultiplayerAvatar parent, long interval, long length)
	{
		super(interval, length);
		this.parent = parent;
		
		parent.soundWave = new SoundWave(parent);
	}

	@Override
	protected synchronized void executeStep()
	{
		parent.playSounds();
	}
}
