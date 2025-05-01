package com.theaiexplained.website.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.model.DynamoResultList;
import com.theaiexplained.website.constant.ContentCategoryType;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.dao.model.ContentAssociation;
import com.theaiexplained.website.dao.model.FeaturedContent;
import org.springframework.beans.BeanUtils;

public class ViewHomeContent {
    private DynamoResultList<FeaturedContent> blogPostList;

    public DynamoResultList<FeaturedContent> getBlogPostList() {
        return blogPostList;
    }

    public void setBlogPostList(DynamoResultList<FeaturedContent> blogPostList) {
        this.blogPostList = blogPostList;
    }
}