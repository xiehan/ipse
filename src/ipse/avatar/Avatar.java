package ipse.avatar;

import java.util.ArrayList;
import java.util.List;

import apwidgets.PButton;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import madparker.gametools.screen.Screen;

import ipse.screen.AvatarScreen;
import ipse.screen.PlayScreen;
import ipse.timer.HurtTimer;
import ipse.util.Color;


public class Avatar extends MultiplayerAvatar 
{
	private static PVector avScreenPosition = 
		new PVector(pApplet.width/2, pApplet.height/3 + 50);
	
	public String name = "";
	
	public int maxParticles = 5;
	
	private boolean selected = false;	
	private boolean outerSelected = false;
	private PVector startRotationSelect;
	
	public boolean playMode = false;
	
	public float energyLevel = 0;
	
	public boolean immune = false;
	public boolean blinked = false;

	
	public Avatar(Screen parent, String username, String name)
	{
		this.parent = parent;
		this.username = username;
		this.name = name;
		setup();
	}
	
	@Override
	public void setup()
	{		
		super.setup();

		position = avScreenPosition;
		maxParticles = 5 + (level-1);
		if (particles.size() > maxParticles)
			maxParticles = particles.size();
	}
	
	@Override
	public void parseDB(Attribute a)
	{
		if (a.getName().equals("Level"))
		{
			level = Integer.parseInt(a.getValue());
			maxParticles = 5 + (level-1);
		}
		else if (a.getName().equals("EnergyLevel"))
			energyLevel = Float.parseFloat(a.getValue());
		else if (a.getName().equals("WorldDestination"))
			return;
		else
			super.parseDB(a);

		if (particles.size() > maxParticles)
			maxParticles = particles.size();
	}

	public boolean addStationaryParticle(float x, float y, boolean updateDB)
	{
		if (particles.size() >= maxParticles)
			return false;
		
		Particle p = new StationaryParticle(this, x, y);
		particles.add(p);
		int i = particles.size();
		if (updateDB)
			updateDB(p, i);		
		
		return true;
	}
	
	public boolean addMovingParticle(float x, float y, boolean updateDB)
	{
		if (particles.size() >= maxParticles)
			return false;

		float v1 = pApplet.random(-1.0f, 1.0f);
		float v2 = pApplet.random(-1.0f, 1.0f);
		Particle p = new MovingParticle(this, x, y, v1, v2);
		particles.add(p);
		int i = particles.size();
		if (updateDB)
			updateDB(p, i);
		
		return true;
	}
	
	public boolean addMovingParticle(float x, float y, float v1, float v2, boolean updateDB)
	{
		if (particles.size() >= maxParticles)
			return false;
		
		Particle p = new MovingParticle(this, x, y, v1, v2);
		particles.add(p);
		int i = particles.size();
		if (updateDB)
			updateDB(p, i);
		
		return true;
	}
	
	public void removeParticle(int i)
	{
		int j = i + 1;
		for ( ; j <= particles.size(); j++)
			updateDB(particles.get(j-1), j-1);
		
		DeleteAttributesRequest dar = new DeleteAttributesRequest(
				parent.parent.domain, username);
		dar.withAttributes(new Attribute().withName("Particle"+j+"_Position"),
				new Attribute().withName("Particle"+j+"_Stationary"), 
				new Attribute().withName("Particle"+j+"_Velocity"));
		parent.parent.sdb.deleteAttributes(dar);
		
		particles.remove(i);
		updateDB("NumParticles", ""+particles.size());
	}
	
	public void saveParticles()
	{
		for (int i = 0; i < particles.size(); i++)
		{
			Particle p = particles.get(i);
			updateDB(p, i+1);
		}
	}
	
