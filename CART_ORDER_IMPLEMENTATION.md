# Cart and Order Feature Implementation Guide

## Overview
This document describes the complete Cart and Order management system implemented for the aptechMall Spring Boot project.

## Features Implemented

### Phase 1: Cart Feature ✅
- Add products to cart
- View cart with all items
- Update item quantity
- Remove items from cart
- Clear entire cart
- Automatic cart creation for new users
- Duplicate product detection (updates quantity instead of creating new item)
- Real-time total calculation

### Phase 2: Order Feature ✅
- Checkout (create order from cart)
- View user's orders with pagination
- View order details
- Update order status
- Cancel order (only PENDING status)
- Unique order number generation
- Automatic cart clearing after checkout

---

## Database Schema

### 1. User Entity
**Table:** `users`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| email | VARCHAR(100) | NOT NULL, UNIQUE, INDEXED |
| password | VARCHAR(255) | NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(20) | |
| address | VARCHAR(500) | |
| created_at | DATETIME | NOT NULL, AUTO |
| updated_at | DATETIME | NOT NULL, AUTO |

### 2. Cart Entity
**Table:** `carts`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| user_id | BIGINT | NOT NULL, UNIQUE, FK → users.id |
| created_at | DATETIME | NOT NULL, AUTO |
| updated_at | DATETIME | NOT NULL, AUTO |

**Relationships:**
- `@OneToOne` with User
- `@OneToMany` with CartItem (cascade ALL, orphanRemoval)

### 3. CartItem Entity
**Table:** `cart_items`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| cart_id | BIGINT | NOT NULL, FK → carts.id |
| product_id | VARCHAR(255) | NOT NULL |
| product_name | VARCHAR(500) | NOT NULL |
| product_image | VARCHAR(1000) | |
| price | DECIMAL(10,2) | NOT NULL |
| quantity | INTEGER | NOT NULL |
| marketplace | VARCHAR(20) | NOT NULL (ENUM) |
| created_at | DATETIME | NOT NULL, AUTO |

**Indexes:**
- `idx_cart_product` on (cart_id, product_id, marketplace) - for duplicate detection

**Relationships:**
- `@ManyToOne` with Cart (LAZY loading)

### 4. Order Entity
**Table:** `orders`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| user_id | BIGINT | NOT NULL, FK → users.id |
| order_number | VARCHAR(50) | NOT NULL, UNIQUE, INDEXED |
| total_amount | DECIMAL(10,2) | NOT NULL |
| status | VARCHAR(20) | NOT NULL (ENUM) |
| shipping_address | VARCHAR(500) | NOT NULL |
| phone | VARCHAR(20) | NOT NULL |
| note | VARCHAR(1000) | |
| created_at | DATETIME | NOT NULL, AUTO |
| updated_at | DATETIME | NOT NULL, AUTO |

**Indexes:**
- `idx_order_number` on order_number
- `idx_user_id` on user_id
- `idx_status` on status
- `idx_created_at` on created_at

**Relationships:**
- `@ManyToOne` with User (LAZY loading)
- `@OneToMany` with OrderItem (cascade ALL, orphanRemoval)

### 5. OrderItem Entity
**Table:** `order_items`

| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| order_id | BIGINT | NOT NULL, FK → orders.id |
| product_id | VARCHAR(255) | NOT NULL |
| product_name | VARCHAR(500) | NOT NULL |
| product_image | VARCHAR(1000) | |
| price | DECIMAL(10,2) | NOT NULL |
| quantity | INTEGER | NOT NULL |
| marketplace | VARCHAR(20) | NOT NULL (ENUM) |

**Relationships:**
- `@ManyToOne` with Order (LAZY loading)

### 6. Enums

**Marketplace:**
```java
public enum Marketplace {
    ALIEXPRESS,
    ALIBABA1688
}
```

**OrderStatus:**
```java
public enum OrderStatus {
    PENDING,      // Order created, awaiting payment/confirmation
    CONFIRMED,    // Order confirmed and being processed
    SHIPPING,     // Order shipped and in transit
    DELIVERED,    // Order successfully delivered
    CANCELLED     // Order cancelled by user or system
}
```

---

## API Endpoints

### Cart Endpoints (`/api/cart`)

#### 1. Get Cart
```http
GET /api/cart?userId={userId}
```

