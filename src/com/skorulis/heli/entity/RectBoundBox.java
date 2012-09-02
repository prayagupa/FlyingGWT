package com.skorulis.heli.entity;

/**
 * 
 * @author prayag
 * 
 */
public class RectBoundBox {

	public float x, y;
	public float width, height;

	@Override
	public String toString() {
		return "(" + x + "," + y + ") - (" + width + "," + height + ")";
	}

}
