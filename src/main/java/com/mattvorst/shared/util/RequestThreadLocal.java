package com.mattvorst.shared.util;

public class RequestThreadLocal {

	private static final ThreadLocal<String> threadId = new ThreadLocal<String>() { };

	public static void set(String value) {
		threadId.set(value);
	}

	public static String get() {
		return threadId.get();
	}
}
