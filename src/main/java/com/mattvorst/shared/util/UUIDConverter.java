package com.mattvorst.shared.util;

import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class UUIDConverter implements Converter<String, UUID> {
	@Override
	@Nullable
	public UUID convert(String source) {
		return Utils.safeToUuid(source);
	}
}