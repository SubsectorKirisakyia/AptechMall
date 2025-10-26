# Alibaba 1688 API Integration Guide

**Version:** 1.0.0
**Date:** 2025-10-24
**Status:** ✅ PRODUCTION READY

---

## Overview

The aptechMall application now supports **Alibaba 1688** marketplace alongside AliExpress. Both platforms share the same API structure and return consistent DTO formats to the frontend.

**Key Features:**
- ✅ Product search with pagination
- ✅ Product details retrieval
- ✅ Product reviews
- ✅ Consistent DTOs across platforms
- ✅ Type-safe Integer returns for ratings/reviews
- ✅ Support for special formats ("1.2k", "5k+")

---

## Quick Start

### 1. Configuration

Add your 1688 API credentials to `application.properties` or use environment variables:

```properties
# Alibaba 1688 API Configuration
rapidapi.alibaba1688.key=${RAPIDAPI_ALIBABA1688_KEY:your_api_key_here}
rapidapi.alibaba1688.host=otapi-alibaba1688.p.rapidapi.com
rapidapi.alibaba1688.base-url=https://otapi-alibaba1688.p.rapidapi.com
```

### 2. Start the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/1688
```

---

### 1. Search Products (Simplified) ⭐ RECOMMENDED

**Endpoint:** `GET /api/1688/search/simple`

**Description:** Search 1688 products with simplified response format (platform-agnostic DTO)

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| keyword | String | ✅ Yes | - | Search keyword (supports Chinese) |
| language | String | ❌ No | "en" | Language code |
| framePosition | Integer | ❌ No | 0 | Starting position (for pagination) |
| frameSize | Integer | ❌ No | 12 | Number of items per page |

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/search/simple?keyword=联想&frameSize=10"
```

**Example Response:**
```json
{
  "meta": {
    "keyword": "联想",
    "currentPage": 1,
    "pageSize": 10,
    "totalResults": 41825,
    "sortOptions": ["Default", "PriceAsc", "PriceDesc", "Sales"]
  },
  "products": [
    {
      "itemId": "abb-802318698033",
      "itemIdNumeric": "802318698033",
      "title": "联想（Lenovo）thinkplus口红电源65W GaN充电器",
      "imageUrl": "https://cbu01.alicdn.com/img/ibank/...",
      "productUrl": "https://detail.1688.com/offer/802318698033.html",
      "currentPrice": "14.90",
      "originalPrice": "16.90",
      "currencySign": "¥",
      "salesCount": 78,
      "hasDiscount": true,
      "rating": 5,
      "reviewCount": 400,
      "vendorName": "b2b-2210916576640aeb98",
      "brandName": "Lenovo/联想",
      "promotionPercent": 12,
      "imageUrls": ["...", "..."]
    }
  ]
}
```

---

### 2. Search Products (Full Response)

**Endpoint:** `GET /api/1688/search`

**Description:** Get raw 1688 API response (for advanced use/debugging)

**Query Parameters:** Same as simplified endpoint

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/search?keyword=联想&frameSize=10"
```

---

### 3. Search with Page-Based Pagination

**Endpoint:** `GET /api/1688/search/page`

**Description:** Alternative search endpoint with traditional page/pageSize parameters

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| keyword | String | ✅ Yes | - | Search keyword |
| page | Integer | ❌ No | 1 | Page number (starts from 1) |
| pageSize | Integer | ❌ No | 10 | Items per page |

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/search/page?keyword=联想&page=2&pageSize=10"
```

**Pagination Formula:**
```
framePosition = (page - 1) * pageSize
```

---

### 4. Get Product Details

**Endpoint:** `GET /api/1688/products/{productId}`

