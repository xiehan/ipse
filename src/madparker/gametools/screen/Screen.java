package madparker.gametools.screen;

import apwidgets.PWidget;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import ipse.app.Ipse;
import madparker.gametools.util.GameElement;

abstract public class Screen extends GameElement 
{
	public Ipse parent;
	
	public PImage defaultBG = pApplet.loadImage("bg-0.jpg");
	private PVector basicScreenVelocity = new PVector(0, 0.1f);
	
	public PFont hugeFont = pApplet.loadFont("Calibri-72.vlw");
	public PFont bigFont = pApplet.loadFont("Calibri-50.vlw");
	public PFont medFont = pApplet.loadFont("Calibri-24.vlw");
	public PFont medFontIta = pApplet.loadFont("Calibri-Italic-24.vlw");
	public PFont smallFont = pApplet.loadFont("Calibri-18.vlw");
	public PFont smallFontIta = pApplet.loadFont("Calibri-Italic-18.vlw");

	public boolean isInit = true;
	
	
	public void init()
	{
		isInit = false;
	}
	
	public void setup()
	{
		if(isInit)
			init();
	}
	
	public void draw()
	{
		pApplet.background(0);
		
		pApplet.pushMatrix();
		pApplet.translate(-parent.basicScreenPosition.x, -parent.basicScreenPosition.y);
		
		pApplet.image(defaultBG, 0, 0);
		
		pApplet.popMatrix();
	}
	
	public void update()
	{
		if (parent.basicScreenPosition.y + basicScreenVelocity.y >= 
			(defaultBG.height - pApplet.height))
			parent.basicScreenPosition.y *= -1;
		else if (parent.basicScreenPosition.y + basicScreenVelocity.y <= 0)
			parent.basicScreenPosition.y *= -1;
		
		parent.basicScreenPosition.x += basicScreenVelocity.x;
		parent.basicScreenPosition.y += basicScreenVelocity.y;
	}
	
	public void multiplayerUpdate() { }
	
	public void resetWidgets() { }
	
	public void mousePressed() { }
	
	public void mouseReleased() { }
	
	public void mouseDragged() { }
	
	public void keyPressed() { }
	
	public void keyReleased() { }
	
	public void onKeyDown(int keycode) { }
	
	public void onClickWidget(PWidget widget) { }
	
	public void stop() { }
}
