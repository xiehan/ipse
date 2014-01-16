package ipse.screen;

import processing.core.PConstants;
import apwidgets.PButton;
import apwidgets.PWidget;
import madparker.gametools.screen.Screen;
import ipse.app.Ipse;


public class StartScreen extends Screen 
{
	private PButton loginButton;
	private PButton signupButton;
	
	
	public StartScreen(Ipse parent)
	{
		this.parent = parent;
		setup();
	}
	
	@Override
	public void setup()
	{
		super.setup();
		
		loginButton = new PButton(80, pApplet.height-120, "LOG IN");
		signupButton = new PButton(pApplet.width-190, pApplet.height-120, "SIGN UP");
		parent.widgetContainer.addWidget(loginButton);
		parent.widgetContainer.addWidget(signupButton);
	}

	@Override
	public void draw()
	{
		super.draw();
		
		pApplet.pushStyle();
		pApplet.fill(200);
		pApplet.textAlign(PConstants.RIGHT, PConstants.TOP);
		pApplet.textFont(hugeFont);
		pApplet.text("(ipse)", pApplet.width-80, pApplet.height/2);
		pApplet.textFont(medFontIta);
		pApplet.text("explore the self through sound", 
				pApplet.width-80, pApplet.height/2+100);
		
		pApplet.popStyle();		
	}

	@Override
	public void onClickWidget(PWidget widget)
	{
		if(widget == loginButton && !parent.amazonOffline)
		{
			parent.widgetContainer.removeWidget(loginButton);
			parent.widgetContainer.removeWidget(signupButton);
			parent.currentScreen = new LogInScreen(parent);
		}
		else if(widget == signupButton)
		{
			parent.widgetContainer.removeWidget(loginButton);
			parent.widgetContainer.removeWidget(signupButton);
			parent.currentScreen = new SignUpScreen(parent);
		}
	}
}