**Response:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "id": 1,
        "productId": "1234567890",
        "productName": "Product Name",
        "productImage": "https://...",
        "price": 29.99,
        "quantity": 2,
        "marketplace": "ALIEXPRESS",
        "subtotal": 59.98,
        "createdAt": "2023-10-25T10:30:00"
      }
    ],
    "totalItems": 2,
    "totalAmount": 59.98,
    "createdAt": "2023-10-25T10:30:00",
    "updatedAt": "2023-10-25T10:35:00"
  }
}
```

#### 2. Add to Cart
```http
POST /api/cart/items?userId={userId}
Content-Type: application/json

{
  "productId": "1234567890",
  "productName": "Product Name",
  "productImage": "https://...",
  "price": 29.99,
  "quantity": 1,
  "marketplace": "ALIEXPRESS"
}
```

**Validation:**
- productId: required, not blank
- productName: required, max 500 chars
- productImage: max 1000 chars
- price: required, min 0.01
- quantity: required, min 1, max 999
- marketplace: required (ALIEXPRESS or ALIBABA1688)

**Response:** 201 Created + CartResponse

#### 3. Update Item Quantity
```http
PUT /api/cart/items/{itemId}?userId={userId}
Content-Type: application/json

{
  "quantity": 3
}
```

**Validation:**
- quantity: required, min 1, max 999

**Response:** 200 OK + CartResponse

#### 4. Remove Item
```http
DELETE /api/cart/items/{itemId}?userId={userId}
```

**Response:** 200 OK + CartResponse

#### 5. Clear Cart
```http
DELETE /api/cart/clear?userId={userId}
```

**Response:** 200 OK + Empty CartResponse

---

### Order Endpoints (`/api/orders`)

#### 1. Checkout (Create Order)
```http
POST /api/orders/checkout?userId={userId}
Content-Type: application/json

{
  "shippingAddress": "123 Main St, City, Country",
  "phone": "+1234567890",
  "note": "Please deliver after 5 PM"
}
```

**Validation:**
- shippingAddress: required, max 500 chars
- phone: required, max 20 chars
- note: optional, max 1000 chars

**Business Rules:**
- Cart must not be empty
- User must exist
- Cart is automatically cleared after successful checkout
- Order number is auto-generated (format: ORD-{timestamp}-{random})
- Initial status is PENDING

**Response:** 201 Created
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "orderNumber": "ORD-20231025143522-A3F9",
    "status": "PENDING",
    "totalAmount": 59.98,
    "shippingAddress": "123 Main St, City, Country",
    "phone": "+1234567890",
    "note": "Please deliver after 5 PM",
    "totalItems": 2,
    "items": [
      {
        "id": 1,
        "productId": "1234567890",
        "productName": "Product Name",
        "productImage": "https://...",
        "price": 29.99,
        "quantity": 2,
        "marketplace": "ALIEXPRESS",
        "subtotal": 59.98
      }
    ],
    "createdAt": "2023-10-25T14:35:22",
    "updatedAt": "2023-10-25T14:35:22"
  }
}
```

#### 2. Get User Orders (Paginated)
```http
GET /api/orders?userId={userId}&page=0&size=10
```

**Query Parameters:**
- userId: required
- page: optional, default 0
- size: optional, default 10

**Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [
      {
        "id": 1,
        "userId": 1,
        "orderNumber": "ORD-20231025143522-A3F9",
        "status": "PENDING",
        "totalAmount": 59.98,
        "shippingAddress": "123 Main St",
        "phone": "+1234567890",
        "note": "Please deliver after 5 PM",
        "totalItems": 2,
        "createdAt": "2023-10-25T14:35:22",
        "updatedAt": "2023-10-25T14:35:22"
      }
    ],
    "currentPage": 0,
    "totalItems": 15,
    "totalPages": 2,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

#### 3. Get Order Detail
```http
GET /api/orders/{orderId}?userId={userId}
```

**Response:** 200 OK + Full OrderResponse (with items)

#### 4. Get Order by Order Number
```http
GET /api/orders/number/{orderNumber}?userId={userId}
```

**Response:** 200 OK + Full OrderResponse (with items)