**Description:** Get full product details by ID

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| productId | String | Product ID (numeric only, without "abb-" prefix) |

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/products/802318698033"
```

**Example Response:**
```json
{
  "ErrorCode": "Ok",
  "RequestId": "...",
  "Result": {
    "Item": {
      "Id": "abb-802318698033",
      "Title": "联想（Lenovo）thinkplus口红电源65W GaN充电器",
      "Price": {
        "OriginalPrice": 16.90,
        "ConvertedPrice": "16.90¥",
        "CurrencySign": "¥"
      },
      ...
    }
  }
}
```

---

### 5. Get Product Reviews

**Endpoint:** `GET /api/1688/products/{productId}/reviews`

**Description:** Get product reviews with pagination

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| productId | String | Product ID |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | ❌ No | 1 | Page number |

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/products/802318698033/reviews?page=1"
```

---

### 6. Health Check

**Endpoint:** `GET /api/1688/health`

**Description:** Check if 1688 service is running

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/health"
```

**Example Response:**
```json
{
  "status": "UP",
  "service": "Alibaba 1688 API",
  "marketplace": "Alibaba1688"
}
```

---

### 7. Marketplace Info

**Endpoint:** `GET /api/1688/info`

**Description:** Get information about 1688 marketplace

**Example Request:**
```bash
curl "http://localhost:8080/api/1688/info"
```

**Example Response:**
```json
{
  "marketplace": "Alibaba1688",
  "description": "Alibaba 1688 is China's leading wholesale marketplace",
  "currency": "CNY (¥)",
  "language": "Chinese (Simplified)",
  "baseUrl": "https://www.1688.com",
  "endpoints": {
    "search": "/api/1688/search/simple?keyword={keyword}",
    "productDetails": "/api/1688/products/{productId}",
    "reviews": "/api/1688/products/{productId}/reviews?page={page}"
  }
}
```

---

## DTO Structure

### ProductSearchDTO (Simplified Response)

**Platform-Agnostic:** Same structure for both AliExpress and 1688

```typescript
interface ProductSearchDTO {
  meta: {
    keyword: string;
    currentPage: number;
    pageSize: number;
    totalResults: number;
    sortOptions: string[];
  };
  products: ProductSummaryDTO[];
}

interface ProductSummaryDTO {
  itemId: string;              // "abb-802318698033"
  itemIdNumeric: string;       // "802318698033"
  title: string;
  imageUrl: string;
  productUrl: string;
  currentPrice: string;        // "14.90"
  originalPrice: string | null;
  currencySign: string;        // "¥" for 1688
  salesCount: number | null;
  hasDiscount: boolean;
  rating: number;              // Integer (0-5)
  reviewCount: number;         // Integer (parsed from "1.2k" → 1200)
  vendorName: string;
  brandName: string;
  promotionPercent: number | null;
  imageUrls: string[];
}
```

---

## Features & Improvements

### ✅ Fixed Issues (from AliExpress model)

1. **Correct ID Prefix:** Uses `"abb-"` instead of `"ae-"`
2. **Integer Types:** `getRating()` and `getReviewCount()` return `Integer` (not `String`)
3. **Special Format Parsing:**
   - `"4.5"` → `5` (rounded)
   - `"1.2k"` → `1200`
   - `"5k+"` → `5000`
   - `"1.5m"` → `1500000`
4. **Added TaobaoItemUrl Field:** Captures 1688-specific URL

### 🆕 1688-Specific Features

1. **Currency:** Default currency sign is `¥` (CNY)
2. **TaobaoItemUrl:** Prefers `taobaoItemUrl` over `externalItemUrl` when available
3. **Chinese Support:** Handles Chinese keywords and titles
4. **Wholesale Pricing:** Supports 1688's wholesale price structure

---

## Comparison: AliExpress vs 1688

| Feature | AliExpress | 1688 |
|---------|-----------|------|
| **Endpoint Prefix** | `/api/aliexpress` | `/api/1688` |
| **ID Prefix** | `ae-` | `abb-` |
| **Default Currency** | USD ($) | CNY (¥) |
| **Language** | English | Chinese |
| **Target Market** | B2C (Retail) | B2B (Wholesale) |
| **DTO Structure** | ✅ Same | ✅ Same |
| **Rating Type** | `Integer` | `Integer` |
| **ReviewCount Type** | `Integer` | `Integer` |

---

## Frontend Integration Example

### React/Next.js

```typescript
// Search 1688 products
const search1688Products = async (keyword: string, page: number = 1) => {
  const response = await fetch(
    `http://localhost:8080/api/1688/search/simple?keyword=${encodeURIComponent(keyword)}&page=${page}`
  );
  const data: ProductSearchDTO = await response.json();
  return data;
};

