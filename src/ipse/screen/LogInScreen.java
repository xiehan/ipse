package ipse.screen;

import java.util.List;

import processing.core.PConstants;

import android.view.inputmethod.InputMethodManager;
import apwidgets.PButton;
import apwidgets.PWidget;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

import madparker.gametools.screen.Screen;
import ipse.app.Ipse;
import ipse.screen.ui.InputField;
import ipse.util.PasswordService;


public class LogInScreen extends Screen 
{
	private InputField usernameField;
	private InputField passwordField;
	private PButton submitButton;
	
	private String errorText = "";
	
	
	public LogInScreen(Ipse parent)
	{
		this.parent = parent;
		setup();
	}
	
	@Override
	public void setup()
	{
		super.setup();
		
		usernameField = new InputField(this, "E-mail Address", 
				pApplet.width/4, pApplet.height/4);
		usernameField.setEmail();
		passwordField = new InputField(this, "Password",  
				pApplet.width/4, pApplet.height/4+80);
		passwordField.setPassword();
		
		submitButton = new PButton(pApplet.width/2-50, 
				pApplet.height/4+160, "SUBMIT");
		parent.widgetContainer.addWidget(submitButton);
	}

	@Override
	public void draw()
	{
		super.draw();
		
		usernameField.draw();
		passwordField.draw();
		
		pApplet.pushStyle();
		pApplet.fill(200, 0, 0);
		pApplet.textAlign(PConstants.LEFT, PConstants.TOP);
		pApplet.text(errorText, pApplet.width/4, 100, pApplet.width/2, 60);
		pApplet.popStyle();
	}

	@Override
	public void update() 
	{
		super.update();
		
		usernameField.update();
		passwordField.update();
	}

	@Override
	public void mousePressed()
	{
		usernameField.mousePressed();
		passwordField.mousePressed();
	}

	@Override
	public void mouseReleased()
	{
		usernameField.mouseReleased();
		passwordField.mouseReleased();
	}
	
	@Override
	public void onClickWidget(PWidget widget)
	{
		if (widget == submitButton)
		{
			parent.imm.hideSoftInputFromWindow(
					parent.getCurrentFocus().getWindowToken(), 
					InputMethodManager.HIDE_NOT_ALWAYS);
			
			errorText = "";
			
			String username = usernameField.inputText;
			if (username.length() < 1)
			{
				errorText = "You must enter a username";
				return;
			}
			
			String password = passwordField.inputText;
			if (password.length() < 1)
			{
				errorText = "You must enter a password";
				return;
			}			
			String encryptedPassword = PasswordService.encrypt(password);
	
			GetAttributesRequest gar = new GetAttributesRequest(parent.domain, username);
			gar = gar.withAttributeNames("Name", "Password");
			GetAttributesResult result = parent.sdb.getAttributes(gar);
			@SuppressWarnings("unchecked")
			List<Attribute> attributes = result.getAttributes();
			if (attributes.size() < 2)
			{
				usernameField.inputText = "";
				passwordField.inputText = "";
				errorText = "Username not found";
				return;
			}
			String passFromDB = attributes.get(1).getValue();
			if (!passFromDB.equals(encryptedPassword))
			{
				passwordField.inputText = "";
				errorText = "Password incorrect";
				return;
			}
			parent.widgetContainer.removeWidget(submitButton);
			parent.playerName = attributes.get(0).getValue();
			parent.username = username;
			@SuppressWarnings("unused")
			Screen s = new AvatarScreen(parent);
		}
	}
	
	@Override
	public void keyPressed()
	{
		usernameField.keyPressed();
		passwordField.keyPressed();
	}
	
	@Override
	public void onKeyDown(int keycode)
	{
		if (keycode == PConstants.BACK)
		{
			pApplet.keyCode = 0;  // don't quit by default
			parent.widgetContainer.removeWidget(submitButton);
			parent.currentScreen = new StartScreen(parent);
		}
		else
		{
			usernameField.onKeyDown(keycode);
			passwordField.onKeyDown(keycode);
		}
	}
}
