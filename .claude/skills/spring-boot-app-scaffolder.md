# Spring Boot App Scaffolder

You are an expert Spring Boot application scaffolder. Your task is to help developers create new Spring Boot applications following the established patterns from theBridgeToAI.com codebase.

## Application Architecture

The target architecture uses:
- **Spring Boot 4.0** with **Spring Framework 7.0**
- **Java 17**
- **Gradle 8.14.3** for build automation
- **AWS DynamoDB** with Enhanced Async Client for data persistence
- **Spring Security 7.0** with OAuth2/JWT authentication
- **Async/CompletableFuture** pattern throughout
- **Multi-module source structure** (main/java, util/java, proto/java, test/java)

---

## Directory Structure Template

When creating a new Spring Boot application, follow this structure:

```
project-root/
├── gradle/                          # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.{company}.{project}/
│   │   │   │   ├── Application.java           # Main Spring Boot class
│   │   │   │   ├── async/
│   │   │   │   │   └── processor/
│   │   │   │   │       └── AppTaskProcessor.java
│   │   │   │   ├── config/                    # Configuration classes
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── TomcatConfig.java      # If using AJP
│   │   │   │   │   └── WebConfig.java
│   │   │   │   ├── constant/                  # Enums and constants
│   │   │   │   │   └── {EntityType}.java
│   │   │   │   ├── controller/                # REST controllers
│   │   │   │   │   ├── AdminController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── {Entity}Controller.java
│   │   │   │   ├── dao/                       # Data Access Objects
│   │   │   │   │   ├── {Entity}Dao.java
│   │   │   │   │   ├── convert/               # DynamoDB converters
│   │   │   │   │   │   └── {Type}AttributeConverter.java
│   │   │   │   │   └── model/                 # DynamoDB entities
│   │   │   │   │       └── {Entity}.java
│   │   │   │   ├── model/                     # View models (DTOs)
│   │   │   │   │   └── View{Entity}.java
│   │   │   │   ├── service/                   # Business logic layer
│   │   │   │   │   └── {Entity}Service.java
│   │   │   │   ├── task/                      # Background async tasks
│   │   │   │   │   └── {Entity}Task.java
│   │   │   │   └── util/                      # Utility classes
│   │   │   └── com.{company}.shared/          # Shared framework (optional)
│   │   │       ├── async/
│   │   │       ├── constant/
│   │   │       ├── controller/
│   │   │       │   └── BaseRestController.java
│   │   │       ├── dao/
│   │   │       │   ├── BaseDao.java
│   │   │       │   └── convert/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       │   ├── Auditable.java
│   │   │       │   └── DefaultAuditable.java
│   │   │       ├── security/
│   │   │       │   └── service/
│   │   │       │       └── JwtService.java
│   │   │       └── util/
│   │   │           ├── FieldValidator.java
│   │   │           └── DynamoDbUtils.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── messages.properties             # i18n messages
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── jsp/                        # JSP templates (if needed)
│   ├── test/
│   │   └── java/
│   │       └── com.{company}.{project}/
│   │           ├── controller/
│   │           ├── dao/
│   │           └── service/
│   │               └── {Entity}ServiceTest.java
│   ├── util/java/                              # Deployment utilities (optional)
│   │   └── com.{company}.deploy.main/
│   └── proto/java/                             # Prototyping code (optional)
│       └── com.{company}.prototype/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── CLAUDE.md                                    # Project instructions
```

---

## Naming Conventions

### Package Names
- **Lowercase only**, no underscores
- **Hierarchical by function**: `dao.model`, `dao.convert`, `security.service`
- **Company package**: `com.{company}.{project}.*`
- **Shared framework**: `com.{company}.shared.*`

### Class Names

| Type | Pattern | Examples |
|------|---------|----------|
| Main Application | `Application.java` | `Application` |
| Controllers | `*Controller.java` | `ContentController`, `AuthController` |
| Services | `*Service.java` | `ContentService`, `EmailService` |
| DAOs | `*Dao.java` | `ContentDao`, `UserDao` |
| Entities | Descriptive nouns | `Content`, `User`, `Newsletter` |
| View Models | `View*` prefix | `ViewContent`, `ViewUser` |
| Config Classes | `*Config.java` | `SecurityConfig`, `WebConfig` |
| Async Tasks | `*Task.java` | `ContentUpdateTask`, `EmailSendTask` |
| Converters | `*AttributeConverter.java` | `DateAttributeConverter` |
| Enums/Types | Descriptive `*Type` | `ContentCategoryType`, `Status` |
| Exceptions | `*Exception.java` | `ValidationException` |
| Utils | `*Utils.java` | `DynamoDbUtils`, `CursorUtils` |

### Method Names