#### 5. Update Order Status
```http
PUT /api/orders/{orderId}/status?userId={userId}
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**Validation:**
- status: required (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)

**Response:** 200 OK + OrderResponse

#### 6. Cancel Order
```http
DELETE /api/orders/{orderId}?userId={userId}
```

**Business Rules:**
- Only orders with PENDING status can be cancelled
- Throws OrderNotCancellableException if status is not PENDING

**Response:** 200 OK + OrderResponse with CANCELLED status

---

## Exception Handling

All exceptions are handled globally by `GlobalExceptionHandler` with standardized JSON responses.

### Custom Exceptions

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| UserNotFoundException | 404 | User not found |
| CartNotFoundException | 404 | Cart not found for user |
| CartItemNotFoundException | 404 | Cart item not found |
| EmptyCartException | 400 | Cannot checkout with empty cart |
| OrderNotFoundException | 404 | Order not found |
| OrderNotCancellableException | 400 | Order cannot be cancelled (not PENDING) |
| IllegalArgumentException | 400 | Invalid request data |
| IllegalStateException | 400 | Invalid state operation |
| MethodArgumentNotValidException | 400 | Validation failed |

### Error Response Format
```json
{
  "status": 404,
  "error": "Cart Not Found",
  "message": "Cart not found for user ID: 123"
}
```

### Validation Error Format
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "fieldErrors": {
    "quantity": "Quantity must be at least 1",
    "price": "Price must be greater than 0"
  }
}
```

---

## Business Logic Implementation

### Cart Service

**Key Features:**
1. **Auto-create cart:** Creates cart automatically when user adds first item
2. **Duplicate detection:** If product already exists in cart, updates quantity instead of adding new item
3. **User verification:** Validates user exists before operations
4. **Security:** Verifies cart item belongs to user's cart before update/delete
5. **Real-time calculations:** Total amount and item count calculated dynamically

**Methods:**
- `getCart(userId)` - Get or return empty cart
- `addToCart(userId, request)` - Add item or update quantity
- `updateItemQuantity(userId, itemId, quantity)` - Update quantity
- `removeItem(userId, itemId)` - Remove single item
- `clearCart(userId)` - Remove all items

### Order Service

**Key Features:**
1. **Unique order number:** Format `ORD-{yyyyMMddHHmmss}-{4-digit-hex}`
2. **Cart validation:** Ensures cart is not empty before checkout
3. **Atomic operations:** Cart → Order conversion is transactional
4. **Auto-clear cart:** Cart cleared after successful checkout
5. **Security:** User can only access their own orders
6. **Pagination:** Supports paginated order listing
7. **Status management:** Enforces business rules for status changes

**Methods:**
- `checkout(userId, request)` - Create order from cart
- `getUserOrders(userId, pageable)` - Get orders with pagination
- `getOrderDetail(userId, orderId)` - Get full order details
- `getOrderByNumber(userId, orderNumber)` - Search by order number
- `updateOrderStatus(userId, orderId, status)` - Change status
- `cancelOrder(userId, orderId)` - Cancel if PENDING

---

## Testing Guide

### Prerequisites
1. MySQL server running on localhost:3306
2. Database `test_db` created
3. Application started: `mvn spring-boot:run`

### Test Scenario: Complete Flow

**Step 1: Add products to cart**
```bash
curl -X POST "http://localhost:8080/api/cart/items?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "123456",
    "productName": "Test Product 1",
    "productImage": "https://example.com/image1.jpg",
    "price": 29.99,
    "quantity": 2,
    "marketplace": "ALIEXPRESS"
  }'
```

**Step 2: Add another product**
```bash
curl -X POST "http://localhost:8080/api/cart/items?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "789012",
    "productName": "Test Product 2",
    "productImage": "https://example.com/image2.jpg",
    "price": 49.99,
    "quantity": 1,
    "marketplace": "ALIBABA1688"
  }'
```

**Step 3: View cart**
```bash
curl "http://localhost:8080/api/cart?userId=1"
```

**Step 4: Update quantity**
```bash
curl -X PUT "http://localhost:8080/api/cart/items/1?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3}'
```

**Step 5: Checkout**
```bash
curl -X POST "http://localhost:8080/api/orders/checkout?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "123 Main Street, City, Country",
    "phone": "+1234567890",
    "note": "Please call before delivery"
  }'
```

**Step 6: View orders**
```bash
curl "http://localhost:8080/api/orders?userId=1&page=0&size=10"
```

**Step 7: View order detail**
```bash
curl "http://localhost:8080/api/orders/1?userId=1"
```

**Step 8: Update order status**
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

**Step 9: Try to cancel (should fail since status is CONFIRMED)**
```bash
curl -X DELETE "http://localhost:8080/api/orders/1?userId=1"
# Returns 400: Order cannot be cancelled
```

### Test Cases

