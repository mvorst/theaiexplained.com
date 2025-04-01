package com.mattvorst.shared.dao.model.image;

import java.util.Objects;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class CropData {
	private int x;
	private int y;
	private int width;
	private int height;
	private int scaleE4;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getScaleE4() {
		return scaleE4;
	}

	public void setScaleE4(int scaleE4) {
		this.scaleE4 = scaleE4;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CropData cropData = (CropData) o;
		return x == cropData.x &&
				y == cropData.y &&
				width == cropData.width &&
				height == cropData.height &&
				scaleE4 == cropData.scaleE4;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, width, height, scaleE4);
	}
}
