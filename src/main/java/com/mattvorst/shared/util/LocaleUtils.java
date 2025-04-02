package com.mattvorst.shared.util;

import java.util.Locale;

public class LocaleUtils {

	public final static String en_US = "en_US";

	public static Locale safeToLocale(String localeString){
		Locale locale = null;
		if(localeString != null) {
			localeString = localeString.replaceAll("_", "-");
			locale = Locale.forLanguageTag(localeString);
		}

		return locale;
	}

	public static String safeToString(Locale locale){
		String localeString = null;
		if(locale != null) {
			localeString = locale.getLanguage() + "_" + locale.getCountry().toUpperCase();
		}

		return localeString;
	}
}