| Type | Pattern | Examples |
|------|---------|----------|
| Create | `create*()`, `save*()` | `createContent()`, `saveContent()` |
| Read | `get*()`, `get*By*()` | `getContent()`, `getContentByType()` |
| Update | `update*()`, `save*()` | `updateContent()` |
| Delete | `delete*()` | `deleteContent()` |
| List/Query | `get*List()`, `getAll*()` | `getContentList()`, `getAllContent()` |
| Validation | `validate*()` | `validateContent()` |
| Boolean checks | `is*()`, `has*()` | `isFeatured()`, `hasPermission()` |

### Variable Names
- **camelCase** for all variables
- **Autowired fields**: Match service/DAO name (e.g., `private ContentService contentService;`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `public static final String TABLE_NAME = "content";`)
- **Method parameters**: Match field/property names exactly

---

## Code Templates

### 1. Main Application Class

```java
package com.{company}.{project};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
```

---

### 2. Entity (DynamoDB Model)

```java
package com.{company}.{project}.dao.model;

import java.util.Date;
import java.util.UUID;

import com.{company}.shared.dao.convert.DateAttributeConverter;
import com.{company}.shared.model.DefaultAuditable;
import com.{company}.shared.util.Utils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * {Entity} entity representing a DynamoDB table.
 * Extends DefaultAuditable for automatic audit field management (createdDate, updatedDate, etc.)
 */
@DynamoDbBean
public class {Entity} extends DefaultAuditable {
    public static final String TABLE_NAME = "{entity_lowercase}";

    private UUID {entity}Uuid;
    private String name;
    private {EntityType} {entity}Type;
    private Date createdDate;

    // Constructors
    public {Entity}() {}

    public {Entity}(UUID {entity}Uuid) {
        this.{entity}Uuid = {entity}Uuid;
    }

    // Primary Key
    @DynamoDbPartitionKey
    public UUID get{Entity}Uuid() {
        return {entity}Uuid;
    }

    public void set{Entity}Uuid(UUID {entity}Uuid) {
        this.{entity}Uuid = {entity}Uuid;
    }

    // Standard Fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // GSI Partition Key (for queries by type)
    @DynamoDbSecondaryPartitionKey(indexNames = {"{entity}Type-createdDate-index"})
    @DynamoDbConvertedBy({EntityType}AttributeConverter.class)
    public {EntityType} get{Entity}Type() {
        return {entity}Type;
    }

    public void set{Entity}Type({EntityType} {entity}Type) {
        this.{entity}Type = {entity}Type;
    }

    // GSI Sort Key (for sorting by date)
    @DynamoDbConvertedBy(DateAttributeConverter.class)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Computed Composite Sort Key (timestamp|uuid for unique ordering)
    @DynamoDbSecondarySortKey(indexNames = {"{entity}Type-createdDateAnd{Entity}Uuid-index"})
    public String getCreatedDateAnd{Entity}Uuid() {
        return Utils.toUtcTimestamp(createdDate) + "|" + {entity}Uuid;
    }

    public void setCreatedDateAnd{Entity}Uuid(String createdDateAnd{Entity}Uuid) {
        // Required setter but not used (computed field)
    }
}
```

**Key Points:**
- Extend `DefaultAuditable` for audit fields
- Define `TABLE_NAME` constant
- Use `@DynamoDbPartitionKey` for primary key
- Use `@DynamoDbSecondaryPartitionKey` and `@DynamoDbSecondarySortKey` for GSI
- Use `@DynamoDbConvertedBy` for custom type conversion (enums, dates)
- Composite sort keys use pattern: `timestamp|uuid` for unique ordering

---

### 3. View Model (DTO)

```java
package com.{company}.{project}.model;

import java.util.Date;
import java.util.UUID;

import com.{company}.{project}.dao.model.{Entity};
import org.springframework.beans.BeanUtils;

/**
 * View model (DTO) for {Entity}.
 * Used for API requests/responses to decouple entity from API contract.
 */
public class View{Entity} {
    private UUID {entity}Uuid;
    private String name;
    private {EntityType} {entity}Type;
    private Date createdDate;

    // No-arg constructor
    public View{Entity}() {}

    // Entity constructor for conversion
    public View{Entity}({Entity} {entity}) {
        if ({entity} != null) {
            BeanUtils.copyProperties({entity}, this);
        }
    }

    // Getters and Setters
    public UUID get{Entity}Uuid() {
        return {entity}Uuid;
    }

    public void set{Entity}Uuid(UUID {entity}Uuid) {
        this.{entity}Uuid = {entity}Uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public {EntityType} get{Entity}Type() {
        return {entity}Type;
    }

    public void set{Entity}Type({EntityType} {entity}Type) {
        this.{entity}Type = {entity}Type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
```

**Key Points:**
- Prefix with `View`
- Plain POJOs (no annotations)
- Constructor accepting entity for conversion
- Use `BeanUtils.copyProperties()` for entity-to-DTO mapping

---

### 4. DAO (Data Access Object)

