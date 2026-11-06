package com.example.daon.vo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
@Data
public class SalesOrderVo {
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String productCode;
    private String productName;
    private Integer orderedQty;
    private Integer producedQty;
    private Integer defectiveQty;
    private Integer shippedQty;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate orderDate;
    private LocalDate dueDate;
    private String orderStatus;
    private String memo;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public int getProductionRemaining() { return Math.max(0, (orderedQty == null ? 0 : orderedQty) - (producedQty == null ? 0 : producedQty)); }
    public int getShipmentRemaining() { return Math.max(0, (orderedQty == null ? 0 : orderedQty) - (shippedQty == null ? 0 : shippedQty)); }
}
