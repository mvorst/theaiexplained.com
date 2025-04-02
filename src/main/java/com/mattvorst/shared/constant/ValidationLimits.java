package com.mattvorst.shared.constant;

public class ValidationLimits {

	private int min;
	private int max;

	public ValidationLimits() {
		super();
	}

	public ValidationLimits(int min, int max) {
		super();

		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public String toString() {
		return "(" + min + "," + max + ")";
	}
}
