package com.mattvorst.shared.util;

import java.util.Locale;

public class LocaleThreadLocal {

	private static final ThreadLocal<Locale> threadLocal = new ThreadLocal<Locale>() { };

	public static void set(Locale locale) {
		threadLocal.set(locale);
	}

	public static Locale get() {
		return threadLocal.get();
	}
}
