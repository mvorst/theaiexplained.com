# Key-Value Storage System Design

## Overview
A generic key-value storage system providing REST endpoints for storing and retrieving arbitrary JSON objects without requiring custom tables for each use case. The system uses two DynamoDB tables to support both simple key-value pairs and sorted/ordered data collections.

## Architecture

### DynamoDB Tables

#### 1. data_store Table
**Purpose**: Simple key-value storage for individual objects

**Schema**:
```
Primary Key:
- Hash Key: dataKey (String) - Format: "<applicationUuid>|<namespace>|<id>"

Attributes:
- dataKey: String (Hash Key)
- dataValue: String (JSON serialized data)
- className: String representing the Java class if deserialization is needed (optional)
- createdDate: Date (from DefaultAuditable)
- createdBySubject: String (from DefaultAuditable)
- updatedDate: Date (from DefaultAuditable)
- updatedBySubject: String (from DefaultAuditable)
```

#### 2. data_store_list Table
**Purpose**: Ordered/sorted data collections

**Schema**:
```
Composite Primary Key:
- Hash Key: dataKey (String) - Format: "<applicationUuid>|<namespace>|<id>"
- Range Key: sortKey (String) - User-defined sort key

Attributes:
- dataKey: String (Hash Key)
- sortKey: String (Range Key)
- dataValue: String (JSON serialized data)
- className: String representing the Java class if deserialization is needed (optional)
- createdDate: Date (from DefaultAuditable)
- createdBySubject: String (from DefaultAuditable)
- updatedDate: Date (from DefaultAuditable)
- updatedBySubject: String (from DefaultAuditable)
```

## REST API Endpoints

### Base Path
`/rest/api/v1/data`

### Simple Key-Value Operations (data_store)

#### 1. Create Value
**Endpoint**: `POST /rest/rest/api/v1/data/{applicationUuid}/{namespace}/{id}/`

**Request Body**:
```json
{
  "data": {
    // Any JSON object
  }
}
```

