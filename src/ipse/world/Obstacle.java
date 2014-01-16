package ipse.world;

import ipse.app.Ipse;
import ipse.screen.PlayScreen;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.util.GameElement;


public class Obstacle extends GameElement
{
	private PlayScreen parent;
	
	@SuppressWarnings("unused")
	private float size;
	protected PVector position;
	private float rotation = 0;
	
	
	public Obstacle(PlayScreen parent)
	{
		this(parent, pApplet.random(0, pApplet.width), pApplet.random(0, pApplet.height));
	}
	
	public Obstacle(PlayScreen parent, float x, float y)
	{
		this.parent = parent;
		this.position = new PVector(x + (Ipse.BLOCK_SIZE/2), y + (Ipse.BLOCK_SIZE/2));
		setup();
	}
	
	public void setup()
	{
		size = Ipse.BLOCK_SIZE;
	}
	
	@Override
	public void draw()
	{
		pApplet.pushMatrix();
		pApplet.translate(position.x, position.y);
		pApplet.rotate(PApplet.radians(rotation));
		
		pApplet.pushStyle();
		pApplet.imageMode(PConstants.CENTER);
		pApplet.image(parent.obstacleImage, 0, 0);
		
		pApplet.popStyle();
		pApplet.popMatrix();
	}
	
	@Override
	public void update()
	{
		rotation = (rotation + 10) % 360;
		
		parent.avatar.checkIfHurt(position);
	}
	
	public void setPosition(float x, float y)
	{
		position = new PVector(x, y);
	}
}
