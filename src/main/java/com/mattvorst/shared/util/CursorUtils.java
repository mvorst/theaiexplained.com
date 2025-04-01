package com.mattvorst.shared.util;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mattvorst.shared.constant.EnvironmentConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class CursorUtils {

	private static final Logger log = LoggerFactory.getLogger(CursorUtils.class);

	public static String encodeCursorFromLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey){
		if(!Utils.empty(lastEvaluatedKey)) {
			try {
				Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes fieldAttributes) {
						return false;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						if (clazz.equals(ByteBuffer.class)) {
							return true;
						}
						return false;
					}
				}).create();

				String encoded = gson.toJson(lastEvaluatedKey);

				byte[] password = Environment.getPasswordForKey(EnvironmentConstants.CURSOR_PASSWORD);
				byte[] iv = Environment.getIVForKey(EnvironmentConstants.CURSOR_IV);

				return Encryption.encryptAES256ToBase64(encoded, password, iv);
			}catch (Throwable t){
				log.warn("LAST_EVALUATED_KEY_CURSOR_NOT_CREATED", t);
				return null;
			}
		}else{
			return null;
		}
	}

	public static Map<String, AttributeValue> decodeLastEvaluatedKeyFromCursor(String cursor){
		if(!Utils.empty(cursor) && !"null".equalsIgnoreCase(cursor) && !"undefined".equalsIgnoreCase(cursor)) {
			try {
				Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes fieldAttributes) {
						return false;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						if (clazz.equals(ByteBuffer.class)) {
							return true;
						}
						return false;
					}
				}).create();

				byte[] password = Environment.getPasswordForKey(EnvironmentConstants.CURSOR_PASSWORD);
				byte[] iv = Environment.getIVForKey(EnvironmentConstants.CURSOR_IV);

				Type type = new TypeToken<Map<String, AttributeValue>>() {}.getType();

				cursor = cursor.replaceAll(" ", "+");

				Map<String, AttributeValue> lastEvaluatedKey = gson.fromJson(Encryption.decryptAES256FromBase64(cursor, password, iv), type);

				return lastEvaluatedKey;
			}catch (Throwable t){
				log.warn("LAST_EVALUATED_KEY_CURSOR_NOT_CREATED", t);
				return null;
			}
		}else{
			return null;
		}
	}

	public static String encodeCursorFromString(String cursor){
		if(!Utils.empty(cursor)) {
			try {
				byte[] password = Environment.getPasswordForKey(EnvironmentConstants.CURSOR_PASSWORD);
				byte[] iv = Environment.getIVForKey(EnvironmentConstants.CURSOR_IV);

				return Encryption.encryptAES256ToBase64(cursor, password, iv);
			}catch (Throwable t){
				log.warn("STRING_CURSOR_NOT_CREATED", t);
				return null;
			}
		}else{
			return null;
		}
	}
}
