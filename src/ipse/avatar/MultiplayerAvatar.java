package ipse.avatar;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import madparker.gametools.screen.Screen;
import madparker.gametools.util.GameElement;
import madparker.gametools.util.Util;

import ipse.app.Ipse;
import ipse.screen.PlayScreen;
import ipse.timer.ToneTimer;
import ipse.util.Color;


public class MultiplayerAvatar extends GameElement 
{
	public Screen parent;
	
	public String username;
	
	public float w = 240;
	public float h = 180;
	
	public PVector position;
	public PVector velocity;
	public PVector acceleration;
	
	public PVector worldPosition;
	public PVector destination;
	public long lastUpdated;
	
	public int color;
	
	public ArrayList<Particle> particles = new ArrayList<Particle>();
	public int level = 1;
	
	public ToneTimer toneTimer;
	public SoundWave soundWave;
	public boolean playingSounds = false;
	
	public float rotation = 0.0f;
	public float rotationPerSec = 0.0f;

	public boolean particleMoved = false;
	
	public static float playModeScale = 0.667f;
	
	public boolean fusing = false;
	public String fusingWith = "";

	
	public MultiplayerAvatar() { }
	
	public MultiplayerAvatar(Screen parent, String username)
	{
		this.parent = parent;
		this.username = username;
		setup();
	}
	
	public void setup()
	{		
		worldPosition = new PVector(
				pApplet.random(4*Ipse.BLOCK_SIZE, 18*Ipse.BLOCK_SIZE), 
				pApplet.random(4*Ipse.BLOCK_SIZE, 18*Ipse.BLOCK_SIZE));
		position = Util.cloneVector(worldPosition);
		destination = Util.cloneVector(worldPosition);
		velocity = new PVector(0, 0);

		color = Color.HSBtoRGB(0.5f, 0.8f, 0.9f);
		
		toneTimer = new ToneTimer(this, 5000);
		parent.parent.timersToBeAdded.add(toneTimer);
		toneTimer.trigger();
	}
	
	public void parseDB(Attribute a)
	{
		if (a.getName().equals("WorldPosition"))
		{
			String[] posStr = a.getValue().split(",");
			if (posStr.length >= 2)
				worldPosition = new PVector(Float.parseFloat(posStr[0]),
					Float.parseFloat(posStr[1]));
		}
		else if (a.getName().equals("WorldDestination"))
		{
			String[] desStr = a.getValue().split(",");
			if (desStr.length >= 2)
				destination = new PVector(Float.parseFloat(desStr[0]),
					Float.parseFloat(desStr[1]));
		}
		else if (a.getName().equals("Level"))
			level = Integer.parseInt(a.getValue());
		else if (a.getName().equals("NumParticles"))
		{
			int numParticles = Integer.parseInt(a.getValue());
			if (numParticles > 0)
				for (int i = 1; i <= numParticles; i++)
					parseDBParticle(""+i);
		}
		else if (a.getName().equals("MembraneColor"))
			color = Integer.parseInt(a.getValue());
		else if (a.getName().equals("MembraneWidth"))
			w = Float.parseFloat(a.getValue());
		else if (a.getName().equals("MembraneHeight"))
			h = Float.parseFloat(a.getValue());
		else if (a.getName().equals("Rotation"))
			rotation = Float.parseFloat(a.getValue());
		else if (a.getName().equals("RotationPerSec"))
			rotationPerSec = Float.parseFloat(a.getValue());
		else if (a.getName().equals("LastUpdated"))
			lastUpdated = Long.parseLong(a.getValue());
	}
	
	public void parseDBParticle(String i)
	{
		parseDBParticle(i, username);
	}
	
