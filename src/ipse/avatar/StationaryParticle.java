package ipse.avatar;

public class StationaryParticle extends Particle 
{
	public StationaryParticle(MultiplayerAvatar parent)
	{
		super(parent);
	}
	
	public StationaryParticle(MultiplayerAvatar parent, float x, float y)
	{
		super(parent, x, y);
	}

	@Override
	public void setup()
	{
		color = pApplet.color(205, 51, 94);
		stationary = true;
		diameter = 12.0f;
	}
}
