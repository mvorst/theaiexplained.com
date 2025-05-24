package com.theaiexplained.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.theaiexplained.website.async.processor.AppTaskProcessor;
import com.theaiexplained.website.constant.ContentCategoryType;
import com.theaiexplained.website.dao.ContentDao;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.dao.model.FeaturedContent;
import com.theaiexplained.website.model.ViewContent;
import com.theaiexplained.website.model.ViewHomeContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private AppTaskProcessor appTaskProcessor;

    @Mock
    private ContentDao contentDao;

    @InjectMocks
    private ContentService contentService;

    private UUID testContentUuid;
    private Content testContent;
    private ViewContent testViewContent;

    @BeforeEach
    void setUp() {
        testContentUuid = UUID.randomUUID();
        testContent = new Content();
        testContent.setContentUuid(testContentUuid);
        testContent.setTitle("Test Title");
        testContent.setCardTitle("Test Card Title");

        testViewContent = new ViewContent();
        testViewContent.setContentUuid(testContentUuid);
        testViewContent.setTitle("Test Title");
        testViewContent.setCardTitle("Test Card Title");
    }

    @Test
    void getContent_ReturnsContent_WhenContentExists() {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(testContent));

        Content result = contentService.getContent(testContentUuid);

        assertNotNull(result);
        assertEquals(testContentUuid, result.getContentUuid());
        verify(contentDao).getContent(testContentUuid);
    }

    @Test
    void getContent_ReturnsNull_WhenContentDoesNotExist() {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(null));

        Content result = contentService.getContent(testContentUuid);

        assertNull(result);
        verify(contentDao).getContent(testContentUuid);
    }

    @Test
    void createContent_CreatesContent_WhenValidInput() throws ValidationException {
        when(contentDao.getContent(any(UUID.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(contentDao.saveContent(any(Content.class))).thenReturn(CompletableFuture.completedFuture(null));

        Content result = contentService.createContent(testViewContent);

        assertNotNull(result);
        assertEquals(testViewContent.getTitle(), result.getTitle());
        assertEquals(testViewContent.getCardTitle(), result.getCardTitle());
        verify(contentDao).saveContent(any(Content.class));
        verify(appTaskProcessor).processLocally(any());
    }


    @Test
    void updateContent_UpdatesContent_WhenContentExists() throws ValidationException {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(testContent));
        when(contentDao.saveContent(any(Content.class))).thenReturn(CompletableFuture.completedFuture(null));

        testViewContent.setTitle("Updated Title");
        Content result = contentService.updateContent(testContentUuid, testViewContent);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(contentDao).saveContent(any(Content.class));
        verify(appTaskProcessor).processLocally(any());
    }

    @Test
    void updateContent_ReturnsNull_WhenContentDoesNotExist() throws ValidationException {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(null));

        Content result = contentService.updateContent(testContentUuid, testViewContent);

        assertNull(result);
        verify(contentDao, never()).saveContent(any(Content.class));
        verify(appTaskProcessor, never()).processLocally(any());
    }

    @Test
    void deleteContent_DeletesContent_WhenContentExists() {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(testContent));
        when(contentDao.deleteContent(testContent)).thenReturn(CompletableFuture.completedFuture(null));

        Content result = contentService.deleteContent(testContentUuid);

        assertNotNull(result);
        assertEquals(testContentUuid, result.getContentUuid());
        verify(contentDao).deleteContent(testContent);
    }

    @Test
    void deleteContent_ReturnsNull_WhenContentDoesNotExist() {
        when(contentDao.getContent(testContentUuid)).thenReturn(CompletableFuture.completedFuture(null));

        Content result = contentService.deleteContent(testContentUuid);

        assertNull(result);
        verify(contentDao, never()).deleteContent(any(Content.class));
    }

    @Test
    void getAllContent_ReturnsContentList() {
        DynamoResultList<Content> mockResultList = new DynamoResultList<>();
        when(contentDao.getAllContent(anyInt(), any())).thenReturn(CompletableFuture.completedFuture(mockResultList));

        DynamoResultList<Content> result = contentService.getAllContent(10, Map.of());

        assertNotNull(result);
        verify(contentDao).getAllContent(10, Map.of());
    }

    @Test
    void getContentListByDate_ReturnsContentList() {
        DynamoResultList<Content> mockResultList = new DynamoResultList<>();
        when(contentDao.getContentListByDate(any(ContentCategoryType.class), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockResultList));

        DynamoResultList<Content> result = contentService.getContentListByDate(ContentCategoryType.BLOG_POST, 10, Map.of());

        assertNotNull(result);
        verify(contentDao).getContentListByDate(ContentCategoryType.BLOG_POST, 10, Map.of());
    }

    @Test
    void getHomeContent_ReturnsViewHomeContent() {
        DynamoResultList<FeaturedContent> mockFeaturedContent = new DynamoResultList<>();
        when(contentDao.getFeaturedContentByCategoryAndDate(any(ContentCategoryType.class), anyInt(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockFeaturedContent));

        ViewHomeContent result = contentService.getHomeContent();

        assertNotNull(result);
        verify(contentDao, times(4)).getFeaturedContentByCategoryAndDate(any(ContentCategoryType.class), eq(3), isNull());
    }
}