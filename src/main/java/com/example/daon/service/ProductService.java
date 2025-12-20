package com.example.daon.service;

import java.util.List;

import com.example.daon.vo.ProductVo;

public interface ProductService {
	
    List<ProductVo> getProductList();

    void insertProduct(ProductVo productVo);
    

    void stockIn(List<String> productCodes, int qty);
    void stockOut(List<String> productCodes, int qty);
    


}
