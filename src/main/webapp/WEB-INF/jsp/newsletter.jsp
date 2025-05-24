<% request.setAttribute("htmlTitle", "Newsletter"); %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%@ include file="./include/header-body.jsp" %>

<div id="app_container">
    <!-- Newsletter preview page -->
    <div class="homepage">
        <!-- Include header component -->
        <%@ include file="./include/navigation-header.jsp" %>

        <!-- Newsletter Hero Section -->
        <section class="newsletter-hero">
            <div class="container">
                <div class="newsletter-hero-content">
                    <h1>Stay Updated with AI Insights</h1>
                    <p class="hero-subtitle">Get weekly newsletters with the latest AI developments, tutorials, and practical tips delivered to your inbox.</p>
                    
                    <div class="newsletter-stats">
                        <div class="stat-item">
                            <span class="stat-number">1000+</span>
                            <span class="stat-label">Subscribers</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">Weekly</span>
                            <span class="stat-label">Updates</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-number">Free</span>
                            <span class="stat-label">Forever</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Newsletter Signup Section -->
        <section class="newsletter-signup-section">
            <div class="container">
                <div class="signup-card">
                    <h2>Subscribe to Our Newsletter</h2>
                    <p>Get weekly AI insights and tutorials delivered to your inbox.</p>
                    
                    <form class="newsletter-form" id="newsletterSignupForm">
                        <div class="form-group">
                            <input type="email" 
                                   id="email" 
                                   name="email" 
                                   placeholder="Enter your email address" 
                                   class="newsletter-input" 
                                   required />
                        </div>
                        <div class="form-group">
                            <input type="text" 
                                   id="firstName" 
                                   name="firstName" 
                                   placeholder="First name (optional)" 
                                   class="newsletter-input" />
                        </div>
                        <button type="submit" class="btn btn-primary btn-large">Subscribe</button>
                        <p class="privacy-note">We respect your privacy. Unsubscribe at any time.</p>
                    </form>
                    
                    <div id="signup-message" class="signup-message" style="display: none;"></div>
                </div>
            </div>
        </section>

        <!-- Latest Newsletter Preview -->
        <c:if test="${not empty newsletter}">
            <section class="newsletter-preview">
                <div class="container">
                    <div class="section-header centered">
                        <h2>Latest Newsletter</h2>
                        <p>Here's a preview of our most recent newsletter</p>
                    </div>

                    <div class="newsletter-preview-card">
                        <div class="newsletter-header">
                            <div class="newsletter-meta">
                                <h3>${newsletter.title}</h3>
                                <p class="newsletter-subject">${newsletter.subject}</p>
                                <div class="newsletter-date">
                                    <fmt:formatDate value="${newsletter.sentDate}" pattern="MMMM dd, yyyy" />
                                </div>
                            </div>
                        </div>
                        
                        <div class="newsletter-content">
                            <c:if test="${not empty newsletter.previewText}">
                                <div class="newsletter-preview-text">
                                    ${newsletter.previewText}
                                </div>
                            </c:if>
                            
                            <div class="newsletter-body">
                                ${newsletter.htmlContent}
                            </div>
                        </div>
                        
                        <div class="newsletter-footer">
                            <p>Like what you see? Subscribe above to get newsletters like this delivered to your inbox!</p>
                        </div>
                    </div>
                </div>
            </section>
        </c:if>

        <!-- Newsletter Benefits Section -->
        <section class="newsletter-benefits">
            <div class="container">
                <div class="section-header centered">
                    <h2>What You'll Get</h2>
                    <p>Our newsletter delivers value every week</p>
                </div>

                <div class="benefits-grid">
                    <div class="benefit-item">
                        <div class="benefit-icon">🤖</div>
                        <h3>Latest AI News</h3>
                        <p>Stay informed about the newest developments in artificial intelligence and machine learning.</p>
                    </div>
                    <div class="benefit-item">
                        <div class="benefit-icon">📚</div>
                        <h3>Tutorials & Guides</h3>
                        <p>Step-by-step tutorials to help you understand and implement AI technologies.</p>
                    </div>
                    <div class="benefit-item">
                        <div class="benefit-icon">💡</div>
                        <h3>Practical Tips</h3>
                        <p>Actionable insights and tips you can apply to your work or personal projects.</p>
                    </div>
                    <div class="benefit-item">
                        <div class="benefit-icon">🔍</div>
                        <h3>Tool Reviews</h3>
                        <p>Honest reviews and comparisons of the latest AI tools and platforms.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Include footer -->
        <%@ include file="./include/footer.jsp" %>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('newsletterSignupForm');
    const messageDiv = document.getElementById('signup-message');
    
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const formData = new FormData(form);
        const email = formData.get('email');
        const firstName = formData.get('firstName');
        
        try {
            const response = await fetch('/rest/api/1/subscribe', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    firstName: firstName
                })
            });
            
            if (response.ok) {
                messageDiv.className = 'signup-message success';
                messageDiv.textContent = 'Thank you for subscribing! Please check your email to confirm your subscription.';
                messageDiv.style.display = 'block';
                form.reset();
            } else {
                throw new Error('Subscription failed');
            }
        } catch (error) {
            messageDiv.className = 'signup-message error';
            messageDiv.textContent = 'Sorry, something went wrong. Please try again later.';
            messageDiv.style.display = 'block';
        }
    });
});
</script>

