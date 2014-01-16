package ipse.avatar;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.util.GameElement;


public class Particle extends GameElement 
{
	private static float SELECTION_BUFFER = 6.0f;
	
	protected MultiplayerAvatar parent;
	
	public PVector position;
	public PVector velocity;
	public PVector acceleration;
	
	protected float diameter = 3.0f;
	protected int color;
	
	protected boolean stationary = true;
	protected boolean selected = false;
	
	protected boolean soundPlayed = false;
	private boolean bounced = false;
	private int bouncedTimes = 0;
	
	
	public Particle(MultiplayerAvatar parent)
	{
		this(parent, pApplet.random(-parent.w/3, parent.w/3), 
				pApplet.random(-parent.h/3, parent.h/3));
	}
	
	public Particle(MultiplayerAvatar parent, float x, float y)
	{
		this.parent = parent;
		position = new PVector(x, y);
		velocity = new PVector(0, 0);
		acceleration = new PVector(0, 0);
		setup();
	}
	
	public void setup()
	{
		color = pApplet.color(200);
	}
	
	@Override
	public void draw()
	{
		pApplet.pushMatrix();
		pApplet.translate(position.x, position.y);
		if (selected)
			pApplet.scale(1.5f);
		
		pApplet.pushStyle();
		pApplet.ellipseMode(PConstants.CENTER);
		pApplet.noStroke();

		pApplet.fill(color, 30);
		pApplet.ellipse(0, 0, diameter+5, diameter+5);

		pApplet.fill(color, 100);
		pApplet.ellipse(0, 0, diameter+2, diameter+2);
		
		pApplet.fill(color, 255);
		pApplet.ellipse(0, 0, diameter, diameter);
		
		pApplet.popStyle();
		
		pApplet.popMatrix();
	}
	
	@Override
	public void update()
	{		
		if (selected)
		{
			if (position.x != pApplet.mouseX - parent.position.x || 
				position.y != pApplet.mouseY - parent.position.y)
				parent.particleMoved = true;
			position.x = pApplet.mouseX - parent.position.x;
			position.y = pApplet.mouseY - parent.position.y;
		}

		float ans = (PApplet.sq(position.x)/PApplet.sq(parent.w/2)) + 
					(PApplet.sq(position.y)/PApplet.sq(parent.h/2));
		if (ans >= 1.0f)
			position = PVector.mult(position, 1/ans);
		
		if (selected) return;
		if (stationary) return;
		
		boundaryCollisionTest();
		
		velocity.x += acceleration.x;
		velocity.y += acceleration.y;
		position.x += velocity.x;
		position.y += velocity.y;
	}
	
	private void boundaryCollisionTest()
	{
		PVector pos = parent.calculateRelativePos(position);
		float ans = (PApplet.sq(pos.x+velocity.x)/PApplet.sq(parent.w/2)) + 
					(PApplet.sq(pos.y+velocity.y)/PApplet.sq(parent.h/2));
		
		if (!bounced && ans >= 0.95f)
		{
			velocity.x *= -1;
			velocity.y *= -1;
			bounced = true;
			bouncedTimes = 0;
			playSound();
		}
		else if (bounced)
		{
			if (bouncedTimes < 30)
				bouncedTimes++;
			else
				bounced = false;
		}
	}
	
	public void mousePressed()
	{
		float maxX = position.x + (diameter/2) + parent.position.x + SELECTION_BUFFER;
		float minX = position.x - (diameter/2) + parent.position.x - SELECTION_BUFFER;
		float maxY = position.y + (diameter/2) + parent.position.y + SELECTION_BUFFER;
		float minY = position.y - (diameter/2) + parent.position.y - SELECTION_BUFFER;
		
		if (pApplet.mouseX > minX && pApplet.mouseX < maxX &&
			pApplet.mouseY > minY && pApplet.mouseY < maxY)
			selected = true;
		else
			selected = false;
	}
	
	public void mouseReleased()
	{
		selected = false;
	}
	
	public void playSound()
	{
		if (soundPlayed)
			return;

		float distToCenter = PApplet.sqrt(PApplet.sq(position.x)+PApplet.sq(position.y));
		
		if (!stationary)
		{
			float maxDist = parent.w/2;
			float minDist = parent.h/2;
			if (parent.h > parent.w)
			{
				maxDist = parent.h/2;
				minDist = parent.w/2;
			}
			
			int pos = (int) PApplet.constrain(PApplet.map(distToCenter, minDist, maxDist, 
					0, parent.parent.parent.bass.length-1), 
					0, parent.parent.parent.bass.length-1);
			parent.parent.parent.bass[pos].start();
			//soundPlayed = true;
		}
		else
		{
			float x = ((parent.w/2)*(parent.h/2)) / PApplet.sqrt(
						(PApplet.sq(parent.w/2)*PApplet.sq(position.y)) +
						(PApplet.sq(parent.h/2)*PApplet.sq(position.x))) * position.x;
			float y = ((parent.w/2)*(parent.h/2)) / PApplet.sqrt(
						(PApplet.sq(parent.w/2)*PApplet.sq(position.y)) +
						(PApplet.sq(parent.h/2)*PApplet.sq(position.x))) * position.y;
			float totalDist = PApplet.sqrt(PApplet.sq(x)+PApplet.sq(y));
			
			int pos = (int) PApplet.constrain(PApplet.map(distToCenter, 0, totalDist, 
					0, parent.parent.parent.tones.length-1), 
					0, parent.parent.parent.tones.length-1);
			parent.parent.parent.tones[pos].start();
			soundPlayed = true;
		}
	}
	
	public void reset()
	{
		soundPlayed = false;
	}
}
