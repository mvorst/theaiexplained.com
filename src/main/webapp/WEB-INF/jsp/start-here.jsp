<% request.setAttribute("htmlTitle", "Start Here"); %>

<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%@ include file="./include/header-body.jsp" %>

<div id="app_container">
    <div class="homepage">
        <!-- Include header component -->
        <%@ include file="./include/navigation-header.jsp" %>

        <!-- Hero Section -->
        <section class="hero">
            <div class="container">
                <div class="hero-content">
                    <div class="hero-text">
                        <h1>A Friendly Introduction to AI</h1>
                        <p class="subtitle">Understanding artificial intelligence doesn't have to be complicated. We're here to help you feel confident and curious about this technology.</p>
                        <div class="hero-cta">
                            <button class="btn btn-primary btn-lg" onclick="location.href='#what-is-ai'">Learn About AI</button>
                            <button class="btn btn-secondary btn-lg" onclick="location.href='#daily-life'">See Real-Life Examples</button>
                        </div>
                    </div>
                    <div class="hero-image">
                        <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/intro_ai.png" alt="AI Explained Simply" />
                    </div>
                </div>
            </div>
        </section>

        <!-- Introduction Section -->
        <section class="intro" id="intro">
            <div class="container">
                <div class="section-header centered">
                    <h2>Welcome to Your AI Journey</h2>
                    <p>Whether you're 18 or 80, tech-savvy or just getting started, this guide is for you. Our goal is to keep things simple, friendly, and fun so you can feel curious and hopeful about the future instead of overwhelmed.</p>
                </div>
            </div>
        </section>

        <!-- What Is AI Section -->
        <section class="content-section bg-light" id="what-is-ai">
            <div class="container">
                <div class="content-detail-container">
                    <h2 class="content-detail-title">What Is AI? (In Plain Language)</h2>
                    <div class="content-detail-body">
                        <p>Artificial Intelligence (AI) means teaching computers to learn and make decisions similar to how humans do, but without the jargon. In simple terms, AI is like a smart helper: it enables your phone or computer to recognize your voice, identify faces in photos, or suggest useful information automatically.</p>

                        <p>AI isn't a magical robot or a sci-fi genius – it's a computer program with lots of examples and practice. For example, if you show a computer many pictures of cats and dogs, it can learn to tell a cat from a dog over time.</p>

                        <p>In short, AI learns from experience (much like we do) and helps machines perform everyday tasks that normally require human smarts. Everything is happening behind the scenes, so you don't have to worry about how it works – just know that AI is there to assist you in simpler ways than you might think.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Why AI Matters Section -->
        <section class="content-section" id="why-matters">
            <div class="container">
                <div class="content-detail-container">
                    <h2 class="content-detail-title">Why AI Matters to You</h2>
                    <div class="content-detail-body">
                        <p>You might be wondering, "Why should I care about AI?" The answer is: AI is already part of your daily life, often in ways you don't notice.</p>

                        <p>AI matters because it can make everyday tasks easier, safer, and more enjoyable for you. For instance, AI helps filter out spam emails, so you only see the messages that matter. It powers navigation apps (like Google Maps) to find the best route and streaming services (like Netflix or Spotify) to recommend movies or music you might love.</p>

                        <p>Even if you didn't grow up with this technology, AI is becoming as common as electricity – it's just there, working for you. By understanding a bit about AI, you'll feel more comfortable using modern gadgets and services.</p>

                        <p>You'll be better prepared to take advantage of helpful tools (and avoid any pitfalls) as technology advances. In short, AI matters because it's here to help you, and knowing a little about it can empower you to live more independently, stay connected with family, and feel confident in our fast-changing world.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Common Misconceptions Section -->
        <section class="content-section bg-light" id="misconceptions">
            <div class="container">
                <div class="content-detail-container">
                    <h2 class="content-detail-title">Common Misconceptions about AI</h2>
                    <div class="content-detail-body">
                        <p>Let's clear up a few myths you might have heard about AI. Here are some common misconceptions (and the reality behind them):</p>

                        <div class="misconception-card">
                            <h3>"AI is only for young people or tech experts."</h3>
                            <p><strong>Reality:</strong> Not true! Many AI-powered tools are designed to be user-friendly for everyone, including older adults and beginners. If you can tap an app or ask a voice assistant a question, you can use AI. No special technical skill is required to enjoy its benefits.</p>
                        </div>

                        <div class="misconception-card">
                            <h3>"AI is a scary robot that will take over the world."</h3>
                            <p><strong>Reality:</strong> Despite what movies show, AI today is usually invisible and task-specific – it might be sorting your email or adjusting your thermostat, not plotting world domination. AI is a tool created and controlled by people to help with specific jobs. Think of it as a helpful assistant, not an all-powerful robot overlord.</p>
                        </div>

                        <div class="misconception-card">
                            <h3>"AI will replace humans and take away jobs or human contact."</h3>
                            <p><strong>Reality:</strong> AI is meant to assist people, not replace them. It can handle repetitive chores or analyze information quickly, but it can't replace human warmth, creativity, or judgment. In many cases, AI frees up time so real people (like doctors, customer service reps, or even your family members) can spend more time caring for you or focusing on human-to-human interaction.</p>
                        </div>

                        <div class="misconception-card">
                            <h3>"I'll lose my privacy or security if I use AI."</h3>
                            <p><strong>Reality:</strong> It's smart to be mindful of privacy, but using AI doesn't automatically mean giving up safety. Many AI applications, like fraud detectors and security systems, are actually there to protect you. For example, your bank's AI might flag an unusual charge to prevent theft, or your email's AI will filter suspicious messages. You stay in control by choosing what data to share and using trusted services.</p>
                        </div>

                        <p>Feel free to ask questions or explore our resources if you have other concerns – part of being informed is knowing the facts behind the fiction!</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Daily Life Examples Section -->
        <section class="content-section" id="daily-life">
            <div class="container">
                <div class="content-detail-container">
                    <h2 class="content-detail-title">How AI Can Help in Daily Life</h2>
                    <div class="content-detail-body">
                        <p>One of the best ways to understand AI is to see it in action in everyday life. Here are a few friendly examples of how AI can help you on a day-to-day basis:</p>

                        <div class="feature-cards">
                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/voice-assistant.png" alt="Voice Assistant Icon" />
                                </div>
                                <h3>Voice Assistants and Smart Speakers</h3>
                                <p>Devices like Amazon Alexa, Google Assistant, or Siri on your phone are powered by AI. You can simply talk to them to set reminders ("Alexa, remind me to take my 2 PM pill"), ask questions about the weather, play your favorite music, or even make a hands-free phone call.</p>
                            </div>

                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/photos.png" alt="Photos Icon" />
                                </div>
                                <h3>Organizing Photos and Memories</h3>
                                <p>Ever wondered how your smartphone groups your photos by faces or places? That's AI at work. Photo apps can recognize your family's faces and gather all those pictures in one album automatically, or sort your vacation shots by location.</p>
                            </div>

                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/security.png" alt="Security Icon" />
                                </div>
                                <h3>Fraud Detection and Scam Prevention</h3>
                                <p>Banks and credit card companies use AI to spot unusual activity – if a purchase looks suspicious, the system can alert you or freeze the charge to prevent fraud. Email services use AI to filter out phishing scams and spam emails.</p>
                            </div>
                        </div>

                        <div class="feature-cards">
                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/health.png" alt="Health Icon" />
                                </div>
                                <h3>Health and Wellness Tools</h3>
                                <p>Smartwatches and health apps can track your steps, heart rate, or sleep patterns and alert you to changes that might need attention. Some can even detect if you've fallen and send an alert for help. AI can also remind you to take your medications on time.</p>
                            </div>

                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/entertainment.png" alt="Entertainment Icon" />
                                </div>
                                <h3>Entertainment and Hobbies</h3>
                                <p>Streaming services like Netflix, YouTube, or Spotify use AI to recommend movies, shows, or music tailored to your tastes. If you enjoy reading, AI can suggest articles or books you'd find interesting. Into cooking or gardening? AI-powered apps can provide customized tips.</p>
                            </div>

                            <div class="feature-card">
                                <div class="icon">
                                    <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/more.png" alt="More Examples Icon" />
                                </div>
                                <h3>And Much More</h3>
                                <p>AI is showing up in many parts of daily life, often simplifying tasks or adding a bit of extra convenience. The key takeaway is that AI is here to help, in small helpful ways, making life a little easier for everyone.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Staying Curious Section -->
        <section class="content-section bg-light" id="staying-curious">
            <div class="container">
                <div class="content-detail-container">
                    <h2 class="content-detail-title">Staying Curious Without Feeling Overwhelmed</h2>
                    <div class="content-detail-body">
                        <p>Technology is changing fast, and it's normal to feel a bit overwhelmed at times. The good news is, you don't have to learn everything at once. This website is here to help you explore AI step by step, at your own pace.</p>

                        <p>Being curious is the first step – and you've already taken it by being here! Remember, it's okay not to have all the answers. Even experts are always learning, because AI is a big field.</p>

                        <p>Our advice: stay curious and take small steps. If something sounds confusing, that's alright – we'll break it down for you in plain language. If you read about a new AI gadget or app, and it interests you, give it a try or read our beginner-friendly explanation of it.</p>

                        <p>And if you ever start feeling overwhelmed, take a deep breath – you're in control of what you want to learn or use. There's no rush and no test at the end. Think of learning about AI like exploring a new hobby: you can dip your toe in, have a look around, and do as much or as little as you're comfortable with.</p>

                        <p>We're here to make sure learning about AI stays fun, positive, and pressure-free.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Next Steps Section -->
        <section class="content-section" id="next-steps">
            <div class="container">
                <div class="section-header centered">
                    <h2>Explore Further: Next Steps</h2>
                    <p>This "Start Here" page is just the beginning. We've prepared lots of easy-to-read resources and fun activities for you to continue your AI journey.</p>
                </div>

                <div class="feature-cards">
                    <div class="feature-card">
                        <div class="icon">
                            <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/blog.png" alt="Blog Icon" />
                        </div>
                        <h3>Read Our Blog</h3>
                        <p>Check out our blog posts for stories and simple explanations of how AI is used in different areas of life. From AI in healthcare to smart home tips, our articles focus on real-life examples.</p>
                        <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/blog" class="card-link">Visit the Blog →</a>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/guides.png" alt="Guides Icon" />
                        </div>
                        <h3>Beginner-Friendly Guides</h3>
                        <p>Dive into our step-by-step guides on topics like "How to Try a Voice Assistant", "Using AI Features on Your Phone", or "Staying Safe Online with AI's Help".</p>
                        <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/resources" class="card-link">Browse Guides →</a>
                    </div>

                    <div class="feature-card">
                        <div class="icon">
                            <img src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/img/start-here/interactive.png" alt="Interactive Icon" />
                        </div>
                        <h3>Interactive Learning</h3>
                        <p>If you're feeling adventurous, try out an interactive demo or tutorial. For example, we have a simple AI chatbot you can talk to right in your web browser – kind of like texting a helpful computer friend.</p>
                        <a href="<%= Environment.get(EnvironmentConstants.BASE_URL) %>/interactive" class="card-link">Try Interactive Tools →</a>
                    </div>
                </div>

                <div class="cta-content" style="margin-top: 3rem; text-align: center;">
                    <p>Each of these next steps is optional – choose whatever piques your interest. The important thing is to keep exploring in a way that you enjoy. We're constantly adding new content and tools, so there will always be something new to discover when you're ready.</p>
                </div>
            </div>
        </section>

        <!-- Newsletter Signup Section -->
        <%@ include file="./include/newsletter-signup.jsp" %>

        <!-- Include footer component -->
        <%@ include file="./include/footer.jsp" %>
    </div>
</div>

<!-- Add page-specific JavaScript functions -->
<script type="text/javascript">
  // Smooth scrolling for anchor links
  document.addEventListener('DOMContentLoaded', function() {
    const links = document.querySelectorAll('a[href^="#"]');

    for (const link of links) {
      link.addEventListener('click', function(e) {
        e.preventDefault();

        const targetId = this.getAttribute('href').substring(1);
        const targetElement = document.getElementById(targetId);

        if (targetElement) {
          window.scrollTo({
            top: targetElement.offsetTop - 100,
            behavior: 'smooth'
          });
        }
      });
    }
  });
</script>

<%@ include file="./include/footer-body.jsp" %>