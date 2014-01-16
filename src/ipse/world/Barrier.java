package ipse.world;

import java.util.Collection;

import ipse.app.Ipse;
import ipse.avatar.MultiplayerAvatar;
import ipse.screen.PlayScreen;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.util.GameElement;


@SuppressWarnings("unused")
public class Barrier extends GameElement 
{
	private PlayScreen parent;
	
	private float size;
	public PVector position;
	
	public int level = 4;
	
	
	public Barrier(PlayScreen parent, int level)
	{
		this(parent, level, pApplet.random(0, pApplet.width), pApplet.random(0, pApplet.height));
	}
	
	public Barrier (PlayScreen parent, int level, float x, float y)
	{
		this.parent = parent;
		this.level = level;
		this.position = new PVector(x, y);
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
		pApplet.translate(position.x + (size/2), position.y + (size/2));
		
		pApplet.pushStyle();
		pApplet.noStroke();
		pApplet.imageMode(PConstants.CENTER);
		pApplet.shapeMode(PConstants.CENTER);
		
		pApplet.tint(255, 120);
		pApplet.image(parent.barrierImage, 0, 0);

		/*pApplet.fill(color);
		pApplet.text(position.x + "," + position.y, 0, 0);*/
		
		pApplet.popStyle();
		pApplet.popMatrix();
	}
	
	@Override
	public void update()
	{
		parent.avatar.checkBarrierCollision(position, level);
		
		/*Collection<MultiplayerAvatar> others = parent.otherPlayers.values();
		for (MultiplayerAvatar otherPlayer : others)
			otherPlayer.checkBarrierCollision(position, level);*/
	}
	
	public void setPosition(float x, float y)
	{
		position = new PVector(x, y);
	}
}
