# AliExpress API Endpoints Documentation

## ⚠️ Important Changes (October 2025)

The old AliExpress DataHub API endpoint `/item_search_5` has been **deprecated** and replaced with `/BatchSearchItemsFrame`. All endpoints have been updated to use the new API.

## 🔄 Migration Guide

### Old Parameters → New Parameters
- ❌ `q` / `keyword` (removed)
- ❌ `page` (removed)
- ❌ `sort` (removed)
- ✅ `ItemTitle` (required) - Search keyword
- ✅ `language` (optional, default: "en")
- ✅ `framePosition` (optional, default: 0)
- ✅ `frameSize` (optional, default: 10)

### Example Migration

**Before:**
```
GET /api/aliexpress/search/simple?keyword=iphone&page=1&sort=0
```

**After:**
```
GET /api/aliexpress/search/simple?keyword=iphone&language=en&framePosition=0&frameSize=10
```

---

## 📍 Available Endpoints

### 1. **Search Products (Simplified)** ⭐ RECOMMENDED
**Endpoint:** `GET /api/aliexpress/search/simple`

**Parameters:**
- `keyword` (required) - Search term (mapped to ItemTitle internally)
- `language` (optional, default: "en") - Language code
- `framePosition` (optional, default: 0) - Starting position (0, 10, 20, ...)
- `frameSize` (optional, default: 10) - Number of items per page

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/search/simple?keyword=iphone&language=en&framePosition=0&frameSize=10"
```

**Response:** `ProductSearchDTO` with enriched data:
```json
{
  "meta": {
    "keyword": "iphone",
    "currentPage": 1,
    "pageSize": 10,
    "totalResults": 182991,
    "sortOptions": ["Default", "PriceAsc", "PriceDesc", "Sales"]
  },
  "products": [
    {
      "itemId": "ae-1005005891237816",
      "itemIdNumeric": "1005005891237816",
      "title": "Product Title",
      "imageUrl": "https://...",
      "productUrl": "https://www.aliexpress.us/item/...",
      "currentPrice": "0.99",
      "originalPrice": "2.86",
      "currencySign": "$",
      "salesCount": 4000,
      "hasDiscount": true,
      "rating": "4.5",
      "reviewCount": "321",
      "vendorName": "3C Phone Screen Protector And Case Store",
      "brandName": "YTHUIYT",
      "promotionPercent": 65,
      "imageUrls": ["https://...", "https://..."]
    }
  ]
}
```

---

### 2. **Search Products (Full Response)**
**Endpoint:** `GET /api/aliexpress/search`

**Parameters:**
- `keyword` (required) - Search term
- `language` (optional, default: "en") - Language code
- `framePosition` (optional, default: 0) - Starting position
- `frameSize` (optional, default: 10) - Items per page

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/search?keyword=iphone&language=en&framePosition=0&frameSize=10"
```

**Response:** `NewProductSearchResponse` (complete raw API response)

---

### 3. **Search Products V2 (Full Response)**
**Endpoint:** `GET /api/aliexpress/search/v2`

Same as endpoint #2, alternative route with same parameters.

---

### 4. **Search Products V2 (Simplified)**
**Endpoint:** `GET /api/aliexpress/search/v2/simple`

Same as endpoint #1, alternative route with same parameters.

---

### 5. **Get Product Details (Full)**
**Endpoint:** `GET /api/aliexpress/products/{productId}`

**Parameters:**
- `productId` (path parameter) - Product ID (numeric part)

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/products/1005005891237816"
```

**Response:** `AliExpressProductResponse`

---

### 6. **Get Product Details (Simplified)**
**Endpoint:** `GET /api/aliexpress/products/{productId}/simple`

**Parameters:**
- `productId` (path parameter) - Product ID

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/products/1005005891237816/simple"
```

**Response:** `ProductDetailDTO`

---

### 7. **Get Product Reviews**
**Endpoint:** `GET /api/aliexpress/products/{productId}/reviews`

