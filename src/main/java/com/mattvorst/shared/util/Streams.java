package com.mattvorst.shared.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class Streams {
	public static <T> Stream<T> of(Collection<T> collection) {
		return Optional.ofNullable(collection)
				.map(Collection::stream)
				.orElseGet(Stream::empty);
	}
}
