package ipse.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

import apwidgets.PButton;
import apwidgets.PWidget;

import madparker.gametools.screen.Screen;

import ipse.app.Ipse;
import ipse.avatar.Avatar;
import ipse.avatar.MultiplayerAvatar;
import ipse.world.Barrier;
import ipse.world.Food;
import ipse.world.Obstacle;


public class PlayScreen extends Screen 
{
	public Avatar avatar;
	
	private int levelWidth = 130;
	private int levelHeight = 30;
	private int worldWidth = levelWidth * 3;
	private int worldHeight = levelHeight;
	public int worldLevel = 1;
	
	private PVector screenPosition;
	private PVector screenDestination;
	private PVector screenVelocity;
	private PVector screenAtLastReposition;
	private static PVector SCREEN_BLOCK_BUFFER = new PVector(12, 10); 

	public ArrayList<Food> allFood = new ArrayList<Food>();
	public ArrayList<Obstacle> allObstacles = new ArrayList<Obstacle>();
	public ArrayList<Barrier> allBarriers = new ArrayList<Barrier>();
	
	public PImage barrierImage;
	public PImage obstacleImage;
	public PImage foodImage;
	
	private PImage bg1;
	private PImage bg2;
	private float bg1Offset = 0;
	private float bg2Offset = 0;
	
	private boolean avatarMoved = false;
	
	public HashMap<String,MultiplayerAvatar> otherPlayers = 
		new HashMap<String,MultiplayerAvatar>();
	public ArrayList<PButton> buttons = new ArrayList<PButton>();


	public PlayScreen(Ipse parent, Avatar avatar)
	{
		this.parent = parent;
		this.avatar = avatar;
		avatar.parent = this;
		avatar.activatePlayMode();
		setup();
		avatar.updateDB("Online", Boolean.toString(true));
	}
	
	@Override
	public void setup()
	{
		super.setup();
		
		barrierImage = pApplet.loadImage("barrier.png");
		obstacleImage = pApplet.loadImage("obstacle.png");
		foodImage = pApplet.loadImage("food.png");
		
		screenPosition = new PVector();
		screenPosition.x = PApplet.constrain(avatar.position.x - (pApplet.width/2), 
				0, worldWidth*Ipse.BLOCK_SIZE - pApplet.width);
		screenPosition.y = PApplet.constrain(avatar.position.y - (pApplet.height/2),
				0, worldHeight*Ipse.BLOCK_SIZE - pApplet.height);
		screenDestination = new PVector(screenPosition.x, screenPosition.y);
		
		positionElements();

		parent.resetWidgets = true;
	}
	
	@Override
	public synchronized void resetWidgets()
	{
		int width = 180;
		PButton b1 = new PButton((pApplet.width/2)-(width/2), 
				(pApplet.height*2/3), width, 54, "GIVE PARTICLE");
		PButton b2 = new PButton((pApplet.width/2)-(width/2),
				(pApplet.height*2/3)+60, width, 54, "TAKE PARTICLE");
		PButton b3 = new PButton((pApplet.width/2)-(width/2),
				(pApplet.height*2/3)+120, width, 54, "DO NOTHING");
		parent.widgetContainer.addWidget(b1);
		parent.widgetContainer.addWidget(b2);
		parent.widgetContainer.addWidget(b3);
		buttons.add(b1);
		buttons.add(b2);
		buttons.add(b3);
	}