```java
package com.{company}.{project}.dao;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.{company}.shared.dao.BaseDao;
import com.{company}.shared.model.DynamoResultList;
import com.{company}.shared.security.AuthorizationUtils;
import com.{company}.shared.util.DynamoDbUtils;
import com.{company}.{project}.dao.model.{Entity};
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Data Access Object for {Entity}.
 * All methods return CompletableFuture for async operations.
 */
@Component
public class {Entity}Dao extends BaseDao {

    public {Entity}Dao() {
        super();
    }

    public {Entity}Dao(String profile) {
        super(profile);
    }

    /**
     * Get a single {entity} by UUID.
     * @param {entity}Uuid The UUID of the {entity}
     * @return CompletableFuture with the {Entity} or null if not found
     */
    public CompletableFuture<{Entity}> get{Entity}(UUID {entity}Uuid) {
        DynamoDbAsyncTable<{Entity}> table = dynamoDbEnhancedAsyncClient.table(
            {Entity}.TABLE_NAME,
            TableSchema.fromBean({Entity}.class)
        );
        return table.getItem(GetItemEnhancedRequest.builder()
            .key(Key.builder().partitionValue({entity}Uuid.toString()).build())
            .consistentRead(true)
            .build()
        );
    }

    /**
     * Save (create or update) a {entity}.
     * Automatically updates audit properties before saving.
     * @param {entity} The {Entity} to save
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> save{Entity}({Entity} {entity}) {
        DynamoDbAsyncTable<{Entity}> table = dynamoDbEnhancedAsyncClient.table(
            {Entity}.TABLE_NAME,
            TableSchema.fromBean({Entity}.class)
        );
        AuthorizationUtils.updateAuditProperties({entity});
        return table.putItem({entity});
    }

    /**
     * Delete a {entity}.
     * @param {entity} The {Entity} to delete
     * @return CompletableFuture with the deleted {Entity}
     */
    public CompletableFuture<{Entity}> delete{Entity}({Entity} {entity}) {
        DynamoDbAsyncTable<{Entity}> table = dynamoDbEnhancedAsyncClient.table(
            {Entity}.TABLE_NAME,
            TableSchema.fromBean({Entity}.class)
        );
        return table.deleteItem({entity});
    }

    /**
     * Get all {entity}s with pagination.
     * @param pageSize Maximum number of items to return
     * @param exclusiveStartKey Last evaluated key for pagination (null for first page)
     * @return CompletableFuture with DynamoResultList
     */
    public CompletableFuture<DynamoResultList<{Entity}>> getAll{Entity}s(
            int pageSize,
            Map<String, AttributeValue> exclusiveStartKey) {

        DynamoDbAsyncTable<{Entity}> table = dynamoDbEnhancedAsyncClient.table(
            {Entity}.TABLE_NAME,
            TableSchema.fromBean({Entity}.class)
        );

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
            .limit(pageSize)
            .exclusiveStartKey(exclusiveStartKey)
            .build();

        return DynamoDbUtils.queryWithPagination(table, request);
    }

    /**
     * Query {entity}s by type with pagination.
     * Uses GSI for efficient querying.
     * @param {entity}Type The type to query by
     * @param pageSize Maximum number of items to return
     * @param exclusiveStartKey Last evaluated key for pagination
     * @return CompletableFuture with DynamoResultList
     */
    public CompletableFuture<DynamoResultList<{Entity}>> get{Entity}ListByType(
            {EntityType} {entity}Type,
            int pageSize,
            Map<String, AttributeValue> exclusiveStartKey) {

        DynamoDbAsyncTable<{Entity}> table = dynamoDbEnhancedAsyncClient.table(
            {Entity}.TABLE_NAME,
            TableSchema.fromBean({Entity}.class)
        );
        DynamoDbAsyncIndex<{Entity}> index = table.index("{entity}Type-createdDate-index");

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(
                Key.builder().partitionValue({entity}Type.toString()).build())
            )
            .limit(pageSize)
            .exclusiveStartKey(exclusiveStartKey)
            .scanIndexForward(false) // Descending order (newest first)
            .build();

        return DynamoDbUtils.queryWithPagination(index, request);
    }
}
```

**Key Points:**
- Extend `BaseDao` (provides DynamoDB clients)
- Annotate with `@Component`
- All methods return `CompletableFuture<T>`
- Use `DynamoDbEnhancedAsyncClient` from base class
- Call `AuthorizationUtils.updateAuditProperties()` before saves
- Use GSI for non-primary-key queries
- Use `DynamoDbUtils.queryWithPagination()` for paginated results
- Include JavaDoc comments for all public methods

---

### 5. Service Layer

