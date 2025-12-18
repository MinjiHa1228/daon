package com.example.daon.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.daon.dao.ProductDao;
import com.example.daon.vo.ProductVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    @Override
    public List<ProductVo> getProductList() {
        return productDao.selectProductList();
    }

    @Override
    public void insertProduct(ProductVo productVo) {
        productDao.insertProduct(productVo);
    }
}
