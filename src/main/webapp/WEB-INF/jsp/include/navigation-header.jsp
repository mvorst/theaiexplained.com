<%@ page import="com.mattvorst.shared.util.Environment" %>
<%@ page import="com.mattvorst.shared.constant.EnvironmentConstants" %>

<!-- Header -->
<header class="header">
    <div class="container">
        <div class="header-content">
            <div class="logo">
                <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/index.jsp"><img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/LogoH.svg" alt="The AI Explained Logo" /></a>
            </div>
            <nav class="main-nav">
                <ul>
                    <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/start-here.jsp">Start Here</a></li>
                    <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/learn-ai.jsp">Learn AI</a></li>
                    <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/resources.jsp">Resources</a></li>
                    <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog.jsp">Blog</a></li>
                    <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/about.jsp">About</a></li>
                </ul>
            </nav>
            <div class="header-buttons">
                <button class="btn btn-primary" onclick="location.href='<%= Environment.get(EnvironmentConstants.BASE_URL) %>/get-started.jsp'">Get Started</button>
                <button class="btn btn-outline" onclick="location.href='<%= Environment.get(EnvironmentConstants.BASE_URL) %>/newsletter.jsp'">Newsletter</button>
            </div>
        </div>
    </div>
</header>