	public void updateDB(Particle p, int i)
	{
		if (parent.parent.amazonOffline)
			return;
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(parent.parent.username).withAttributes(
                new ReplaceableAttribute("Particle"+i+"_Position", 
                		p.position.x+","+p.position.y, true),
                new ReplaceableAttribute("Particle"+i+"_Stationary", 
                		Boolean.toString(p.stationary), true),
                new ReplaceableAttribute("Particle"+i+"_Velocity", 
                		p.velocity.x+","+p.velocity.y, true),
                new ReplaceableAttribute("NumParticles", 
                        ""+particles.size(), true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.domain, data));
	}
	
	public void updateDB(String property, String value)
	{
		if (parent.parent.amazonOffline)
			return;
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(parent.parent.username).withAttributes(
                new ReplaceableAttribute(property, value, true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.domain, data));
	}
	
	public void batchUpdateDB()
	{
		if (parent.parent.amazonOffline)
			return;
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(parent.parent.username).withAttributes(
                new ReplaceableAttribute("WorldPosition", 
    					position.x+","+position.y, true),
    	        new ReplaceableAttribute("WorldDestination", 
    					destination.x+","+destination.y, true),
                new ReplaceableAttribute("LastUpdated", ""+currentTime, true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.domain, data));
	}
	
	public synchronized void activatePlayMode()
	{
		playMode = true;
		position = worldPosition;
	}
	
	public synchronized void switchToScreenMode()
	{
		playMode = false;
		position = avScreenPosition;
		fusing = false;
		fusingWith = "";
		updateDB("Online", Boolean.toString(false));
	}
	
	@Override
	public void draw()
	{
		if (blinked)
			return;
		
		pApplet.pushMatrix();
		pApplet.translate(position.x, position.y);
		pApplet.rotate(PApplet.radians(rotation));
		
		if (playMode)
			pApplet.scale(playModeScale);
		
		pApplet.pushStyle();
		pApplet.ellipseMode(PConstants.CENTER);
		pApplet.shapeMode(PConstants.CENTER);
		pApplet.strokeJoin(PConstants.ROUND);
		drawShape(20.0f, 5);
		drawShape(16.0f, 5);
		drawShape(12.0f, 5);
		drawShape(10.0f, 10);
		drawShape(7.0f, 10);
		drawShape(5.0f, 10);
		drawShape(3.0f, 20);
		drawShape(1.0f, 50);
		pApplet.popStyle();
		
		if (playingSounds)
			soundWave.draw();

		pApplet.rotate(-PApplet.radians(rotation));
		
		for (Particle p : particles)
			p.draw();
		
		pApplet.popMatrix();
	}
	
	public void blink()
	{
		if (!blinked)
			blinked = true;
		else
			blinked = false;
	}
	
	@Override
	public void update()
	{
		if (!playMode)
		{
			particleMoved = false;
			
			if (((AvatarScreen) parent).selectedTabIndex == 0 && selected)
			{
				w = PApplet.constrain(Math.abs(pApplet.mouseX-position.x)*2, 120, 300);
				h = PApplet.constrain(Math.abs(pApplet.mouseY-position.y)*2, 120, 300);
			}
			else if (((AvatarScreen) parent).selectedTabIndex == 3 && selected)
				changeColor();
			
			if (outerSelected)
			{
				PVector endRotationSelect = new PVector(pApplet.mouseX-position.x, 
						pApplet.mouseY-position.y);
				float tempRotation = PApplet.degrees(
						PVector.angleBetween(startRotationSelect, endRotationSelect))/30;
				rotation = (rotation+tempRotation) % 360;
				for (Particle p : particles)
				{
					PVector temp = new PVector(0, 0);
					float angle = PApplet.radians(tempRotation);
					temp.x = p.position.x*PApplet.cos(angle) - 
							 p.position.y*PApplet.sin(angle);
					temp.y = p.position.x*PApplet.sin(angle) + 
							 p.position.y*PApplet.cos(angle);
					p.position = temp;
				}
			}
		}
		else
		{
			if (!fusing)
				((PlayScreen) parent).buttons.clear();
			
			if (position.x != destination.x && position.y != destination.y)
			{
				PVector toBeTravelled = PVector.sub(destination, position);

				if (toBeTravelled.x < 0)
					position.x = PApplet.max(position.x + velocity.x, destination.x);
				else
					position.x = PApplet.min(position.x + velocity.x, destination.x);

				if (toBeTravelled.y < 0)
					position.y = PApplet.max(position.y + velocity.y, destination.y);
				else
					position.y = PApplet.min(position.y + velocity.y, destination.y);
				
				worldPosition.x = position.x;
				worldPosition.y = position.y;
				
				if (position.x == destination.x && position.y == destination.y)
				{
					velocity = new PVector(0, 0);
					updateDB("WorldPosition", worldPosition.x+","+worldPosition.y);
				}
			}
		}
		
		if (!outerSelected && rotationPerSec != 0)
			rotation = (rotation+((rotationPerSec/1000)*30)) % 360;
		
		soundWave.update();
		
		for (Particle p : particles)
			p.update();
	}
	
