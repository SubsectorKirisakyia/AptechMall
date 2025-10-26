# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

aptechMall is a Spring Boot 3.5.6 REST API that integrates with the AliExpress DataHub API via RapidAPI. The application provides a proxy service for searching AliExpress products and retrieving product details, designed to be consumed by frontend applications (React/Next.js/Vite).

**Tech Stack:**
- Java 17
- Spring Boot 3.5.6 (Spring Web MVC)
- Spring WebFlux (WebClient for external API calls)
- Maven
- Lombok
- Jackson (JSON processing)
- MySQL 8 (configured but not actively used for AliExpress integration)

## Build and Run Commands

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```
The application starts on the default port 8080.

### Run tests
```bash
mvn test
```

### Run a single test class
```bash
mvn test -Dtest=AptechMallApplicationTests
```

### Package as JAR
```bash
mvn clean package
```
The JAR file will be in `target/aptechMall-0.0.1-SNAPSHOT.jar`

## Architecture

### Layer Structure
The application follows a standard Spring Boot layered architecture:

1. **Controller Layer** (`com.aptech.aptechMall.Controller`)
   - `AliExpressController`: REST endpoints for AliExpress product operations
   - Returns reactive `Mono<ResponseEntity<T>>` types for async handling
   - Provides both full and simplified response endpoints

2. **Service Layer** (`com.aptech.aptechMall.service`)
   - `AliExpressService`: Business logic and external API integration
   - Uses Spring WebFlux `WebClient` for non-blocking HTTP calls to RapidAPI
   - Transforms complex AliExpress API responses into simplified DTOs

3. **DTO Layer** (`com.aptech.aptechMall.dto`)
   - `ProductSearchDTO`: Simplified search results for frontend
   - `ProductDetailDTO`: Simplified product details for frontend
   - `ApiResponse`: Generic API response wrapper

4. **Model Layer** (`com.aptech.aptechMall.model`)
   - `AliExpressProductResponse`: Maps complete AliExpress API responses
   - `ProductSearchResponse`: Maps search API responses
   - Complex nested structures matching AliExpress API schema

5. **Configuration** (`com.aptech.aptechMall.config`)
   - `CorsConfig`: CORS configuration for frontend origins (localhost:3000, 5173, 4200)
   - Uses `CorsFilter` (Spring MVC), NOT `CorsWebFilter` (WebFlux)

6. **Exception Handling** (`com.aptech.aptechMall.Exception`)
   - `GlobalExceptionHandler`: Centralized error handling with `@RestControllerAdvice`

### API Integration Pattern

The service uses a **dual-response strategy**:
- **Full endpoints**: Return complete AliExpress API responses (for debugging/advanced use)
- **Simplified endpoints**: Return cleaned DTOs with only essential fields (for frontend)

**Example:**
- `/api/aliexpress/products/{id}` → Full `AliExpressProductResponse`
- `/api/aliexpress/products/{id}/simple` → Simplified `ProductDetailDTO`

### WebClient Configuration

WebClient is configured in `AliExpressService` constructor:
- Base URL from `application.properties`
- API key and host injected via `@Value` annotations
- **Buffer size: 10MB** (increased from default 256KB to handle large API responses)
- 30-second timeout on all requests
- Detailed logging for debugging API responses

**Important:** The BatchSearchItemsFrame API can return large responses (>256KB), so the buffer size has been increased to 10MB using `ExchangeStrategies`.

### Reactive Flow

Even though the app uses Spring MVC (not WebFlux), it leverages `WebClient` for async external API calls:
1. Controller returns `Mono<ResponseEntity<T>>`
2. Service makes async call to AliExpress API
3. Response is transformed (map/flatMap operators)
4. Error handling via `onErrorResume` and global exception handler

## Configuration

### Required Environment Variables

The application expects RapidAPI credentials in `application.properties`:
```properties
rapidapi.aliexpress.key=${RAPIDAPI_ALIEXPRESS_KEY:default_key}
rapidapi.aliexpress.host=aliexpress-datahub.p.rapidapi.com
rapidapi.aliexpress.base-url=https://aliexpress-datahub.p.rapidapi.com
```

**IMPORTANT:** The default API key in the properties file should be replaced with environment variables in production.

### Database Configuration

MySQL is configured but not actively used for the AliExpress proxy functionality:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test_db
spring.datasource.username=root
spring.datasource.password=
```

### CORS Origins

Update `CorsConfig.java` if adding new frontend origins beyond:
- http://localhost:3000 (Next.js)
- http://localhost:5173 (Vite)
- http://localhost:4200 (Angular)

## API Endpoints

⚠️ **IMPORTANT UPDATE (October 2025):** The old API endpoint `/item_search_5` has been deprecated. All search endpoints now use the new `/BatchSearchItemsFrame` API.

All endpoints are under `/api/aliexpress`:

### Search Endpoints (Updated - BatchSearchItemsFrame API)
- `GET /search?keyword={q}&language=en&framePosition=0&frameSize=10` - Search products (full response)
- `GET /search/simple?keyword={q}&language=en&framePosition=0&frameSize=10` - Search products (simplified, **RECOMMENDED**)
- `GET /search/v2?keyword={q}&language=en&framePosition=0&frameSize=10` - Search products V2 (full response)
- `GET /search/v2/simple?keyword={q}&language=en&framePosition=0&frameSize=10` - Search products V2 (simplified)

### Product Details Endpoints
- `GET /products/{id}` - Get product details (full response)
- `GET /products/{id}/simple` - Get product details (simplified)
- `GET /products/{id}/reviews?page={n}` - Get product reviews

### Utility Endpoints
- `GET /health` - Health check endpoint

**Parameters:**
- ✅ `keyword` - Search term (required)
- ✅ `language` - Language code (optional, default: "en")
- ✅ `framePosition` - Starting position (optional, default: 0)
- ✅ `frameSize` - Items per page (optional, default: 10)

**Pagination:** Use frame-based pagination (framePosition = page * frameSize)

See `API_ENDPOINTS.md` for detailed documentation.

## Development Guidelines

### Adding New API Endpoints

1. Define model/DTO classes in respective packages
2. Add method to `AliExpressService` with WebClient call
3. Add controller method with proper error handling
4. Consider providing both full and simplified response versions

### Error Handling Pattern

All service methods should:
- Use `.doOnError()` for logging
- Let `GlobalExceptionHandler` handle exceptions centrally
- Controller methods use `.onErrorResume()` for custom fallback responses

### JSON Parsing Issues

The application has specific Jackson configuration to handle AliExpress API inconsistencies:
```properties
spring.jackson.serialization.fail-on-empty-beans=false
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.default-property-inclusion=non_null
```

When adding new model classes, use Lombok `@Data` and handle null values defensively.

### Testing External API Calls

Use the `/health` endpoint to verify the service is running. For API testing, the `DebugController` can be used for troubleshooting (if present).

## Common Pitfalls

1. **CORS Configuration**: This app uses Spring MVC, so use `CorsFilter` not `CorsWebFilter`
2. **Reactive vs Blocking**: WebClient calls are reactive but the app is MVC-based (not full WebFlux)
3. **API Rate Limits**: RapidAPI has rate limits; implement caching if needed
4. **Null Safety**: AliExpress API responses have many optional fields; always null-check nested objects
5. **Price Handling**: Prices are returned as strings (includes currency symbols); parse carefully if doing calculations
