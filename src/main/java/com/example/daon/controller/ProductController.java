package com.example.daon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.daon.service.ProductService;


@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/list")
    public String home(Model model) {
        model.addAttribute("list", productService.getProductList());
        return "product_list";
    }
}
