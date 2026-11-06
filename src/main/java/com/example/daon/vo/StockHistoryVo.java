package com.example.daon.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StockHistoryVo {
	
	    private Long historyId;          // 입출고 장부 번호 (PK)

	    private String productCode;       // 상품코드 (CB-PNK 등)
	    private String productName;       // 상품명 (도마)
	    private String color;             // 색상

	    private String historyType;       // OPENING / IN / OUT / BAD / COMP / MOVE / LACK / ADJUST
	    // IN   : 입고
	    // OUT  : 출고
	    // BAD  : 불량
	    // COMP : 보상분
	    // MOVE : 재고이동
	    // LACK : 수량부족

	    private int quantity;             // 증감 수량(+/-)
	    private int balanceAfter;         // 처리 후 재고

	    private String memo;              // 메모

	    private Long customerId;
	    private String customerName;

	    private String createdBy;          // 작성자
	    private LocalDateTime createdAt;   // 작성일

	    private String updatedBy;          // 수정자
	    private LocalDateTime updatedAt;   // 수정일
        private String cancelYn;
        private String cancelledBy;
        private LocalDateTime cancelledAt;

}
