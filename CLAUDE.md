# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

www.theBridgeToAI.com is a website devoted to helping people learn how they can learn about AI and apply it to their lives. It contains a blog with interesting articles, tips, news articles, and useful resources.

Alternate domain: www.theBridgeTo.ai

## Build Commands

### Java/Spring Boot Backend
- **Build WAR file**: `./gradlew makeWar` - Creates `build/BridgeToAI_Webapp.war`
- **Build docs bundle**: `./gradlew makeDocs` - Creates docs ZIP for deployment
- **Build util JAR**: `./gradlew makeUtilJar`
- **Run all tests**: `./gradlew test`
- **Run single test class**: `./gradlew test --tests "com.thebridgetoai.website.service.ContentServiceTest"`
- **Run single test method**: `./gradlew test --tests "com.thebridgetoai.website.service.ContentServiceTest.getContent_ReturnsContent_WhenContentExists"`
- **Run Spring Boot app**: `./gradlew bootRun`

### Frontend (React/Vite)
Navigate to `src/main/docs/` for frontend commands:
- **Install dependencies**: `npm install`
- **Build JS bundles**: `npm run build` (runs rollup for admin.jsx and login.jsx)
- **Development server**: `npm run dev`
- **Lint**: `npm run lint`

## Architecture Overview

### Backend Structure
- **Spring Boot 4.0.0** application with **Spring Framework 7.0** and **Java 17**
- **Gradle 8.14.3** for build automation
- **Multi-module source structure**:
  - `src/main/java` - Main application code
  - `src/util/java` - Utility classes (deployment tools)
  - `src/proto/java` - Prototype/one-off utilities
  - `src/sample/java` - Sample/example code
- **Shared framework**: `com.mattvorst.shared` provides reusable components (security, logging, AWS services, etc.)
- **Application package**: `com.thebridgetoai.website` contains the main website logic

### Key Backend Components
- **Security**: JWT-based authentication with Spring Security 7.0 OAuth2 (`SecurityConfig.java`, `JwtService.java`)
- **Data layer**: DynamoDB with enhanced client, S3 for file storage
- **Controllers**: REST controllers for admin, auth, content, newsletter, and web pages
- **Content management**: Content entities with categories (`ContentCategoryType`), featured content, and associations
- **Newsletter system**: Template-based newsletters with `EmailTemplateProcessor` and Thymeleaf
- **Async processing**: Task processor system (`AppTaskProcessor`) for background operations
- **Tomcat configuration**: Custom AJP connector setup in `TomcatConfig.java`

### Frontend Structure
- **React 18** with React Router for navigation
- **Build system**: Rollup for production bundles (outputs to `src/main/docs/dist/`), Vite for development
- **Admin interface**: `js/admin.jsx` - content management, newsletters, assets, accounts
- **Public interface**: `js/login.jsx` - authentication components
- **Styling**: CSS files in `style/` directory

### JSP Templates
- Server-side rendering with JSP templates in `src/main/webapp/WEB-INF/jsp/`
- Includes system for reusable components (`include/` directory)
- Integration between JSP and React components via script includes

### AWS Integration
- **DynamoDB**: Primary data store with attribute converters for enums
- **S3**: File storage and upload handling
- **SES**: Email services for newsletters
- **SQS**: Logging and async message processing

### Development Notes
- Environment configuration through `Environment.instance()` - can be passed as first argument to main method
- Tests use JUnit 5 with Mockito for mocking (`@ExtendWith(MockitoExtension.class)`)
- The application uses a custom auditable entity pattern (`DefaultAuditable`) for tracking creation/modification
- Image processing capabilities with cropping and SVG support via Apache Batik
- Jackson 2 compatibility mode enabled via `spring.jackson.use-jackson2-defaults=true`

## Spring Boot 4.0 Notes

This project uses Spring Boot 4.0 with its modularized architecture. Key package locations:

| Class | Package |
|-------|---------|
| `TomcatServletWebServerFactory` | `org.springframework.boot.tomcat.servlet` |
| `AutoConfigureMockMvc` | `org.springframework.boot.webmvc.test.autoconfigure` |

### Starters Used
- `spring-boot-starter-webmvc` (replaces `spring-boot-starter-web`)
- `spring-boot-starter-thymeleaf`
- `spring-boot-jackson2` (Jackson 2 compatibility)
- `spring-boot-starter-webmvc-test` (for MockMvc tests)