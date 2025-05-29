# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

www.theBridgeToAI.com is a website devoted to helping people learn how they can learn about AI and apply it to their lives. It contains a blog with interesting articles, tips, news articles, and useful resources.

Alternate domain: www.theBridgeTo.ai

## Build Commands

### Java/Spring Boot Backend
- **Build WAR file**: `./gradlew makeWar` - Creates `AIExplained_Webapp-CI.war`
- **Build docs bundle**: `./gradlew makeDocs` - Creates `AIExplained_Docs-CI.zip`
- **Build util JAR**: `./gradlew makeUtilJar` - Creates `AIExplained_Utils-CI.jar`
- **Run tests**: `./gradlew test`
- **Run Spring Boot app**: `./gradlew bootRun`

### Frontend (React/Vite)
Navigate to `src/main/docs/` for frontend commands:
- **Build JS bundles**: `npm run build` (runs rollup build script)
- **Development server**: `npm run dev`
- **Lint**: `npm run lint`

## Architecture Overview

### Backend Structure
- **Spring Boot 3.4.4** application with Java 17
- **Multi-module source structure**: 
  - `src/main/java` - Main application code
  - `src/util/java` - Utility classes
  - `src/proto/java` - Prototype/one-off utilities
  - `src/sample/java` - Sample/example code
- **Shared framework**: `com.mattvorst.shared` provides reusable components (security, logging, AWS services, etc.)
- **Application package**: `com.thebridgetoai.website` contains the main website logic

### Key Backend Components
- **Security**: JWT-based authentication with Spring Security OAuth2
- **Data layer**: DynamoDB with enhanced client, S3 for file storage
- **Controllers**: Separate admin and public web controllers
- **Content management**: Content entities with categories, featured content, and associations
- **Async processing**: Task processor system for background operations
- **Logging**: Custom SQS appender for centralized logging

### Frontend Structure
- **React 18** with React Router for navigation
- **Build system**: Rollup for production bundles, Vite for development
- **Admin interface**: Separate admin React app (`js/admin.jsx`)
- **Public interface**: Login and content display components
- **Styling**: CSS files in `style/` directory

### JSP Templates
- Server-side rendering with JSP templates in `src/main/webapp/WEB-INF/jsp/`
- Includes system for reusable components (`include/` directory)
- Integration between JSP and React components

### AWS Integration
- **DynamoDB**: Primary data store with attribute converters for enums
- **S3**: File storage and upload handling
- **SES**: Email services
- **SQS**: Logging and async message processing

### Development Notes
- Environment configuration through `Environment.instance()` - can be passed as first argument to main method
- The application uses a custom auditable entity pattern for tracking creation/modification
- Image processing capabilities with cropping and SVG support via Apache Batik