<style>
/* Newsletter Page Styles */
.newsletter-hero {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 80px 0;
    text-align: center;
}

.newsletter-hero-content h1 {
    font-size: 3.5rem;
    margin-bottom: 1rem;
    font-weight: 700;
}

.hero-subtitle {
    font-size: 1.2rem;
    margin-bottom: 3rem;
    opacity: 0.9;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
}

.newsletter-stats {
    display: flex;
    justify-content: center;
    gap: 3rem;
    margin-top: 2rem;
}

.stat-item {
    text-align: center;
}

.stat-number {
    display: block;
    font-size: 2rem;
    font-weight: 700;
}

.stat-label {
    display: block;
    font-size: 0.9rem;
    opacity: 0.8;
    margin-top: 0.5rem;
}

.newsletter-signup-section {
    padding: 60px 0;
    background: #f8f9fa;
}

.signup-card {
    background: white;
    padding: 3rem;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(0,0,0,0.1);
    text-align: center;
    max-width: 500px;
    margin: 0 auto;
}

.signup-card h2 {
    margin-bottom: 1rem;
    color: #333;
    font-size: 2rem;
}

.signup-card p {
    margin-bottom: 2rem;
    color: #666;
    font-size: 1.1rem;
}

.newsletter-form .form-group {
    margin-bottom: 1.5rem;
}

.newsletter-input {
    width: 100%;
    padding: 15px;
    border: 2px solid #e1e5e9;
    border-radius: 8px;
    font-size: 1rem;
    transition: border-color 0.3s ease;
}

.newsletter-input:focus {
    outline: none;
    border-color: #667eea;
}

.btn-large {
    padding: 15px 40px;
    font-size: 1.1rem;
    width: 100%;
    margin-bottom: 1rem;
}

.privacy-note {
    font-size: 0.85rem;
    color: #666;
    margin: 0;
}

.newsletter-preview {
    padding: 60px 0;
}

.newsletter-preview-card {
    background: white;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(0,0,0,0.1);
    overflow: hidden;
    max-width: 800px;
    margin: 0 auto;
}

.newsletter-header {
    background: #f8f9fa;
    padding: 2rem;
    border-bottom: 1px solid #e1e5e9;
}

.newsletter-meta h3 {
    margin-bottom: 0.5rem;
    color: #333;
}

.newsletter-subject {
    font-style: italic;
    color: #666;
    margin-bottom: 1rem;
}

.newsletter-date {
    font-size: 0.9rem;
    color: #999;
}

.newsletter-content {
    padding: 2rem;
}

.newsletter-preview-text {
    background: #e3f2fd;
    padding: 1rem;
    border-radius: 8px;
    margin-bottom: 2rem;
    font-style: italic;
}

.newsletter-body {
    line-height: 1.6;
}

.newsletter-footer {
    background: #f8f9fa;
    padding: 1.5rem 2rem;
    text-align: center;
    color: #666;
    border-top: 1px solid #e1e5e9;
}

.newsletter-benefits {
    padding: 60px 0;
    background: white;
}

.benefits-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 2rem;
    margin-top: 3rem;
}

.benefit-item {
    text-align: center;
    padding: 2rem;
}

.benefit-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.benefit-item h3 {
    margin-bottom: 1rem;
    color: #333;
}

.benefit-item p {
    color: #666;
    line-height: 1.6;
}

.signup-message {
    padding: 1rem;
    border-radius: 8px;
    margin-top: 1rem;
    text-align: center;
}

.signup-message.success {
    background: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
}

.signup-message.error {
    background: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}

/* Responsive */
@media (max-width: 768px) {
    .newsletter-hero-content h1 {
        font-size: 2.5rem;
    }
    
    .newsletter-stats {
        flex-direction: column;
        gap: 1.5rem;
    }
    
    .signup-card {
        padding: 2rem;
        margin: 0 1rem;
    }
    
    .benefits-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 480px) {
    .newsletter-signup-section {
        padding: 40px 0;
    }
    
    .signup-card {
        padding: 1.5rem;
        margin: 0 0.5rem;
    }
    
    .signup-card h2 {
        font-size: 1.5rem;
    }
}
</style>

<%@ include file="./include/footer-body.jsp" %>