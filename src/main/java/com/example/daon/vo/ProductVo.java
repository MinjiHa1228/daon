package com.example.daon.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("product")
public class ProductVo {
	
	    private String productCode;   // CB-PNK
	    private String productName;   // 도마
	    private String type;          // SINGLE / SET
	    private String category;  // 도마 / 거치대 / 세트
	    private String color;         // 핑크
	    private int price;            // 단가
	    private String useYn;          // Y / N

}
