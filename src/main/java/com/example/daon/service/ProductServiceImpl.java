package com.example.daon.service;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.daon.mapper.ProductMapper;
import com.example.daon.vo.CustomerVo;
import com.example.daon.vo.ProductVo;
import com.example.daon.vo.StockHistoryVo;
import com.example.daon.vo.StockLedgerVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final AuthService authService;

    @Override
    public List<ProductVo> getProductList() {
        return productMapper.selectProductList();
    }
    @Override public ProductVo getProduct(String code){ ProductVo p=productMapper.selectProductByCode(code); if(p==null) throw new IllegalArgumentException("상품을 찾을 수 없습니다."); return p; }
    @Override @Transactional public void updateProduct(ProductVo p,String user){ ProductVo before=getProduct(p.getProductCode()); if(productMapper.updateProduct(p)==0) throw new IllegalArgumentException("상품을 수정할 수 없습니다."); authService.audit(user,"UPDATE","PRODUCT",p.getProductCode(),before.getProductName()+" → "+p.getProductName()); }
    @Override @Transactional public void deleteProduct(String code,String user){ ProductVo p=getProduct(code); if(productMapper.softDeleteProduct(code)==0) throw new IllegalArgumentException("상품을 삭제할 수 없습니다."); authService.audit(user,"DELETE","PRODUCT",code,p.getProductName()+" 상품 삭제(이력 보존)"); }

    @Override
	public void insertProduct(ProductVo productVo, String username) {
    	productMapper.insertProduct(productVo);
    	
        // 상품 등록 시 재고 0으로 초기화 (중요)
        productMapper.insertStockIfNotExists(productVo.getProductCode());
        authService.audit(username, "CREATE", "PRODUCT", productVo.getProductCode(),
                productVo.getProductName() + " / " + productVo.getColor());

    }
    
    @Override
    @Transactional
    public void recordMovement(String productCode, String historyType, int quantity,
            Long customerId, String memo, LocalDate movementDate, String username) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        if (movementDate == null) {
            movementDate = LocalDate.now();
        }
        if (movementDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜로는 재고를 처리할 수 없습니다.");
        }

        int changeQty = switch (historyType) {
            case "IN", "COMP" -> quantity;
            case "OUT", "BAD", "MOVE", "LACK" -> -quantity;
            default -> throw new IllegalArgumentException("지원하지 않는 처리 유형입니다.");
        };

        int updated = productMapper.updateStock(productCode, changeQty);
        if (updated == 0) {
            throw new IllegalStateException("재고가 부족하거나 상품이 존재하지 않습니다.");
        }
        int balanceAfter = productMapper.selectStock(productCode);
        productMapper.insertHistory(productCode, historyType, changeQty, balanceAfter,
                customerId, memo, movementDate.atTime(12, 0), username);
        authService.audit(username, "STOCK_" + historyType, "PRODUCT", productCode,
                "수량 " + changeQty + ", 처리 후 " + balanceAfter + ", 날짜 " + movementDate);
    }

    @Override
    @Transactional
    public void adjustStock(String productCode, int targetQuantity, String reason, String username) {
        if (targetQuantity < 0) {
            throw new IllegalArgumentException("현재고는 0개 이상이어야 합니다.");
        }
        int before = productMapper.selectStock(productCode);
        int changeQty = targetQuantity - before;
        if (changeQty == 0) {
            throw new IllegalArgumentException("기존 현재고와 같은 수량입니다.");
        }
        if (productMapper.updateStock(productCode, changeQty) == 0) {
            throw new IllegalArgumentException("상품을 찾을 수 없거나 수량을 변경할 수 없습니다.");
        }
        String memo = "관리자 재고 정정" + (reason == null || reason.isBlank() ? "" : " - " + reason.trim());
        productMapper.insertHistory(productCode, "ADJUST", changeQty, targetQuantity,
                null, memo, LocalDateTime.now(), username);
        authService.audit(username, "STOCK_ADJUST", "PRODUCT", productCode,
                "현재고 " + before + " → " + targetQuantity + (reason == null || reason.isBlank() ? "" : ", 사유: " + reason.trim()));
    }

    @Override
    public List<StockHistoryVo> getStockHistory(int page,int size) {
        return productMapper.selectStockHistory(Math.max(0,page)*size,size);
    }
    @Override public int getStockHistoryCount(){return productMapper.countStockHistory();}
    @Override @Transactional public void cancelStockHistory(Long historyId,String user){
        StockHistoryVo h=productMapper.selectStockHistoryById(historyId);if(h==null)throw new IllegalArgumentException("입출고 이력을 찾을 수 없습니다.");
        if("Y".equals(h.getCancelYn()))throw new IllegalArgumentException("이미 취소된 이력입니다.");
        if(List.of("RESET","OPENING").contains(h.getHistoryType()))throw new IllegalArgumentException("초기화·기초재고 이력은 취소할 수 없습니다.");
        int reverse=-h.getQuantity();if(productMapper.updateStock(h.getProductCode(),reverse)==0)throw new IllegalArgumentException("현재 재고가 부족하여 이 이력을 취소할 수 없습니다.");
        if(productMapper.markStockHistoryCancelled(historyId,user)==0)throw new IllegalArgumentException("이미 취소된 이력입니다.");
        int balance=productMapper.selectStock(h.getProductCode());productMapper.insertHistory(h.getProductCode(),"ADJUST",reverse,balance,h.getCustomerId(),"이력 #"+historyId+" 취소·정정",LocalDateTime.now(),user);
        authService.audit(user,"CANCEL","STOCK_HISTORY",String.valueOf(historyId),h.getProductCode()+" 수량 "+h.getQuantity()+" 취소");
    }

    @Override
    public List<StockLedgerVo> getMonthlyLedger(YearMonth month) {
        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
        LocalDateTime nextMonth = month.plusMonths(1).atDay(1).atStartOfDay();
        List<StockHistoryVo> changesFromMonth = productMapper.selectStockHistoryFrom(monthStart);
        Map<String, List<StockHistoryVo>> byProduct = changesFromMonth.stream()
                .collect(Collectors.groupingBy(StockHistoryVo::getProductCode));

        return productMapper.selectProductList().stream().map(product -> {
            StockLedgerVo row = new StockLedgerVo();
            row.setProductCode(product.getProductCode());
            row.setProductName(product.getProductName());
            row.setDisplayName(product.getType().equals("SET")
                    ? "세트_" + product.getProductName()
                    : product.getProductName() + (product.getColor() == null ? "" : "_" + product.getColor()));
            row.setCurrentStock(product.getQuantity());

            List<StockHistoryVo> productChanges = byProduct.getOrDefault(product.getProductCode(), List.of());
            LocalDateTime latestReset = productChanges.stream()
                    .filter(h -> "RESET".equals(h.getHistoryType()))
                    .map(StockHistoryVo::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (latestReset != null) {
                productChanges = productChanges.stream()
                        .filter(h -> h.getCreatedAt().isAfter(latestReset))
                        .toList();
            }
            int changesSinceStart = productChanges.stream().mapToInt(StockHistoryVo::getQuantity).sum();
            row.setPreviousStock(product.getQuantity() - changesSinceStart);

            Map<String, Integer> daily = new LinkedHashMap<>();
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                daily.put(String.format("%02d/%02d", month.getMonthValue(), day), 0);
            }
            productChanges.stream()
                    .filter(h -> h.getCreatedAt().isBefore(nextMonth))
                    .forEach(h -> {
                        if ("IN".equals(h.getHistoryType())) {
                            String key = String.format("%02d/%02d", h.getCreatedAt().getMonthValue(), h.getCreatedAt().getDayOfMonth());
                            daily.merge(key, h.getQuantity(), Integer::sum);
                            row.setMonthlyInbound(row.getMonthlyInbound() + h.getQuantity());
                        } else if ("COMP".equals(h.getHistoryType())) {
                            row.setCompensationQty(row.getCompensationQty() + h.getQuantity());
                        } else if ("OUT".equals(h.getHistoryType())) {
                            row.setOutboundQty(row.getOutboundQty() + Math.abs(h.getQuantity()));
                        } else if ("MOVE".equals(h.getHistoryType())) {
                            row.setMoveQty(row.getMoveQty() + Math.abs(h.getQuantity()));
                        } else if ("LACK".equals(h.getHistoryType())) {
                            row.setLackQty(row.getLackQty() + Math.abs(h.getQuantity()));
                        } else if ("BAD".equals(h.getHistoryType())) {
                            row.setBadQty(row.getBadQty() + Math.abs(h.getQuantity()));
                        }
                    });
            row.setDailyInbound(daily);
            return row;
        }).toList();
    }

    @Override
    public List<CustomerVo> getCustomerList() {
        return productMapper.selectCustomerList();
    }

    @Override
    public CustomerVo getCustomer(Long customerId) {
        CustomerVo customer = productMapper.selectCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("거래처를 찾을 수 없습니다.");
        }
        return customer;
    }

    @Override
    public void insertCustomer(CustomerVo customerVo, String username) {
        customerVo.setCreatedBy(username);
        customerVo.setUpdatedBy(username);
        productMapper.insertCustomer(customerVo);
        authService.audit(username, "CREATE", "CUSTOMER", String.valueOf(customerVo.getCustomerId()),
                customerVo.getCustomerName());
    }

    @Override
    @Transactional
    public void updateCustomer(CustomerVo customerVo, String username) {
        CustomerVo before = getCustomer(customerVo.getCustomerId());
        customerVo.setUpdatedBy(username);
        if (productMapper.updateCustomer(customerVo) == 0) {
            throw new IllegalArgumentException("수정할 거래처를 찾을 수 없습니다.");
        }
        authService.audit(username, "UPDATE", "CUSTOMER", String.valueOf(customerVo.getCustomerId()),
                before.getCustomerName() + " → " + customerVo.getCustomerName());
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId, String username) {
        CustomerVo customer = getCustomer(customerId);
        if (productMapper.deleteCustomer(customerId, username) == 0) {
            throw new IllegalArgumentException("삭제할 거래처를 찾을 수 없습니다.");
        }
        authService.audit(username, "DELETE", "CUSTOMER", String.valueOf(customerId),
                customer.getCustomerName() + " (사용 중지)");
    }
}
