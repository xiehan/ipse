package ipse.app;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DomainMetadataRequest;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import apwidgets.PMediaPlayer;
import apwidgets.PWidget;
import apwidgets.PWidgetContainer;

import madparker.gametools.screen.Screen;
import madparker.gametools.util.GameElement;
import madparker.gametools.util.ProcessingObject;
import madparker.gametools.util.Timer;

import ipse.screen.StartScreen;
import ipse.timer.AmazonTimer;
import ipse.timer.GeneralTimer;
import ipse.timer.MultiplayerTimer;


public class Ipse extends PApplet 
{
	public Screen currentScreen;
	public PVector basicScreenPosition = new PVector(0, 0);
	
	public List<Timer> timers;
	public static long UPDATE_INTERVAL = 30;
	public List<Timer> timersToBeAdded;
	
	public boolean amazonOffline = false;
	public AmazonSimpleDB sdb;
	public String domain = "IpsePlayerData";
	public String username = "";
	public String playerName = "";
	public String encryptedPass = "";
	public String worldDomain = "IpseWorldData";
	public String multiplayerDomain = "IpsePlayerTransactions";
	
	public PMediaPlayer tones[] = new PMediaPlayer[6];
	public PMediaPlayer bass[] = new PMediaPlayer[2];

	public PWidgetContainer widgetContainer; 
	public boolean resetWidgets = false;
	
	public InputMethodManager imm;
	public NotificationManager gNotificationManager;
	public Notification gNotification;
	public long[] gVibrate = {0,150,0};
	
	public static int VERTICAL = 0;
	public static int HORIZONTAL = 1;
	public int orientation = VERTICAL; 
	public static float BLOCK_SIZE = 40;
	
	
	@Override
	public void setup()
	{
		orientation(PORTRAIT);

		ProcessingObject.pApplet = this;
		GameElement.prevTime = System.currentTimeMillis();
		GameElement.currentTime = System.currentTimeMillis();
		
		for (int i = 0; i < bass.length; i++)
		{
			PMediaPlayer player = new PMediaPlayer(this);
			player.setMediaFile("tone-" + PApplet.nf((i*2)+1, 2) + ".mp3");
			player.setLooping(false);
			player.setVolume(1.0f, 1.0f);
			bass[i] = player;
		}
		for (int i = 0; i < tones.length; i++)
		{
			PMediaPlayer player = new PMediaPlayer(this);
			player.setMediaFile("tone-" + PApplet.nf((i*2)+3+bass.length, 2) + ".mp3");
			player.setLooping(false);
			player.setVolume(1.0f-((i+1)/10), 1.0f-((i+1)/10));
			tones[i] = player;
		}
		
		widgetContainer = new PWidgetContainer(this);

		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		timers = new ArrayList<Timer>();
		timersToBeAdded = new ArrayList<Timer>();
		
		attemptAmazonConnect();

		currentScreen = new StartScreen(this);

		Timer generalTimer = new GeneralTimer(this, UPDATE_INTERVAL);
		timers.add(generalTimer);
		generalTimer.trigger();

		smooth();
	}
	
	public void attemptAmazonConnect()
	{
		sdb = new AmazonSimpleDBClient(new PropertiesCredentials(
		        createInput("AwsCredentials.properties")));
		try {
			sdb.listDomains();
		} catch (AmazonClientException ace) {
			amazonOffline = true;
		}
		
		if (!amazonOffline) 
		{
			try {
				sdb.domainMetadata(new DomainMetadataRequest(domain));
			} catch (NoSuchDomainException nsd) {
				sdb.createDomain(new CreateDomainRequest(domain));
			}
			try {
				sdb.domainMetadata(new DomainMetadataRequest(worldDomain));
			} catch (NoSuchDomainException nsd) {
				sdb.createDomain(new CreateDomainRequest(worldDomain));
			}
			try {
				sdb.domainMetadata(new DomainMetadataRequest(multiplayerDomain));
			} catch (NoSuchDomainException nsd) {
				sdb.createDomain(new CreateDomainRequest(multiplayerDomain));
			}
		}
		
		if (amazonOffline)
		{
			AmazonTimer amazonTimer = new AmazonTimer(this);
			timers.add(amazonTimer);
			amazonTimer.trigger();
		}
		else
		{
			MultiplayerTimer multiplayerTimer = new MultiplayerTimer(this);
			timers.add(multiplayerTimer);
			multiplayerTimer.trigger();
		}
	}
	
	@Override
	public void draw()
	{
		GameElement.prevTime = GameElement.currentTime;
		GameElement.currentTime = System.currentTimeMillis();
		
		if (resetWidgets)
		{
			widgetContainer = new PWidgetContainer(this);
			resetWidgets = false;
		}
		
		for (Timer timer : timers)
			if(timer.isActive())
				timer.update();
		
		if (timersToBeAdded.size() > 0)
		{
			timers.addAll(timersToBeAdded);
			timersToBeAdded.clear();
		}
	}
	
	public synchronized void update()
	{	
		currentScreen.update();
		currentScreen.draw();
	}
	
	public void multiplayerUpdate()
	{
		currentScreen.multiplayerUpdate();
	}
	
	public void onClickWidget(PWidget widget)
	{
		currentScreen.onClickWidget(widget);
	}
	
	@Override
	public void mousePressed()
	{
		currentScreen.mousePressed();
	}
	
	@Override
	public void mouseReleased()
	{
		currentScreen.mouseReleased();
	}
	
	@Override
	public void keyPressed()
	{
		currentScreen.keyPressed();
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		gNotificationManager = (NotificationManager) 
			getSystemService(Context.NOTIFICATION_SERVICE);
		gNotification = new Notification();
		gNotification.vibrate = gVibrate;
	}

	
	@Override
	public void stop()
	{
		currentScreen.stop();
		super.stop();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy(); //call onDestroy on super class
		for (int i = 0; i < bass.length; i++)
			if (bass[i] != null) 
				bass[i].release();
		for (int i = 0; i < tones.length; i++)
			if (tones[i] != null) 
				tones[i].release();
		widgetContainer.release();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		currentScreen.onKeyDown(keycode);
		return super.onKeyDown(keycode, event);
	}
	
	public static void main(String _args[])
	{
		PApplet.main(new String[]{ Ipse.class.getName() } );
	}
}