	public boolean parseDBParticle(String i, String item)
	{
		int numParticlesBefore = particles.size();
		
		String domain = parent.parent.domain;
		if (!item.equals(username))
			domain = parent.parent.multiplayerDomain;
		
		GetAttributesRequest gar = 
			new GetAttributesRequest(domain, item);
		gar = gar.withAttributeNames("Particle"+i+"_Position", 
				"Particle"+i+"_Stationary", "Particle"+i+"_Velocity", 
				"Particle_Index");
		GetAttributesResult result = parent.parent.sdb.getAttributes(gar);
		@SuppressWarnings("unchecked")
		List<Attribute> particleData = result.getAttributes();
		
		PVector pPos = null, pVel = null;
		boolean stationary = true;
		for (Attribute d : particleData)
		{
			if (d.getName().equals("Particle"+i+"_Position"))
			{
				String[] pPosStr = d.getValue().split(",");
				if (pPosStr.length < 2)
					continue;
				pPos = new PVector(Float.parseFloat(pPosStr[0]),
						Float.parseFloat(pPosStr[1]));

				float ans = (PApplet.sq(pPos.x)/PApplet.sq(w/2)) + 
							(PApplet.sq(pPos.y)/PApplet.sq(h/2));
				if (ans >= 1.0f)
					pPos = PVector.mult(pPos, 1/ans);
			}
			else if (d.getName().equals("Particle"+i+"_Stationary"))
				stationary = Boolean.parseBoolean(d.getValue());
			else if (d.getName().equals("Particle"+i+"_Velocity"))
			{
				String[] pVelStr = d.getValue().split(",");
				if (pVelStr.length < 2)
					continue;
				pVel = new PVector(Float.parseFloat(pVelStr[0]),
						Float.parseFloat(pVelStr[1]));
			}
			else if (d.getName().equals("Particle_Index"))
			{
				if (((PlayScreen) parent).otherPlayers.containsKey(item))
				{
					MultiplayerAvatar otherPlayer = ((PlayScreen) parent).otherPlayers.get(item);
					otherPlayer.particles.remove(Integer.parseInt(d.getValue())-1);
				}
			}
		}
		if (pPos != null)
		{
			if (stationary)
				addStationaryParticle(pPos.x, pPos.y);
			else if (pVel != null)
				addMovingParticle(pPos.x, pPos.y, pVel.x, pVel.y);
			else
				addMovingParticle(pPos.x, pPos.y);
		}
		
		if (particles.size() > numParticlesBefore)
			return true;
		else
			return false;
	}
	
	public boolean addStationaryParticle(float x, float y)
	{
		Particle p = new StationaryParticle(this, x, y);
		particles.add(p);		
		return true;
	}
	
	public boolean addMovingParticle(float x, float y)
	{
		float v1 = pApplet.random(-1.0f, 1.0f);
		float v2 = pApplet.random(-1.0f, 1.0f);
		return addMovingParticle(x, y, v1, v2);
	}
	
	public boolean addMovingParticle(float x, float y, float v1, float v2)
	{
		Particle p = new MovingParticle(this, x, y, v1, v2);
		particles.add(p);		
		return true;
	}
	
	@Override
	public void draw()
	{
		pApplet.pushMatrix();
		pApplet.translate(position.x, position.y);
		pApplet.rotate(PApplet.radians(rotation));
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
	
	public void drawShape(float weight, int alpha)
	{
		pApplet.pushStyle();
		pApplet.strokeWeight(weight);
		pApplet.stroke(color, alpha);
		pApplet.noFill();
		pApplet.ellipse(0, 0, w, h);
		pApplet.popStyle();
	}
	
	@Override
	public void update()
	{
		/*if ((currentTime - lastUpdated) > 2 * 60 * 1000)
		{
			PlayScreen p = ((PlayScreen) parent);
			p.otherPlayers.remove(username);
			if (p.avatar.fusingWith.equals(username))
			{
				p.avatar.fusing = false;
				p.avatar.fusingWith = "";
			}
			return;
		}*/
		
		if (!fusing)
		{
			Avatar a = ((PlayScreen) parent).avatar;
			if (PVector.dist(position, a.position) < Ipse.BLOCK_SIZE * 2)
			{
				fuse(a);
				a.fuse(this);
			}
		}
		
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
				velocity = new PVector(0, 0);
		}

		if (rotationPerSec != 0)
			rotation = (rotation+((rotationPerSec/1000)*30)) % 360;

		soundWave.update();

		for (Particle p : particles)
			p.update();
	}
	
