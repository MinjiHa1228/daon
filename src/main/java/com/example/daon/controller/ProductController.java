package com.example.daon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.daon.service.ProductService;
import com.example.daon.vo.ProductVo;


@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/list")
    
    public String home(Model model) {
    	
        model.addAttribute("list", productService.getProductList());
        return "product_list";
    }
    
    // 등록 화면
    @GetMapping("/product/insert")
    public String insertForm() {
        return "product_insert";
    }

   
    
}
