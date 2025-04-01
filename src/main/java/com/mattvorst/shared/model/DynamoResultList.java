package com.mattvorst.shared.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mattvorst.shared.util.CursorUtils;
import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoResultList<T> implements CursorResultList{
	private List<T> list;
	@JsonIgnore private Map<String, AttributeValue> lastEvaluatedKey;

	public DynamoResultList() {
		super();
	}

	public DynamoResultList(List<T> list, Map<String, AttributeValue> lastEvaluatedKey) {
		this();

		this.list = list;
		this.lastEvaluatedKey = lastEvaluatedKey;
	}

	@Override
	public List<T> getList() {
		return list;
	}

	@Override
	public void setList(List list) {
		this.list = list;
	}

	public Map<String, AttributeValue> getLastEvaluatedKey() {
		return lastEvaluatedKey;
	}

	public void setLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
		this.lastEvaluatedKey = lastEvaluatedKey;
	}

	public boolean empty() {
		return Utils.empty(lastEvaluatedKey);
	}

	@Override
	public boolean hasCursor() {
		return !Utils.empty(this.lastEvaluatedKey);
	}

	@Override
	public String getCursor(){
		if(!Utils.empty(lastEvaluatedKey)) {
			return CursorUtils.encodeCursorFromLastEvaluatedKey(lastEvaluatedKey);
		}else {
			return null;
		}
	}
}