**Parameters:**
- `productId` (path parameter) - Product ID
- `page` (optional, default: 1) - Page number

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/products/1005005891237816/reviews?page=1"
```

---

### 8. **Health Check**
**Endpoint:** `GET /api/aliexpress/health`

**Example:**
```bash
curl "http://localhost:8080/api/aliexpress/health"
```

**Response:**
```json
{
  "status": "UP",
  "service": "AliExpress API"
}
```

---

## 🆕 New Features (New API)

The new API provides additional data not available in the old API:

✅ **Product Rating** - Average rating (e.g., "4.5")
✅ **Review Count** - Number of reviews
✅ **Vendor/Store Name** - Store display name
✅ **Brand Name** - Product brand
✅ **Promotion Percentage** - Discount percentage
✅ **Multiple Images** - Array of image URLs
✅ **Total Sales** - Number of items sold
✅ **Featured Values** - Additional metadata

---

## 🔧 Configuration

Make sure you have the following in your `application.properties`:

```properties
rapidapi.aliexpress.key=${RAPIDAPI_ALIEXPRESS_KEY:your_api_key_here}
rapidapi.aliexpress.host=aliexpress-datahub.p.rapidapi.com
rapidapi.aliexpress.base-url=https://aliexpress-datahub.p.rapidapi.com
```

**⚠️ IMPORTANT:** Replace `your_api_key_here` with your actual RapidAPI key or set the `RAPIDAPI_ALIEXPRESS_KEY` environment variable.

---

## 📝 Notes

1. **Pagination:** The new API uses simple page numbers (1, 2, 3...) instead of frame positions
2. **Sorting:** The new API doesn't support custom sorting via parameters (uses default sorting)
3. **Rate Limits:** Check your RapidAPI subscription for rate limits
4. **CORS:** Configured for `localhost:5173` and `localhost:3000` by default

---

## 🐛 Troubleshooting

### Error: "Endpoint '/item_search_5' does not exist"
**Solution:** Update your application code to use the new endpoints. The old API has been deprecated.

### Error: "Missing API key"
**Solution:** Make sure your RapidAPI key is properly configured in `application.properties` or as an environment variable.

### Error: "404 Not Found"
**Solution:** Verify you're using the correct endpoint path and HTTP method (GET).

### Error: "DataBufferLimitException: Exceeded limit on max bytes to buffer"
**Problem:** API response is too large (>256KB default buffer size)

**Solution:** ✅ Already fixed! The WebClient is configured with 10MB buffer size in `AliExpressService`:
```java
ExchangeStrategies strategies = ExchangeStrategies.builder()
    .codecs(configurer -> configurer
        .defaultCodecs()
        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
    .build();
```

**Why this happens:** The BatchSearchItemsFrame API returns large JSON responses (especially with frameSize=50+), which can exceed the default 256KB buffer limit.

---

## 🚀 Quick Start

```bash
# 1. Clone the repository
git clone <repository-url>

# 2. Set your API key
export RAPIDAPI_ALIEXPRESS_KEY="your-api-key-here"

# 3. Build the project
mvn clean install

# 4. Run the application
mvn spring-boot:run

# 5. Test the API
curl "http://localhost:8080/api/aliexpress/search/simple?keyword=iphone&language=en&framePosition=0&frameSize=10"
```

---

## 📊 Pagination Guide

The BatchSearchItemsFrame API uses frame-based pagination:

- **First page (items 0-9):** `framePosition=0&frameSize=10`
- **Second page (items 10-19):** `framePosition=10&frameSize=10`
- **Third page (items 20-29):** `framePosition=20&frameSize=10`
- **Custom page size (items 0-19):** `framePosition=0&frameSize=20`

**Formula:** `framePosition = (page - 1) * frameSize`

---

**Last Updated:** October 23, 2025
**API Version:** BatchSearchItemsFrame (AliExpress DataHub)
