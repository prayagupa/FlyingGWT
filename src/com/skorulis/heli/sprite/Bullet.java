package com.skorulis.heli.sprite;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.skorulis.heli.components.Gen2fComponent;
import com.skorulis.heli.entity.IRenderComponent;
import com.skorulis.heli.entity.IUpdateComponent;

/**
 * bullet.png
 * 
 * @author prayag
 * 
 */
public class Bullet implements IRenderComponent, IUpdateComponent {

	ImageElement image;
	Gen2fComponent loc;

	public Bullet() {
		image = (ImageElement) new Image("bullet.png").getElement().cast();
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void render(Context2d context) {

	}

}
