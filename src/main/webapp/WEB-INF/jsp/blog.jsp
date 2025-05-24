<% request.setAttribute("htmlTitle", "Blog"); %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%@ include file="./include/header-body.jsp" %>

<div id="app_container">
    <!-- Homepage content converted from React JSX to JSP/HTML -->
    <div class="homepage">
        <!-- Include header component -->
        <%@ include file="./include/navigation-header.jsp" %>

        <!-- Blog Entries Section -->
        <section class="blog-entries">
            <div class="container">
                <div class="section-header centered">
                    <h2>Latest Articles</h2>
                    <p>Stay updated with our latest insights and resources</p>
                </div>

                <c:choose>
                    <c:when test="${empty contentList.list}">
                        <div class="no-content">
                            <p>No articles available at this time. Check back soon!</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- Featured article (first item) -->
                        <c:if test="${not empty contentList.list}">
                            <div class="featured-article clickable-featured" onclick="navigateToBlogPost('${contentList.list[0].contentUuid}', '${fn:escapeXml(contentList.list[0].cardTitle)}')">
                                <div class="featured-article-content">
                                    <c:choose>
                                        <c:when test="${not empty contentList.list[0].headerImageUrl}">
                                            <div class="featured-article-image">
                                                <img src="${contentList.list[0].headerImageUrl}" alt="${contentList.list[0].title}" />
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="featured-article-image placeholder-image">
                                                <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/placeholder.png" alt="Placeholder" />
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="featured-article-text">
                                        <c:if test="${not empty contentList.list[0].publishedDate}">
                                            <div class="article-date">
                                                <fmt:formatDate pattern="MMMM d, yyyy" value="${contentList.list[0].publishedDate}" />
                                            </div>
                                        </c:if>
                                        <h3 class="featured-article-title">${contentList.list[0].cardTitle}</h3>
                                        <p class="featured-article-subtitle">${contentList.list[0].cardSubtitle}</p>
                                        <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog/${contentList.list[0].contentUuid}/${fn:replace(fn:replace(fn:toLowerCase(contentList.list[0].cardTitle), ' ', '-'), '--', '-')}" class="btn btn-primary" onclick="event.stopPropagation()">
                                            ${not empty contentList.list[0].cardCTATitle ? contentList.list[0].cardCTATitle : 'Read Article'}
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <!-- Blog Grid (remaining items) -->
                        <div class="blog-grid">
                            <c:forEach var="content" items="${contentList.list}" begin="1" varStatus="status">
                                <c:set var="content" value="${content}" scope="request" />
                                <%@ include file="./include/content-card.jsp" %>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- Pagination -->
                <c:if test="${contentList.hasCursor()}">
                    <div class="pagination">
                        <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog.action?cursor=${contentList.cursor}" class="btn btn-outline">Load More Articles</a>
                    </div>
                </c:if>
            </div>
        </section>

        <!-- Include newsletter signup component -->
        <%@ include file="./include/newsletter-signup.jsp" %>

        <!-- Include footer component -->
        <%@ include file="./include/footer.jsp" %>
    </div>
</div>

<!-- Add page-specific JavaScript functions -->
<script type="text/javascript">
  // Blog post navigation
  function navigateToBlogPost(contentUuid, title) {
    // Create SEO-friendly URL slug from title for the existing URL pattern
    const slug = title ? title.toLowerCase()
                              .replace(/[^a-z0-9\s-]/g, '') // Remove special characters
                              .replace(/\s+/g, '-') // Replace spaces with hyphens
                              .replace(/-+/g, '-') // Replace multiple hyphens with single
                              .trim('-') : 'article'; // Remove leading/trailing hyphens
    
    // Use existing SEO-friendly URL pattern: /blog/{contentUuid}/{slug}
    // The .htaccess file will rewrite this to /blog-detail.action?contentUuid={contentUuid}
    window.location.href = "<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog/" + contentUuid + "/" + slug;
  }

  // Story navigation
  function nextStory() {
    // Code to navigate to next story
    console.log("Navigate to next story");
  }

  function prevStory() {
    // Code to navigate to previous story
    console.log("Navigate to previous story");
  }

  // Event registration
  function registerEvent(eventId) {
    // Code to handle event registration
    console.log("Register for event: " + eventId);
    window.location.href = "<%= Environment.get(EnvironmentConstants.BASE_URL) %>/event-registration.jsp?eventId=" + eventId;
  }

  // Consultation booking
  function bookConsultation() {
    // Code to handle consultation booking
    console.log("Book consultation");
    window.location.href = "<%= Environment.get(EnvironmentConstants.BASE_URL) %>/book-consultation.jsp";
  }

  // Add hover effects and cursor styles for clickable elements
  document.addEventListener('DOMContentLoaded', function() {
    // Add pointer cursor to clickable cards
    const clickableCards = document.querySelectorAll('.clickable-card, .clickable-featured');
    clickableCards.forEach(function(card) {
      card.style.cursor = 'pointer';
      
      // Add hover effect
      card.addEventListener('mouseenter', function() {
        this.style.transform = 'translateY(-2px)';
        this.style.transition = 'transform 0.2s ease';
      });
      
      card.addEventListener('mouseleave', function() {
        this.style.transform = 'translateY(0)';
      });
    });
  });
</script>

<%@ include file="./include/footer-body.jsp" %>