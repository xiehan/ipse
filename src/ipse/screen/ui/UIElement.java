package ipse.screen.ui;

import madparker.gametools.util.GameElement;

public abstract class UIElement extends GameElement 
{
	abstract public void draw();
	
	abstract public void update();
	
	abstract public void mousePressed();
	
	abstract public void mouseReleased();
	
	public void keyPressed() { }
}
