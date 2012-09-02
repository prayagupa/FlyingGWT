package com.skorulis.heli.sprite;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.skorulis.heli.entity.IEntity;
import com.skorulis.heli.entity.IRenderComponent;
import com.skorulis.heli.entity.IUpdateComponent;
import com.skorulis.heli.math.Vec2f;

/**
 * CssColor.make("rgba(55,55,55,1)")
 * 
 * @author prayag
 * 
 */
public class HelicopterSmoke implements IEntity, IRenderComponent,
		IUpdateComponent {

	private static final float fadeRate = 0.4f;
	private float life;
	private final Vec2f loc;
	public float landRate;
	CssColor color = CssColor.make("rgba(55,55,55,1)");

	public HelicopterSmoke(Vec2f loc) {
		this.loc = new Vec2f(loc.x, loc.y);
		life = 0.7f;
	}

	@Override
	public void update(float delta) {
		loc.x -= landRate * delta;
		life -= delta * fadeRate;
	}

	@Override
	public boolean isAlive() {
		return life > 0;
	}

	@Override
	public void render(Context2d context) {
		context.setGlobalAlpha(Math.max(life, 0));
		context.setFillStyle(color);
		context.fillRect(loc.x, loc.y, 20, 20);
		context.fill();
		context.setGlobalAlpha(1);
	}

}
