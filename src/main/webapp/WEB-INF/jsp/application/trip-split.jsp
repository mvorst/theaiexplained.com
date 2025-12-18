<% request.setAttribute("htmlTitle", "Trip Splitter"); %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%@ include file="../include/header-body.jsp" %>

<div id="app_container">
    <!-- Homepage content converted from React JSX to JSP/HTML -->
    <div class="homepage">
        <!-- Include header component -->
        <%@ include file="../include/navigation-header.jsp" %>

        <div id="app_container"></div>

        <!-- Include newsletter signup component -->
        <%@ include file="../include/newsletter-signup.jsp" %>

        <!-- Include footer component -->
        <%@ include file="../include/footer.jsp" %>
    </div>
</div>

<%@ include file="../include/footer-body.jsp" %>