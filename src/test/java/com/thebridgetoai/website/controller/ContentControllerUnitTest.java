package com.thebridgetoai.website.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.model.DynamoResultList;
import com.thebridgetoai.website.dao.model.Content;
import com.thebridgetoai.website.model.ViewContent;
import com.thebridgetoai.website.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ContentControllerUnitTest {

    @Mock
    private ContentService contentService;

    @InjectMocks
    private ContentController contentController;

    private UUID testContentUuid;
    private Content testContent;

    @BeforeEach
    void setUp() {
        testContentUuid = UUID.randomUUID();
        
        testContent = new Content();
        testContent.setContentUuid(testContentUuid);
        testContent.setTitle("Test Title");
        testContent.setCardTitle("Test Card Title");
    }

    @Test
    void getContent_ReturnsContent_WhenContentExists() {
        when(contentService.getContent(testContentUuid)).thenReturn(testContent);

        ResponseEntity<ViewContent> result = contentController.getContent(testContentUuid);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(testContentUuid, result.getBody().getContentUuid());
        assertEquals("Test Title", result.getBody().getTitle());
        verify(contentService).getContent(testContentUuid);
    }

    @Test
    void getContent_ReturnsNotFound_WhenContentDoesNotExist() {
        when(contentService.getContent(testContentUuid)).thenReturn(null);

        ResponseEntity<ViewContent> result = contentController.getContent(testContentUuid);

        assertEquals(404, result.getStatusCodeValue());
        assertNull(result.getBody());
        verify(contentService).getContent(testContentUuid);
    }

    @Test
    void getAllContent_ReturnsContentList_WithDefaultParameters() {
        List<Content> contentList = List.of(testContent);
        DynamoResultList<Content> resultList = new DynamoResultList<>(contentList, Map.of());
        
        when(contentService.getAllContent(eq(10), any())).thenReturn(resultList);

        ResponseEntity<DynamoResultList<ViewContent>> result = contentController.getAllContent(null, 10);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getList().size());
        assertEquals(testContentUuid, result.getBody().getList().get(0).getContentUuid());
        verify(contentService).getAllContent(eq(10), any());
    }

    @Test
    void getAllContent_ReturnsContentList_WithCustomParameters() {
        List<Content> contentList = List.of(testContent);
        DynamoResultList<Content> resultList = new DynamoResultList<>(contentList, Map.of());
        
        when(contentService.getAllContent(eq(5), any())).thenReturn(resultList);

        ResponseEntity<DynamoResultList<ViewContent>> result = contentController.getAllContent("someCursor", 5);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getList().size());
        verify(contentService).getAllContent(eq(5), any());
    }

    @Test
    void getAllContent_HandlesEmptyResult() {
        DynamoResultList<Content> emptyResultList = new DynamoResultList<>(List.of(), Map.of());
        
        when(contentService.getAllContent(eq(10), any())).thenReturn(emptyResultList);

        ResponseEntity<DynamoResultList<ViewContent>> result = contentController.getAllContent(null, 10);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getList().isEmpty());
        verify(contentService).getAllContent(eq(10), any());
    }
}