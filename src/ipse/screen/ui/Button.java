package ipse.screen.ui;

import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.screen.Screen;


public class Button extends UIElement 
{
	private Screen parent;
	
	public String label = "";
	
	public PVector position;
	private float w = 80;
	private float h = 30;
	private float cornerRadius = 8;
	
	private int color;
	private int normalColor;
	private int selectedColor;
	
	private boolean selected = false;
	
	
	public Button(Screen parent, String label, float x, float y)
	{
		this(parent, label, x, y, 80, 30);
	}
	
	public Button(Screen parent, String label, float x, float y, float w, float h)
	{
		this.parent = parent;
		this.label = label;
		this.w = w;
		this.h = h;
		position = new PVector(x, y);
		
		color = normalColor = pApplet.color(200);
		selectedColor = pApplet.color(150);
	}
	
	public void draw()
	{
		pApplet.pushStyle();
		pApplet.rectMode(PConstants.CORNER);
		pApplet.fill(color);
		pApplet.noStroke();

		pApplet.rect(position.x, position.y+cornerRadius, w, h-(cornerRadius*2));
		pApplet.rect(position.x+cornerRadius, position.y, w-(cornerRadius*2), h);
		pApplet.arc(position.x+cornerRadius, position.y+cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.PI, PConstants.TWO_PI-PConstants.PI/2);
		pApplet.arc(position.x+cornerRadius, position.y+h-cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.PI/2, PConstants.PI);
		pApplet.arc(position.x+w-cornerRadius, position.y+cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.TWO_PI-PConstants.PI/2, PConstants.TWO_PI);
		pApplet.arc(position.x+w-cornerRadius, position.y+h-cornerRadius, cornerRadius*2, 
				cornerRadius*2, 0, PConstants.PI/2);
		
		pApplet.fill(50);
		pApplet.textFont(parent.medFont);
		pApplet.textAlign(PConstants.CENTER, PConstants.CENTER);
		pApplet.text(label.toUpperCase(), position.x, position.y, w, h);
		
		pApplet.popStyle();
	}

	@Override
	public void update() 
	{
		if (selected)
			color = selectedColor;
		else
			color = normalColor;
	}

	@Override
	public void mousePressed() 
	{
		float minX = position.x;
		float maxX = position.x+w;
		float minY = position.y;
		float maxY = position.y+h;
		
		if (pApplet.mouseX > minX && pApplet.mouseX < maxX && 
			pApplet.mouseY > minY && pApplet.mouseY < maxY)
			selected = true;
		else
			selected = false;
	}

	@Override
	public void mouseReleased() { }
	
	public boolean wasPushed()
	{
		if (selected)
			return true;
		else
			return false;
	}
}