**Response** (201 Created):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123",
  "data": {
    // Stored JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

**Response** (409 Conflict): If the key already exists
```json
{
  "error": "Key already exists",
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123"
}
```

#### 2. Update Value
**Endpoint**: `PUT /rest/api/v1/data/{applicationUuid}/{namespace}/{id}`

**Request Body**:
```json
{
  "data": {
    // Any JSON object
  }
}
```

**Response** (200 OK):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123",
  "data": {
    // Updated JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

**Response** (404 Not Found): If the key doesn't exist
```json
{
  "error": "Key not found",
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123"
}
```

#### 3. Get Value
**Endpoint**: `GET /rest/api/v1/data/{applicationUuid}/{namespace}/{id}`

**Response** (200 OK):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123",
  "data": {
    // Retrieved JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

**Response** (404 Not Found):
```json
{
  "error": "Key not found",
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-preferences",
  "id": "user123"
}
```

#### 4. Delete Value
**Endpoint**: `DELETE /rest/api/v1/data/{applicationUuid}/{namespace}/{id}`

**Response** (204 No Content): Empty response on successful deletion

### Sorted/List Operations (data_store_list)

#### 5. Create List Item
**Endpoint**: `POST /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}/`

**Request Body**:
```json
{
  "data": {
    // Any JSON object
  }
}
```

**Response** (201 Created):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-activities",
  "id": "user123",
  "sortKey": "2024-01-15T10:30:00Z",
  "data": {
    // Stored JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

**Response** (409 Conflict): If the key/sortKey combination already exists
```json
{
  "error": "Item already exists",
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-activities",
  "id": "user123",
  "sortKey": "2024-01-15T10:30:00Z"
}
```

#### 6. Update List Item
**Endpoint**: `PUT /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}`

**Request Body**:
```json
{
  "data": {
    // Any JSON object
  }
}
```

**Response** (200 OK):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-activities",
  "id": "user123",
  "sortKey": "2024-01-15T10:30:00Z",
  "data": {
    // Updated JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

**Response** (404 Not Found): If the item doesn't exist
```json
{
  "error": "Item not found",
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-activities",
  "id": "user123",
  "sortKey": "2024-01-15T10:30:00Z"
}
```

#### 7. Get List Item
**Endpoint**: `GET /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}`

**Response** (200 OK):
```json
{
  "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
  "namespace": "user-activities",
  "id": "user123",
  "sortKey": "2024-01-15T10:30:00Z",
  "data": {
    // Retrieved JSON object
  },
  "createdDate": "2024-01-15T10:30:00Z",
  "createdBySubject": "user@example.com",
  "updatedDate": "2024-01-15T10:30:00Z",
  "updatedBySubject": "user@example.com"
}
```

#### 8. List Items (with Range)
**Endpoint**: `GET /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list/`

**Query Parameters**:
- `startKey` (optional): Start of range (inclusive)
- `endKey` (optional): End of range (exclusive)
- `limit` (optional, default=100, max=1000): Maximum items to return
- `sortOrder` (optional, default=ASC): ASC or DESC
- `cursor` (optional): Pagination cursor from previous response

**Response** (200 OK):
```json
{
  "list": [
    {
      "applicationUuid": "d53065bd-f932-4841-83fb-849717d8df0f",
      "namespace": "user-activities",
      "id": "user123",
      "sortKey": "2024-01-15T10:30:00Z",
      "data": {
        // JSON object
      },
      "createdDate": "2024-01-15T10:30:00Z",
      "createdBySubject": "user@example.com",
      "updatedDate": "2024-01-15T10:30:00Z",
      "updatedBySubject": "user@example.com"
    }
    // ... more items
  ],
  "cursor": "eyJza2V5IjoiMjAyNC0wMS0xNVQxMTo0..."  // Only if more results available
}
```

#### 9. Delete List Item
**Endpoint**: `DELETE /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/{sortKey}`

**Response** (204 No Content): Empty response on successful deletion

#### 10. Delete All List Items
**Endpoint**: `DELETE /rest/api/v1/data/{applicationUuid}/{namespace}/{id}/list`

**Response** (204 No Content): Empty response on successful deletion

## Data Models

### Java Entity Models

#### DataStore.java

```java
import java.util.UUID;

@DynamoDbBean
public class DataStore extends DefaultAuditable {
	private UUID applicationUuid;
	private String namespace;
	private String id;
	private String dataValue; // JSON string
	private String className; // Optional class name for deserialization

	// Transient fields for API
	@DynamoDbIgnore private Map<String, Object> data;

	// DynamoDB key generation
	@DynamoDbPartitionKey
	@DynamoDbAttribute("dataKey")
	public String getDataKey() {
		if (applicationUuid != null && namespace != null && id != null) {
			return applicationUuid.toString() + "|" + namespace + "|" + id;
		}
		return null;
	}

	public void setDataKey(String dataKey) {
		if (dataKey != null) {
			String[] parts = dataKey.split("\\|", 3);
			if (parts.length >= 3) {
				try {
					this.applicationUuid = UUID.fromString(parts[0]);
					this.namespace = parts[1];
					this.id = parts[2];
				} catch (IllegalArgumentException e) {
					// Handle invalid UUID format
				}
			}
		}
	}

	// Regular getters/setters
	public UUID getApplicationUuid() {
		return applicationUuid;
	}

	public void setApplicationUuid(UUID applicationUuid) {
		this.applicationUuid = applicationUuid;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

// Additional getters/setters...
```

#### DataStoreList.java
```java
import java.util.UUID;

@DynamoDbBean
public class DataStoreList extends DefaultAuditable {
    private UUID applicationUuid;
    private String namespace;
    private String id;
    private String sortKey;
    private String dataValue; // JSON string
    private String className; // Optional class name for deserialization
    
    // Transient fields for API
    @DynamoDbIgnore
    private Map<String, Object> data;
    
    // DynamoDB key generation
    @DynamoDbPartitionKey
    @DynamoDbAttribute("dataKey")
    public String getDataKey() {
        if (applicationUuid != null && namespace != null && id != null) {
            return applicationUuid.toString() + "|" + namespace + "|" + id;
        }
        return null;
    }
    
    public void setDataKey(String dataKey) {
        if (dataKey != null) {
            String[] parts = dataKey.split("\\|", 3);
            if (parts.length >= 3) {
                try {
                    this.applicationUuid = UUID.fromString(parts[0]);
                    this.namespace = parts[1];
                    this.id = parts[2];
                } catch (IllegalArgumentException e) {
                    // Handle invalid UUID format
                }
            }
        }
    }
    
    @DynamoDbSortKey
    public String getSortKey() {
        return sortKey;
    }
    
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }
    
    // Regular getters/setters
    public UUID getApplicationUuid() {
        return applicationUuid;
    }
    
    public void setApplicationUuid(UUID applicationUuid) {
        this.applicationUuid = applicationUuid;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    // Additional getters/setters...
}
```

**Note**: 
- Both entities extend `DefaultAuditable` which provides audit fields (createdDate, createdBySubject, updatedDate, updatedBySubject)
- The `dataKey` is dynamically generated by joining `applicationUuid`, `namespace` and `id` properties with "|"
- The `setDataKey` method parses the composite key back into namespace and id components
- These fields are automatically converted for DynamoDB using `DateAttributeConverter`

### View Models (DTOs)

#### DataStoreRequest.java
```java
public class DataStoreRequest {
    private Map<String, Object> data;
    // Getters/setters
}
```

#### DataStoreResponse.java
```java
public class DataStoreResponse {
    private String applicationUuid;
    private String namespace;
    private String id;
    private String sortKey; // Only for list items
    private Map<String, Object> data;
    private Date createdDate;
    private String createdBySubject;
    private Date updatedDate;
    private String updatedBySubject;
}
```

#### DataStoreListResponse.java
```java
public class DataStoreListResponse {
    private List<DataStoreResponse> list;
    private String cursor; // Encoded pagination cursor
    
    // Methods to check if has more results
    public boolean hasCursor() {
        return cursor != null && !cursor.isEmpty();
    }
}
```

## Implementation Classes

### Controller
`DataStoreController.java` - REST endpoint implementation

### Service
`DataStoreService.java` - Business logic, validation, and transformation

### DAOs
- `DataStoreDao.java` - Simple key-value operations
- `DataStoreListDao.java` - Sorted/list operations

### Utilities
- `DataStoreKeyBuilder.java` - Helper for building composite keys
- `DataStoreSerializer.java` - JSON serialization/deserialization

## Key Design Decisions

### 1. Composite Key Format
- Format: `<applicationUuid>|<namespace>|<id>` using pipe delimiter
- Dynamically generated from separate `applicationUuid`, `namespace` and `id` properties
- `getDataKey()` method joins properties with "|" separator
- `setDataKey()` method splits composite key back into components
- Allows efficient queries by namespace
- Simple to parse and construct

### 2. JSON Storage
- Values stored as JSON strings in DynamoDB
- Provides flexibility for arbitrary object structures
- ObjectMapper handles serialization/deserialization

### 3. Audit Trail
- All records extend `DefaultAuditable` class
- Provides audit fields: createdDate, createdBySubject, updatedDate, updatedBySubject
- Uses existing `DateAttributeConverter` for DynamoDB Date conversion
- Prepared for future access control with `userUuid`

### 4. Separation of Tables
- `data_store`: Optimized for simple key-value lookups
- `data_store_list`: Enables efficient range queries and sorting
- Avoids unnecessary range key overhead for simple use cases

### 5. Public Access
- No authentication required initially
- Structure supports adding security later via `userUuid`
- Can add JWT validation when needed

## Usage Examples

### Example 1: User Preferences
```bash
# Create user preferences
POST /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/preferences/user123/
{
  "data": {
    "theme": "dark",
    "language": "en",
    "notifications": true
  }
}

# Update user preferences
PUT /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/preferences/user123
{
  "data": {
    "theme": "light",
    "language": "en",
    "notifications": false
  }
}

# Retrieve user preferences
GET /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/preferences/user123
```

### Example 2: Activity Log
```bash
# Create activity entry (using timestamp as sort key)
POST /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/activities/user123/2024-01-15T10:30:00Z/
{
  "data": {
    "action": "login",
    "ip": "192.168.1.1"
  }
}

# Get recent activities
GET /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/activities/user123/list/?limit=50&sortOrder=DESC
```

### Example 3: Configuration Management
```bash
# Create application config
POST /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/config/app-settings/
{
  "data": {
    "maxUploadSize": 10485760,
    "enableDebug": false,
    "apiRateLimit": 1000
  }
}
```

### Example 4: Hierarchical Data
```bash
# Create hierarchical data using sortKey for hierarchy
POST /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/menu/main-nav/001-home/
{
  "data": {
    "label": "Home",
    "url": "/",
    "icon": "home"
  }
}

POST /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/menu/main-nav/002-products/
{
  "data": {
    "label": "Products",
    "url": "/products",
    "icon": "box"
  }
}

# Get menu items in order
GET /rest/api/v1/data/d53065bd-f932-4841-83fb-849717d8df0f/menu/main-nav/list/
```

## Error Handling

### HTTP Status Codes
- `200 OK`: Successful retrieval or update
- `201 Created`: New resource created (POST requests)
- `204 No Content`: Successful deletion
- `400 Bad Request`: Invalid request format or parameters
- `404 Not Found`: Key doesn't exist (PUT/GET requests)
- `409 Conflict`: Key already exists (POST requests)
- `413 Payload Too Large`: Data exceeds size limit
- `500 Internal Server Error`: Server-side error

### Error Response Format
```json
{
  "error": "Error message",
  "details": "Detailed error information",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Performance Considerations

### DynamoDB Optimization
- Composite keys enable efficient partition access
- Range keys in list table allow efficient queries
- No global secondary indexes needed initially

### Data Size Limits
- DynamoDB item size limit: 400KB
- Practical limit set to < 1MB for JSON data
- Consider compression for larger payloads if needed

### Pagination
- List operations support pagination via `nextToken`
- Default limit of 100 items, max 1000
- Prevents memory issues with large datasets

## Future Enhancements

### Phase 2 Considerations
1. **Access Control**
   - Integrate JWT authentication
   - Per-namespace permissions
   - User-specific data filtering

2. **Advanced Features**
   - TTL support for automatic expiration
   - Versioning with history tracking
   - Batch operations for efficiency
   - Search capabilities with GSI

3. **Monitoring**
   - CloudWatch metrics integration
   - Access logging
   - Performance monitoring

4. **Data Validation**
   - JSON schema validation
   - Size limit enforcement
   - Rate limiting

## Testing Strategy

### Unit Tests
- DAO layer with mocked DynamoDB
- Service layer business logic
- Controller request/response mapping

### Integration Tests
- End-to-end REST API tests
- DynamoDB Local for testing
- Various data size scenarios

### Test Scenarios
1. CRUD operations for both tables
2. Pagination and range queries
3. Error handling cases
4. Concurrent updates
5. Large payload handling

## Migration Plan

### Step 1: Create DynamoDB Tables
```javascript
// data_store table
{
  TableName: "data_store",
  KeySchema: [
    { AttributeName: "dataKey", KeyType: "HASH" }
  ],
  AttributeDefinitions: [
    { AttributeName: "dataKey", AttributeType: "S" }
  ],
  BillingMode: "PAY_PER_REQUEST"
}

// data_store_list table
{
  TableName: "data_store_list",
  KeySchema: [
    { AttributeName: "dataKey", KeyType: "HASH" },
    { AttributeName: "sortKey", KeyType: "RANGE" }
  ],
  AttributeDefinitions: [
    { AttributeName: "dataKey", AttributeType: "S" },
    { AttributeName: "sortKey", AttributeType: "S" }
  ],
  BillingMode: "PAY_PER_REQUEST"
}
```

### Step 2: Deploy Code
1. Deploy entity classes and DAOs
2. Deploy service layer
3. Deploy REST controllers
4. Update API documentation

### Step 3: Testing
1. Run integration tests
2. Performance testing
3. Load testing with expected data volumes

## Security Considerations

### Input Validation
- Namespace and ID format validation
- JSON structure validation
- Size limit enforcement

### Data Isolation
- Namespace separation ensures data isolation
- Future: Add user-level access control

### Audit Trail
- All operations logged with user and timestamp
- Enables compliance and debugging

## Conclusion

This key-value storage system provides a flexible, scalable solution for storing arbitrary JSON data without requiring custom tables. The dual-table approach balances simplicity for basic key-value needs with the power of sorted collections for more complex use cases. The design aligns with existing patterns in the codebase and provides a foundation for future enhancements.