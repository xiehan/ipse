package ipse.screen;

import java.util.ArrayList;
import java.util.List;

import processing.core.PConstants;

import android.view.inputmethod.InputMethodManager;
import apwidgets.PButton;
import apwidgets.PWidget;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

import madparker.gametools.screen.Screen;
import ipse.app.Ipse;
import ipse.screen.ui.InputField;
import ipse.util.PasswordService;


public class SignUpScreen extends Screen 
{	
	private InputField nameField;
	private InputField usernameField;
	private InputField passwordField;
	private PButton submitButton;
	
	private String errorText = "";
	
	
	public SignUpScreen(Ipse parent)
	{
		this.parent = parent;
		setup();
	}
	
	public void setup()
	{
		super.setup();
		
		nameField = new InputField(this, "First Name", 
				pApplet.width/4, pApplet.height/4-80);
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
		
		nameField.draw();
		usernameField.draw();
		passwordField.draw();
		
		pApplet.pushStyle();
		pApplet.fill(200, 0, 0);
		pApplet.textAlign(PConstants.LEFT, PConstants.TOP);
		pApplet.text(errorText, pApplet.width/4, 40, pApplet.width/2, 60);
		pApplet.popStyle();
	}

	@Override
	public void update() 
	{
		super.update();
		
		nameField.update();
		usernameField.update();
		passwordField.update();
	}

	@Override
	public void mousePressed()
	{
		nameField.mousePressed();
		usernameField.mousePressed();
		passwordField.mousePressed();
	}

	@Override
	public void mouseReleased()
	{
		nameField.mouseReleased();
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
			
			if (nameField.inputText.length() < 2)
			{
				errorText = "Please enter your first name";
				return;
			}
			String name = parent.playerName = nameField.inputText;

			if (usernameField.inputText.length() < 6)
			{
				errorText = "Please enter your e-mail address";
				return;
			}
			String username = parent.username = usernameField.inputText;

			if (passwordField.inputText.length() < 8)
			{
				errorText = "Password must be at least 8 characters";
				return;
			}
			String password = passwordField.inputText;
			String encryptedPassword = PasswordService.encrypt(password);
			
			if (parent.amazonOffline)
				parent.encryptedPass = encryptedPassword;
			else
			{
				GetAttributesRequest gar = new 
					GetAttributesRequest(parent.domain, username);
				GetAttributesResult result = parent.sdb.getAttributes(gar);
				@SuppressWarnings("unchecked")
				List<Attribute> attributes = result.getAttributes();
				if (attributes.size() > 0)
				{
					usernameField.inputText = "";
					passwordField.inputText = "";
					errorText = "Account is already registered";
					return;
				}
	
				List<ReplaceableItem> data = new ArrayList<ReplaceableItem>();
		        data.add(new ReplaceableItem(username).withAttributes(
		                new ReplaceableAttribute("Username", username, false),
		                new ReplaceableAttribute("Password", encryptedPassword, false),
		                new ReplaceableAttribute("Name", name, true)));
				parent.sdb.batchPutAttributes(
					new BatchPutAttributesRequest(parent.domain, data));
			}

			parent.widgetContainer.removeWidget(submitButton);
			parent.currentScreen = new AvatarScreen(parent);
		}
	}
	
	@Override
	public void keyPressed()
	{
		nameField.keyPressed();
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
			nameField.onKeyDown(keycode);
			usernameField.onKeyDown(keycode);
			passwordField.onKeyDown(keycode);
		}
	}
}
