package com.mattvorst.shared.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface CursorResultList<T>  {
	List<T> getList();
	void setList(List<T> list);
	boolean hasCursor();

	@JsonGetter
	String getCursor();
}
