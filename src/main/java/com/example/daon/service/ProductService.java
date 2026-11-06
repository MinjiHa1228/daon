package com.example.daon.service;

import java.util.List;

import com.example.daon.vo.ProductVo;
import com.example.daon.vo.CustomerVo;
import com.example.daon.vo.StockHistoryVo;
import com.example.daon.vo.StockLedgerVo;
import java.time.YearMonth;
import java.time.LocalDate;

public interface ProductService {
	
    List<ProductVo> getProductList();

    void insertProduct(ProductVo productVo, String username);
    ProductVo getProduct(String productCode);
    void updateProduct(ProductVo productVo,String username);
    void deleteProduct(String productCode,String username);
    

    void recordMovement(String productCode, String historyType, int quantity, Long customerId,
            String memo, LocalDate movementDate, String username);

    void adjustStock(String productCode, int targetQuantity, String reason, String username);

    List<StockHistoryVo> getStockHistory(int page,int size);
    int getStockHistoryCount();
    void cancelStockHistory(Long historyId,String username);

    List<StockLedgerVo> getMonthlyLedger(YearMonth month);

    List<CustomerVo> getCustomerList();

    CustomerVo getCustomer(Long customerId);

    void insertCustomer(CustomerVo customerVo, String username);

    void updateCustomer(CustomerVo customerVo, String username);

    void deleteCustomer(Long customerId, String username);


}
