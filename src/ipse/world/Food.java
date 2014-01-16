package ipse.world;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

import ipse.app.Ipse;
import ipse.screen.PlayScreen;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.util.GameElement;


public class Food extends GameElement
{
	private PlayScreen parent;
	
	@SuppressWarnings("unused")
	private float size;
	public PVector position;
	
	private boolean visible = true;
	private static long defaultFoodTime = 1 * 60 * 1000;
	private long timeLeft = defaultFoodTime;
	
	
	public Food(PlayScreen parent)
	{
		this(parent, pApplet.random(0, pApplet.width), pApplet.random(0, pApplet.height));
	}
	
	public Food(PlayScreen parent, float x, float y)
	{
		this.parent = parent;
		this.position = new PVector(x + (Ipse.BLOCK_SIZE/2), y + (Ipse.BLOCK_SIZE/2));
		setup();
	}
	
	public void setup()
	{
		size = 12;
	}
	
	public void updateDB()
	{
		if (parent.parent.amazonOffline)
			return;
		
		String positionStr = (position.x-(Ipse.BLOCK_SIZE/2)) + 
			"," + (position.y-(Ipse.BLOCK_SIZE/2));
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(positionStr).withAttributes(
                new ReplaceableAttribute("Position", positionStr, true),
                new ReplaceableAttribute("Visible", Boolean.toString(visible), true),
                new ReplaceableAttribute("TimeLeft", ""+timeLeft, true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.worldDomain, data));
	}
	
	@Override
	public void draw()
	{
		if (!visible)
			return;
		
		pApplet.pushStyle();
		pApplet.imageMode(PConstants.CENTER);
		pApplet.image(parent.foodImage, position.x, position.y);
		pApplet.popStyle();
	}
	
	@Override
	public void update()
	{
		if (!visible)
		{
			timeLeft -= Ipse.UPDATE_INTERVAL;
			if (timeLeft <= 0)
			{
				visible = true;
				timeLeft = defaultFoodTime;
				updateDB();
			}
			return;
		}
		
		if (parent.avatar.checkIfEaten(this.position) == true)
		{
			visible = false;
			timeLeft = defaultFoodTime;
			updateDB();
		}
	}
	
	public void setPosition(float x, float y)
	{
		position = new PVector(x, y);
	}
	
	public void setVisibility(boolean visibility)
	{
		visible = visibility;
	}
	
	public void setTimeLeft(int timeLeft)
	{
		this.timeLeft = timeLeft;
	}

	public void stop() 
	{
		updateDB();
	}
}
