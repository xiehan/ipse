package ipse.avatar;

import processing.core.PApplet;
import madparker.gametools.util.GameElement;

public class SoundWave extends GameElement 
{
	private MultiplayerAvatar parent;
	
	private float currScale = 1.0f;
	private float interval = 0.1f;

	
	public SoundWave(MultiplayerAvatar parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void draw()
	{		
		pApplet.pushMatrix();
		pApplet.scale(currScale);
		parent.drawShape(3.0f, 30);
		parent.drawShape(1.0f, 100);
		pApplet.popMatrix();
	}
	
	public void update()
	{
		if (currScale >= 1.0f)
		{
			parent.playingSounds = false;
			return;
		}

		currScale += interval;
		
		checkIntersections();
	}
	
	private void checkIntersections()
	{
		for (Particle p : parent.particles)
		{
			if (!p.stationary) continue;
			float ans = (PApplet.sq(p.position.x)/PApplet.sq((currScale*parent.w)/2)) + 
						(PApplet.sq(p.position.y)/PApplet.sq((currScale*parent.h)/2));
			if (ans >= 0.25f & ans <= 1.0f)
				p.playSound();
		}
	}

	public void start(float soundWaveInterval) 
	{
		this.interval = soundWaveInterval;
		currScale = soundWaveInterval;
	}
}