```java
package com.{company}.{project}.service;

import java.util.Map;
import java.util.UUID;

import com.{company}.shared.exception.ValidationException;
import com.{company}.shared.model.DynamoResultList;
import com.{company}.shared.util.FieldValidator;
import com.{company}.{project}.async.processor.AppTaskProcessor;
import com.{company}.{project}.dao.{Entity}Dao;
import com.{company}.{project}.dao.model.{Entity};
import com.{company}.{project}.model.View{Entity};
import com.{company}.{project}.task.{Entity}UpdateTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Service layer for {Entity} business logic.
 * Handles validation, DTO conversion, and orchestration.
 */
@Service
public class {Entity}Service {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AppTaskProcessor appTaskProcessor;

    @Autowired
    private {Entity}Dao {entity}Dao;

    /**
     * Get a {entity} by UUID.
     * @param {entity}Uuid The UUID of the {entity}
     * @return The {Entity} or null if not found
     */
    public {Entity} get{Entity}(UUID {entity}Uuid) {
        return {entity}Dao.get{Entity}({entity}Uuid).join();
    }

    /**
     * Create a new {entity}.
     * @param view{Entity} The view model containing {entity} data
     * @return The created {Entity}
     * @throws ValidationException if validation fails
     */
    public {Entity} create{Entity}(View{Entity} view{Entity}) throws ValidationException {
        validate{Entity}(view{Entity});

        // Generate UUID (with collision check)
        int count = 0;
        do {
            UUID {entity}Uuid = UUID.randomUUID();
            {Entity} existing = {entity}Dao.get{Entity}({entity}Uuid).join();
            if (existing == null) {
                view{Entity}.set{Entity}Uuid({entity}Uuid);
                break;
            }
        } while (count++ < 10);

        // Convert DTO to entity
        {Entity} {entity} = new {Entity}();
        BeanUtils.copyProperties(view{Entity}, {entity});

        // Save to database
        {entity}Dao.save{Entity}({entity}).join();

        // Trigger async post-processing
        appTaskProcessor.processLocally(
            new {Entity}UpdateTask.Parameters({entity}.get{Entity}Uuid())
        );

        return {entity};
    }

    /**
     * Update an existing {entity}.
     * @param {entity}Uuid The UUID of the {entity} to update
     * @param view{Entity} The view model containing updated data
     * @return The updated {Entity} or null if not found
     * @throws ValidationException if validation fails
     */
    public {Entity} update{Entity}(UUID {entity}Uuid, View{Entity} view{Entity})
            throws ValidationException {
        validate{Entity}(view{Entity});

        // Get existing entity
        {Entity} {entity} = get{Entity}({entity}Uuid);
        if ({entity} == null) {
            return null;
        }

        // Update fields (exclude UUID and audit fields)
        BeanUtils.copyProperties(view{Entity}, {entity},
            "{entity}Uuid", "createdDate", "createdBySubject");

        // Save changes
        {entity}Dao.save{Entity}({entity}).join();

        // Trigger async post-processing
        appTaskProcessor.processLocally(
            new {Entity}UpdateTask.Parameters({entity}.get{Entity}Uuid())
        );

        return {entity};
    }

    /**
     * Delete a {entity}.
     * @param {entity}Uuid The UUID of the {entity} to delete
     * @return The deleted {Entity} or null if not found
     */
    public {Entity} delete{Entity}(UUID {entity}Uuid) {
        {Entity} {entity} = get{Entity}({entity}Uuid);
        if ({entity} != null) {
            {entity}Dao.delete{Entity}({entity}).join();
        }
        return {entity};
    }

    /**
     * Get all {entity}s with pagination.
     * @param count Maximum number of items to return
     * @param attributeValueMap Last evaluated key for pagination
     * @return DynamoResultList with {entity}s
     */
    public DynamoResultList<{Entity}> getAll{Entity}s(
            int count,
            Map<String, AttributeValue> attributeValueMap) {
        return {entity}Dao.getAll{Entity}s(count, attributeValueMap).join();
    }

    /**
     * Validate {entity} data.
     * @param view{Entity} The view model to validate
     * @throws ValidationException if validation fails
     */
    private void validate{Entity}(View{Entity} view{Entity}) throws ValidationException {
        FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
            .validateNotEmpty("name", view{Entity}.getName())
            .validateStringLength("name", view{Entity}.getName(), 1, 200)
            .validateNotNull("{entity}Type", view{Entity}.get{Entity}Type())
            .apply();
    }
}
```

**Key Points:**
- Annotate with `@Service`
- Use `.join()` to block on async DAO calls
- Validate input using `FieldValidator` fluent API
- Use `BeanUtils.copyProperties()` for DTO-entity conversion
- Trigger background tasks for post-processing
- Throw `ValidationException` for validation errors
- Include JavaDoc comments for all public methods

---

### 6. REST Controller

