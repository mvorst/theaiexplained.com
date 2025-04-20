<%@ page import="com.mattvorst.shared.util.Environment" %>
<%@ page import="com.mattvorst.shared.constant.EnvironmentConstants" %>

<!-- Footer -->
<footer class="footer">
    <div class="container">
        <div class="footer-content">
            <div class="footer-logo">
                <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/LogoLight.svg" alt="The AI Explained Logo" />
                <p>Empowering everyone to thrive alongside AI</p>
            </div>
            <div class="footer-links">
                <div class="footer-column">
                    <h4>Quick Links</h4>
                    <ul>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/index.jsp">Home</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/start-here.jsp">Start Here</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/learn-ai.jsp">Learn AI</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/resources.jsp">Resources</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog.jsp">Blog</a></li>
                    </ul>
                </div>
                <div class="footer-column">
                    <h4>Learn</h4>
                    <ul>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/ai-101.jsp">AI 101</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/guides.jsp">Guides & Tutorials</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/industries.jsp">AI by Industry</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/case-studies.jsp">Case Studies</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/models.jsp">AI Models</a></li>
                    </ul>
                </div>
                <div class="footer-column">
                    <h4>Connect</h4>
                    <ul>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/about.jsp">About Us</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/contact.jsp">Contact</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/events.jsp">Events</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/community.jsp">Community</a></li>
                        <li><a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/consulting.jsp">Consulting</a></li>
                    </ul>
                </div>
            </div>
            <div class="footer-social">
                <h4>Follow Us</h4>
                <div class="social-icons">
                    <a href="https://twitter.com/theaiexplained" class="social-icon">Tw</a>
                    <a href="https://facebook.com/theaiexplained" class="social-icon">Fb</a>
                    <a href="https://linkedin.com/company/theaiexplained" class="social-icon">In</a>
                    <a href="https://youtube.com/c/theaiexplained" class="social-icon">Yt</a>
                </div>
            </div>
        </div>
        <div class="footer-bottom">
            <p>© <%= new java.util.Date().getYear() + 1900 %> TheAIExplained.com. All rights reserved.</p>
            <div class="footer-legal">
                <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/privacy-policy.jsp">Privacy Policy</a>
                <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/terms-of-use.jsp">Terms of Use</a>
                <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/cookie-policy.jsp">Cookie Policy</a>
            </div>
        </div>
    </div>
</footer>

<!-- Common JavaScript functions -->
<script type="text/javascript">
  // Add any common JavaScript functions here
</script>