	public void mousePressed()
	{
		PVector mouse = calculateRelativeMousePos();
		float ans = (PApplet.sq(mouse.x)/PApplet.sq(w/2)) + 
					(PApplet.sq(mouse.y)/PApplet.sq(h/2));
		
		if (!playMode)
		{
			int stIndex = ((AvatarScreen)parent).selectedTabIndex;
			
			if (stIndex >= 0 && ans >= 0.75f && ans <= 1.25f)
				selected = true;
			else if (stIndex == 0 && ans >= 1.5f)
			{
				outerSelected = true;
				startRotationSelect = new PVector(pApplet.mouseX-position.x, 
						pApplet.mouseY-position.y);
			}
			else if (stIndex != 1 && stIndex != 2)
				for (Particle p : particles)
					p.mousePressed();
		}
	}
	
	public void mouseReleased()
	{
		if (!playMode)
		{
			selected = false;
	
			if (((AvatarScreen) parent).selectedTabIndex == 0 && !outerSelected)
			{
				updateDB("MembraneWidth", ""+w);
				updateDB("MembraneHeight", ""+h);
			}
			else if (((AvatarScreen) parent).selectedTabIndex == 3)
			{
				updateDB("MembraneColor", ""+color);
			}
			
			if (((AvatarScreen) parent).selectedTabIndex == 0 && !outerSelected && 
				rotationPerSec != 0)
			{
				rotationPerSec = 0;
				updateDB("Rotation", ""+rotation);
				updateDB("RotationPerSec", ""+rotationPerSec);
			}
			else if (((AvatarScreen) parent).selectedTabIndex == 0 && !outerSelected && 
				rotationPerSec == 0 && rotation != 0)
			{
				rotation = 0;
				updateDB("Rotation", ""+rotation);
			}
			else if (outerSelected)
			{
				outerSelected = false;
				PVector endRotationSelect = new PVector(pApplet.mouseX-position.x, 
						pApplet.mouseY-position.y);
				rotationPerSec = PApplet.degrees(
						PVector.angleBetween(startRotationSelect, endRotationSelect));
				updateDB("RotationPerSec", ""+rotationPerSec);
			}
			
			if (!particleMoved)
			{
				PVector mouse = calculateRelativeMousePos();
				float ans = (PApplet.sq(mouse.x)/PApplet.sq(w/2)) + 
							(PApplet.sq(mouse.y)/PApplet.sq(h/2));
				if (((AvatarScreen) parent).selectedTabIndex > 0 && ans < 1.0f)
				{
					if (((AvatarScreen) parent).selectedTabIndex == 1)
						addStationaryParticle(pApplet.mouseX-position.x, 
								pApplet.mouseY-position.y, true);
					if (((AvatarScreen) parent).selectedTabIndex == 2)
						addMovingParticle(pApplet.mouseX-position.x, 
								pApplet.mouseY-position.y, true);
				}
			}
	
			for (Particle p : particles)
				p.mouseReleased();
		}
	}
	