```java
package com.{company}.{project}.controller;

import java.util.Map;
import java.util.UUID;

import com.{company}.shared.controller.BaseRestController;
import com.{company}.shared.exception.ValidationException;
import com.{company}.shared.model.DynamoResultList;
import com.{company}.shared.util.CursorUtils;
import com.{company}.{project}.dao.model.{Entity};
import com.{company}.{project}.model.View{Entity};
import com.{company}.{project}.service.{Entity}Service;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * REST API controller for {Entity} operations.
 * All endpoints are versioned under /rest/api/{{version}}/{entity}.
 */
@RestController
@RequestMapping("/rest/api/{version}/{entity}")
public class {Entity}Controller extends BaseRestController {

    @Autowired
    private {Entity}Service {entity}Service;

    /**
     * Get a single {entity} by UUID.
     * GET /rest/api/v1/{entity}/{{uuid}}
     */
    @GetMapping("/{{entity}Uuid}")
    public ResponseEntity<View{Entity}> get{Entity}(@PathVariable UUID {entity}Uuid) {
        {Entity} {entity} = {entity}Service.get{Entity}({entity}Uuid);
        if ({entity} == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new View{Entity}({entity}));
    }

    /**
     * Create a new {entity}.
     * POST /rest/api/v1/{entity}/
     */
    @PostMapping("/")
    public ResponseEntity<View{Entity}> create{Entity}(@RequestBody View{Entity} view{Entity})
            throws ValidationException {
        {Entity} {entity} = {entity}Service.create{Entity}(view{Entity});
        return ResponseEntity.status(HttpStatus.CREATED).body(new View{Entity}({entity}));
    }

    /**
     * Update an existing {entity}.
     * PUT /rest/api/v1/{entity}/{{uuid}}
     */
    @PutMapping("/{{entity}Uuid}")
    public ResponseEntity<View{Entity}> update{Entity}(
            @PathVariable UUID {entity}Uuid,
            @RequestBody View{Entity} view{Entity}) throws ValidationException {

        {Entity} {entity} = {entity}Service.update{Entity}({entity}Uuid, view{Entity});
        if ({entity} == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new View{Entity}({entity}));
    }

    /**
     * Delete a {entity}.
     * DELETE /rest/api/v1/{entity}/{{uuid}}
     */
    @DeleteMapping("/{{entity}Uuid}")
    public ResponseEntity<Void> delete{Entity}(@PathVariable UUID {entity}Uuid) {
        {Entity} {entity} = {entity}Service.delete{Entity}({entity}Uuid);
        if ({entity} == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all {entity}s with cursor-based pagination.
     * GET /rest/api/v1/{entity}/?cursor={{cursor}}&count={{count}}
     */
    @GetMapping("/")
    public ResponseEntity<DynamoResultList<View{Entity}>> getAll{Entity}s(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "10") int count) {

        Map<String, AttributeValue> lastKey =
            CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);

        DynamoResultList<{Entity}> result = {entity}Service.getAll{Entity}s(count, lastKey);

        return ResponseEntity.ok(new DynamoResultList<>(
            Streams.of(result.getList()).map(View{Entity}::new).toList(),
            result.getLastEvaluatedKey()
        ));
    }
}
```

**Key Points:**
- Extend `BaseRestController` for centralized exception handling
- Annotate with `@RestController` and `@RequestMapping`
- Version in URL: `/rest/api/{version}/`
- Return `ResponseEntity<ViewObject>`
- Convert entities to view models before returning
- Support cursor-based pagination
- Handle null checks explicitly (404 for not found)
- Use standard HTTP status codes (200, 201, 204, 404)

---

### 7. Async Task

```java
package com.{company}.{project}.task;

import java.util.UUID;

import com.{company}.shared.async.model.AbstractTaskParameters;
import com.{company}.shared.async.model.AsyncTask;
import com.{company}.shared.async.model.QueueRunnable;
import com.{company}.{project}.dao.{Entity}Dao;
import com.{company}.{project}.dao.model.{Entity};
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Background task for processing {entity} updates.
 * Executed asynchronously via AppTaskProcessor.
 */
@AsyncTask("{Entity}UpdateTask")
public class {Entity}UpdateTask extends QueueRunnable {

    private static final Logger log = LogManager.getLogger({Entity}UpdateTask.class);

    private final {Entity}Dao {entity}Dao;
    private final Parameters parameters;

    /**
     * No-arg constructor required for deserialization.
     */
    public {Entity}UpdateTask() {
        super();
        this.{entity}Dao = null;
        this.parameters = null;
    }

    /**
     * Constructor with dependencies for execution.
     */
    public {Entity}UpdateTask({Entity}Dao {entity}Dao, Parameters parameters) {
        super();
        this.{entity}Dao = {entity}Dao;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        try {
            log.info("Processing {entity} update: {}", parameters.get{Entity}Uuid());

            {Entity} {entity} = {entity}Dao.get{Entity}(parameters.get{Entity}Uuid()).get();

            if ({entity} != null) {
                // Perform background processing
                // Example: update search index, send notifications, etc.
                log.info("Successfully processed {entity}: {}", {entity}.get{Entity}Uuid());
            } else {
                log.warn("{Entity} not found: {}", parameters.get{Entity}Uuid());
            }

        } catch (Exception e) {
            log.error("Error processing {entity} update", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Task parameters for serialization/deserialization.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameters extends AbstractTaskParameters {

        private UUID {entity}Uuid;
        private long createTime;

        /**
         * No-arg constructor required for deserialization.
         */
        public Parameters() {
            super();
        }

        /**
         * Constructor for task creation.
         */
        public Parameters(UUID {entity}Uuid) {
            this();
            this.{entity}Uuid = {entity}Uuid;
            this.createTime = System.currentTimeMillis();
        }

        public UUID get{Entity}Uuid() {
            return {entity}Uuid;
        }

        public void set{Entity}Uuid(UUID {entity}Uuid) {
            this.{entity}Uuid = {entity}Uuid;
        }

        @Override
        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }
    }
}
```

