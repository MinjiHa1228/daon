package com.example.daon.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.daon.vo.ProductVo;
@Mapper
public interface ProductMapper {

    List<ProductVo> selectProductList();

    void insertProduct(ProductVo productVo);

	void insertStockIfNotExists(String productCode);

	void insertHistory(String code, String string, int qty);

	int decreaseStock(String code, int qty);
}
