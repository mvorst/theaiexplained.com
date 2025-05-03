<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<% request.setAttribute("htmlTitle", "Home"); %>

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

        <!-- Introduction Section -->
        <section class="intro">
            <c:choose>
                <c:when test="${empty homeContent.startHereContentList.list}">
                    <div class="no-content">
                        <p>No articles are available at this time. Check back soon!</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="container">
                        <div class="section-header centered">
                            <h2>AI Doesn't Have to Be Complicated</h2>
                            <p>We simplify AI concepts and show you practical applications that enhance your life.</p>
                        </div>
                        <div class="feature-cards">
                            <c:forEach var="content" items="${homeContent.startHereContentList.list}" begin="0" end="2" varStatus="status">
                                <c:set var="content" value="${content}" scope="request" />
                                <%@ include file="./include/content-card.jsp" %>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- Popular Resources Section -->
        <section class="popular-resources">
            <c:choose>
                <c:when test="${empty homeContent.resourcesContentList.list}">
                    <div class="no-content">
                        <p>No resources are available at this time. Check back soon!</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="container">
                        <div class="section-header centered">
                            <h2>Popular Resources</h2>
                            <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/resources.jsp" class="view-all">View All Resources →</a>
                        </div>
                        <div class="feature-cards">
                            <c:forEach var="content" items="${homeContent.resourcesContentList.list}" begin="0" end="2" varStatus="status">
                                <c:set var="content" value="${content}" scope="request" />
                                <%@ include file="./include/content-card.jsp" %>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- Featured Models Section -->
        <section class="featured-models">
            <c:choose>
                <c:when test="${empty homeContent.modelsContentList.list}">
                    <div class="no-content">
                        <p>No models are available at this time. Check back soon!</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="container">
                        <div class="section-header centered">
                            <h2>AI Models Explained</h2>
                            <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/models.jsp" class="view-all">View All Models →</a>
                        </div>
                        <div class="feature-cards">
                            <c:forEach var="content" items="${homeContent.modelsContentList.list}" begin="0" end="2" varStatus="status">
                                <c:set var="content" value="${content}" scope="request" />
                                <%@ include file="./include/content-card.jsp" %>
                            </c:forEach>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- Include newsletter signup component -->
        <%@ include file="./include/newsletter-signup.jsp" %>

<%--        <!-- Upcoming Events Section -->--%>
<%--        <section class="events">--%>
<%--            <div class="container">--%>
<%--                <div class="section-header">--%>
<%--                    <h2>Upcoming Events</h2>--%>
<%--                    <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/events.jsp" class="view-all">View All Events →</a>--%>
<%--                </div>--%>
<%--                <div class="events-list">--%>
<%--                    <div class="event-card">--%>
<%--                        <div class="event-date">--%>
<%--                            <span class="month">APR</span>--%>
<%--                            <span class="day">15</span>--%>
<%--                        </div>--%>
<%--                        <div class="event-details">--%>
<%--                            <h3>Webinar: Getting Started with AI Assistants</h3>--%>
<%--                            <p class="event-meta">Online • 2:00 PM EST • Free</p>--%>
<%--                            <p class="event-description">Learn how to effectively use AI assistants to streamline your workflow and increase productivity.</p>--%>
<%--                            <button class="btn btn-outline" onclick="registerEvent('webinar-ai-assistants')">Register Now</button>--%>
<%--                        </div>--%>
<%--                    </div>--%>
<%--                    <div class="event-card">--%>
<%--                        <div class="event-date">--%>
<%--                            <span class="month">APR</span>--%>
<%--                            <span class="day">22</span>--%>
<%--                        </div>--%>
<%--                        <div class="event-details">--%>
<%--                            <h3>Workshop: AI for Small Business Owners</h3>--%>
<%--                            <p class="event-meta">Online • 1:00 PM EST • $29</p>--%>
<%--                            <p class="event-description">Discover affordable AI solutions that can help small businesses compete with larger companies.</p>--%>
<%--                            <button class="btn btn-outline" onclick="registerEvent('workshop-small-business')">Register Now</button>--%>
<%--                        </div>--%>
<%--                    </div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </section>--%>

<%--        <!-- Consulting CTA Section -->--%>
<%--        <section class="consulting-cta">--%>
<%--            <div class="container">--%>
<%--                <div class="cta-content">--%>
<%--                    <div class="cta-text">--%>
<%--                        <h2>Need Personalized AI Guidance?</h2>--%>
<%--                        <p>Our experts can help you integrate AI solutions tailored to your specific business needs.</p>--%>
<%--                        <button class="btn btn-primary btn-lg" onclick="bookConsultation()">Book a Free Consultation</button>--%>
<%--                    </div>--%>
<%--                    <div class="cta-image">--%>
<%--                        <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/consulting.jpg" alt="AI Consulting" />--%>
<%--                    </div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </section>--%>

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