	private PVector calculateRelativeMousePos()
	{
		PVector mouse = new PVector(pApplet.mouseX-position.x, pApplet.mouseY-position.y);
		return calculateRelativePos(mouse);
	}
	
	public void setDestination(PVector destination, boolean onlyIfNotFusing)
	{
		if (onlyIfNotFusing && fusing)
			return;
		setDestination(destination);
	}
	
	public void changeColor()
	{
		PVector startColorSelect = new PVector(0, -h);
		if (rotation != 0)
		{
			PVector temp = new PVector(0, 0);
			float angle = -PApplet.radians(rotation);
			temp.x = startColorSelect.x*PApplet.cos(angle) - 
						startColorSelect.y*PApplet.sin(angle);
			temp.y = startColorSelect.x*PApplet.sin(angle) + 
						startColorSelect.y*PApplet.cos(angle);
			startColorSelect = temp;
		}
		PVector endColorSelect = new PVector(pApplet.mouseX-position.x, 
				pApplet.mouseY-position.y);
		float angle = PApplet.degrees(
				PVector.angleBetween(startColorSelect, endColorSelect)) % 360;
		if (pApplet.mouseX-position.x < 0)
			angle *= -1;
		//PApplet.println(angle);
		float hue = PApplet.map(angle, -180, 180, 0, 1);
		color = Color.HSBtoRGB(hue, 0.8f, 0.9f);
	}
	
	public boolean checkIfEaten(PVector foodPosition)
	{
		float minX = position.x - ((w/2)*playModeScale);
		float maxX = position.x + ((w/2)*playModeScale);
		float minY = position.y - ((h/2)*playModeScale);
		float maxY = position.y + ((h/2)*playModeScale);
		
		if (foodPosition.x > minX && foodPosition.x < maxX &&
			foodPosition.y > minY && foodPosition.y < maxY)
		{
			float energyLevelInterval = 1.0f / (float)(level*5);
			energyLevel += energyLevelInterval;
			if (energyLevel >= 1.0f)
				levelUp();
			else
				updateDB("EnergyLevel", ""+energyLevel);
			return true;
		}
		
		return false;
	}
	
	/*@Override
	public boolean checkBarrierCollision(PVector barrierPosition, int barrierLevel)
	{
		boolean result = super.checkBarrierCollision(barrierPosition, barrierLevel);
		
		if (result)
			updateDB("WorldPosition", worldPosition.x+","+worldPosition.y);
		
		return result;
	}*/
	
	public boolean checkIfHurt(PVector obstaclePosition)
	{
		if (immune)
			return false;
		
		float minX = position.x - ((w/2)*playModeScale);
		float maxX = position.x + ((w/2)*playModeScale);
		float minY = position.y - ((h/2)*playModeScale);
		float maxY = position.y + ((h/2)*playModeScale);
		
		if (obstaclePosition.x > minX && obstaclePosition.x < maxX &&
			obstaclePosition.y > minY && obstaclePosition.y < maxY)
		{
			if (energyLevel > 0)
			{
				float energyLevelInterval = 1.0f / (float)(level*5*2);
				energyLevel = PApplet.max(energyLevel-energyLevelInterval, 0);
				updateDB("EnergyLevel", ""+energyLevel);
			}
			immune = true;
			parent.parent.gNotificationManager.notify(1, parent.parent.gNotification);
			HurtTimer ht = new HurtTimer(this, 150, 8000);
			parent.parent.timersToBeAdded.add(ht);
			ht.trigger();
			return true;
		}
		return false;
	}
	
	public void levelUp()
	{
		level++;
		maxParticles++;
		energyLevel = 0;
		for (PButton button : ((PlayScreen) parent).buttons)
			parent.parent.widgetContainer.removeWidget(button);
		((PlayScreen) parent).buttons.clear();
		parent.parent.currentScreen = new AvatarScreen(parent.parent, this);
		updateDB("Level", ""+level);
		updateDB("EnergyLevel", ""+energyLevel);
	}
	
