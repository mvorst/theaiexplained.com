---
name: patterns
description: Implements coding patterns from this codebase. Use when creating new Java classes (controllers, services, DAOs, entities, tests, async tasks, enums, attribute converters) or new React components, admin screens, or JSP templates. Automatically apply these patterns whenever writing new code for this project.
argument-hint: [component-type] [name]
---

# TheBridgeTo.ai Codebase Patterns

When writing new code for this project, follow these established patterns exactly. Read existing files of the same type before creating new ones to stay consistent.

If `$ARGUMENTS` is provided, interpret it as: `[component-type] [name]` where component-type is one of: controller, service, dao, entity, view-model, enum, converter, async-task, unit-test, integration-test, react-screen, react-control, jsp-page.

---

## Java Backend Patterns

### REST Controller

- Annotate with `@RestController` and class-level `@RequestMapping("/rest/api/{version}/<resource>")`
- Extend `BaseRestController` (from `com.mattvorst.shared.controller`)
- Use `@Autowired` field injection for service dependencies
- Return `ResponseEntity<ViewXxx>` from all endpoints
- Use `ResponseEntity.ok()` for 200, `.notFound().build()` for 404, `.status(HttpStatus.CREATED)` for 201
- Check for null before returning entity responses
- Use `@PathVariable UUID` for entity identifiers
- Use `@RequestParam` with `defaultValue` for optional pagination params
- Decode pagination cursors with `CursorUtils.decodeLastEvaluatedKeyFromCursor()`

```java
package com.thebridgetoai.website.controller;

import com.mattvorst.shared.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rest/api/{version}/<resource>")
public class ExampleController extends BaseRestController {

    @Autowired private ExampleService exampleService;

    @GetMapping("/{exampleUuid}")
    public ResponseEntity<ViewExample> getExample(@PathVariable UUID exampleUuid) {
        Example example = exampleService.getExample(exampleUuid);
        if (example == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewExample(example));
    }

    @PostMapping("/")
    public ResponseEntity<ViewExample> createExample(@RequestBody ViewExample viewExample) throws ValidationException {
        Example example = exampleService.createExample(viewExample);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ViewExample(example));
    }

    @PutMapping("/{exampleUuid}")
    public ResponseEntity<ViewExample> updateExample(@PathVariable UUID exampleUuid, @RequestBody ViewExample viewExample) throws ValidationException {
        viewExample.setExampleUuid(exampleUuid);
        Example example = exampleService.updateExample(viewExample);
        return ResponseEntity.ok(new ViewExample(example));
    }

    @DeleteMapping("/{exampleUuid}")
    public ResponseEntity<Void> deleteExample(@PathVariable UUID exampleUuid) {
        exampleService.deleteExample(exampleUuid);
        return ResponseEntity.ok().build();
    }
}
```

### Admin Controller

Admin endpoints use the path `/rest/admin/{version}/<resource>` instead of `/rest/api/`.

### Service

- Annotate with `@Service`
- Use `@Autowired` field injection for DAOs, `MessageSource`, `AppTaskProcessor`
- Call `.join()` on `CompletableFuture` returns from DAOs
- Validate inputs using `FieldValidator` fluent API before data operations
- Use `BeanUtils.copyProperties()` to copy between view models and entities
- Generate UUIDs with collision-check loop (up to 10 attempts)
- Trigger async tasks via `appTaskProcessor.processLocally()` after mutations

```java
package com.thebridgetoai.website.service;

import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.util.FieldValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExampleService {

    @Autowired private MessageSource messageSource;
    @Autowired private AppTaskProcessor appTaskProcessor;
    @Autowired private ExampleDao exampleDao;

    public Example getExample(UUID exampleUuid) {
        return exampleDao.getExample(exampleUuid).join();
    }

    public Example createExample(ViewExample viewExample) throws ValidationException {
        validateExample(viewExample);

        int count = 0;
        do {
            UUID exampleUuid = UUID.randomUUID();
            Example existing = exampleDao.getExample(exampleUuid).join();
            if (existing == null) {
                viewExample.setExampleUuid(exampleUuid);
                break;
            }
        } while (count++ < 10);

        Example example = new Example();
        BeanUtils.copyProperties(viewExample, example);
        exampleDao.saveExample(example).join();
        return example;
    }

    public Example updateExample(ViewExample viewExample) throws ValidationException {
        validateExample(viewExample);

        Example example = exampleDao.getExample(viewExample.getExampleUuid()).join();
        BeanUtils.copyProperties(viewExample, example, "exampleUuid", "createdDate", "createdBySubject");
        exampleDao.saveExample(example).join();
        return example;
    }

    public void deleteExample(UUID exampleUuid) {
        Example example = exampleDao.getExample(exampleUuid).join();
        if (example != null) {
            exampleDao.deleteExample(example);
        }
    }

    private void validateExample(ViewExample viewExample) throws ValidationException {
        FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
                .validateNotEmpty("name", viewExample.getName())
                .apply();
    }
}
```