**Key Points:**
- Annotate with `@AsyncTask("TaskName")`
- Extend `QueueRunnable`
- Implement `run()` method
- Inner static `Parameters` class extends `AbstractTaskParameters`
- No-arg constructor required (for deserialization)
- Constructor with dependencies for execution
- Invoked via `appTaskProcessor.processLocally(parameters)`

---

### 8. JUnit Test

```java
package com.{company}.{project}.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.{company}.shared.exception.ValidationException;
import com.{company}.shared.model.DynamoResultList;
import com.{company}.{project}.async.processor.AppTaskProcessor;
import com.{company}.{project}.dao.{Entity}Dao;
import com.{company}.{project}.dao.model.{Entity};
import com.{company}.{project}.model.View{Entity};
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

/**
 * Unit tests for {Entity}Service.
 */
@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private AppTaskProcessor appTaskProcessor;

    @Mock
    private {Entity}Dao {entity}Dao;

    @InjectMocks
    private {Entity}Service {entity}Service;

    private UUID test{Entity}Uuid;
    private {Entity} test{Entity};
    private View{Entity} testView{Entity};

    @BeforeEach
    void setUp() {
        test{Entity}Uuid = UUID.randomUUID();

        test{Entity} = new {Entity}();
        test{Entity}.set{Entity}Uuid(test{Entity}Uuid);
        test{Entity}.setName("Test {Entity}");

        testView{Entity} = new View{Entity}();
        testView{Entity}.set{Entity}Uuid(test{Entity}Uuid);
        testView{Entity}.setName("Test {Entity}");
    }

    @Test
    void get{Entity}_Returns{Entity}_WhenExists() {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(test{Entity}));

        {Entity} result = {entity}Service.get{Entity}(test{Entity}Uuid);

        assertNotNull(result);
        assertEquals(test{Entity}Uuid, result.get{Entity}Uuid());
        verify({entity}Dao).get{Entity}(test{Entity}Uuid);
    }

    @Test
    void get{Entity}_ReturnsNull_WhenNotExists() {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(null));

        {Entity} result = {entity}Service.get{Entity}(test{Entity}Uuid);

        assertNull(result);
        verify({entity}Dao).get{Entity}(test{Entity}Uuid);
    }

    @Test
    void create{Entity}_Creates{Entity}_WhenValidInput() throws ValidationException {
        when({entity}Dao.get{Entity}(any(UUID.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when({entity}Dao.save{Entity}(any({Entity}.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        {Entity} result = {entity}Service.create{Entity}(testView{Entity});

        assertNotNull(result);
        assertEquals(testView{Entity}.getName(), result.getName());
        verify({entity}Dao).save{Entity}(any({Entity}.class));
        verify(appTaskProcessor).processLocally(any());
    }

    @Test
    void update{Entity}_Updates{Entity}_WhenExists() throws ValidationException {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(test{Entity}));
        when({entity}Dao.save{Entity}(any({Entity}.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        testView{Entity}.setName("Updated {Entity}");
        {Entity} result = {entity}Service.update{Entity}(test{Entity}Uuid, testView{Entity});

        assertNotNull(result);
        assertEquals("Updated {Entity}", result.getName());
        verify({entity}Dao).save{Entity}(any({Entity}.class));
        verify(appTaskProcessor).processLocally(any());
    }

    @Test
    void update{Entity}_ReturnsNull_WhenNotExists() throws ValidationException {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(null));

        {Entity} result = {entity}Service.update{Entity}(test{Entity}Uuid, testView{Entity});

        assertNull(result);
        verify({entity}Dao, never()).save{Entity}(any({Entity}.class));
        verify(appTaskProcessor, never()).processLocally(any());
    }

    @Test
    void delete{Entity}_Deletes{Entity}_WhenExists() {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(test{Entity}));
        when({entity}Dao.delete{Entity}(test{Entity}))
            .thenReturn(CompletableFuture.completedFuture(test{Entity}));

        {Entity} result = {entity}Service.delete{Entity}(test{Entity}Uuid);

        assertNotNull(result);
        assertEquals(test{Entity}Uuid, result.get{Entity}Uuid());
        verify({entity}Dao).delete{Entity}(test{Entity});
    }

    @Test
    void delete{Entity}_ReturnsNull_WhenNotExists() {
        when({entity}Dao.get{Entity}(test{Entity}Uuid))
            .thenReturn(CompletableFuture.completedFuture(null));

        {Entity} result = {entity}Service.delete{Entity}(test{Entity}Uuid);

        assertNull(result);
        verify({entity}Dao, never()).delete{Entity}(any({Entity}.class));
    }

    @Test
    void getAll{Entity}s_Returns{Entity}List() {
        DynamoResultList<{Entity}> mockResultList = new DynamoResultList<>();
        when({entity}Dao.getAll{Entity}s(anyInt(), any()))
            .thenReturn(CompletableFuture.completedFuture(mockResultList));

        DynamoResultList<{Entity}> result = {entity}Service.getAll{Entity}s(10, Map.of());

        assertNotNull(result);
        verify({entity}Dao).getAll{Entity}s(10, Map.of());
    }
}
```

