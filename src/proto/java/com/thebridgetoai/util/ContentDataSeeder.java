package com.thebridgetoai.util;

import java.util.UUID;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.util.Environment;
import com.thebridgetoai.website.dao.ContentDao;
import com.thebridgetoai.website.dao.model.Content;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentDataSeeder {

	public static void main(String[] args) {

		Environment.instance(EnvironmentConstants.ENV_VORST);

		ContentDao contentDao = new ContentDao("theaiexplained-ci");


		contentDao.saveContent(createLearnAIFundamentalsContent()).join();
		contentDao.saveContent(createAIPromptingTipsContent()).join();

		System.out.println("Complete");
		System.exit(0);
	}

	private static Content createLearnAIFundamentalsContent() {
		Content content = new Content();

		content.setContentUuid(UUID.randomUUID());

		// Card information
		content.setCardTitle("Learn AI Fundamentals");
		content.setCardSubtitle("Understand the basics of AI and how it can help your work.");
		content.setCardCTATitle("Start Learning");
		content.setCardHeaderImageUrl("https://www.theaiexplained.com/images/learn-ai-fundamentals.jpg");

		// Content information
		content.setTitle("AI Fundamentals: Getting Started with Artificial Intelligence");
		content.setSubtitle("A beginner's guide to understanding AI and how it can benefit your career");
		content.setHeaderImageUrl("https://www.theaiexplained.com/images/ai-fundamentals-header.jpg");

		// Rich content
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("<h2>What is Artificial Intelligence?</h2>");
		contentBuilder.append("<p>Artificial Intelligence (AI) refers to computer systems designed to perform tasks that typically require human intelligence. These tasks include learning, reasoning, problem-solving, perception, and language understanding.</p>");

		contentBuilder.append("<h2>Key Concepts in AI</h2>");
		contentBuilder.append("<p>Before diving deep into AI applications, it's important to understand these foundational concepts:</p>");
		contentBuilder.append("<ul>");
		contentBuilder.append("<li><strong>Machine Learning</strong>: Systems that learn from data rather than being explicitly programmed</li>");
		contentBuilder.append("<li><strong>Neural Networks</strong>: Computing systems inspired by the human brain's structure</li>");
		contentBuilder.append("<li><strong>Deep Learning</strong>: Advanced neural networks with multiple layers</li>");
		contentBuilder.append("<li><strong>Natural Language Processing</strong>: AI systems that can understand and generate human language</li>");
		contentBuilder.append("</ul>");

		contentBuilder.append("<h2>Getting Started with AI Tools</h2>");
		contentBuilder.append("<p>Many AI tools are now accessible without technical expertise. Here are some ways to start incorporating AI into your workflow:</p>");
		contentBuilder.append("<ol>");
		contentBuilder.append("<li>Use AI writing assistants to improve your communication</li>");
		contentBuilder.append("<li>Try AI image generators to create visuals for your projects</li>");
		contentBuilder.append("<li>Explore AI research tools to gather information more efficiently</li>");
		contentBuilder.append("<li>Implement AI-powered analytics to gain insights from your data</li>");
		contentBuilder.append("</ol>");

		contentBuilder.append("<h2>The Future of Work with AI</h2>");
		contentBuilder.append("<p>Rather than replacing humans, AI is creating new opportunities for those who learn to work alongside it. By understanding AI capabilities and limitations, you can position yourself to thrive in an AI-augmented workplace.</p>");

		content.setMarkupContent(contentBuilder.toString());

		// Reference information
		content.setReferenceUrl("https://www.theaiexplained.com/resources/ai-glossary");
		content.setReferenceUrlTitle("AI Glossary: Learn More Terms");

		// SEO metadata
		content.setMetaTitle("AI Fundamentals: Getting Started with Artificial Intelligence | theBridgeToAI.com");
		content.setMetaDescription("Learn the basics of artificial intelligence, key AI concepts, and how to incorporate AI tools into your daily workflow to enhance productivity and career growth.");
		content.setMetaType("article");
		content.setMetaUrl("https://www.theaiexplained.com/learn/ai-fundamentals");
		content.setMetaImage("https://www.theaiexplained.com/images/ai-fundamentals-social.jpg");
		content.setMetaTwitterImageAltText("AI Fundamentals learning guide from theBridgeToAI.com");
		content.setMetaTwiterCard("summary_large_image");
		content.setMetaTwitterSite("@theaiexplained");

		return content;
	}

	private static Content createAIPromptingTipsContent() {
		Content content = new Content();

		content.setContentUuid(UUID.randomUUID());

		// Card information
		content.setCardTitle("AI Prompting Tips");
		content.setCardSubtitle("Learn how to craft effective prompts for better AI results.");
		content.setCardCTATitle("Improve Your Prompts");
		content.setCardHeaderImageUrl("https://www.theaiexplained.com/images/prompting-tips.jpg");

		// Content information
		content.setTitle("The Art of Prompting: How to Get Better Results from AI");
		content.setSubtitle("Master the skill of crafting effective prompts to unlock AI's full potential");
		content.setHeaderImageUrl("https://www.theaiexplained.com/images/prompting-header.jpg");

		// Rich content
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("<h2>Why Prompting Matters</h2>");
		contentBuilder.append("<p>The way you communicate with AI systems significantly impacts the quality of responses you receive. Good prompting is the difference between getting generic, unhelpful answers and precise, valuable insights tailored to your needs.</p>");

		contentBuilder.append("<h2>Essential Prompting Techniques</h2>");
		contentBuilder.append("<h3>1. Be Specific and Clear</h3>");
		contentBuilder.append("<p>Vague prompts lead to vague responses. Include relevant details and context in your prompt.</p>");
		contentBuilder.append("<p><strong>Instead of:</strong> \"Write about climate change.\"</p>");
		contentBuilder.append("<p><strong>Try:</strong> \"Write a 300-word explanation of how climate change affects marine ecosystems, focusing on coral reefs.\"</p>");

		contentBuilder.append("<h3>2. Define the Format</h3>");
		contentBuilder.append("<p>Specify the format you want the response in.</p>");
		contentBuilder.append("<p><strong>Example:</strong> \"Create a bullet-point list of 5 strategies for reducing workplace stress.\"</p>");

		contentBuilder.append("<h3>3. Assume a Role</h3>");
		contentBuilder.append("<p>Ask the AI to respond from a particular perspective or expertise level.</p>");
		contentBuilder.append("<p><strong>Example:</strong> \"Explain quantum computing as if you're teaching a 10-year-old.\"</p>");

		contentBuilder.append("<h3>4. Use Iterative Prompting</h3>");
		contentBuilder.append("<p>Start with a basic prompt, then refine based on the response to gradually get to your desired output.</p>");

		contentBuilder.append("<h2>Advanced Prompting Strategies</h2>");
		contentBuilder.append("<p>Once you've mastered the basics, try these advanced techniques:</p>");
		contentBuilder.append("<ul>");
		contentBuilder.append("<li><strong>Chain of Thought</strong>: Ask the AI to walk through its reasoning step by step</li>");
		contentBuilder.append("<li><strong>Few-Shot Learning</strong>: Provide examples of the type of response you want</li>");
		contentBuilder.append("<li><strong>Negative Prompting</strong>: Specify what you don't want in the response</li>");
		contentBuilder.append("</ul>");

		content.setMarkupContent(contentBuilder.toString());

		// Reference information
		content.setReferenceUrl("https://www.theaiexplained.com/resources/prompting-examples");
		content.setReferenceUrlTitle("View Our Prompting Examples Library");

		// SEO metadata
		content.setMetaTitle("The Art of AI Prompting: Get Better Results from AI | TheAIExplained");
		content.setMetaDescription("Learn essential techniques for crafting effective AI prompts. Improve your interactions with AI systems and get more accurate, relevant, and useful responses.");
		content.setMetaType("article");
		content.setMetaUrl("https://www.theaiexplained.com/learn/ai-prompting-tips");
		content.setMetaImage("https://www.theaiexplained.com/images/prompting-social.jpg");
		content.setMetaTwitterImageAltText("AI Prompting Tips from theBridgeToAI.com");
		content.setMetaTwiterCard("summary_large_image");
		content.setMetaTwitterSite("@theaiexplained");

		return content;
	}
}