**Cart Tests:**
- ✅ Add product to empty cart → creates cart automatically
- ✅ Add duplicate product → updates quantity
- ✅ Update item quantity → recalculates total
- ✅ Remove item → updates cart
- ✅ Clear cart → empties all items
- ✅ Add product with invalid data → returns validation errors

**Order Tests:**
- ✅ Checkout with items → creates order and clears cart
- ✅ Checkout with empty cart → returns 400 error
- ✅ View orders with pagination → returns paginated results
- ✅ Cancel PENDING order → changes status to CANCELLED
- ✅ Cancel CONFIRMED order → returns 400 error
- ✅ Update order status → changes status
- ✅ Access other user's order → returns 404 error

---

## File Structure

```
src/main/java/com/aptech/aptechMall/
├── entity/
│   ├── User.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Marketplace.java (enum)
│   └── OrderStatus.java (enum)
├── repository/
│   ├── UserRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── dto/
│   ├── cart/
│   │   ├── AddToCartRequest.java
│   │   ├── UpdateCartItemRequest.java
│   │   ├── CartItemDTO.java
│   │   └── CartResponse.java
│   └── order/
│       ├── CheckoutRequest.java
│       ├── UpdateOrderStatusRequest.java
│       ├── OrderItemDTO.java
│       └── OrderResponse.java
├── service/
│   ├── CartService.java
│   └── OrderService.java
├── Controller/
│   ├── CartController.java
│   └── OrderController.java
└── Exception/
    ├── GlobalExceptionHandler.java
    ├── UserNotFoundException.java
    ├── CartNotFoundException.java
    ├── CartItemNotFoundException.java
    ├── EmptyCartException.java
    ├── OrderNotFoundException.java
    └── OrderNotCancellableException.java
```

---

## Configuration Changes

### pom.xml
Added dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### application.properties
No changes required. Existing configuration works:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/test_db
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

The `ddl-auto=update` will automatically create/update tables based on entities.

---

## Future Enhancements

### Recommended Next Steps:
1. **Authentication & Authorization**
   - Implement Spring Security
   - Use JWT tokens instead of query parameter userId
   - Add role-based access control (USER, ADMIN)

2. **Payment Integration**
   - Add Payment entity
   - Integrate payment gateways (Stripe, PayPal)
   - Add payment status tracking

3. **Inventory Management**
   - Add stock tracking
   - Validate product availability before checkout
   - Update stock after order placement

4. **Order Tracking**
   - Add shipping integration
   - Send email notifications
   - Add order history timeline

5. **Performance Optimization**
   - Add Redis caching for cart data
   - Implement lazy loading optimization
   - Add database indexes

6. **Testing**
   - Add unit tests (JUnit)
   - Add integration tests
   - Add API endpoint tests (MockMvc)

---

## Notes

1. **User Authentication**: This implementation uses `userId` as a query parameter for simplicity. In production, implement proper authentication (JWT/OAuth2) and extract userId from the security context.

2. **Transaction Management**: All service methods are annotated with `@Transactional` to ensure data consistency.

3. **Cascade Operations**:
   - Deleting a Cart will cascade delete all CartItems
   - Deleting an Order will cascade delete all OrderItems
   - Deleting a User will NOT cascade delete Cart/Orders (orphanRemoval = false)

4. **Lazy Loading**: Cart and Order relationships use `FetchType.LAZY` to avoid N+1 query problems. Use `@Query` with `JOIN FETCH` for eager loading when needed.

5. **Database Schema**: Tables will be auto-created on first run thanks to `spring.jpa.hibernate.ddl-auto=update`.

---

## Troubleshooting

### Issue: Build fails with JPA errors
**Solution:** Ensure `spring-boot-starter-data-jpa` is in pom.xml

### Issue: Database connection failed
**Solution:** Check MySQL is running and credentials in application.properties

### Issue: Tables not created
**Solution:** Check `spring.jpa.hibernate.ddl-auto=update` in application.properties

### Issue: Cart not found error
**Solution:** Cart is auto-created on first add. Ensure userId exists in users table

### Issue: Cannot cancel order
**Solution:** Only PENDING orders can be cancelled. Check order status first

---

## Conclusion

The Cart and Order management system is now fully implemented and tested. The system follows Spring Boot best practices with:
- Clean layered architecture (Entity → Repository → Service → Controller)
- Comprehensive validation
- Global exception handling
- Transaction management
- RESTful API design
- Proper DTO usage
- Security considerations (user verification)
- Pagination support
- Business rule enforcement

Ready for integration with frontend applications (React/Next.js/Vite).
