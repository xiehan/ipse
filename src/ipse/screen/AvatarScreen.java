package ipse.screen;

import java.util.ArrayList;
import java.util.List;

import apwidgets.PButton;
import apwidgets.PWidget;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.screen.Screen;
import ipse.app.Ipse;
import ipse.avatar.Avatar;
import ipse.screen.ui.Button;
import ipse.screen.ui.Tab;


@SuppressWarnings("unused")
public class AvatarScreen extends Screen 
{	
	private Avatar avatar;
	
	public ArrayList<Tab> tabs = new ArrayList<Tab>();
	private static int NUM_TABS = 4;
	public int selectedTabIndex = -1;
	
	private PButton saveButton;
	
	private boolean loadingPlayScreen = false;
	
	
	public AvatarScreen(Ipse parent)
	{
		this.parent = parent;
		avatarSetup();
		setup();
	}
	
	public AvatarScreen(Ipse parent, Avatar avatar)
	{
		this.parent = parent;
		this.avatar = avatar;
		avatar.switchToScreenMode();
		this.avatar.parent = this;
		setup();
	}
	
	public void avatarSetup()
	{
		avatar = new Avatar(this, parent.username, parent.playerName);
		
		if (parent.amazonOffline)
		{
			parent.currentScreen = this;
			return;
		}
		
		GetAttributesRequest gar = new GetAttributesRequest(parent.domain, parent.username);
		gar = gar.withAttributeNames("WorldPosition", "Level", "NumParticles", 
				"MembraneColor", "MembraneWidth", "MembraneHeight", 
				"Rotation", "RotationPerSec", "EnergyLevel");
		GetAttributesResult result = parent.sdb.getAttributes(gar);
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = result.getAttributes();
		for (Attribute a : attributes)
			avatar.parseDB(a);
		
		if (avatar.particles.size() >= avatar.maxParticles)
		{
			parent.currentScreen = new PlayScreen(parent, avatar);
			loadingPlayScreen = true;
			return;
		}
		else
			parent.currentScreen = this;
	}
	
	@Override
	public void setup()
	{
		super.setup();
		
		tabs.add(new Tab(this, 0, "resize.png", 
				"Tap and drag your finger across the membrane to change the " +
				"shape of your avatar\n\n" +
				"Tap and drag outside the membrane to rotate your avatar; " +
				"Tap again to stop the rotation"));
		tabs.add(new Tab(this, 1, "stationaryparticle.png", 
				"Tap anywhere inside the membrane to place a stationary sound particle\n\n" +
				"A stationary sound particle produces sound when the sound wave passes " +
				"through it; its tone depends on its position within the membrane"));
		tabs.add(new Tab(this, 2, "movingparticle.png", 
				"Tap anywhere inside the membrane to place a moving sound particle\n\n" +
				"A moving sound particle produces sound when it interacts with the membrane"));
		tabs.add(new Tab(this, 3, "colorwheel.png", 
				"Drag your finger around the outline of the ellipse to change your hue"));
		
		saveButton = new PButton(pApplet.width-100, 10, "SAVE");
		parent.widgetContainer.addWidget(saveButton);
		parent.widgetContainer.hide();
	}

	@Override
	public void draw()
	{
		pApplet.background(0);
		
		avatar.draw();
		
		if (loadingPlayScreen)
			return;
		
		pApplet.pushStyle();
		pApplet.stroke(80);
		pApplet.strokeWeight(3);
		float w = pApplet.width / tabs.size();
		float h = pApplet.height / tabs.size();
		for (int i = 0; i < tabs.size(); i++)
		{
			if (parent.orientation == Ipse.VERTICAL)
				pApplet.line(w*i, pApplet.height-Tab.size+1, w*i, pApplet.height);
			else
				pApplet.line(pApplet.width-Tab.size+1, h*i, pApplet.width, h*i);
			
			tabs.get(i).draw();
		}

		pApplet.noStroke();
		pApplet.fill(200);
		
		if (selectedTabIndex < 0)
		{
			pApplet.textFont(smallFontIta);
			pApplet.textAlign(PConstants.LEFT, PConstants.BOTTOM);
			String instructions = "";
			if (avatar.level < 2)
				instructions += "this is you\n\n";
			if (avatar.particles.size() == 0)
				instructions += "tap one of the tabs below to begin customizing " +
					"your avatar and form a sonic representation of yourself";
			else if (avatar.particles.size() > 0 && avatar.particles.size() < avatar.maxParticles)
				instructions += "you must add " + 
					(avatar.maxParticles - avatar.particles.size()) + " particle(s) " +
					"to continue; you can also experiment with moving sound particles " +
					"around by dragging them to a new position";
			else
				instructions += "tap to select a sound particle and drag it to a new " +
					"position; you can experiment with how this changes your personal song";
			pApplet.text(instructions.toLowerCase(), 50, pApplet.height-80-50, 
					pApplet.width-100, (pApplet.height*(2/3)-(80+50)));
		}

		pApplet.textFont(bigFont);
		pApplet.textAlign(PConstants.LEFT, PConstants.BASELINE);
		pApplet.text(avatar.name.toLowerCase(), 50, 60);
		pApplet.fill(100);
		pApplet.textFont(medFont);
		pApplet.textAlign(PConstants.RIGHT, PConstants.BASELINE);
		if (avatar.particles.size() < avatar.maxParticles)
		{
			pApplet.text("Particles:   " + avatar.particles.size() + " / " 
					+ avatar.maxParticles, pApplet.width-50, 60);
		}
		else
			parent.widgetContainer.show();
		
		pApplet.popStyle();
	}

	@Override
	public void update()
	{
		avatar.update();
		
		if (loadingPlayScreen)
			return;
		
		for (Tab t : tabs)
			t.update();
	}

	@Override
	public void mousePressed()
	{
		if (loadingPlayScreen)
			return;
		
		avatar.mousePressed();
		
		for (Tab t : tabs)
			t.mousePressed();
	}
	
	@Override
	public void mouseReleased()
	{
		if (loadingPlayScreen)
			return;
		
		avatar.mouseReleased();
	}
	
	@Override
	public void onClickWidget(PWidget widget)
	{
		if (widget == saveButton)
		{
			avatar.saveParticles();
			parent.widgetContainer.hide();
			parent.widgetContainer.removeWidget(saveButton);
			parent.currentScreen = new PlayScreen(parent, avatar);
			return;
		}
	}
}
