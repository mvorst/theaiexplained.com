package com.mattvorst.shared.util;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	private static final List<String> auditAttrs = Arrays.asList("updatedDate", "updatedBy", "createdDate", "createdBy");

	public static final char[] charArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z' };
	public static final char[] letterArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	public static final char[] specialArray = { '!', '#', '$', '%', '&', '(', ')', '*', '+', '-', '.', '/', ':', ';', '<', '=', '>', '?', '{', '}', '|', '\\', '~' };
	public static final DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
	public static final DateTimeFormatter yyyyMMFormat = DateTimeFormatter.ofPattern("yyyyMM").withZone(ZoneOffset.UTC);
	private static final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_DATE_TIME;
	public static String EMAIL_ADDRESS_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	public static String UUID_REGEX = "[0-9a-fA-F]{8}(?:\\-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
	private static ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


	public static DateFormat gmtDateFormat() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		return dateFormat;
	}

	public static <T> T convertMap(Map map, Class<T> clazz) {
		return objectMapper.convertValue(map, clazz);
	}

	public static <T> Map<String, String> convertObject(T object) {
		return objectMapper.convertValue(object, Map.class);
	}

	public static Gson gson() {
		return new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
				.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
				.registerTypeAdapter(Boolean.class, new BooleanSerializer())
				.registerTypeAdapter(boolean.class, new BooleanSerializer())
				.registerTypeAdapter(StackTraceElement.class, STACK_TRACE_ELEMENT_TYPE_ADAPTER)
				.create();
	}

	public static boolean equal(String string01, String string02) {
		boolean equal = true;

		if (string01 == null ^ string02 == null) {
			equal = false;
		} else if (string01 != null && string02 != null) {
			equal = string01.equals(string02);
		}

		return equal;
	}

	public static boolean equal(UUID uuid01, UUID uuid02) {
		boolean equal = true;

		if (uuid01 == null ^ uuid02 == null) {
			equal = false;
		} else if (uuid01 != null && uuid02 != null) {
			equal = uuid01.equals(uuid02);
		}

		return equal;
	}

	public static boolean equal(Date date01, Date date02) {
		boolean equal = true;

		if (date01 == null ^ date02 == null) {
			equal = false;
		} else if (date01 != null && date02 != null) {
			equal = date01.equals(date02);
		}

		return equal;
	}

	public static boolean equal(Enum<?> enum01, Enum<?> enum02) {
		boolean equal = true;

		if (enum01 == null ^ enum02 == null) {
			equal = false;
		} else if (enum01 != null && enum02 != null) {
			equal = enum01.equals(enum02);
		}

		return equal;
	}

	public static String toCSV(Set<String> set) {
		StringBuilder stringBuilder = new StringBuilder();

		if (set != null && set.size() > 0) {
			for (String string : set) {
				if (string != null) {
					if (stringBuilder.length() > 0) {
						stringBuilder.append(",");
					}
					stringBuilder.append(string.trim());
				}
			}
		}

		return stringBuilder.toString();
	}

	public static String toCSV(String... array) {
		StringBuilder stringBuilder = new StringBuilder();

		if (array != null && array.length > 0) {
			for (String string : array) {
				if (string != null) {
					if (stringBuilder.length() > 0) {
						stringBuilder.append(",");
					}
					stringBuilder.append(string.trim());
				}
			}
		}

		return stringBuilder.toString();
	}

	public static String asText(Date date) {
		String text = "-";

		if (date != null) {
			long difference = (System.currentTimeMillis() - date.getTime());

			if (difference < 60000) {
				text = "seconds ago";
			} else if (difference < 600000) {
				text = "minutes ago";
			} else if (difference < 14400000) {
				text = "hours ago";
			} else if (difference < 86400000) {
				text = "last 24 hours";
			} else if (difference < 172800000) {
				text = "yesterday";
			} else if (difference >= 172800000) {
				text = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(date);
			}
		}

		return text;
	}

	public static <K, V> List<V> valuesList(Map<K, V> map) {
		if (map == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(map.values());
		}
	}

	public static <V> Set<V> asSet(Collection<V> collection) {
		if (collection == null) {
			return new HashSet<>();
		} else {
			return new HashSet<>(collection);
		}
	}

	public static <V> Set<String> asStringSet(Collection<V> collection) {
		Set<String> set = new HashSet<>();

		if (collection != null) {
			for (Object object : collection) {
				set.add(object.toString());
			}
		}

		return set;
	}

	public static <V> List<V> asList(Collection<V> collection) {
		if (collection == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(collection);
		}
	}

	public static <V> List<V> asList(V[] array) {
		if (array == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(Arrays.asList(array));
		}
	}

	public static <V> List<String> asStringList(V[] array) {
		if (array == null) {
			return new ArrayList<>();
		} else {
			List<String> list = new ArrayList<>();

			for (V object : array) {
				list.add(object.toString());
			}

			return list;
		}
	}

	public static <V> List<String> asStringList(Collection<V> collection) {
		if (collection == null) {
			return new ArrayList<>();
		} else {
			List<String> list = new ArrayList<>();

			for (V object : collection) {
				list.add(object.toString());
			}

			return list;
		}
	}

	public static <T extends Enum<T>> List<T> asEnumList(Collection<String> collection, Class<T> c) {
		if (collection == null) {
			return new ArrayList<>();
		} else {
			List<T> list = new ArrayList<>();
			for (String enumString : collection) {
				list.add(Enum.valueOf(c, enumString));
			}
			return list;
		}
	}

	public static <T extends Enum<T>> Set<T> asEnumSet(String[] deviceType, Class<T> c) {
		if(deviceType == null || deviceType.length == 0){
			return new HashSet<>();
		}

		Set<T> set = new HashSet<>();
		Arrays.stream(deviceType).iterator().forEachRemaining(stringValue -> {
			try{
				set.add(Enum.valueOf(c, stringValue));
			}catch (Exception e){
				log.warn("Data Conversion Exception", e);
			}
		});

		return set;
	}

	public static List<UUID> asUUIDList(String[] array) {
		if (array == null) {
			return new ArrayList<>();
		} else {
			List<UUID> list = new ArrayList<>();
			for (String uuidString : array) {
				if (!Utils.empty(uuidString)) {
					list.add(UUID.fromString(uuidString));
				}
			}
			return list;
		}
	}

	public static List<UUID> asUUIDList(Collection<String> collection) {
		if (collection == null) {
			return new ArrayList<>();
		} else {
			List<UUID> list = new ArrayList<>();
			for (String uuidString : collection) {
				list.add(UUID.fromString(uuidString));
			}
			return list;
		}
	}

	public static <K, V> List<V> valuesListFromJson(Map<K, String> map, Class<V> c) throws IOException {
		List<V> results = new ArrayList<>();

		if (map != null) {
			for (String jsonString : map.values()) {
				results.add(gson().fromJson(jsonString, c));
			}
		}
		return results;
	}

	public static <K> Map<K, String> mapToJsonMap(Map<K, ?> map) {
		Map<K, String> results = new HashMap<>();

		if (map != null) {
			Gson gson = gson();
			for (K key : map.keySet()) {
				try {
					results.put(key, gson.toJson(map.get(key)));
				} catch (Exception e) {
					log.warn("Data Conversion Exception", e);
				}
			}
		}
		return results;
	}

	public static <T extends Enum<T>> T enumFromString(String string, Class<T> c) {
		if (!empty(string) && !"null".equalsIgnoreCase(string)) {
			T enumValue = null;

			try {
				enumValue = Enum.valueOf(c, string);
			} catch (Exception e) {
				log.warn("Data Conversion Exception", e);
			}

			return enumValue;
		} else {
			return null;
		}
	}

	public static <T extends Enum<T>> T safeToEnum(Object object, Class<T> c) {
		if (object != null) {
			T resultingObject = null;

			try {
				resultingObject = enumFromString(object.toString(), c);
			} catch (Throwable t) {
				log.debug("Data Conversion Exception", t);
				resultingObject = null;
			}

			return resultingObject;
		} else {
			return null;
		}
	}

	public static <T extends Enum<T>> T safeToEnum(Object object, Class<T> c, T defaultValue) {
		if (object != null) {
			T resultingObject = null;

			try {
				resultingObject = enumFromString(object.toString(), c);
			} catch (Throwable t) {
				log.debug("Data Conversion Exception", t);
				resultingObject = defaultValue;
			}

			return resultingObject;
		} else {
			return defaultValue;
		}
	}

	public static <T extends Enum<T>> T enumFromString(String string, Class<T> c, T nullOption) {
		if (string != null && string.trim().length() > 0) {
			return enumFromString(string, c);
		} else {
			return nullOption;
		}
	}

	public static String stringFromEnum(Enum<?> e) {
		if (e != null) {
			return e.name();
		} else {
			return null;
		}
	}

	public static UUID uuidFromString(String uuidString) {
		UUID uuid = null;

		if (!empty(uuidString)) {
			try {uuid = UUID.fromString(uuidString);} catch (Exception e) {log.warn("Data Conversion Exception", e);}
		}
		return uuid;
	}

	public static UUID safeToUuid(Object uuidObject) {
		UUID uuid = null;

		if (uuidObject != null) {
			try {uuid = UUID.fromString(uuidObject.toString());} catch (Exception e) {log.debug("Data Conversion Exception", e);}
		}
		return uuid;
	}

	public static Date safeToDate(Date inputDate) {
		Date date = null;

		if (inputDate != null) {
			try {date = new Date(inputDate.getTime());} catch (Exception e) {log.debug("Data Conversion Exception", e);}
		}
		return date;
	}

	public static Date safeToDate(String input) {
		Date date = null;

		if (input != null) {
			try {date = gmtDateFormat().parse(input);} catch (Exception e) {log.debug("Data Conversion Exception", e);}
		}
		return date;
	}

	public static Date safeToDateFromMillis(String input) {
		Date date = null;

		if (input != null) {
			try {date = new Date(Long.parseLong(input));} catch (Exception e) {log.debug("Data Conversion Exception", e);}
		}
		return date;
	}

	public static String stringFromUuid(UUID uuid) {
		if (uuid != null) {
			return uuid.toString();
		} else {
			return null;
		}
	}

	public static Boolean safeToBoolean(String value) {
		if (!empty(value)) {
			return "TRUE".equalsIgnoreCase(value);
		} else {
			return null;
		}
	}

	public static int safeToInt(Number value) {
		if (value == null) {
			return 0;
		} else {
			return value.intValue();
		}
	}

	public static int safeToInt(String value) {
		if (value == null) {
			return 0;
		} else {
			try {
				return Integer.parseInt(value);
			}catch (Exception e){
				return 0;
			}
		}
	}

	public static long safeToLong(String value) {
		if (value == null) {
			return 0;
		} else {
			try {
				return Long.parseLong(value);
			}catch (Exception e){
				return 0;
			}
		}
	}

	public static String safeTrim(String value) {
		if (!empty(value)) {
			return value.trim();
		} else {
			return value;
		}
	}

	public static Timestamp timestampFromDate(Date date) {
		if (date != null) {
			return new Timestamp(date.getTime());
		} else {
			return null;
		}
	}

	public static <T extends Object> T jsonToObject(String json, Class<T> c) throws IOException {
		T object = null;

		if (json != null && json.trim().length() > 0) {
			object = gson().fromJson(json, c);
		}

		return object;
	}

	public static <T extends Enum<T>> Set<T> enumSetFromStringSet(Set<String> stringSet, Class<T> c) {
		Set<T> set = new HashSet<>();

		if (stringSet != null && stringSet.size() > 0) {
			for (String string : stringSet) {
				try {
					T e = enumFromString(string, c);
					set.add(e);
				} catch (Exception e) {log.warn("Data Conversion Exception", e);}
			}
		}

		return set;
	}

	public static String stringFromE2(int valueE2) {
		String value;

		if (valueE2 < 100) {
			value = "0." + valueE2;
		} else {
			String temp = Integer.toString(valueE2);
			int length = temp.length();

			value = temp.substring(0, length - 2) + "." + temp.substring(length - 2);
		}

		return value;
	}

	public static int e2FromBigDecimal(BigDecimal bigDecimal) {
		if (bigDecimal != null) {
			return bigDecimal.multiply(new BigDecimal(100)).intValueExact();
		} else {
			return 0;
		}
	}

	public static String merge(String... stringArray) {
		StringBuilder value = new StringBuilder();

		int maxLength = 0;
		for (String string : stringArray) {
			if (string.length() > maxLength) {
				maxLength = string.length();
			}
		}

		for (int index = 0; index < maxLength; index++) {
			for (String string : stringArray) {
				if (string.length() > index) {
					value.append(string.charAt(index));
				} else {
					value.append(" ");
				}
			}
		}

		return value.toString();
	}

	public static String[] split(String string, int parts) throws IOException {
		String[] resultArray = new String[parts];
		StringBuilder[] stringBuilderArray = new StringBuilder[string.length()];
		for (int index = 0; index < parts; index++) {
			stringBuilderArray[index] = new StringBuilder();
		}

		StringReader reader = new StringReader(string);
		int c = 0;
		int index = 0;
		while ((c = reader.read()) > 0) {
			stringBuilderArray[index].append((char) c);
			index++;
			if (index >= parts) {
				index = 0;
			}
		}

		for (index = 0; index < parts; index++) {
			resultArray[index] = stringBuilderArray[index].toString().trim();
		}

		return resultArray;
	}

	public static String[] safeSplit(String string, String regex) throws IOException {
		String[] resultArray = null;

		try {
			if (!empty(string)) {
				resultArray = string.split(regex);
			}
		} catch (Throwable t) {
			log.debug("Data Conversion Exception", t);
		}

		return resultArray;
	}

	public static GregorianCalendar asGregorianCalendar(Calendar calendar) {
		if (calendar != null && calendar.getTime() != null) {
			GregorianCalendar gregorianCalendar = new GregorianCalendar();

			gregorianCalendar.setTime(calendar.getTime());

			return gregorianCalendar;
		} else {
			return null;
		}
	}

	public static Date asDate(Calendar calendar) {
		if (calendar != null) {
			return calendar.getTime();
		} else {
			return null;
		}
	}

	public static String randomString(int length, boolean includeSpecial) {
		StringBuilder stringBuilder = new StringBuilder();

		Random random = new Random(System.currentTimeMillis());

		int maxRandom = includeSpecial ? (charArray.length + specialArray.length) : charArray.length;

		for (int x = 0; x < length; x++) {
			int index = random.nextInt(maxRandom);
			if (index < charArray.length) {
				stringBuilder.append(charArray[index]);
			} else {
				stringBuilder.append(specialArray[index - charArray.length]);
			}
		}

		return stringBuilder.toString();
	}

	public static String randomString36(int length) {
		StringBuilder stringBuilder = new StringBuilder();

		Random random = new Random(System.currentTimeMillis());

		int maxRandom = 35;

		for (int x = 0; x < length; x++) {
			int index = random.nextInt(maxRandom);

			stringBuilder.append(charArray[index]);
		}

		return stringBuilder.toString();
	}

	public static byte[] concat(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];

		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);

		return result;
	}

	public static String asCSV(List<String> list) {
		StringBuilder sb = new StringBuilder();

		for (String string : list) {
			sb.append("\"" + string.replaceAll("\"", "\"\"") + "\"");
		}

		return sb.toString();
	}

	public static List<String> asListFromCSV(String delimitedString) {
		if (delimitedString != null) {
			return Arrays.asList(delimitedString.split(","));
		} else {
			return null;
		}
	}

	public static List<String> safeListFromCSV(String delimitedString) {
		if (delimitedString != null) {
			return Arrays.asList(delimitedString.split(","));
		} else {
			return new ArrayList<>();
		}
	}

	public static String safeToString(Object object) {
		if (object != null) {
			return object.toString();
		} else {
			return null;
		}
	}

	public static long safeLong(Object object) {
		if (object != null) {
			try {
				return Long.valueOf(object.toString());
			} catch (Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static int safeInt(Object object) {
		if (object != null) {
			try {
				return Integer.valueOf(object.toString());
			} catch (Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static int daysSince(Date referenceDate) {
		int days = 0;
		if (referenceDate != null) {
			days = Math.max(0, (int) Math.floor((System.currentTimeMillis() / 1000 - referenceDate.getTime() / 1000) / 86400));
		}

		return days;
	}

	public static int daysUntil(Date referenceDate) {
		int days = 0;
		if (referenceDate != null) {
			days = Math.max(0, (int) Math.floor((referenceDate.getTime() / 1000 - System.currentTimeMillis() / 1000) / 86400));
		}

		return days;
	}

	public static Date nextDateAtTimeForTimeZone(Date referenceDate, int hour, int minute, String timeZoneString) {
		if (referenceDate != null) {
			GregorianCalendar gregorianCalendar = new GregorianCalendar();

			gregorianCalendar.setTimeInMillis(referenceDate.getTime());

			try {
				TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
				gregorianCalendar.setTimeZone(timeZone);
			} catch (Exception e) {log.warn("Timezone Not Found Exception", e);}

			gregorianCalendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
			gregorianCalendar.set(GregorianCalendar.MINUTE, minute);
			gregorianCalendar.set(GregorianCalendar.SECOND, 0);
			gregorianCalendar.set(GregorianCalendar.MILLISECOND, 0);

			if (gregorianCalendar.getTimeInMillis() < referenceDate.getTime()) {
				gregorianCalendar.add(GregorianCalendar.DATE, 1);
			}

			return gregorianCalendar.getTime();
		} else {
			return null;
		}
	}

	public static Date addCalendarDays(Date referenceDate, int days, String timeZoneString) {
		if (referenceDate != null) {
			GregorianCalendar gregorianCalendar = new GregorianCalendar();

			gregorianCalendar.setTimeInMillis(referenceDate.getTime());

			try {
				TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
				gregorianCalendar.setTimeZone(timeZone);
			} catch (Exception e) {log.warn("Timezone Not Found Exception", e);}

			gregorianCalendar.add(GregorianCalendar.DATE, days);

			return gregorianCalendar.getTime();
		} else {
			return null;
		}
	}

	public static int age(Date date) {
		GregorianCalendar now = new GregorianCalendar();
		now.setTime(new Date(System.currentTimeMillis()));

		GregorianCalendar dob = new GregorianCalendar();
		dob.setTime(date);

		int age = now.get(GregorianCalendar.YEAR) - dob.get(GregorianCalendar.YEAR);

		if (now.get(GregorianCalendar.DAY_OF_YEAR) < dob.get(GregorianCalendar.DAY_OF_YEAR)) {
			age -= 1;
		}

		return age;
	}

	public static final boolean empty(String string) {
		return string == null || string.trim().length() == 0;
	}

	public static final boolean emptyString(String string) {
		return string == null || string.trim().length() == 0;
	}

	public static final boolean empty(Collection<?> collection) {
		return collection == null || collection.size() == 0;
	}

	public static final boolean empty(Map<?, ?> map) {
		return map == null || map.size() == 0;
	}

	public static final boolean empty(Object[] array) {
		return array == null || array.length == 0;
	}

	public static final boolean equals(String string0, String string1) {

		boolean empty0 = empty(string0);
		boolean empty1 = empty(string1);

		if (empty0 != empty1) {
			// One is empty the other isn't.
			return false;
		} else {
			// They're both empty or not empty.
			if (empty0) {
				// They're both equally empty.
				return true;
			} else {
				// They're both NOT empty. We need to compare them.
				return string0.equals(string1);
			}
		}
	}

	public static <V> List<V> copyFirst(List<V> list, int count) {

		List<V> returnList = new ArrayList<>();

		for (V object : list) {
			if (returnList.size() < count) {
				returnList.add(object);
			} else {
				break;
			}
		}

		return returnList;
	}

	public static <T> void overwriteProperties(T source, T target, Set<String> properties) {
		for (String property: properties) {
			PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(source.getClass(), property);

			if (descriptor != null) {
				try {
					descriptor.getWriteMethod().invoke(target, descriptor.getReadMethod().invoke(source));
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void copyNonNullProperties(Object source, Object target) {
		final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
		BeanUtils.copyProperties(source, target, Stream.of(wrappedSource.getPropertyDescriptors())
				.map(FeatureDescriptor::getName)
				.filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
				.toArray(String[]::new));
	}

	public static <T> void mergeModelProperties(T updatedModel, T existingModel, String... ignoreAttributes) {
		List<String> ignoreAttributeList = new ArrayList<>();

		if (ignoreAttributes != null) {
			ignoreAttributeList.addAll(Arrays.asList(ignoreAttributes));
		}

		ignoreAttributeList.addAll(List.of("createdDate", "createdBySubject", "updatedDate", "updatedBySubject", "version"));

		BeanUtils.copyProperties(updatedModel, existingModel, ignoreAttributeList.toArray(new String[ignoreAttributeList.size()]));
	}

	public static <T> void mergeReplicationProperties(T updatedModel, T existingModel, Set<String> updatedAttributes, String... ignoreAttributes) {
		List<String> ignoreAttributeList = new ArrayList<>();

		if (ignoreAttributes != null) {
			ignoreAttributeList.addAll(Arrays.asList(ignoreAttributes));
		}

		ignoreAttributeList.add("version");

		// If the updated model has a null value, BeanUtils will clobber the existing model with a null value. In some cases, this is exactly what we want, in other cases
		// it is not. For example, there may be multiple upstream tables replicating into a single downstream table. In this case, only a subset of attributes will be present
		// at a given time, and we don't want to clobber the other attributes in the DDB item. By default, we will not clobber with null values unless the property is present
		// in the updatedAttributes. This will indicate the attribute was explicitly changed to null.
		BeanWrapper wrappedSource = new BeanWrapperImpl(updatedModel);
		List<String> nullPropertiesToIgnore = Stream.of(wrappedSource.getPropertyDescriptors())
				.map(FeatureDescriptor::getName)
				.filter(propertyName -> {
					try {
						return !updatedAttributes.contains(propertyName) && wrappedSource.getPropertyValue(propertyName) == null;
					} catch (Exception e) {
						return false;
					}
				}).toList();

		ignoreAttributeList.addAll(nullPropertiesToIgnore);
		BeanUtils.copyProperties(updatedModel, existingModel, ignoreAttributeList.toArray(new String[ignoreAttributeList.size()]));
	}

	public static final <V> List<V> toList(V[] array) {
		List<V> list = new ArrayList<>();

		if (array != null) {
			for (V object : array) {
				list.add(object);
			}
		}

		return list;
	}

	public static int minutesBetween(Date currentDate, Date referenceDate) {
		if (currentDate == null && referenceDate == null) {
			return 0;
		} else if (currentDate == null) {
			return Integer.MIN_VALUE;
		} else if (referenceDate == null) {
			return Integer.MAX_VALUE;
		} else {
			long difference = (currentDate.getTime() - referenceDate.getTime()) / 60000L;
			return (int) difference;
		}
	}

	public static <V> boolean contains(Collection<V> collection, V value) {
		if (collection != null) {
			for (V temp : collection) {
				if (temp.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public static <V> boolean contains(V[] array, V value) {
		if (array != null) {
			for (V temp : array) {
				if (temp.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public static <V> boolean containsAny(Collection<V> collection0, Collection<V> collection1) {
		if (collection0 != null && collection1 != null) {
			return collection0.stream().anyMatch(collection1::contains);
		}

		return false;
	}

	public static <T> T safe(Object o, Supplier<T> s) {
		if (o != null) {
			return s.get();
		} else {
			return null;
		}
	}

	public static String formatDate(Date date, String pattern, String timeZoneString) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		if (!empty(timeZoneString)) {
			try {
				TimeZone timeZone = TimeZoneUtils.standardTimeZone(timeZoneString);
				simpleDateFormat.setTimeZone(timeZone);
			} catch (Throwable t) {
				log.warn("formatDate invalid time zone", t);
			}
		}

		return simpleDateFormat.format(date);
	}

	public static String dateToYearMonthDay(Date date, ZoneId zone) {
		if (date != null) {
			return yyyyMMddFormat.withZone(zone).format(date.toInstant());
		}

		return null;
	}

	public static String dateToYearMonth(Date date, ZoneId zone) {
		if (date != null) {
			return yyyyMMFormat.withZone(zone).format(date.toInstant());
		}

		return null;
	}

	public static String uuidToBase64(UUID uuid) {
		ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return Base64.encodeBase64String(buffer.array());

	}

	public static UUID uuidFromBase64(String str) {
		byte[] bytes = Base64.decodeBase64(str);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return new UUID(buffer.getLong(), buffer.getLong());
	}

	public static String uuidToBase32(UUID uuid) {
		ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return new Base32().encodeAsString(buffer.array());
	}

	public static UUID uuidFromBase32(String str) {
		byte[] bytes = new Base32().decode(str);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return new UUID(buffer.getLong(), buffer.getLong());
	}

	public static <V> Set<V> union(Set<V> set1, Set<V> set2) {

		Set<V> resultSet = new HashSet<>();

		if (set1 != null && set2 != null) {
			for (V v1 : set1) {
				if (set2.contains(v1)) {
					resultSet.add(v1);
				}
			}
		}

		return resultSet;
	}

	public static String toUtcTimestamp(Date date) {
		if(date != null) {
			return isoDateFormatter.format(OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC));
		}else{
			return null;
		}
	}

	public static Date fromUtcTimestamp(String utcTimestamp) {
		return new Date(OffsetDateTime.from(Utils.isoDateFormatter.parse(utcTimestamp)).toInstant().toEpochMilli());
	}

	/**
	 * We need a custom TypeAdapter for {@code StackTraceElement} because Java 9 modules made base Java classes
	 * inaccessible.
	 */
	public static final TypeAdapter<StackTraceElement> STACK_TRACE_ELEMENT_TYPE_ADAPTER = new TypeAdapter<StackTraceElement>() {
		public StackTraceElement read(JsonReader reader) throws IOException {
			String fieldName = null;

			String declaringClass = null;
			String methodName = null;
			String fileName = null;
			int lineNumber = 0;

			reader.beginObject();

			while (reader.hasNext()) {
				JsonToken token = reader.peek();

				if (token.equals(JsonToken.NAME)) {
					fieldName = reader.nextName();
				}

				if ("declaringClass".equals(fieldName)) {
					declaringClass = reader.nextString();
				} else if ("methodName".equals(fieldName)) {
					methodName = reader.nextString();
				} else if ("fileName".equals(fieldName)) {
					fileName = reader.nextString();
				} else if ("lineNumber".equals(fieldName)) {
					lineNumber = reader.nextInt();
				} else {
					// Unexpected field name. We need to advance or end up in an infinite loop
					reader.skipValue();
				}
			}

			reader.endObject();

			return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
		}

		@Override
		public void write(JsonWriter writer, StackTraceElement stackTraceElement) throws IOException {
			if (stackTraceElement != null) {
				writer.beginObject();
				writer.name("declaringClass");
				writer.value(stackTraceElement.getClassName());
				writer.name("methodName");
				writer.value(stackTraceElement.getMethodName());
				writer.name("fileName");
				writer.value(stackTraceElement.getFileName());
				writer.name("lineNumber");
				writer.value(stackTraceElement.getLineNumber());
				writer.endObject();
			} else {
				writer.nullValue();
			}
		}
	};

	public static long getMinuteSinceEpoch(Date date) {
		if(date != null) {
			return date.getTime() / 60000;
		}else{
			return -1;
		}
	}

	/**
	 * Guard against a boolean being serialized as 0|1. This is common when data is serialized with the Dynamodb mapper, and the field is not annotated as a BOOL data type.
	 * The mapper will serialize it as a number with value 0|1 and that is what the Lambda will pass over. By default, Gson doesn't know what to do with 0|1 and a boolean data type.
	 * Ideally, the field will be annotated as a BOOL type in DDB, but we want to be resilient enough to handle cases where it was not.
	 */
	public static class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
		@Override
		public JsonElement serialize(Boolean bool, Type arg1, JsonSerializationContext arg2) {
			return new JsonPrimitive(Boolean.TRUE.equals(bool));
		}

		@Override
		public Boolean deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)  {
			JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
			if (jsonPrimitive.isBoolean()) {
				return jsonPrimitive.getAsBoolean();
			} else if (jsonPrimitive.isNumber()) {
				return element.getAsInt() == 1;
			} else {
				return false;
			}
		}
	}


	public static UUID safeParseUuid(String regex, String source){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		if(matcher.matches()){
			try{
				UUID uuid = UUID.fromString(matcher.group(1));
				return uuid;
			}catch (Exception e){
			}
		}

		return null;
	}
	public static String uppercaseTrimmed(String string){
		if(!empty(string)){
			return string.trim().toUpperCase();
		}else{
			return null;
		}
	}

	public static String nameSortKey(String string){
		if(!empty(string)){
			return string.trim().toUpperCase().replaceAll("[^a-zA-Z]+","_");
		}else{
			return null;
		}
	}
}
