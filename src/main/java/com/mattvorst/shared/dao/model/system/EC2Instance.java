package com.mattvorst.shared.dao.model.system;

import java.util.Date;
import java.util.Set;

import com.mattvorst.shared.constant.EnvironmentType;
import com.mattvorst.shared.constant.ServerType;
import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.dao.convert.EnvironmentTypeAttributeConverter;
import com.mattvorst.shared.dao.convert.ServerTypeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class EC2Instance {

	public static final String TABLE_NAME = "ec2_instance";

	private String instanceId;
	private Date pendingTime;
	private int buildNumber;
	private String instanceLifeCycle;
	private String instanceType;
	private String reservationId;
	private Set<String> securityGroups;
	private String accountId;
	private String architecture;
	private String availabilityZone;
	private String imageId;
	private String region;
	private String privateIp;
	private EnvironmentType environmentType;
	private ServerType serverType;
	private String description;

	@DynamoDbPartitionKey
	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getPendingTime() {
		return pendingTime;
	}

	public void setPendingTime(Date pendingTime) {
		this.pendingTime = pendingTime;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(int buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getInstanceLifeCycle() {
		return instanceLifeCycle;
	}

	public void setInstanceLifeCycle(String instanceLifeCycle) {
		this.instanceLifeCycle = instanceLifeCycle;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public Set<String> getSecurityGroups() {
		return securityGroups;
	}

	public void setSecurityGroups(Set<String> securityGroups) {
		this.securityGroups = securityGroups;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public String getAvailabilityZone() {
		return availabilityZone;
	}

	public void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPrivateIp() {
		return privateIp;
	}

	public void setPrivateIp(String privateIp) {
		this.privateIp = privateIp;
	}

	@DynamoDbConvertedBy(EnvironmentTypeAttributeConverter.class)
	public EnvironmentType getEnvironmentType() {
		return environmentType;
	}

	public void setEnvironmentType(EnvironmentType environmentType) {
		this.environmentType = environmentType;
	}

	@DynamoDbConvertedBy(ServerTypeAttributeConverter.class)
	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
