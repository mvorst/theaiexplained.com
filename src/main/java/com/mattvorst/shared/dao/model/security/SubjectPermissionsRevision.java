package com.mattvorst.shared.dao.model.security;

import java.util.List;
import java.util.UUID;

import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.security.constant.SubjectType;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class SubjectPermissionsRevision extends DefaultAuditable {
	private String subjectUuidAndType;
	private long savedDate;

	private UUID subjectUuid;
	private SubjectType type;

	List<SubjectPermission> permissionList;

	public UUID getSubjectUuid() {
		return subjectUuid;
	}

	public void setSubjectUuid(UUID subjectUuid) {
		this.subjectUuid = subjectUuid;
	}

	public SubjectType getType() {
		return type;
	}

	public void setType(SubjectType type) {
		this.type = type;
	}

	@DynamoDbPartitionKey
	public String getSubjectUuidAndType() {
		if (this.subjectUuid != null && this.type != null) {
			return this.subjectUuid + "|" + this.type;
		} else {
			return null;
		}
	}

	public void setSubjectUuidAndType(String subjectUuidAndType) {
		if (subjectUuidAndType != null) {
			String[] components = subjectUuidAndType.split("\\|");
			this.subjectUuid = UUID.fromString(components[0]);
			this.type = SubjectType.valueOf(components[1]);
		}

		this.subjectUuidAndType = subjectUuidAndType;
	}

	@DynamoDbSortKey
	public long getSavedDate() {
		return savedDate;
	}

	public void setSavedDate(long savedDate) {
		this.savedDate = savedDate;
	}

	public List<SubjectPermission> getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(List<SubjectPermission> permissionList) {
		this.permissionList = permissionList;
	}
}