**Key Points:**
- Use `@ExtendWith(MockitoExtension.class)`
- Use `@Mock` for dependencies
- Use `@InjectMocks` for class under test
- Test naming: `methodName_expectedBehavior_whenCondition`
- Return `CompletableFuture.completedFuture()` from mocked DAO calls
- Test both success and failure paths

---

### 9. Attribute Converter (for Enums)

```java
package com.{company}.{project}.dao.convert;

import com.{company}.{project}.constant.{EntityType};
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * DynamoDB attribute converter for {EntityType} enum.
 * Converts between {EntityType} and DynamoDB String (S) type.
 */
public class {EntityType}AttributeConverter implements AttributeConverter<{EntityType}> {

    @Override
    public AttributeValue transformFrom({EntityType} input) {
        return AttributeValue.builder().s(input.toString()).build();
    }

    @Override
    public {EntityType} transformTo(AttributeValue input) {
        return {EntityType}.valueOf(input.s());
    }

    @Override
    public EnhancedType<{EntityType}> type() {
        return EnhancedType.of({EntityType}.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
```

**Key Points:**
- Implement `AttributeConverter<T>`
- Convert Java types to DynamoDB `AttributeValue`
- Typically used for enums, dates, complex types
- Referenced via `@DynamoDbConvertedBy(Converter.class)` on entity getters

---

### 10. Security Configuration

```java
package com.{company}.{project}.config;

import java.security.interfaces.RSAPublicKey;

import com.nimbusds.jose.jwk.JWKSource;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.{company}.shared.security.service.JwtService;
import com.{company}.shared.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration with JWT authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey rsaPublicKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/*.action").permitAll()
                .requestMatchers("/rest/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> {
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder(jwkSource())));
            })
            .userDetailsService(userDetailsService());

        return http.build();
    }

    @Bean
    public JwtService jwtService(
            RSAPublicKey rsaPublicKey,
            JWKSource<SecurityContext> jwkSource,
            AuthorizationServerSettings authorizationServerSettings) {

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);

        return new JwtService(jwtDecoder, jwtEncoder, authorizationServerSettings);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // Configure JWK source
        // Implementation depends on your key management strategy
        return null; // Replace with actual implementation
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}
```

**Key Points:**
- Annotate with `@Configuration` and `@EnableWebSecurity`
- Use Spring Security 7.0 lambda DSL
- JWT-based authentication with RSA keys
- Stateless session management
- Configure public/protected endpoints

---

### 11. build.gradle

```gradle
plugins {
    id 'java'
    id 'war'
    id 'org.springframework.boot' version '4.0.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.{company}'
version = '1.0.0-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-jackson2'

    // AWS SDK v2
    implementation platform('software.amazon.awssdk:bom:2.20.0')
    implementation 'software.amazon.awssdk:dynamodb-enhanced'
    implementation 'software.amazon.awssdk:s3'
    implementation 'software.amazon.awssdk:ses'
    implementation 'software.amazon.awssdk:sqs'

    // Logging
    implementation 'org.apache.logging.log4j:log4j-core:2.20.0'
    implementation 'org.apache.logging.log4j:log4j-api:2.20.0'

    // Utilities
    implementation 'org.apache.commons:commons-lang3:3.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.3'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.3.1'

    // Tomcat (provided by container)
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Custom task to build WAR
task makeWar(type: War) {
    archiveFileName = '{ProjectName}_Webapp.war'
    from sourceSets.main.output
}
```

---

### 12. application.properties

