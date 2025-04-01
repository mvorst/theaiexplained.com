package com.mattvorst.shared.model;

import java.util.Date;

public interface Auditable {
	String getUpdatedBySubject();
	void setUpdatedBySubject(String updatedBySubject);

	Date getUpdatedDate();
	void setUpdatedDate(Date updatedDate);

	String getCreatedBySubject();
	void setCreatedBySubject(String createdBySubject);

	Date getCreatedDate();
	void setCreatedDate(Date createdDate);
}