	public synchronized void positionElements()
	{
		for (Food f : allFood)
			f.updateDB();
		
		allBarriers.clear();
		allObstacles.clear();
		allFood.clear();

		PVector avatarBlock = new PVector(
				PApplet.floor(avatar.position.x/Ipse.BLOCK_SIZE),
				PApplet.floor(avatar.position.y/Ipse.BLOCK_SIZE));
		int xLevel = PApplet.ceil(avatarBlock.x/levelWidth);
		int yLevel = PApplet.floor(avatarBlock.y/levelHeight);
		worldLevel = xLevel + (yLevel*3);
		
		int minHorizBlock = PApplet.floor(screenPosition.x/Ipse.BLOCK_SIZE);
		int maxHorizBlock = PApplet.ceil((screenPosition.x+pApplet.width)/Ipse.BLOCK_SIZE);
		int minVertiBlock = PApplet.floor(screenPosition.y/Ipse.BLOCK_SIZE);
		int maxVertiBlock = PApplet.ceil((screenPosition.y+pApplet.height)/Ipse.BLOCK_SIZE);
		
		minHorizBlock = PApplet.max(minHorizBlock - (int)SCREEN_BLOCK_BUFFER.x, 0);
		maxHorizBlock = PApplet.min(maxHorizBlock + (int)SCREEN_BLOCK_BUFFER.x, worldWidth-1);
		minVertiBlock = PApplet.max(minVertiBlock - (int)SCREEN_BLOCK_BUFFER.y, 0);
		maxVertiBlock = PApplet.min(maxVertiBlock + (int)SCREEN_BLOCK_BUFFER.y, worldHeight-1);

		String spaceStr = "";
		for (int i = 0; i < levelWidth; i++)
			spaceStr += " ";
		
		String[] level1Strings = pApplet.loadStrings("level1");
		String[] level2Strings = pApplet.loadStrings("level2");
		String[] level3Strings = pApplet.loadStrings("level3");
		for(int j = minVertiBlock; j <= maxVertiBlock; j++)
		{
			String level1Str = (level1Strings[j]+spaceStr).substring(0, levelWidth);
			String level2Str = (level2Strings[j]+spaceStr).substring(0, levelWidth);
			String level3Str = (level3Strings[j]+spaceStr).substring(0, levelWidth);

			String spriteStr = level1Str;
			if (maxHorizBlock >= levelWidth)
				spriteStr += level2Str;
			if (maxHorizBlock >= (levelWidth*2))
				spriteStr += level3Str;
			
			for(int i = minHorizBlock; i <= maxHorizBlock; i++)
			{
				if (i >= spriteStr.length())
					break;
				
				char c = spriteStr.charAt(i);
				switch (c)
				{
					case 'X' :
						Obstacle o = new Obstacle(this, i*Ipse.BLOCK_SIZE, j*Ipse.BLOCK_SIZE);
						allObstacles.add(o);
						break;
					case 'o' :
						Food f = new Food(this, i*Ipse.BLOCK_SIZE, j*Ipse.BLOCK_SIZE);
						String positionStr = (i*Ipse.BLOCK_SIZE) + "," + (j*Ipse.BLOCK_SIZE);
						GetAttributesRequest gar = 
							new GetAttributesRequest(parent.worldDomain, positionStr);
						gar = gar.withAttributeNames("TimeLeft", "Visible");
						GetAttributesResult result = parent.sdb.getAttributes(gar);
						@SuppressWarnings("unchecked")
						List<Attribute> attributes = result.getAttributes();
						for (Attribute a : attributes)
						{
							if (a.getName().equals("TimeLeft"))
								f.setTimeLeft(Integer.parseInt(a.getValue()));
							else if (a.getName().equals("Visible"))
								f.setVisibility(Boolean.parseBoolean(a.getValue()));
						}
						if (attributes.size() < 1)
							f.updateDB();
						allFood.add(f);
						break;
					case '/' :
						int bLevelX = PApplet.ceil(i/levelWidth);
						int bLevelY = PApplet.floor(j/levelHeight);
						int barrierLevel = bLevelX + (bLevelY*3);
						Barrier b = new Barrier(this, barrierLevel+3,
								i*Ipse.BLOCK_SIZE, j*Ipse.BLOCK_SIZE);
						allBarriers.add(b);
						break;
					default:
						break;
				}
			}
		}

		screenAtLastReposition = new PVector(screenPosition.x, screenPosition.y);
		
		@SuppressWarnings("unused")
		int bg1width = 1920, bg2width = 1920, bg4width = 1920, bg5width = 1920, 
			bg6width = 1920, bg7width = 1920, bg9width = 1920;
		int bg3width = 1600, bg8width = 1600;
		float cumulativeWidth = bg1width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-1.jpg");
			bg1Offset = 0;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg2width - pApplet.width)
		if ((minHorizBlock*Ipse.BLOCK_SIZE) <= bg1width + pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-3.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg2width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg2width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-3.jpg");
			bg1Offset = cumulativeWidth - bg2width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg3width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-6.jpg");
			bg2Offset = cumulativeWidth;
		} 
		cumulativeWidth += bg3width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg3width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-6.jpg");
			bg1Offset = cumulativeWidth - bg3width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg2width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-10.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg4width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg4width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-10.jpg");
			bg1Offset = cumulativeWidth - bg4width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg5width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-9.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg5width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg5width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-9.jpg");
			bg1Offset = cumulativeWidth - bg5width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + bg6width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-8.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg6width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg6width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-8.jpg");
			bg1Offset = cumulativeWidth - bg6width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg7width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-4.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg7width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg7width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-4.jpg");
			bg1Offset = cumulativeWidth - bg7width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg8width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-5.jpg");
			bg2Offset = cumulativeWidth;
		}
		cumulativeWidth += bg8width;
		if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - bg8width + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) <= cumulativeWidth + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-5.jpg");
			bg1Offset = cumulativeWidth - bg8width;
			/*}
		if ((maxHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth - pApplet.width && 
			(maxHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg9width - pApplet.width)
		{*/
			bg2 = pApplet.loadImage("bg-7.jpg");
			bg2Offset = cumulativeWidth;
		}
		/*if ((minHorizBlock*Ipse.BLOCK_SIZE) > cumulativeWidth + pApplet.width && 
			(minHorizBlock*Ipse.BLOCK_SIZE) < cumulativeWidth + bg9width + pApplet.width)
		{
			bg1 = pApplet.loadImage("bg-7.jpg");
			bg1Offset = cumulativeWidth;
		}*/
	}

	@Override
	public void draw()
	{
		pApplet.background(0);
		
		pApplet.pushStyle();
		pApplet.textFont(smallFont);
		
		pApplet.pushMatrix();
		pApplet.translate(-screenPosition.x, -screenPosition.y);
		
		if (bg1 != null)
			pApplet.image(bg1, bg1Offset, 0);
		if (bg2 != null)
			pApplet.image(bg2, bg2Offset, 0);
		
		for (int i = 0; i < allBarriers.size(); i++)
			allBarriers.get(i).draw();
		
		avatar.draw();
		
		Collection<MultiplayerAvatar> others = otherPlayers.values();
		for (MultiplayerAvatar otherPlayer : others)
			otherPlayer.draw();
		
		for (int i = 0; i < allObstacles.size(); i++)
			allObstacles.get(i).draw();
		
		for (int i = 0; i < allFood.size(); i++)
			allFood.get(i).draw();

		pApplet.popMatrix();
		
		drawInterface();
		
		pApplet.popStyle();
	}
	
	public void drawInterface()
	{
		pApplet.fill(0, 200);
		pApplet.rect(0, 0, pApplet.width, 80);
		pApplet.fill(200);
		pApplet.textFont(bigFont);
		pApplet.textAlign(PConstants.LEFT, PConstants.BASELINE);
		pApplet.text(avatar.name.toLowerCase(), 50, 60);
		pApplet.fill(100);
		pApplet.textFont(medFont);
		pApplet.textAlign(PConstants.RIGHT, PConstants.BASELINE);
		pApplet.text("LEVEL " + avatar.level, pApplet.width-50, 60);

		pApplet.fill(0, 200);
		pApplet.rect(0, pApplet.height-80, pApplet.width, 80);
		pApplet.noFill();
		pApplet.stroke(100);
		pApplet.strokeWeight(2.0f);
		pApplet.line(50, pApplet.height-60, 50, pApplet.height-40);
		pApplet.line(50, pApplet.height-40, pApplet.width-50, pApplet.height-40);
		pApplet.line(pApplet.width-50, pApplet.height-60, pApplet.width-50, pApplet.height-40);
		pApplet.noStroke();
		pApplet.fill(200);
		
		float barWidth = (pApplet.width-100)*avatar.energyLevel;
		pApplet.rect(50, pApplet.height-60, barWidth, 20);
		pApplet.fill(100);
		pApplet.textFont(smallFont);
		pApplet.textAlign(PConstants.RIGHT, PConstants.TOP);
		pApplet.text("TO NEXT LEVEL", 50, pApplet.height-30, pApplet.width-100, 30);
	}

	@Override
	public void update()
	{
		if (pApplet.mousePressed)
			avatar.setDestination(new PVector(
				pApplet.mouseX+screenPosition.x, pApplet.mouseY+screenPosition.y), true);

		avatarMoved = false;
		PVector oldPosition = new PVector(avatar.position.x, avatar.position.y);
		
		avatar.update();
		
		if (avatar.position.x != oldPosition.x || avatar.position.y != oldPosition.y)
			avatarMoved = true;
		
		screenDestination.x = PApplet.constrain(avatar.position.x - (pApplet.width/2), 
				0, worldWidth*Ipse.BLOCK_SIZE - pApplet.width);
		screenDestination.y = PApplet.constrain(avatar.position.y - (pApplet.height/2),
				0, worldHeight*Ipse.BLOCK_SIZE - pApplet.height);
		screenVelocity = PVector.sub(screenDestination, screenPosition);
		screenVelocity.limit(3.0f);
		
		if (screenPosition.x != screenDestination.x || screenPosition.y != screenDestination.y)
		{
			PVector toBeShifted = PVector.sub(screenDestination, screenPosition);

			if (toBeShifted.x < 0)
				screenPosition.x = 
					PApplet.max(screenPosition.x + screenVelocity.x, screenDestination.x);
			else
				screenPosition.x = 
					PApplet.min(screenPosition.x + screenVelocity.x, screenDestination.x);

			if (toBeShifted.y < 0)
				screenPosition.y = 
					PApplet.max(screenPosition.y + screenVelocity.y, screenDestination.y);
			else
				screenPosition.y = 
					PApplet.min(screenPosition.y + screenVelocity.y, screenDestination.y);
		}
		
		System.out.println((screenVelocity.x) + 
				"," + (screenVelocity.y));
		
		PVector numBlocksShifted = new PVector(
				Math.abs(screenPosition.x-screenAtLastReposition.x)/Ipse.BLOCK_SIZE,
				Math.abs(screenPosition.y-screenAtLastReposition.y)/Ipse.BLOCK_SIZE);
		if (numBlocksShifted.x >= SCREEN_BLOCK_BUFFER.x || 
			numBlocksShifted.y >= SCREEN_BLOCK_BUFFER.y)
			positionElements();

		Collection<MultiplayerAvatar> others = otherPlayers.values();
		for (MultiplayerAvatar otherPlayer : others)
			otherPlayer.update();
		
		if (avatarMoved)
		{
			for (int i = 0; i < allBarriers.size(); i++)
				allBarriers.get(i).update();
			
			for (int i = 0; i < allFood.size(); i++)
				allFood.get(i).update();
		}
		
		for (int i = 0; i < allObstacles.size(); i++)
			allObstacles.get(i).update();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void multiplayerUpdate()
	{
		if (otherPlayers.size() > 0)
		{
			SelectRequest sr = new SelectRequest("select * from " + 
					parent.domain + " where Online = '" + Boolean.toString(false) + 
					"' " + "and Username != '" + parent.username + "'");
			SelectResult result = parent.sdb.select(sr);
			List<Item> results = result.getItems();
			for (Item i : results)
			{
				String otherName = i.getName();
				if (otherPlayers.containsKey(otherName))
				{
					otherPlayers.remove(otherName);
					if (avatar.fusingWith.equals(otherName))
					{
						avatar.fusingWith = "";
						avatar.fusing = false;
					}
				}
			}
		}
		
		SelectRequest sr = new SelectRequest("select * from " + 
				parent.domain + " where Online = '" + Boolean.toString(true) + "' " +
				"and Username != '" + parent.username + "'");
		SelectResult result = parent.sdb.select(sr);
		List<Item> results = result.getItems();
		if (results.size() > 0)
		{
			for (Item i : results)
			{
				String otherName = i.getName();
				if (otherPlayers.containsKey(otherName))
				{
					MultiplayerAvatar otherPlayer = otherPlayers.get(otherName);
					List<Attribute> attributes = i.getAttributes();
					for (Attribute a : attributes)
						if (a.getName().equals("WorldPosition") || 
							a.getName().equals("WorldDestination") || 
							a.getName().equals("LastUpdated"))
								otherPlayer.parseDB(a);
					otherPlayer.position = otherPlayer.worldPosition;
				}
				else
				{
					MultiplayerAvatar otherPlayer = new MultiplayerAvatar(this, otherName);
					otherPlayers.put(otherName, otherPlayer);
					List<Attribute> attributes = i.getAttributes();
					for (Attribute a : attributes)
						otherPlayer.parseDB(a);
					otherPlayer.position = otherPlayer.worldPosition;
					MultiplayerAvatar m = otherPlayers.put(otherName, otherPlayer);
					if (m == null)
						PApplet.println(true);
				}
			}
			
			avatar.batchUpdateDB();
			
			SelectRequest sr2 = new SelectRequest("select * from " + 
					parent.multiplayerDomain + " where Receiver = '" + 
					avatar.username + "' " +
					"and Username != '" + parent.username + "'");
			SelectResult result2 = parent.sdb.select(sr2);
			List<Item> results2 = result2.getItems();
			for (Item j : results2)
			{
				if (avatar.parseDBParticle("", j.getName()))
				{
					parent.sdb.deleteAttributes(
						new DeleteAttributesRequest(parent.multiplayerDomain, j.getName()));
					avatar.saveParticles();
				}
			}
			
			SelectRequest sr3 = new SelectRequest("select * from " + 
					parent.multiplayerDomain + " where Giver = '" + 
					avatar.username + "' " +
					"and Username != '" + parent.username + "'");
			SelectResult result3 = parent.sdb.select(sr3);
			List<Item> results3 = result3.getItems();
			for (Item k : results3)
				avatar.giveParticle(k.getName(), false);
		}
	}

	@Override
	public void mousePressed()
	{
		avatar.setDestination(new PVector(
			pApplet.mouseX+screenPosition.x, pApplet.mouseY+screenPosition.y), true);
	}
	
	@Override
	public void onClickWidget(PWidget widget)
	{
		if (buttons.size() < 3)
			return;
		
		if (widget == buttons.get(0))
		{
			avatar.giveParticle(avatar.fusingWith, true);
			prepareToResetWidgets();
		}
		else if (widget == buttons.get(1))
		{
			avatar.takeParticle();
			prepareToResetWidgets();
		}
		else if (widget == buttons.get(2))
		{
			avatar.doNothing();
			prepareToResetWidgets();
		}
	}
	
	public void prepareToResetWidgets()
	{
		parent.widgetContainer.hide();
		for (PButton b : buttons)
			parent.widgetContainer.removeWidget(b);
		parent.resetWidgets = true;
		buttons.clear();
	}
	
	@Override
	public void stop()
	{
		avatar.stop();
		
		for (int i = 0; i < allFood.size(); i++)
			allFood.get(i).stop();
	}
}
