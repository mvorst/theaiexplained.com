<%@ page import="com.mattvorst.shared.util.Environment" %>
<%@ page import="com.mattvorst.shared.constant.EnvironmentConstants" %>

<!-- Newsletter Section -->
<section class="newsletter">
  <div class="container">
    <div class="newsletter-content">
      <div class="newsletter-text">
        <h2>Stay Updated on AI Advancements</h2>
        <p>Join our newsletter for weekly insights, tutorials, and tips on making the most of AI.</p>
      </div>
      <form class="newsletter-form" action="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/rest/api/1/subscribe" method="post">
        <input type="email" name="email" placeholder="Your email address" class="newsletter-input" required />
        <button type="submit" class="btn btn-primary">Subscribe</button>
      </form>
    </div>
  </div>
</section>