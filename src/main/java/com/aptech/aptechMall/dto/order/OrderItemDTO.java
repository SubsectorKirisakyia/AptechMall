package com.aptech.aptechMall.dto.order;

import com.aptech.aptechMall.entity.enums.Marketplace;
import com.aptech.aptechMall.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing an order item in response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long id;
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Marketplace marketplace;
    private BigDecimal subtotal;

    /**
     * Convert OrderItem entity to DTO
     * @param orderItem OrderItem entity
     * @return OrderItemDTO
     */
    public static OrderItemDTO fromEntity(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .marketplace(orderItem.getMarketplace())
                .subtotal(orderItem.getSubtotal())
                .build();
    }
}
