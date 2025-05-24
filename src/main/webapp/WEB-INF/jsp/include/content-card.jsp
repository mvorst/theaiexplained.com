<%@ page import="com.mattvorst.shared.constant.EnvironmentConstants" %>
<%@ page import="com.mattvorst.shared.util.Environment" %>
<%@ taglib uri="jakarta.tags.core" prefix="core" %>
<%@ taglib uri="jakarta.tags.functions" prefix="functions" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="formats" %>

<div class="blog-card clickable-card" onclick="navigateToBlogPost('${content.contentUuid}', '${functions:escapeXml(content.cardTitle)}')">
  <div class="blog-card-image">
    <core:choose>
      <core:when test="${not empty content.cardHeaderImageUrl}">
        <img src="${content.cardHeaderImageUrl}" alt="${content.cardTitle}" />
      </core:when>
      <core:otherwise>
        <div class="placeholder-image">
          <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/placeholder.png" alt="Placeholder" />
        </div>
      </core:otherwise>
    </core:choose>
    <core:if test="${not empty content.publishedDate}">
      <div class="blog-card-date-badge">
        <formats:formatDate pattern="MMM d" value="${content.publishedDate}" />
      </div>
    </core:if>
  </div>
  <div class="blog-card-content">
    <h3 class="blog-card-title">${content.cardTitle}</h3>
    <p class="blog-card-subtitle">${content.cardSubtitle}</p>
    <div class="blog-card-footer">
      <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog/${content.contentUuid}/${functions:replace(functions:replace(functions:toLowerCase(content.cardTitle), ' ', '-'), '--', '-')}" class="blog-card-link" onclick="event.stopPropagation()">
        ${not empty content.cardCTATitle ? content.cardCTATitle : 'Continue Reading'} <span class="arrow-icon">&rarr;</span>
      </a>
    </div>
  </div>
</div>