// Get product details
const get1688ProductDetails = async (productId: string) => {
  const response = await fetch(
    `http://localhost:8080/api/1688/products/${productId}`
  );
  const data = await response.json();
  return data;
};

// Usage
const results = await search1688Products("联想", 1);
console.log(`Found ${results.meta.totalResults} products`);
console.log(`First product: ${results.products[0].title}`);
```

---

## Error Handling

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Product not found |
| 500 | Internal server error or API error |

### Example Error Response

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "1688 API Error: Invalid API key"
}
```

---

## Rate Limits

- **RapidAPI Free Tier:** Typically 100-500 requests/month
- **Recommended:** Implement caching for frequently accessed products
- **Best Practice:** Use simplified endpoints to reduce response size

---

## Testing with cURL

### Test Search
```bash
curl -X GET "http://localhost:8080/api/1688/search/simple?keyword=lenovo&frameSize=5" \
  -H "Accept: application/json"
```

### Test Product Details
```bash
curl -X GET "http://localhost:8080/api/1688/products/802318698033" \
  -H "Accept: application/json"
```

### Test Health Check
```bash
curl -X GET "http://localhost:8080/api/1688/health"
```

---

## Troubleshooting

### Issue: "401 Unauthorized"
**Solution:** Check your API key in `application.properties`

### Issue: "No products found"
**Solution:** Try Chinese keywords (1688 is a Chinese marketplace)

### Issue: "DataBufferLimitException"
**Solution:** Buffer size is already set to 10MB in `Alibaba1688Service`

### Issue: "NumberFormatException"
**Solution:** Already fixed in the updated model (uses try-catch with fallbacks)

---

## Architecture

### Service Layer

```java
@Service
public class Alibaba1688Service implements ProductMarketplaceService {
    // Implements standard interface for consistency
    Mono<ProductSearchDTO> searchProducts(String keyword, int page, int sort);
    Mono<ProductDetailDTO> getProductDetails(String productId);
    Mono<String> getProductReviews(String productId, int page);
    String getMarketplaceName(); // Returns "Alibaba1688"
}
```

### Controller Layer

```java
@RestController
@RequestMapping("/api")
public class Alibaba1688Controller {
    // Provides REST endpoints for 1688 operations
    // Returns reactive Mono<ResponseEntity<T>> types
}
```

---

## Future Enhancements

- [ ] Implement full `ProductDetailDTO` transformation
- [ ] Add caching layer (Redis)
- [ ] Implement rate limiting
- [ ] Add search filters (price range, rating, etc.)
- [ ] Support for product comparison between AliExpress and 1688
- [ ] Unified search endpoint (`/api/marketplace/search?platform=1688`)

---

## Related Documentation

- [API_ENDPOINTS.md](./API_ENDPOINTS.md) - AliExpress API documentation
- [MULTI_PLATFORM_FIXES_SUMMARY.md](./MULTI_PLATFORM_FIXES_SUMMARY.md) - Multi-platform fixes
- [CLAUDE.md](./CLAUDE.md) - Project guidelines

---

## Support

For issues or questions:
1. Check logs: `logging.level.com.aptech.aptechMall=DEBUG`
2. Test with health endpoint: `/api/1688/health`
3. Verify API key is valid in RapidAPI dashboard
4. Check CORS settings if calling from frontend

---

**Happy coding! 🚀**
