package ipse.avatar;

import processing.core.PVector;


public class MovingParticle extends Particle 
{
	public MovingParticle(MultiplayerAvatar parent)
	{
		super(parent);
	}
	
	public MovingParticle(MultiplayerAvatar parent, float x, float y)
	{
		super(parent, x, y);
		velocity = new PVector(pApplet.random(-1.0f, 1.0f), pApplet.random(-1.0f, 1.0f));
	}
	
	public MovingParticle(MultiplayerAvatar parent, float x, float y, float v1, float v2)
	{
		super(parent, x, y);
		velocity = new PVector(v1, v2);
	}

	@Override
	public void setup()
	{
		color = pApplet.color(225, 225, 125);
		stationary = false;
		diameter = 7.5f;
	}
}