### DAO (Data Access)

- Annotate with `@Component` (preferred) or `@Repository`
- Extend `BaseDao` (from `com.mattvorst.shared.dao`)
- Return `CompletableFuture<T>` from all methods (async-first)
- Use `DynamoDbEnhancedAsyncClient` with `TableSchema.fromBean()`
- Use `GetItemEnhancedRequest` with `consistentRead(true)` for reads
- Use `QueryEnhancedRequest` with `QueryConditional.keyEqualTo()` for index queries
- Use `DynamoDbUtils.queryWithPagination()` for paginated queries returning `DynamoResultList<T>`
- Call `AuthorizationUtils.updateAuditProperties()` before saving
- Access secondary indexes via `table.index("indexName")`

```java
package com.thebridgetoai.website.dao;

import com.mattvorst.shared.dao.BaseDao;
import com.mattvorst.shared.security.AuthorizationUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class ExampleDao extends BaseDao {

    public CompletableFuture<Example> getExample(UUID exampleUuid) {
        DynamoDbAsyncTable<Example> table = dynamoDbEnhancedAsyncClient
                .table(Example.TABLE_NAME, TableSchema.fromBean(Example.class));
        return table.getItem(GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(exampleUuid.toString()).build())
                .consistentRead(true)
                .build());
    }

    public CompletableFuture<Void> saveExample(Example example) {
        DynamoDbAsyncTable<Example> table = dynamoDbEnhancedAsyncClient
                .table(Example.TABLE_NAME, TableSchema.fromBean(Example.class));
        AuthorizationUtils.updateAuditProperties(example);
        return table.putItem(example);
    }

    public void deleteExample(Example example) {
        DynamoDbAsyncTable<Example> table = dynamoDbEnhancedAsyncClient
                .table(Example.TABLE_NAME, TableSchema.fromBean(Example.class));
        table.deleteItem(example);
    }

    public CompletableFuture<DynamoResultList<Example>> getExampleList(
            String partitionKey, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
        DynamoDbAsyncTable<Example> table = dynamoDbEnhancedAsyncClient
                .table(Example.TABLE_NAME, TableSchema.fromBean(Example.class));
        DynamoDbAsyncIndex<Example> index = table.index("indexName");

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(partitionKey).build()))
                .limit(pageSize)
                .exclusiveStartKey(exclusiveStartKey)
                .scanIndexForward(false)
                .build();

        return DynamoDbUtils.queryWithPagination(index, request);
    }
}
```

### DynamoDB Entity

- Annotate with `@DynamoDbBean`
- Extend `DefaultAuditable` (from `com.mattvorst.shared.model`)
- Define `public static final String TABLE_NAME`
- Mark partition key getter with `@DynamoDbPartitionKey`
- Use `@DynamoDbSecondaryPartitionKey(indexNames = {...})` for GSI partition keys
- Use `@DynamoDbSecondarySortKey(indexNames = {...})` for GSI sort keys
- Use `@DynamoDbConvertedBy(XxxAttributeConverter.class)` for enums and custom types
- Use `@DynamoDbVersionAttribute` for optimistic locking when needed
- Composite sort keys use `Utils.toUtcTimestamp(date) + "|" + uuid` pattern

```java
package com.thebridgetoai.website.dao.model;

import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.Date;
import java.util.UUID;

@DynamoDbBean
public class Example extends DefaultAuditable {

    public static final String TABLE_NAME = "example";

    private UUID exampleUuid;
    private String name;
    private ExampleType exampleType;
    private Date publishedDate;
    private boolean active;

    @DynamoDbPartitionKey
    public UUID getExampleUuid() { return exampleUuid; }
    public void setExampleUuid(UUID exampleUuid) { this.exampleUuid = exampleUuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDbSecondaryPartitionKey(indexNames = {"exampleType-publishedDate-index"})
    @DynamoDbConvertedBy(ExampleTypeAttributeConverter.class)
    public ExampleType getExampleType() { return exampleType; }
    public void setExampleType(ExampleType exampleType) { this.exampleType = exampleType; }

    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

### View Model (DTO)

- Name as `ViewXxx` in `com.thebridgetoai.website.model` package
- Mirror entity fields (no DynamoDB annotations)
- Provide default constructor and constructor from entity using `BeanUtils.copyProperties()`
- Null-check entity in constructor

```java
package com.thebridgetoai.website.model;

