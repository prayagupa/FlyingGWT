package com.skorulis.heli.client;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.skorulis.heli.math.Vec2f;
import com.skorulis.heli.sprite.Helicopter;
import com.skorulis.heli.sprite.HelicopterSmoke;
import com.skorulis.heli.sprite.Landscape;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FlyingGWT implements EntryPoint,KeyUpHandler,KeyDownHandler,MouseMoveHandler,MouseDownHandler,MouseUpHandler {

	private int MAX_KEYS = 256;
	
	private Context2d context;
	private float score,bestScore;
	
	private final static int canvasWidth = 600;
	private final static int canvasHeight = 400;
	private Timer timer;
	
	private int lastUpdate;
	private int current;
	private float smokeUpdate;
	
	Duration duration;
	
	boolean keys[] = new boolean[MAX_KEYS];
	
	private Helicopter helicopter;
	private Landscape landscape;
	private boolean mouseDown;
	final CssColor redrawColor = CssColor.make("rgba(255,255,255,1.0)");
	final CssColor textColor = CssColor.make("rgba(0,0,0,1)");
	
	private Vec2f mouseLoc; 
	private FrameRateCalc frameRate;
	
	private Button startButton;
	
	private LinkedList<HelicopterSmoke> smoke;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		frameRate = new FrameRateCalc();
		smoke = new LinkedList<HelicopterSmoke>();
		
		final Canvas canvas = Canvas.createIfSupported();
		if(canvas==null) {
			RootPanel.get().add(new Label("Canvas is not supported by your browser"));
			return;
		}
		RootPanel.get().add(canvas);
		startButton = new Button("Start");
		startButton.setStyleName("start");
		RootPanel.get().add(startButton);
		
		startButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				canvas.setFocus(true);
				startButton.setVisible(false);
				reset();
				timer.scheduleRepeating(20);
			}
		});
		
		
		canvas.setWidth(canvasWidth+"px");
		canvas.setCoordinateSpaceWidth(canvasWidth);
		
		canvas.setHeight(canvasHeight+"px");
		canvas.setCoordinateSpaceHeight(canvasHeight);
		canvas.addKeyDownHandler(this);
		canvas.addKeyUpHandler(this);
		canvas.addMouseDownHandler(this);
		canvas.addMouseUpHandler(this);	
		
		context = canvas.getContext2d();

		mouseLoc = new Vec2f();
		
		landscape = new Landscape(canvasWidth,canvasHeight);
		helicopter = new Helicopter(canvasWidth,canvasHeight);
		duration = new Duration();
		lastUpdate = duration.elapsedMillis();
		String best = Cookies.getCookie("score");
		if(best!=null) {
			bestScore = Float.valueOf(best);
			landscape.setBestScore(bestScore);
		}
		
		timer = new Timer() {
			@Override
			public void run() {
				update();
			}
		};
	}
	
	public void reset() {
		score = 0;
		smoke.clear();
		helicopter.reset();		
		landscape.reset();
		for(int i=0; i < MAX_KEYS; ++i) {
			keys[i] = false;
		}
		mouseDown = false;
	}
	
	public void update() {
		current = duration.elapsedMillis();
		float delta = (current-lastUpdate)/1000.0f;
		lastUpdate = current;
		if(delta > 0.5f) {
			return; //Don't update when the delta values are very high
		}
		
		
		frameRate.addFrame(delta);
		score+=delta*landscape.slideRate;
		
		context.setFillStyle(redrawColor);
		
	    context.fillRect(0, 0, canvasWidth, canvasHeight);
		
	    landscape.update(delta);
	    landscape.render(context);
	    
	    Iterator<HelicopterSmoke> smokeIt = smoke.iterator();
	    HelicopterSmoke hs;
	    while(smokeIt.hasNext()) {
	    	hs = smokeIt.next();
	    	if(hs.isAlive()) {
	    		hs.landRate = landscape.slideRate;
		    	hs.update(delta);
				hs.render(context);	
	    	} else {
	    		smokeIt.remove();
	    	}
			
	    }
	    
		helicopter.update(delta);
		helicopter.render(context);
		
		if(smokeUpdate > 0.35f) {
			smokeUpdate = 0;
			smoke.add(new HelicopterSmoke(helicopter.loc));
		} else {
			smokeUpdate+=delta;
		}
		
		context.setFillStyle(textColor);
		context.fillText("Score: " + (int)score, 10, 10);
		context.fillText("Best: " + (int) Math.max(score, bestScore), 10, 20);
		context.fillText("" + frameRate.getTmpFrameRate() + " frames a second",10,30);
		context.fill();
		
		if(keys['W'] || mouseDown) {
			helicopter.vel.y+= -130*delta;
		}
		if(keys['D']) {
			helicopter.vel.x+= 80*delta;
		}
		if(keys['A']) {
			helicopter.vel.x+= -80*delta;
		}
		if(keys['S']) {
			//helicopter.vel.y+= 80*delta;
		}
		if(landscape.collides(helicopter.box)) {
			onDeath();
			timer.cancel();
		}
	}
	
	private void onDeath() {
		bestScore = Math.max(score, bestScore);
		Date now = new Date();
		Date expires = new Date(now.getTime() +1000*3600*24*1000);
		
		Cookies.setCookie("score", ""+bestScore, expires);
		landscape.setBestScore(bestScore);
		
		startButton.setVisible(true);
		startButton.setText("Restart");
	}	

	@Override
	public void onKeyDown(KeyDownEvent event) {
		int key = event.getNativeKeyCode();
		if(key < MAX_KEYS) {
			GWT.log("KEY " + key);
			keys[key] = true;
		}
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		int key = event.getNativeKeyCode();
		if(key < MAX_KEYS) {
			keys[key] = false;
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		mouseLoc.x = event.getX(); mouseLoc.y = event.getY();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		mouseDown = false;
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		mouseDown = true;
	}
}
