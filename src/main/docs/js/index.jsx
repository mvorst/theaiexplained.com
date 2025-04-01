import React from 'react';
import {createRoot} from "react-dom/client";

const Homepage = () => {
  return (
    <div className="homepage">
      {/* Header */}
      <header className="header">
        <div className="container">
          <div className="header-content">
            <div className="logo">
              <a href="#"><img src="/img/LogoH.svg" alt="The AI Explained Logo" /></a>
            </div>
            <nav className="main-nav">
              <ul>
                <li><a href="#">Start Here</a></li>
                <li><a href="#">Learn AI</a></li>
                <li><a href="#">Resources</a></li>
                <li><a href="#">Blog</a></li>
                <li><a href="#">About</a></li>
              </ul>
            </nav>
            <div className="header-buttons">
              <button className="btn btn-primary">Get Started</button>
              <button className="btn btn-outline">Newsletter</button>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero">
        <div className="container">
          <div className="hero-content">
            <div className="hero-text">
              <h1>Demystifying AI for Everyone</h1>
              <p className="subtitle">Gain the confidence and skills to thrive alongside AI in your career and daily life.</p>
              <div className="hero-cta">
                <button className="btn btn-primary btn-lg">Explore AI Basics</button>
                <button className="btn btn-secondary btn-lg">Find Tools for Your Industry</button>
              </div>
            </div>
            <div className="hero-image">
              <img src="/api/placeholder/500/300" alt="AI and Human Collaboration" />
            </div>
          </div>
        </div>
      </section>

      {/* Introduction Section */}
      <section className="intro">
        <div className="container">
          <div className="section-header centered">
            <h2>AI Doesn't Have to Be Complicated</h2>
            <p>We simplify AI concepts and show you practical applications that enhance your work and life.</p>
          </div>
          <div className="feature-cards">
            <div className="feature-card">
              <div className="icon">
                <img src="/api/placeholder/50/50" alt="Learn AI" />
              </div>
              <h3>Learn AI Fundamentals</h3>
              <p>Understand the basics of AI and how it can help your work.</p>
              <a href="#" className="card-link">Start Learning →</a>
            </div>
            <div className="feature-card">
              <div className="icon">
                <img src="/api/placeholder/50/50" alt="Industry Solutions" />
              </div>
              <h3>Industry Applications</h3>
              <p>Discover how AI is being used in your specific field.</p>
              <a href="#" className="card-link">Browse Industries →</a>
            </div>
            <div className="feature-card">
              <div className="icon">
                <img src="/api/placeholder/50/50" alt="Practical Tools" />
              </div>
              <h3>Practical AI Tools</h3>
              <p>Find the right AI tools to enhance your productivity.</p>
              <a href="#" className="card-link">Explore Tools →</a>
            </div>
          </div>
        </div>
      </section>

      {/* Popular Resources Section */}
      <section className="popular-resources">
        <div className="container">
          <div className="section-header">
            <h2>Popular Resources</h2>
            <a href="#" className="view-all">View All Resources →</a>
          </div>
          <div className="resources-grid">
            <div className="resource-card">
              <div className="resource-image">
                <img src="/api/placeholder/320/180" alt="Getting Started with ChatGPT" />
              </div>
              <div className="resource-content">
                <span className="tag">Tutorial</span>
                <h3>Getting Started with ChatGPT</h3>
                <p>Learn how to effectively use ChatGPT for everyday tasks and professional work.</p>
                <a href="#" className="resource-link">Read Tutorial →</a>
              </div>
            </div>
            <div className="resource-card">
              <div className="resource-image">
                <img src="/api/placeholder/320/180" alt="AI Models Compared" />
              </div>
              <div className="resource-content">
                <span className="tag">Guide</span>
                <h3>AI Models Compared</h3>
                <p>Understand the differences between popular AI models and which to use when.</p>
                <a href="#" className="resource-link">Read Guide →</a>
              </div>
            </div>
            <div className="resource-card">
              <div className="resource-image">
                <img src="/api/placeholder/320/180" alt="AI in Marketing" />
              </div>
              <div className="resource-content">
                <span className="tag">Industry</span>
                <h3>AI in Marketing</h3>
                <p>Discover how marketers are leveraging AI to enhance campaigns and analytics.</p>
                <a href="#" className="resource-link">Read Article →</a>
              </div>
            </div>
            <div className="resource-card">
              <div className="resource-image">
                <img src="/api/placeholder/320/180" alt="Busting AI Myths" />
              </div>
              <div className="resource-content">
                <span className="tag">Insights</span>
                <h3>Busting Common AI Myths</h3>
                <p>Separate fact from fiction about AI capabilities and limitations.</p>
                <a href="#" className="resource-link">Read Article →</a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Success Stories Section */}
      <section className="success-stories">
        <div className="container">
          <div className="section-header centered">
            <h2>Success Stories</h2>
            <p>Real people seeing real results with AI in their careers</p>
          </div>
          <div className="stories-slider">
            <div className="story-card">
              <div className="story-content">
                <blockquote>
                  "Learning how to use AI tools transformed my productivity as a content marketer. I now complete in hours what used to take days."
                </blockquote>
                <div className="author">
                  <img src="/api/placeholder/60/60" alt="Sarah J." className="author-image" />
                  <div className="author-info">
                    <h4>Sarah J.</h4>
                    <p>Content Marketing Manager</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="story-navigation">
              <button className="nav-btn prev">←</button>
              <div className="nav-dots">
                <span className="dot active"></span>
                <span className="dot"></span>
                <span className="dot"></span>
              </div>
              <button className="nav-btn next">→</button>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Models Section */}
      <section className="featured-models">
        <div className="container">
          <div className="section-header">
            <h2>AI Models Explained</h2>
            <a href="#" className="view-all">View All Models →</a>
          </div>
          <div className="models-grid">
            <div className="model-card">
              <img src="/api/placeholder/80/80" alt="ChatGPT" className="model-logo" />
              <h3>ChatGPT</h3>
              <p>Conversational AI by OpenAI for text-based interactions and content creation.</p>
              <a href="#" className="model-link">Learn More →</a>
            </div>
            <div className="model-card">
              <img src="/api/placeholder/80/80" alt="Claude" className="model-logo" />
              <h3>Claude</h3>
              <p>Anthropic's AI assistant designed for helpfulness, harmlessness, and honesty.</p>
              <a href="#" className="model-link">Learn More →</a>
            </div>
            <div className="model-card">
              <img src="/api/placeholder/80/80" alt="Gemini" className="model-logo" />
              <h3>Gemini</h3>
              <p>Google's multimodal AI model with text, image, and code capabilities.</p>
              <a href="#" className="model-link">Learn More →</a>
            </div>
            <div className="model-card">
              <img src="/api/placeholder/80/80" alt="DALL-E" className="model-logo" />
              <h3>DALL-E</h3>
              <p>Image generation AI by OpenAI that creates visuals from text descriptions.</p>
              <a href="#" className="model-link">Learn More →</a>
            </div>
          </div>
        </div>
      </section>

      {/* Newsletter Section */}
      <section className="newsletter">
        <div className="container">
          <div className="newsletter-content">
            <div className="newsletter-text">
              <h2>Stay Updated on AI Advancements</h2>
              <p>Join our newsletter for weekly insights, tutorials, and tips on making the most of AI.</p>
            </div>
            <form className="newsletter-form">
              <input type="email" placeholder="Your email address" className="newsletter-input" />
              <button type="submit" className="btn btn-primary">Subscribe</button>
            </form>
          </div>
        </div>
      </section>

      {/* Upcoming Events Section */}
      <section className="events">
        <div className="container">
          <div className="section-header">
            <h2>Upcoming Events</h2>
            <a href="#" className="view-all">View All Events →</a>
          </div>
          <div className="events-list">
            <div className="event-card">
              <div className="event-date">
                <span className="month">APR</span>
                <span className="day">15</span>
              </div>
              <div className="event-details">
                <h3>Webinar: Getting Started with AI Assistants</h3>
                <p className="event-meta">Online • 2:00 PM EST • Free</p>
                <p className="event-description">Learn how to effectively use AI assistants to streamline your workflow and increase productivity.</p>
                <button className="btn btn-outline">Register Now</button>
              </div>
            </div>
            <div className="event-card">
              <div className="event-date">
                <span className="month">APR</span>
                <span className="day">22</span>
              </div>
              <div className="event-details">
                <h3>Workshop: AI for Small Business Owners</h3>
                <p className="event-meta">Online • 1:00 PM EST • $29</p>
                <p className="event-description">Discover affordable AI solutions that can help small businesses compete with larger companies.</p>
                <button className="btn btn-outline">Register Now</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Consulting CTA Section */}
      <section className="consulting-cta">
        <div className="container">
          <div className="cta-content">
            <div className="cta-text">
              <h2>Need Personalized AI Guidance?</h2>
              <p>Our experts can help you integrate AI solutions tailored to your specific business needs.</p>
              <button className="btn btn-primary btn-lg">Book a Free Consultation</button>
            </div>
            <div className="cta-image">
              <img src="/api/placeholder/400/250" alt="AI Consulting" />
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-logo">
              <img src="/api/placeholder/150/40" alt="The AI Explained Logo" />
              <p>Empowering everyone to thrive alongside AI</p>
            </div>
            <div className="footer-links">
              <div className="footer-column">
                <h4>Quick Links</h4>
                <ul>
                  <li><a href="#">Home</a></li>
                  <li><a href="#">Start Here</a></li>
                  <li><a href="#">Learn AI</a></li>
                  <li><a href="#">Resources</a></li>
                  <li><a href="#">Blog</a></li>
                </ul>
              </div>
              <div className="footer-column">
                <h4>Learn</h4>
                <ul>
                  <li><a href="#">AI 101</a></li>
                  <li><a href="#">Guides & Tutorials</a></li>
                  <li><a href="#">AI by Industry</a></li>
                  <li><a href="#">Case Studies</a></li>
                  <li><a href="#">AI Models</a></li>
                </ul>
              </div>
              <div className="footer-column">
                <h4>Connect</h4>
                <ul>
                  <li><a href="#">About Us</a></li>
                  <li><a href="#">Contact</a></li>
                  <li><a href="#">Events</a></li>
                  <li><a href="#">Community</a></li>
                  <li><a href="#">Consulting</a></li>
                </ul>
              </div>
            </div>
            <div className="footer-social">
              <h4>Follow Us</h4>
              <div className="social-icons">
                <a href="#" className="social-icon">Tw</a>
                <a href="#" className="social-icon">Fb</a>
                <a href="#" className="social-icon">In</a>
                <a href="#" className="social-icon">Yt</a>
              </div>
            </div>
          </div>
          <div className="footer-bottom">
            <p>© 2025 TheAIExplained.com. All rights reserved.</p>
            <div className="footer-legal">
              <a href="#">Privacy Policy</a>
              <a href="#">Terms of Use</a>
              <a href="#">Cookie Policy</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Homepage;

createRoot(document.getElementById('app_container')).render(<Homepage />);