import org.springframework.beans.BeanUtils;

public class ViewExample {

    private UUID exampleUuid;
    private String name;
    private ExampleType exampleType;
    private Date publishedDate;
    private boolean active;

    public ViewExample() {}

    public ViewExample(Example example) {
        if (example != null) {
            BeanUtils.copyProperties(example, this);
        }
    }

    // Getters and setters...
}
```

### Enum

- Simple Java enums in `com.thebridgetoai.website.constant` package
- Use UPPER_SNAKE_CASE values
- Create a matching `AttributeConverter` for DynamoDB persistence

```java
package com.thebridgetoai.website.constant;

public enum ExampleType {
    TYPE_ONE, TYPE_TWO, TYPE_THREE;
}
```

### Attribute Converter

- Implement `AttributeConverter<T>` from DynamoDB enhanced client
- Place in `com.thebridgetoai.website.dao.convert` package
- `transformFrom()` converts Java to `AttributeValue` (typically `.s()` for strings)
- `transformTo()` converts `AttributeValue` back to Java (use `valueOf()` for enums)

```java
package com.thebridgetoai.website.dao.convert;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ExampleTypeAttributeConverter implements AttributeConverter<ExampleType> {

    @Override
    public AttributeValue transformFrom(ExampleType input) {
        return AttributeValue.builder().s(input.toString()).build();
    }

    @Override
    public ExampleType transformTo(AttributeValue input) {
        return ExampleType.valueOf(input.s());
    }

    @Override
    public EnhancedType<ExampleType> type() {
        return EnhancedType.of(ExampleType.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
```

### Async Task

- Annotate class with `@AsyncTask("TaskName")`
- Extend `QueueRunnable` (from `com.mattvorst.shared.async.model`)
- Define inner `Parameters` class extending `AbstractTaskParameters`
- Mark Parameters with `@JsonInclude(JsonInclude.Include.NON_NULL)`
- Provide two constructors: default (no-args) and full (with DAO dependencies + parameters)
- Implement `run()` with try-catch wrapping checked exceptions in `RuntimeException`
- Invoke from services via `appTaskProcessor.processLocally(new TaskName.Parameters(...))`

```java
package com.thebridgetoai.website.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mattvorst.shared.async.model.AbstractTaskParameters;
import com.mattvorst.shared.async.model.AsyncTask;
import com.mattvorst.shared.async.model.QueueRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@AsyncTask("ExampleTask")
public class ExampleTask extends QueueRunnable {

    private static final Logger log = LogManager.getLogger(ExampleTask.class);

    private final ExampleDao exampleDao;
    private final Parameters parameters;

    public ExampleTask() {
        super();
        this.exampleDao = null;
        this.parameters = null;
    }

    public ExampleTask(ExampleDao exampleDao, Parameters parameters) {
        super();
        this.exampleDao = exampleDao;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        try {
            Example example = exampleDao.getExample(parameters.getExampleUuid()).get();
            if (example != null) {
                // Task logic here
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameters extends AbstractTaskParameters {
        private UUID exampleUuid;
        private long createTime;

        public Parameters() { super(); }

        public Parameters(UUID exampleUuid) {
            this();
            this.exampleUuid = exampleUuid;
            this.createTime = System.currentTimeMillis();
        }

        public UUID getExampleUuid() { return exampleUuid; }

        @Override
        public long getCreateTime() { return createTime; }
    }
}
```

### Unit Test

- Use `@ExtendWith(MockitoExtension.class)` (JUnit 5 + Mockito)
- Mock dependencies with `@Mock`
- Inject class under test with `@InjectMocks`
- Set up test data in `@BeforeEach`
- Name tests: `methodName_ExpectedBehavior_WhenCondition`
- Wrap DAO returns in `CompletableFuture.completedFuture()`
- Verify interactions with `verify(mock).method()`
- Use `assertNotNull`, `assertEquals`, `assertNull`, `assertThrows`

```java
package com.thebridgetoai.website.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private AppTaskProcessor appTaskProcessor;
    @Mock private ExampleDao exampleDao;

    @InjectMocks private ExampleService exampleService;

    private UUID testUuid;
    private Example testExample;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testExample = new Example();
        testExample.setExampleUuid(testUuid);
    }

    @Test
    void getExample_ReturnsExample_WhenExists() {
        when(exampleDao.getExample(testUuid))
                .thenReturn(CompletableFuture.completedFuture(testExample));

        Example result = exampleService.getExample(testUuid);

        assertNotNull(result);
        assertEquals(testUuid, result.getExampleUuid());
        verify(exampleDao).getExample(testUuid);
    }

    @Test
    void getExample_ReturnsNull_WhenNotFound() {
        when(exampleDao.getExample(testUuid))
                .thenReturn(CompletableFuture.completedFuture(null));

        Example result = exampleService.getExample(testUuid);

        assertNull(result);
    }
}
```

### Controller Unit Test

```java
@ExtendWith(MockitoExtension.class)
class ExampleControllerUnitTest {

    @Mock private ExampleService exampleService;
    @InjectMocks private ExampleController exampleController;

    @Test
    void getExample_Returns200_WhenExists() {
        when(exampleService.getExample(testUuid)).thenReturn(testExample);

        ResponseEntity<ViewExample> result = exampleController.getExample(testUuid);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        verify(exampleService).getExample(testUuid);
    }

    @Test
    void getExample_Returns404_WhenNotFound() {
        when(exampleService.getExample(testUuid)).thenReturn(null);

        ResponseEntity<ViewExample> result = exampleController.getExample(testUuid);

        assertEquals(404, result.getStatusCode().value());
    }
}
```

---

## React Frontend Patterns

### Admin List Screen

- Functional component with hooks (`useState`, `useEffect`, `useNavigate`)
- State: `items[]`, `loading`, `error`, `cursor`, `hasMore`, and filter state
- Fetch data with `axios.get()` in async function called from `useEffect`
- Cursor-based pagination with "Load More" button
- Loading/error/empty states with appropriate UI
- Section header with title and "Create" button linking to `/resource/new/detail`
- Data table with clickable rows navigating to `/resource/{id}/detail`
- Filter controls (select dropdowns) that trigger re-fetch

```jsx
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from "axios";

const ExampleList = () => {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cursor, setCursor] = useState(null);
    const [hasMore, setHasMore] = useState(false);
    const navigate = useNavigate();

    useEffect(() => { fetchItems(); }, []);

    const fetchItems = async () => {
        try {
            setLoading(true);
            const response = await axios.get('/rest/admin/1/example/');
            const data = response.data;
            setItems(data.list || []);
            setHasMore(data.cursor !== null);
            setCursor(data.cursor);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const loadMore = async () => {
        if (!cursor || !hasMore) return;
        // Append to existing items...
    };

    if (loading && items.length === 0) return <div className="loading-container"><div className="loading-spinner"></div></div>;
    if (error) return <div className="error-container"><p>{error}</p></div>;

    return (
        <div className="container content-container">
            <div className="section-header">
                <h2>Examples</h2>
                <div className="header-actions">
                    <Link to="/example/new/detail" className="btn btn-primary">+ Create</Link>
                </div>
            </div>
            <div className="table-responsive">
                <table className="data-table">
                    <thead><tr><th>Name</th><th>Date</th></tr></thead>
                    <tbody>
                        {items.map(item => (
                            <tr key={item.exampleUuid} className="content-row"
                                onClick={() => navigate(`/example/${item.exampleUuid}/detail`)}>
                                <td>{item.name}</td>
                                <td>{item.publishedDate}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            {hasMore && <button className="btn btn-secondary" onClick={loadMore}>Load More</button>}
        </div>
    );
};

export default ExampleList;
```

### Admin Detail/Form Screen

- `useParams()` to get ID from route; check if `id === 'new'` for create vs edit
- Single state object for all form fields with spread-based `handleChange`
- `handleChange` handles text inputs, checkboxes, and selects uniformly
- `handleSubmit` uses POST for new, PUT for existing
- Navigate to list screen on success
- S3 file upload: get presigned URL -> PUT to S3 -> POST complete endpoint
- Use `Optional` component for conditional rendering
- Use `QuillEditor` for rich text fields

```jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from "axios";
import Optional from '../../controls/Optional.jsx';
import QuillEditor from '../../controls/QuillEditor.jsx';

const ExampleDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const isNew = id === 'new';
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [example, setExample] = useState({
        exampleUuid: null,
        name: '',
        description: '',
        markupContent: '',
        active: false,
    });

    useEffect(() => {
        if (!isNew) fetchExample();
    }, [id]);

    const fetchExample = async () => {
        try {
            const response = await axios.get(`/rest/admin/1/example/${id}`);
            setExample(response.data);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setExample(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            if (isNew) {
                await axios.post('/rest/admin/1/example/', example);
            } else {
                await axios.put(`/rest/admin/1/example/${id}`, example);
            }
            navigate('/example/');
        } catch (err) {
            setError('Failed to save.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="container content-container">
            <Optional show={error !== null}>
                <div className="content-form-error">{error}</div>
            </Optional>
            <form onSubmit={handleSubmit}>
                {/* Form fields */}
                <div className="form-group">
                    <label>Name</label>
                    <input type="text" name="name" value={example.name} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label>Content</label>
                    <QuillEditor value={example.markupContent}
                        onChange={(html) => setExample(prev => ({ ...prev, markupContent: html }))} />
                </div>
                <div className="form-actions">
                    <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
                    <button type="submit" className="btn btn-primary" disabled={submitting}>
                        {submitting ? 'Saving...' : 'Save'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default ExampleDetail;
```

### Reusable Control Component

- Use PropTypes for type checking
- Accept callback props for events
- Keep components small and focused

```jsx
import React from 'react';
import PropTypes from 'prop-types';

const ExampleControl = ({ label, value, onChange, className = '' }) => {
    return (
        <div className={`example-control ${className}`}>
            <label>{label}</label>
            <input type="text" value={value} onChange={onChange} />
        </div>
    );
};

ExampleControl.propTypes = {
    label: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    className: PropTypes.string
};

export default ExampleControl;
```

### Adding Routes

When adding new screens, register routes in `src/main/docs/js/admin.jsx` inside the `<Routes>` block:

```jsx
<Route path="example/" element={<ExampleList />} />
<Route path="example/:id/detail" element={<ExampleDetail />} />
```

And add navigation in `src/main/docs/js/admin/AdminLayout.jsx`:

```jsx
<NavLink to="/example" className={({ isActive }) => isActive ? 'active' : ''}>Examples</NavLink>
```

### Axios Configuration

- Auth tokens are added automatically via interceptor in `admin.jsx`
- Token is stored/refreshed in `localStorage` via response interceptor
- Use `axios.get()`, `axios.post()`, `axios.put()`, `axios.delete()`
- For S3 uploads, create a separate axios instance without auth headers

---

## File Locations

| Component Type | Package / Directory |
|---|---|
| Controller | `src/main/java/com/thebridgetoai/website/controller/` |
| Service | `src/main/java/com/thebridgetoai/website/service/` |
| DAO | `src/main/java/com/thebridgetoai/website/dao/` |
| Entity | `src/main/java/com/thebridgetoai/website/dao/model/` |
| View Model | `src/main/java/com/thebridgetoai/website/model/` |
| Enum | `src/main/java/com/thebridgetoai/website/constant/` |
| Converter | `src/main/java/com/thebridgetoai/website/dao/convert/` |
| Async Task | `src/main/java/com/thebridgetoai/website/task/` |
| Config | `src/main/java/com/thebridgetoai/website/config/` |
| Unit Tests | `src/test/java/com/thebridgetoai/website/` (mirrors main) |
| React Screens | `src/main/docs/js/admin/screens/` |
| React Controls | `src/main/docs/js/controls/` |
| React Components | `src/main/docs/js/components/` |
| CSS | `src/main/docs/style/` |
| JSP Pages | `src/main/webapp/WEB-INF/jsp/` |
| JSP Includes | `src/main/webapp/WEB-INF/jsp/include/` |

---

## Key Conventions

- **Naming**: Entity `Xxx`, view model `ViewXxx`, DAO `XxxDao`, service `XxxService`, controller `XxxController`
- **Test naming**: `methodName_ExpectedBehavior_WhenCondition`
- **UUID fields**: Always `UUID` type, named `xxxUuid`
- **Audit trail**: Entities extend `DefaultAuditable`; DAOs call `AuthorizationUtils.updateAuditProperties()` before save
- **Pagination**: Cursor-based via `DynamoResultList<T>` and `CursorResultList<T>` interface
- **Error handling**: `ValidationException` for input errors, `AuthorizationException` for access denied, `UnauthorizedException` for 401
- **Logging**: `private static final Logger log = LogManager.getLogger(ClassName.class);`
- **CSS variables**: Use `var(--primary-color)`, `var(--spacing-md)`, etc. from `:root` in `public.css`