	@Override
	public boolean fuse(MultiplayerAvatar otherPlayer)
	{
		if (!playMode) return false;
		if (fusing)
			parent.parent.widgetContainer.show();
		
		boolean result = super.fuse(otherPlayer);
		
		if (result)
		{
			fusing = true;
			parent.resetWidgets();
			parent.parent.widgetContainer.show();
		}
		
		return result;
	}
	
	public boolean giveParticle(String recipient, boolean notRequested)
	{
		if (!playMode || parent.parent.amazonOffline || 
			!((PlayScreen) parent).otherPlayers.containsKey(recipient))
			return false;
		
		int numParticlesBefore = particles.size();
		boolean result = false;
		
		int particleToRemove = PApplet.floor(pApplet.random(particles.size()));
		Particle p = particles.get(particleToRemove);
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(username).withAttributes(
                new ReplaceableAttribute("Username", username, true),
                new ReplaceableAttribute("Giver", username, true),
                new ReplaceableAttribute("Receiver", recipient, true),
                new ReplaceableAttribute("Particle_Position", 
                		p.position.x+","+p.position.y, true),
                new ReplaceableAttribute("Particle_Stationary", 
                		Boolean.toString(p.stationary), true),
                new ReplaceableAttribute("Particle_Velocity",
                		p.velocity.x+","+p.velocity.y, true),
                new ReplaceableAttribute("Particle_Index", ""+(particleToRemove+1), true),
                new ReplaceableAttribute("NotRequested", 
                		Boolean.toString(notRequested), true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.multiplayerDomain, data));
		
		removeParticle(particleToRemove);

		if (particles.size() < numParticlesBefore)
			result = true;
		
		if (result)
		{
			MultiplayerAvatar theRecipient = ((PlayScreen) parent).otherPlayers.get(recipient);
			if (p.stationary)
				theRecipient.addStationaryParticle(p.position.x, p.position.y);
			else
				theRecipient.addMovingParticle(p.position.x, p.position.y, 
						p.velocity.x, p.velocity.y);
			
			if (!notRequested)
			{
				parent.parent.sdb.deleteAttributes(
					new DeleteAttributesRequest(parent.parent.multiplayerDomain, recipient));
				if (pApplet.random(0, 1.0f) <= 0.75f)
				{
					float energyLevelInterval = 1.0f / (float)(level*5);
					energyLevel += pApplet.random(
							0.5f*energyLevelInterval, 2*energyLevelInterval);
					updateDB("EnergyLevel", ""+energyLevel);
				}
			}
		}
		
		fusing = false;
		fusingWith = "";
		
		return result;
	}
	
	public void takeParticle()
	{
		if (!fusing || !playMode || parent.parent.amazonOffline || 
			!((PlayScreen) parent).otherPlayers.containsKey(fusingWith))
			return;
		
		List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
        data.add(new ReplaceableItem(username).withAttributes(
                new ReplaceableAttribute("Username", username, true),
                new ReplaceableAttribute("Giver", fusingWith, true),
                new ReplaceableAttribute("Receiver", username, true)));
		parent.parent.sdb.batchPutAttributes(
			new BatchPutAttributesRequest(parent.parent.multiplayerDomain, data));

		fusing = false;
		fusingWith = "";
	}
	
	public void doNothing()
	{
		fusing = false;
		fusingWith = "";
	}
	
	public void stop()
	{
		updateDB("Online", Boolean.toString(false));
		updateDB("LastUpdated", ""+currentTime);
		updateDB("WorldPosition", worldPosition.x+","+worldPosition.y);
		updateDB("EnergyLevel", ""+energyLevel);
		updateDB("Level", ""+level);
	}
}