```properties
# Application Info
spring.application.name={project-name}
server.port=8080

# Jackson Configuration
spring.jackson.use-jackson2-defaults=true

# Security
jwt.public.key=${JWT_PUBLIC_KEY}
jwt.private.key=${JWT_PRIVATE_KEY}

# AWS Configuration
aws.region=${AWS_REGION:us-east-1}
aws.dynamodb.endpoint=${AWS_DYNAMODB_ENDPOINT:}
aws.s3.bucket=${AWS_S3_BUCKET}

# Logging
logging.level.root=INFO
logging.level.com.{company}=DEBUG
logging.level.org.springframework.security=DEBUG

# Thymeleaf (for email templates)
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

---

## Key Architecture Patterns

### 1. Layered Architecture

```
┌─────────────────────────────────────┐
│     Controllers (REST API)          │  @RestController
│  - Input validation                 │  extends BaseRestController
│  - DTO conversion                   │  Return ResponseEntity<View*>
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     Services (Business Logic)       │  @Service
│  - Validation (FieldValidator)      │  Orchestrate operations
│  - BeanUtils.copyProperties()       │  Trigger async tasks
│  - Transaction coordination         │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     DAOs (Data Access)              │  @Component
│  - DynamoDB operations              │  extends BaseDao
│  - Return CompletableFuture<T>      │  Async operations
│  - Audit property updates           │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     AWS DynamoDB                    │
│  - NoSQL data storage               │
│  - GSI for queries                  │
└─────────────────────────────────────┘
```

### 2. CompletableFuture Pattern

All DAO methods return `CompletableFuture<T>` for async operations:
- Use `.join()` in service layer to block and get result
- Use `CompletableFuture.allOf()` for parallel operations
- Return `CompletableFuture.completedFuture()` in tests

### 3. Validation Pattern

Use `FieldValidator` fluent API with i18n support:
```java
FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
    .validateNotEmpty("name", entity.getName())
    .validateStringLength("description", entity.getDescription(), 1, 500)
    .validateEmailAddress("email", entity.getEmail(), ValidationLimits.EMAIL)
    .validateNotNull("type", entity.getType())
    .apply(); // Throws ValidationException if errors exist
```

### 4. Audit Pattern

All entities extend `DefaultAuditable` for automatic audit fields:
- `createdDate`, `createdBySubject`
- `updatedDate`, `updatedBySubject`
- Updated automatically via `AuthorizationUtils.updateAuditProperties()`

### 5. Pagination Pattern

Use cursor-based pagination with DynamoDB:
- Controller: Decode cursor with `CursorUtils.decodeLastEvaluatedKeyFromCursor()`
- DAO: Pass `exclusiveStartKey` to DynamoDB query
- Response: Return `DynamoResultList` with `lastEvaluatedKey`

---

## Best Practices

1. **Separation of Concerns**
   - Controllers handle HTTP, not business logic
   - Services contain business logic, not data access
   - DAOs contain only data access, not business logic

2. **DTO Usage**
   - Always use View models (DTOs) in API layer
   - Never expose entities directly to API
   - Use `BeanUtils.copyProperties()` for conversion

3. **Async Processing**
   - Long-running operations should be async tasks
   - Trigger tasks via `AppTaskProcessor.processLocally()`
   - Tasks should be idempotent

4. **Error Handling**
   - Use `ValidationException` for validation errors
   - Use `ResponseEntity.notFound()` for 404s
   - Let `BaseRestController` handle exception translation

5. **Testing**
   - Test service layer with mocked DAOs
   - Return `CompletableFuture.completedFuture()` in mocks
   - Test both success and failure paths

6. **Security**
   - Use JWT for authentication
   - Update audit properties before saves
   - Never expose sensitive data in DTOs

7. **Documentation**
   - Include JavaDoc for all public methods
   - Document complex business logic
   - Maintain CLAUDE.md with build commands

---

## Usage Instructions

When the user asks you to scaffold a new entity or application:

1. **Gather Requirements**
   - Entity name (e.g., "Product", "Order")
   - Fields and their types
   - Required indexes (GSI configuration)
   - Business logic requirements

2. **Generate Files**
   - Start with entity class (dao/model)
   - Create DAO with appropriate queries
   - Create view model (DTO)
   - Create service with CRUD operations
   - Create controller with REST endpoints
   - Create async task if needed
   - Create unit tests

3. **Replace Placeholders**
   - `{company}` → company name (e.g., "mattvorst")
   - `{project}` → project name (e.g., "inventory")
   - `{Entity}` → entity name (e.g., "Product")
   - `{entity}` → lowercase entity name (e.g., "product")
   - `{EntityType}` → enum type name (e.g., "ProductType")

4. **Customize**
   - Add entity-specific fields
   - Add custom validation rules
   - Add custom business logic
   - Add custom queries as needed

5. **Verify**
   - Ensure all imports are correct
   - Ensure naming conventions are followed
   - Ensure audit fields are handled
   - Ensure tests cover key scenarios

---

## Example Scaffold Command

When the user says: "Create a Product entity with name, description, price, and category"

You should:
1. Ask clarifying questions about:
   - What type is category? (Enum or String)
   - Should products be queryable by category?
   - Any special validation rules?
   - Need for featured products?

2. Generate all files following the templates above
3. Customize fields and validation
4. Create appropriate GSI configuration
5. Add any custom business logic

Remember to follow ALL naming conventions, patterns, and best practices documented above!
