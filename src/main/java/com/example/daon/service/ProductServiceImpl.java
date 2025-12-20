package com.example.daon.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.daon.mapper.ProductMapper;
import com.example.daon.vo.ProductVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public List<ProductVo> getProductList() {
        return productMapper.selectProductList();
    }

    @Override
    public void insertProduct(ProductVo productVo) {
    	productMapper.insertProduct(productVo);
    	
        // 상품 등록 시 재고 0으로 초기화 (중요)
        productMapper.insertStockIfNotExists(productVo.getProductCode());

    }
    
    @Override
    public void stockIn(List<String> productCodes, int qty) {
        for (String code : productCodes) {
            productMapper.decreaseStock(code, qty);
            productMapper.insertHistory(code, "IN", qty);
        }
    }

    @Override
    public void stockOut(List<String> productCodes, int qty) {
        for (String code : productCodes) {
            int updated = productMapper.decreaseStock(code, qty);
            if (updated == 0) {
                throw new RuntimeException("재고 부족: " + code);
            }
            productMapper.insertHistory(code, "OUT", qty);
        }
    }
}
