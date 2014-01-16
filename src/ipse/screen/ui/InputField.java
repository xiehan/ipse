package ipse.screen.ui;

import android.view.KeyEvent;
import processing.core.PConstants;
import processing.core.PVector;
import madparker.gametools.screen.Screen;


public class InputField extends UIElement 
{
	private Screen parent;
	
	String label = "";
	
	public String inputText = "";
	private boolean isPassword = false;
	private boolean isEmail = false;
	
	public PVector position;
	private float w = pApplet.width/2;
	private float h = 30;
	private float cornerRadius = 3;
	
	private int color;
	private int normalColor;
	private int selectedColor;
	private int selectedStrokeColor;
	
	private boolean selected = false;
	
	
	public InputField(Screen parent, String label, float x, float y)
	{
		this(parent, label, x, y, pApplet.width/2, 30);
	}
	
	public InputField(Screen parent, String label, float x, float y, float w, float h)
	{
		this.parent = parent;
		this.label = label;
		this.w = w;
		this.h = h;
		position = new PVector(x, y);
		
		color = normalColor = pApplet.color(0, 205);
		selectedColor = pApplet.color(50, 135);
		selectedStrokeColor = pApplet.color(180, 100, 0, 205);
	}
	
	public void setPassword()
	{
		isPassword = true;
	}
	
	public void setEmail()
	{
		isEmail = true;
	}
	
	public void draw()
	{
		pApplet.pushStyle();
		pApplet.rectMode(PConstants.CORNER);
		pApplet.fill(color);
		pApplet.noStroke();

		pApplet.rect(position.x, position.y+cornerRadius, w, h-(cornerRadius*2));
		pApplet.rect(position.x+cornerRadius, position.y, w-(cornerRadius*2), h);
		
		if (selected)
		{
			pApplet.stroke(selectedStrokeColor);
			pApplet.strokeWeight(2.0f);
		}
		
		pApplet.arc(position.x+cornerRadius, position.y+cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.PI, PConstants.TWO_PI-PConstants.PI/2);
		pApplet.arc(position.x+cornerRadius, position.y+h-cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.PI/2, PConstants.PI);
		pApplet.arc(position.x+w-cornerRadius, position.y+cornerRadius, cornerRadius*2, 
				cornerRadius*2, PConstants.TWO_PI-PConstants.PI/2, PConstants.TWO_PI);
		pApplet.arc(position.x+w-cornerRadius, position.y+h-cornerRadius, cornerRadius*2, 
				cornerRadius*2, 0, PConstants.PI/2);
		
		if (selected)
		{
			pApplet.line(position.x, position.y+cornerRadius, 
						 position.x, position.y+h-cornerRadius);
			pApplet.line(position.x+w, position.y+cornerRadius, 
						 position.x+w, position.y+h-cornerRadius);
			pApplet.line(position.x+cornerRadius, position.y, 
						 position.x+w-cornerRadius, position.y);
			pApplet.line(position.x+cornerRadius, position.y+h, 
					 	 position.x+w-cornerRadius, position.y+h);
		}
		
		pApplet.fill(200);
		pApplet.textFont(parent.medFont);
		String textToDisplay = inputText;
		if (isPassword && inputText.length() >= 1)
		{
			textToDisplay = "";
			for (int i = 0; i < inputText.length() - 1; i++)
				textToDisplay += '*';
			textToDisplay += inputText.charAt(inputText.length()-1);
		}
		if (pApplet.textWidth(textToDisplay) > w-16)
		{
			while (pApplet.textWidth(textToDisplay) > w-16)
				textToDisplay = textToDisplay.substring(1);
			pApplet.textAlign(PConstants.RIGHT, PConstants.CENTER);
			pApplet.text(textToDisplay, position.x+8, position.y, w-16, h);
		}
		else
		{
			pApplet.textAlign(PConstants.LEFT, PConstants.CENTER);
			pApplet.text(textToDisplay, position.x+8, position.y, w-16, h);
		}
		
		pApplet.textFont(parent.smallFont);
		pApplet.textAlign(PConstants.LEFT, PConstants.BOTTOM);
		pApplet.text(label.toLowerCase(), position.x+8, position.y-8);
		
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
		{
			selected = true;
			parent.parent.imm.showSoftInput(parent.parent.getCurrentFocus(), 0);
		}
		else
			selected = false;
	}

	@Override
	public void mouseReleased() { }
	
	public void keyPressed()
	{
		if (!selected)
			return;

		if (isEmail && inputText.length() >= 64)
			return;
		else if (!isEmail && inputText.length() >= 16)
			return;
		
		char key = pApplet.key;
		
		if (inputText.length() >= 1 && (key == 8 || key == PConstants.BACKSPACE || 
				key == PConstants.DELETE || key == 127))
			inputText = 
				String.copyValueOf(inputText.toCharArray(), 0, inputText.length()-1);
		
		if (isPassword)
		{
			if ((key != 39 && key >= 35 && key <= 59) || (key >= 63 && key <= 95) ||
				(key >= 97 && key <= 122))
				inputText += key;
		}
		else if (isEmail)
		{
			if (key == 46 || (key >= 48 && key <= 57) || (key >= 64 && key <= 90) ||
				key == 95 || (key >= 97 && key <= 122) || key == 43 || key == 45)
				inputText += ("" + key).toLowerCase();
		}
		else
		{
			if ((key >= 65 && key <= 90) || (key >= 97 && key <= 122) || 
				key == 45 || key == 39)
				inputText += key;
		}
	}
	
	public void onKeyDown(int keycode)
	{
		if (!selected)
			return;
		if (inputText.length() >= 1 && (keycode == KeyEvent.KEYCODE_DEL))
			inputText = 
				String.copyValueOf(inputText.toCharArray(), 0, inputText.length()-1);
	}
}
