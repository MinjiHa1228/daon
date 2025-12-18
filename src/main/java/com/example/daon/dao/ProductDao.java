package com.example.daon.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.daon.vo.ProductVo;
@Mapper
public interface ProductDao {

    List<ProductVo> selectProductList();

    int insertProduct(ProductVo productVo);
}