	public PVector calculateRelativePos(PVector pos)
	{
		PVector relativePos = new PVector(pos.x, pos.y);
		if (rotation != 0)
		{
			PVector temp = new PVector(0, 0);
			float angle = -PApplet.radians(rotation);
			temp.x = relativePos.x*PApplet.cos(angle) - 
					 relativePos.y*PApplet.sin(angle);
			temp.y = relativePos.x*PApplet.sin(angle) + 
					 relativePos.y*PApplet.cos(angle);
			relativePos = temp;
		}
		return relativePos;
	}
	
	public void playSounds()
	{
		if (playingSounds)
			return;
		for (Particle p : particles)
			p.reset();
		playingSounds = true;
		soundWave.start(1.0f / (float)((toneTimer.getInterval()-3000)/30));
	}
	
	public void setDestination(PVector destination)
	{
		if (fusing)
			return;
		
		this.destination = destination;
		PVector toBeTravelled = PVector.sub(destination, position);
		toBeTravelled.limit(2.25f);
		velocity = toBeTravelled;
	}
	
	public boolean checkBarrierCollision(PVector barrierPosition, int barrierLevel)
	{
		if (position.x == destination.x && position.y == destination.y)
			return false;
			
		float maxW = ((w/2)*playModeScale);
		float maxH = ((h/2)*playModeScale);
		/*if (rotation != 0 || rotationPerSec != 0)
		{
			if (maxW > maxH)
				maxH = maxW;
			else if (maxH > maxW)
				maxW = maxH;
		}*/
		
		float minX = position.x + velocity.x - maxW;
		float maxX = position.x + velocity.x + maxW;
		float minY = position.y + velocity.y - maxH;
		float maxY = position.y + velocity.y + maxH;
		
		float bMinX = barrierPosition.x;
		float bMaxX = barrierPosition.x + Ipse.BLOCK_SIZE;
		float bMinY = barrierPosition.y;
		float bMaxY = barrierPosition.y + Ipse.BLOCK_SIZE;
		
		boolean result = false;
		if (level >= barrierLevel)
			return result;
		
		if (minX >= bMinX && minX <= bMaxX && 
		   ((minY >= bMinY && minY <= bMaxY) ||
			(maxY >= bMinY && maxY <= bMaxY) || 
			(minY <= bMinY && maxY >= bMaxY)))
		{
			velocity.x = 0;
			destination.x = position.x;
			result = true;
		}
		if (maxX >= bMinX && maxX <= bMaxX && 
		   ((minY >= bMinY && minY <= bMaxY) ||
			(maxY >= bMinY && maxY <= bMaxY) || 
			(minY <= bMinY && maxY >= bMaxY)))
		{
			velocity.x = 0;
			destination.x = position.x;
			result = true;
		}
		if (minY >= bMinY && minY <= bMaxY && 
		   ((minX >= bMinX && minX <= bMaxX) || 
			(maxX >= bMinX && maxX <= bMaxX) || 
			(minX <= bMinX && maxX >= bMaxX)))
		{
			velocity.y = 0;
			destination.y = position.y;
			result = true;
		}
		if (maxY >= bMinY && maxY <= bMaxY && 
		   ((minX >= bMinX && minX <= bMaxX) || 
			(maxX >= bMinX && maxX <= bMaxX) || 
			(minX <= bMinX && maxX >= bMaxX)))
		{
			velocity.y = 0;
			destination.y = position.y;
			result = true;
		}
		
		return result;
	}
	
	public boolean fuse(MultiplayerAvatar otherPlayer)
	{
		if (fusing)
			return false;
		
		fusing = true;
		fusingWith = otherPlayer.username;
		PVector avgPosition = new PVector((position.x+otherPlayer.position.x)/2,
				(position.y+otherPlayer.position.y)/2);
		setDestination(avgPosition);
		return true;
	}
}
