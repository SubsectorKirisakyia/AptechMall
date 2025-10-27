package com.aptech.aptechMall.Controller;

import com.aptech.aptechMall.dto.ApiResponse;
import com.aptech.aptechMall.dto.order.CheckoutRequest;
import com.aptech.aptechMall.dto.order.OrderResponse;
import com.aptech.aptechMall.dto.order.UpdateOrderStatusRequest;
import com.aptech.aptechMall.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for order operations
 * Base path: /api/orders
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:4200"})
public class OrderController {

    private final OrderService orderService;

    /**
     * Checkout - Create order from cart
     * POST /api/orders/checkout?userId={userId}
     *
     * @param userId User ID (from query parameter)
     * @param request CheckoutRequest (shipping address, phone, note)
     * @return Created OrderResponse
     */
    @PostMapping("/checkout")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestParam(name = "userId") Long userId,
            @Valid @RequestBody CheckoutRequest request) {

        log.info("POST /api/orders/checkout - userId: {}", userId);

        OrderResponse order = orderService.checkout(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    /**
     * Get all orders for user with pagination
     * GET /api/orders?userId={userId}&page={page}&size={size}
     *
     * @param userId User ID (from query parameter)
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @return Page of OrderResponse (summary without items)
     */
    @GetMapping
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserOrders(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        log.info("GET /api/orders - userId: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> ordersPage = orderService.getUserOrders(userId, pageable);

        // Build response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("orders", ordersPage.getContent());
        response.put("currentPage", ordersPage.getNumber());
        response.put("totalItems", ordersPage.getTotalElements());
        response.put("totalPages", ordersPage.getTotalPages());
        response.put("pageSize", ordersPage.getSize());
        response.put("hasNext", ordersPage.hasNext());
        response.put("hasPrevious", ordersPage.hasPrevious());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Orders retrieved successfully")
        );
    }

    /**
     * Get order detail by ID
     * GET /api/orders/{orderId}?userId={userId}
     *
     * @param userId User ID (from query parameter, for security check)
     * @param orderId Order ID
     * @return OrderResponse with items
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @RequestParam(name = "userId") Long userId,
            @PathVariable Long orderId) {

        log.info("GET /api/orders/{} - userId: {}", orderId, userId);

        OrderResponse order = orderService.getOrderDetail(userId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(order, "Order detail retrieved successfully")
        );
    }

    /**
     * Get order by order number
     * GET /api/orders/number/{orderNumber}?userId={userId}
     *
     * @param userId User ID (from query parameter, for security check)
     * @param orderNumber Order number
     * @return OrderResponse with items
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @RequestParam(name = "userId") Long userId,
            @PathVariable String orderNumber) {

        log.info("GET /api/orders/number/{} - userId: {}", orderNumber, userId);

        OrderResponse order = orderService.getOrderByNumber(userId, orderNumber);

        return ResponseEntity.ok(
                ApiResponse.success(order, "Order retrieved successfully")
        );
    }

    /**
     * Update order status
     * PUT /api/orders/{orderId}/status?userId={userId}
     *
     * @param userId User ID (from query parameter, for security check)
     * @param orderId Order ID
     * @param request UpdateOrderStatusRequest (new status)
     * @return Updated OrderResponse
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @RequestParam(name = "userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info("PUT /api/orders/{}/status - userId: {}, newStatus: {}",
                orderId, userId, request.getStatus());

        OrderResponse order = orderService.updateOrderStatus(userId, orderId, request.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(order, "Order status updated successfully")
        );
    }

    /**
     * Cancel order (only if status is PENDING)
     * DELETE /api/orders/{orderId}?userId={userId}
     *
     * @param userId User ID (from query parameter, for security check)
     * @param orderId Order ID
     * @return Cancelled OrderResponse
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("#userId == authentication.principal.id or hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @RequestParam(name = "userId") Long userId,
            @PathVariable Long orderId) {

        log.info("DELETE /api/orders/{} - userId: {}", orderId, userId);

        OrderResponse order = orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok(
                ApiResponse.success(order, "Order cancelled successfully")
        );
    }
}
