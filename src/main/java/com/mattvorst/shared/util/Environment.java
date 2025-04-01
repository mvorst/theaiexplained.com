package com.mattvorst.shared.util;

/*
 * Copyright (c) 2025 Matt Vorst
 * All Rights Reserved.
 * Using, copying, modifying, or distributing this software is strictly prohibited without the explicit written consent of Matt Vorst.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.UUID;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.EnvironmentType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

public class Environment {
	private static final Logger log = LoggerFactory.getLogger(Environment.class);

	private static final int TIMEOUT_IN_MILLISECONDS = 5000;

	public static final String CLIENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";

	public static final String ZONE_TO_REGION_REGEX = "[a-zA-Z]$";

	private static String environment;
	private static String awsRegion;
	private static EnvironmentType environmentType;
	private TreeMap<String, String> propertyHash;
	private TreeMap<String, List<String>> listPropertyHash;

	private static Environment singleton;

	private static long lastLoadTime = 0;
	private static boolean instantiated = false;

	private MessageSource errorMessageSource;

	private Environment() {
		propertyHash = new TreeMap<>();
		listPropertyHash = new TreeMap<>();
		lastLoadTime = System.currentTimeMillis();
	}

	public void init() {
		initProperties(propertyHash, listPropertyHash);

		instantiated = true;
	}

	public static synchronized Environment instance(String env) {
		environment = env;

		if (singleton == null) {
			singleton = new Environment();
			singleton.init();
		}
		return singleton;
	}

	public static synchronized String get(String key) {
		if (key == null || key.trim().length() == 0) {
			return "";
		} else {
			String value = singleton.propertyHash.get(key.toLowerCase());
			return value;
		}
	}

	public static synchronized List<String> getList(String key) {
		if (key == null || key.trim().length() == 0) {
			return new ArrayList<>();
		} else {
			List<String> list = singleton.listPropertyHash.get(key.toLowerCase());
			return list;
		}
	}

	public static synchronized void set(String key, String value) {
		if (key != null && key.trim().length() > 0) {
			if ("".equals(get(key)) == false) {
				singleton.propertyHash.put(key.toLowerCase(), value);
				log.debug("SETTING: " + key.toLowerCase() + ": " + value);
			} else {
				log.error("!!! NOT Setting - " + key.toLowerCase() + " value already set!");
			}
		}
	}

	public static synchronized boolean getBoolean(String key) {
		boolean value = "true".equalsIgnoreCase(get(key));
		return value;
	}

	public static synchronized int getInt(String key) {
		int value = 0;
		try {
			value = Integer.parseInt(get(key));
		} catch (Exception e) {
			log.error("Error converting " + key + " to int.", e);
		}

		return value;
	}

	public static synchronized long getLong(String key) {
		long value = 0;
		try {
			value = Long.parseLong(get(key));
		} catch (Exception e) {
			log.error("Error converting " + key + " to long.", e);
		}

		return value;
	}

	public static synchronized UUID getUuid(String key) {
		UUID value = null;
		try {
			String stringValue = get(key);
			if (!Utils.empty(stringValue)) {
				value = UUID.fromString(stringValue);
			}
		} catch (Exception e) {
			log.warn("Error converting " + key + " to UUID.", e);
		}

		return value;
	}

	public static synchronized byte[] getPasswordForKey(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = Encryption.md5Hash(get(key).getBytes("UTF-8"));

		return bytes;
	}

	public static synchronized byte[] getIVForKey(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = Encryption.md5Hash(get(key).getBytes("UTF-8"));

		return bytes;
	}

	public static synchronized byte[] getTokenPassword() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = Encryption.md5Hash(get("token_password").getBytes("UTF-8"));

		return bytes;
	}

	public static synchronized byte[] getTokenIV() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = Encryption.md5Hash(get("token_iv").getBytes("UTF-8"));

		return bytes;
	}

	public static String getEnvironment() {
		return environment;
	}

	public static String getAwsRegion() {
		return awsRegion;
	}

	public static EnvironmentType getEnvironmentType() {
		return environmentType;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	@SuppressWarnings("rawtypes")
	private void initProperties(TreeMap<String, String> propertyHash, TreeMap<String, List<String>> listPropertyHash) {
		DateFormat DATE_FORMAT = new SimpleDateFormat(CLIENT_DATE_FORMAT);
		log.info("Environment cache reloading " + DATE_FORMAT.format(new Date(System.currentTimeMillis())));

		if (Utils.empty(environment)) {
			// Load the environment from Amazon if it's available
			try {
				String awsEnvironment = getAWSProperty("http://169.254.169.254/latest/meta-data/placement/availability-zone");
				if (awsEnvironment != null) {
					environment = awsEnvironment.toUpperCase();
					awsRegion = awsEnvironment.replaceAll(ZONE_TO_REGION_REGEX, "");
					if (!Utils.empty(awsRegion)) {
						propertyHash.put(EnvironmentConstants.AWS_REGION, awsRegion);
					}
				}

				propertyHash.put(EnvironmentConstants.AWS_AVAILABILITY_ZONE, environment);

				// Load the environment from Amazon if it's available
				String instanceId = getAWSProperty("http://169.254.169.254/latest/meta-data/instance-id");
				if (instanceId != null) {
					propertyHash.put(EnvironmentConstants.AWS_INSTANCE_ID, instanceId);
				}

				String amiId = getAWSProperty("http://169.254.169.254/latest/meta-data/ami-id");
				if (amiId != null) {
					propertyHash.put(EnvironmentConstants.AWS_AMI_ID, amiId);
				}

				String localIpv4 = getAWSProperty("http://169.254.169.254/latest/meta-data/local-ipv4");
				if (localIpv4 != null) {
					propertyHash.put(EnvironmentConstants.AWS_LOCAL_IPV4, localIpv4);
				}
			} catch (Exception e) {
				log.error("EC2 Information Not Available", e);
			}
		}

		log.info("ENVIRONMENT: " + environment);

		try {
			for (String key : propertyHash.keySet()) {
				String value = propertyHash.get(key);

				log.debug("PROPERTY: " + key + " : " + value);
			}
		} catch (Exception e) {
			log.error("EC2 Information Not Available", e);
		}

		try {
			SAXReader reader = new SAXReader();

			Document document = reader.read(getClass().getResourceAsStream("/environment.xml"));

			// Load the base properties
			overlayProperties(document, null);

			// Overlay the region specific properties
			if (!Utils.empty(awsRegion)) {
				overlayProperties(document, awsRegion);
			}

			// Overlay the availability-zone or personal properties
			if(environment != null) {
				overlayProperties(document, environment);
			}
		} catch (DocumentException e) {
			log.error("Error parsing the environment.xml file: " + e.getMessage(), e);
		}

		for (String key : propertyHash.keySet()) {
			String value = propertyHash.get(key);
			String valueEncrypted = "";

			if (value != null) {
				try {
					valueEncrypted = Encryption.encryptToBase64(value, "H7&9s!dk^ghw;C8-DsBe$i&gXh".getBytes(), "Lb^h*js)bjwqOpv52*cH".getBytes());
				} catch (Exception e) {
					valueEncrypted = "Property NOT Encrypted";
				}
			}

			log.info(key + ": " + valueEncrypted);
		}

		for (String key : listPropertyHash.keySet()) {
			List<String> list = listPropertyHash.get(key);
			String valueEncrypted = "";

			if (!Utils.empty(list)) {
				int index = 0;
				for (String value : list) {
					try {
						valueEncrypted = Encryption.encryptToBase64(value, "H7&9s!dk^ghw;C8-DsBe$i&gXh".getBytes(), "Lb^h*js)bjwqOpv52*cH".getBytes());
					} catch (Exception e) {
						valueEncrypted = "Property NOT Encrypted";
					}
					log.info(key + "[" + (index++) + "]: " + valueEncrypted);
				}
			}
		}

		log.info("Environment cache reloaded. Version: " + get(EnvironmentConstants.BUILD_NUMBER));
	}

	private void overlayProperties(Document document, String id) {

		Element root = document.getRootElement();

		Iterator environmentItr = root.elementIterator("environment");
		while (environmentItr.hasNext()) {
			Element environmentElement = (Element) environmentItr.next();

			String environmentId = environmentElement.attributeValue("id");

			if ((Utils.empty(environmentId) && Utils.empty(id)) || (!Utils.empty(environmentId) && environmentId.equalsIgnoreCase(id))) {
				environmentType = Utils.enumFromString(environmentElement.attributeValue("type"), EnvironmentType.class);

				log.info("ENVIRONMENT ID: " + id + ", TYPE: " + environmentType);

				Iterator propertyItr = environmentElement.element("properties").elementIterator("property");
				while (propertyItr.hasNext()) {
					Element propertyElement = (Element) propertyItr.next();
					String key = propertyElement.attributeValue("id");
					if (key != null) {
						String value = propertyElement.getTextTrim();
						if (value != null) {
							propertyHash.put(key.trim().toLowerCase(), value);
						}
					}
				}

				Iterator listIterator = environmentElement.element("properties").elementIterator("list");
				while (listIterator.hasNext()) {

					Element listElement = (Element) listIterator.next();
					String key = listElement.attributeValue("id");
					if (key != null) {
						List<String> list = new ArrayList<>();

						Iterator valueIterator = listElement.elementIterator("property");
						while (valueIterator.hasNext()) {

							Element valueElement = (Element) valueIterator.next();
							if (valueElement != null) {
								list.add(valueElement.getTextTrim());
							}
						}
						listPropertyHash.put(key.trim().toLowerCase(), list);
					}
				}
			}
		}
	}

	private String getMetadataToken() {
		String token = null;
		try {
			HttpClient httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofMillis(TIMEOUT_IN_MILLISECONDS))
					.build();

			HttpRequest tokenRequest = HttpRequest.newBuilder()
					.uri(URI.create("http://169.254.169.254/latest/api/token"))
					.timeout(Duration.ofMillis(TIMEOUT_IN_MILLISECONDS))
					.header("X-aws-ec2-metadata-token-ttl-seconds", "21600")
					.PUT(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
			if (tokenResponse.statusCode() == 200) {
				token = tokenResponse.body();
			} else {
				log.warn("Failed to retrieve metadata token: HTTP status {}", tokenResponse.statusCode());
			}
		} catch (Exception e) {
			log.error("Error retrieving metadata token", e);
		}
		return token;
	}
	private String getAWSProperty(String url) {
		String property = null;
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(TIMEOUT_IN_MILLISECONDS))
				.build();

		try {
			String token = getMetadataToken();
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofMillis(TIMEOUT_IN_MILLISECONDS))
					.GET();

			// Only add the token if it was retrieved successfully
			if (token != null) {
				requestBuilder.header("X-aws-ec2-metadata-token", token);
			}

			HttpRequest httpRequest = requestBuilder.build();
			HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (httpResponse.statusCode() == 200) {
				property = httpResponse.body();
			} else {
				log.warn("Failed to retrieve AWS property from {}: HTTP status {}", url, httpResponse.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			log.error("Error retrieving AWS property from {}", url, e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Unexpected error while retrieving AWS property from {}", url, e);
		}

		return property;
	}

	public TreeMap<String, String> getPropertyMap() {
		return propertyHash;
	}

	public TreeMap<String, List<String>> getListPropertyMap() {
		return listPropertyHash;
	}

	public static Date getLastLoadTime() {
		return new Date(lastLoadTime);
	}

	public String getMessage(String messageKey) {
		return errorMessageSource.getMessage(messageKey, null, messageKey, Locale.getDefault());
	}

	public void setErrorMessageSource(MessageSource errorMessageSource) {
		this.errorMessageSource = errorMessageSource;
	}

	public void setEnvironment(String env) {
		environment = env;
	}

	public static boolean isInstantiated() {
		return instantiated;
	}
}
