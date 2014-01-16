package ipse.screen.ui;

import processing.core.PConstants;
import processing.core.PImage;
import ipse.app.Ipse;
import ipse.screen.AvatarScreen;

public class Tab extends UIElement
{
	private AvatarScreen parent;
	
	public static float size = 80;
	
	private int index;
	private PImage icon;
	private String instructions = "";
	
	private int color;
	private int normalColor;
	private int selectedColor;
	private int textColor;
	
	protected boolean selected = false;
	
	
	public Tab(AvatarScreen parent, int index, String iconFile)
	{
		this(parent, index, iconFile, "");
	}
	
	public Tab(AvatarScreen parent, int index, String iconFile, String instructions)
	{
		this.parent = parent;
		this.index = index;
		this.instructions = instructions.toLowerCase();
		this.icon = pApplet.loadImage(iconFile);
		
		color = normalColor = textColor = pApplet.color(200);
		selectedColor = pApplet.color(150);
	}

	@Override
	public void draw()
	{
		pApplet.pushStyle();
		pApplet.rectMode(PConstants.CORNER);
		pApplet.noStroke();
		pApplet.fill(color);
		
		float w = pApplet.width / parent.tabs.size();
		float h = pApplet.height / parent.tabs.size();
		if (parent.parent.orientation == Ipse.VERTICAL)
			pApplet.rect(w*index, pApplet.height-size, w, size);
		else
			pApplet.rect(pApplet.width-size, h*index, size, h);
		
		pApplet.imageMode(PConstants.CENTER);
		if (parent.parent.orientation == Ipse.VERTICAL)
			pApplet.image(icon, w*index + (w/2), pApplet.height-(size/2));
		else
			pApplet.image(icon, pApplet.width-(size/2), h*index + (h/2));
		
		if (selected)
		{
			pApplet.textFont(parent.smallFontIta);
			pApplet.fill(textColor);
			pApplet.textAlign(PConstants.LEFT, PConstants.BOTTOM);
			pApplet.text(instructions, 50, pApplet.height-size-50, 
					pApplet.width-100, (pApplet.height*(2/3)-(size+50)));
		}
		
		pApplet.popStyle();
	}

	@Override
	public void update()
	{
		if (parent.selectedTabIndex == index)
			selected = true;
		if (selected)
		{
			parent.selectedTabIndex = index;
			color = selectedColor;
		}
		else
			color = normalColor;
	}

	@Override
	public void mousePressed() 
	{
		float minX, maxX, minY, maxY;
		
		if (parent.parent.orientation == Ipse.VERTICAL)
		{
			float w = pApplet.width / parent.tabs.size();
			minX = w*index;
			maxX = minX + w;
			minY = pApplet.height-size;
			maxY = minY + size;
		}
		else
		{
			float h = pApplet.height / parent.tabs.size();
			minX = pApplet.width-size;
			maxX = minX + size;
			minY = h*index;
			maxY = minY + h;
		}
		
		if (pApplet.mouseX > minX && pApplet.mouseX < maxX && 
			pApplet.mouseY > minY && pApplet.mouseY < maxY)
		{
			if (selected)
			{
				selected = false;
				parent.selectedTabIndex = -1;
			}
			else
				selected = true;
		}
		else
			selected = false;
		
		if (selected)
			parent.selectedTabIndex = index;
	}

	@Override
	public void mouseReleased() { }
}
