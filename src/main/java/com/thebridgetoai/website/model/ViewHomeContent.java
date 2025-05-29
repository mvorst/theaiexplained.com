package com.thebridgetoai.website.model;

import com.mattvorst.shared.model.DynamoResultList;
import com.thebridgetoai.website.dao.model.FeaturedContent;

public class ViewHomeContent {

    private DynamoResultList<FeaturedContent> startHereContentList;
    private DynamoResultList<FeaturedContent> blogPostContentList;
    private DynamoResultList<FeaturedContent> resourcesContentList;
    private DynamoResultList<FeaturedContent> modelsContentList;

    public DynamoResultList<FeaturedContent> getStartHereContentList() {
        return startHereContentList;
    }

    public void setStartHereContentList(DynamoResultList<FeaturedContent> startHereContentList) {
        this.startHereContentList = startHereContentList;
    }

    public DynamoResultList<FeaturedContent> getBlogPostContentList() {
        return blogPostContentList;
    }

    public void setBlogPostContentList(DynamoResultList<FeaturedContent> blogPostContentList) {
        this.blogPostContentList = blogPostContentList;
    }

    public DynamoResultList<FeaturedContent> getResourcesContentList() {
        return resourcesContentList;
    }

    public void setResourcesContentList(DynamoResultList<FeaturedContent> resourcesContentList) {
        this.resourcesContentList = resourcesContentList;
    }

    public DynamoResultList<FeaturedContent> getModelsContentList() {
        return modelsContentList;
    }

    public void setModelsContentList(DynamoResultList<FeaturedContent> modelsContentList) {
        this.modelsContentList = modelsContentList;
    }
}