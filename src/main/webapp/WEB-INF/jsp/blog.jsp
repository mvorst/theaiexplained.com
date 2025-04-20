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

        <!-- Hero Section -->
        <section class="hero">
            <div class="container">
                <div class="hero-content">
                    <div class="hero-text">
                        <h1>Demystifying AI for Everyone</h1>
                        <p class="subtitle">Gain the confidence and skills to thrive alongside AI in your career and daily life.</p>
                        <div class="hero-cta">
                            <button class="btn btn-primary btn-lg" onclick="location.href='<%= Environment.get(EnvironmentConstants.BASE_URL) %>/ai-basics.jsp'">Explore AI Basics</button>
                            <button class="btn btn-secondary btn-lg" onclick="location.href='<%= Environment.get(EnvironmentConstants.BASE_URL) %>/industries.jsp'">Find Tools for Your Industry</button>
                        </div>
                    </div>
                    <div class="hero-image">
                        <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/home/main_md.png" alt="AI and Human Collaboration" />
                    </div>
                </div>
            </div>
        </section>

        <!-- Blog Entries Section -->
        <section class="blog-entries">
            <div class="container">
                <div class="section-header">
                    <h2>Latest Articles</h2>
                    <p>Stay updated with our latest insights and resources</p>
                </div>

                <div class="blog-grid">
                    <c:choose>
                        <c:when test="${empty contentList.list}">
                            <div class="no-content">
                                <p>No articles available at this time. Check back soon!</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="content" items="${contentList.list}">
                                <div class="blog-card">
                                    <div class="blog-card-image">
                                        <c:choose>
                                            <c:when test="${not empty content.cardHeaderImageUrl}">
                                                <img src="${content.cardHeaderImageUrl}" alt="${content.cardTitle}" />
                                            </c:when>
                                            <c:otherwise>
                                                <div class="placeholder-image">
                                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/placeholder.png" alt="Placeholder" />
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="blog-card-content">
                                        <h3 class="blog-card-title">${content.cardTitle}</h3>
                                        <p class="blog-card-subtitle">${content.cardSubtitle}</p>
                                        <div class="blog-card-footer">
                                            <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/content/${content.contentUuid}" class="blog-card-link">
                                                ${not empty content.cardCTATitle ? content.cardCTATitle : 'Continue Reading'}
                                            </a>
                                            <c:if test="${not empty content.publishedDate}">
                                                <span class="blog-card-date">
                                                    <fmt:formatDate pattern="MMMM d, yyyy" value="${content.publishedDate}" />
                                                </span>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>

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
</script>

<%@ include file="./include/footer-body.jsp" %>