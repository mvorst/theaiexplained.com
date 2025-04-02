package com.mattvorst.shared.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class MarkupUtils {

	public static String replacePwithBR(String markup){
		if(markup == null) {
			return null;
		}else{
			return markup.replaceAll("</p>\\s*?<p(:?\\s+.*?)?>","<br/>")
					.replaceAll("</?p(:?\\s+.*?)?>","");
		}
	}

	public static String sanitize(String markup){
		return sanitize(markup, new String[]{"p", "b", "strong", "i", "u", "s"});
	}

	public static String sanitize(String markup, String[] allowedElements){
		if(markup == null) {
			return null;
		}else{
			PolicyFactory policy = new HtmlPolicyBuilder().allowElements(allowedElements).toFactory();
			return policy.sanitize(markup);
		}
	}

	public static String stripMarkup(String markup){
		return sanitize(markup, new String[]{});
	}
}
