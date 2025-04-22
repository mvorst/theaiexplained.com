<% request.setAttribute("htmlTitle", "Blog"); %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%@ include file="./include/header-body.jsp" %>

<div id="app_container">
    <!-- Blog detail content -->
    <div class="blog-detail-page">
        <!-- Include header component -->
        <%@ include file="./include/navigation-header.jsp" %>

        <!-- Content Detail Section -->
        <section class="content-detail">
            <!-- Header Image -->
            <c:choose>
                <c:when test="${not empty content.headerImageUrl}">
                    <div class="content-detail-header">
                        <img src="${content.headerImageUrl}" alt="${content.title}" class="content-detail-header-image" />
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="content-detail-header placeholder-image">
                        <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/placeholder.png" alt="Placeholder" />
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="content-detail-container">
                <!-- Published Date -->
                <c:if test="${not empty content.publishedDate}">
                    <div class="article-date">
                        <fmt:formatDate pattern="MMMM d, yyyy" value="${content.publishedDate}" />
                    </div>
                </c:if>

                <!-- Title and Subtitle -->
                <h1 class="content-detail-title">${content.title}</h1>
                <c:if test="${not empty content.subtitle}">
                    <h2 class="content-detail-subtitle">${content.subtitle}</h2>
                </c:if>

                <!-- Main Content -->
                <div class="content-detail-body">
                    ${content.markupContent}
                </div>

                <!-- Audio Content if available -->
                <c:if test="${not empty content.audioContentUrl}">
                    <div class="content-detail-audio">
                        <h3>Listen to this article</h3>
                        <audio controls>
                            <source src="${content.audioContentUrl}" type="audio/mpeg">
                            Your browser does not support the audio element.
                        </audio>
                    </div>
                </c:if>

                <!-- Reference URL if available -->
                <c:if test="${not empty content.referenceUrl}">
                    <div class="content-detail-reference">
                        <p>
                            <a href="${content.referenceUrl}" target="_blank" rel="noopener noreferrer">
                                ${not empty content.referenceUrlTitle ? content.referenceUrlTitle : 'Learn More'}
                            </a>
                        </p>
                    </div>
                </c:if>

                <!-- Back to Blog button -->
                <div class="content-detail-actions">
                    <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog" class="btn btn-outline">
                        <span class="arrow-icon" style="transform: rotate(180deg);">&rarr;</span> Back to Articles
                    </a>
                </div>
            </div>
        </section>

        <!-- Related Articles Section -->
        <section class="related-articles">
            <div class="container">
                <div class="section-header centered">
                    <h2>Related Articles</h2>
                    <p>Explore more content you might be interested in</p>
                </div>

                <div id="related-articles-container" class="blog-grid">
                    <!-- Related articles will be loaded here via JavaScript -->
                    <div class="loading-placeholder">Loading related articles...</div>
                </div>
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
    // Function to load related articles
    function loadRelatedArticles() {
        const contentUuid = "${content.contentUuid}";

        // AJAX call to fetch related articles
        fetch("<%= Environment.get(EnvironmentConstants.BASE_URL) %>/rest/api/1/content/related?contentUuid=" + contentUuid)
            .then(response => response.json())
            .then(data => {
                const container = document.getElementById('related-articles-container');

                // Clear loading placeholder
                container.innerHTML = '';

                if (data && data.list && data.list.length > 0) {
                    // Render related articles
                    data.list.forEach(article => {
                        container.appendChild(createArticleCard(article));
                    });
                } else {
                    // No related articles found
                    container.innerHTML = '<div class="no-content"><p>No related articles available at this time.</p></div>';
                }
            })
            .catch(error => {
                console.error('Error loading related articles:', error);
                document.getElementById('related-articles-container').innerHTML =
                    '<div class="no-content"><p>Unable to load related articles. Please try again later.</p></div>';
            });
    }

    // Function to create article card element
    function createArticleCard(article) {
        const card = document.createElement('div');
        card.className = 'blog-card';

        const imageUrl = article.headerImageUrl ||
            "<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/placeholder.png";

        card.innerHTML = `
            <div class="blog-card-image">
                <img src="${imageUrl}" alt="${article.cardTitle}" />
                <div class="blog-card-date-badge">${formatDate(article.publishedDate)}</div>
            </div>
            <div class="blog-card-content">
                <h3 class="blog-card-title">${article.cardTitle}</h3>
                <p class="blog-card-subtitle">${article.cardSubtitle}</p>
                <div class="blog-card-footer">
                    <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog/${article.contentUuid}" class="blog-card-link">
                        ${article.cardCTATitle || 'Continue Reading'} <span class="arrow-icon">&rarr;</span>
                    </a>
                </div>
            </div>
        `;

        return card;
    }

    // Helper function to format date
    function formatDate(dateString) {
        const date = new Date(dateString);
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        return `${months[date.getMonth()]} ${date.getDate()}`;
    }

    // Load related articles when the page loads
    document.addEventListener('DOMContentLoaded', loadRelatedArticles);

    // Social sharing functions
    function shareOnTwitter() {
        const url = encodeURIComponent(window.location.href);
        const text = encodeURIComponent("${content.title}");
        window.open(`https://twitter.com/intent/tweet?url=${url}&text=${text}`, '_blank');
    }

    function shareOnFacebook() {
        const url = encodeURIComponent(window.location.href);
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${url}`, '_blank');
    }

    function shareOnLinkedIn() {
        const url = encodeURIComponent(window.location.href);
        const title = encodeURIComponent("${content.title}");
        const summary = encodeURIComponent("${content.subtitle}");
        window.open(`https://www.linkedin.com/shareArticle?mini=true&url=${url}&title=${title}&summary=${summary}`, '_blank');
    }
</script>

<%@ include file="./include/footer-body.jsp" %>