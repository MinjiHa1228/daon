package com.example.daon.vo;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class StockLedgerVo {
    private String productCode;
    private String productName;
    private String displayName;
    private int previousStock;
    private Map<String, Integer> dailyInbound = new LinkedHashMap<>();
    private int compensationQty;
    private int outboundQty;
    private int moveQty;
    private int lackQty;
    private int monthlyInbound;
    private int badQty;
    private int currentStock;
}
