package com.example.daon.controller;

import java.time.YearMonth;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.daon.vo.CustomerVo;
import com.example.daon.service.ProductService;
import com.example.daon.vo.ProductVo;
import com.example.daon.vo.UserVo;
import jakarta.servlet.http.HttpSession;


@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/list")
    
    public String home(@RequestParam(required = false) String month, Model model) {
        var products = productService.getProductList();
        var currentMonthLedger = productService.getMonthlyLedger(YearMonth.now());
        Map<String, com.example.daon.vo.StockLedgerVo> ledgerByProduct = currentMonthLedger.stream()
                .collect(Collectors.toMap(com.example.daon.vo.StockLedgerVo::getProductCode,
                        Function.identity()));
        model.addAttribute("list", products);
        model.addAttribute("ledgerByProduct", ledgerByProduct);
        model.addAttribute("totalStock", products.stream().mapToInt(ProductVo::getQuantity).sum());
        model.addAttribute("lowStockCount", products.stream().filter(p -> p.getQuantity() < 100).count());
        model.addAttribute("monthlyInbound",
                currentMonthLedger.stream().mapToInt(row -> row.getMonthlyInbound()).sum());
        model.addAttribute("monthlyOutbound",
                currentMonthLedger.stream().mapToInt(row -> row.getOutboundQty()).sum());
        return "inventory_manage";
    }

    @PostMapping("/stock/quick")
    public String quickMovement(@RequestParam String productCode,
            @RequestParam String historyType, @RequestParam int quantity,
            RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productService.recordMovement(productCode, historyType, quantity, null,
                    "재고관리 화면 빠른 처리", LocalDate.now(), user.getUsername());
            redirectAttributes.addFlashAttribute("message",
                    ("IN".equals(historyType) ? "입고" : "출고") + " " + quantity + "개가 처리되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/list";
    }

    @PostMapping("/stock/adjust")
    public String adjustStock(@RequestParam String productCode,
            @RequestParam int targetQuantity, @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productService.adjustStock(productCode, targetQuantity, reason, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "현재 총재고가 관리자 정정되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product/list";
    }

    @GetMapping("/stock/ledger")
    public String ledger(@RequestParam(required = false) String month, Model model) {
        var products = productService.getProductList();
        YearMonth selectedMonth;
        try {
            selectedMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        } catch (RuntimeException e) {
            selectedMonth = YearMonth.now();
        }
        List<String> days = new ArrayList<>();
        for (int day = 1; day <= selectedMonth.lengthOfMonth(); day++) {
            days.add(String.format("%02d/%02d", selectedMonth.getMonthValue(), day));
        }
        model.addAttribute("list", products);
        model.addAttribute("ledger", productService.getMonthlyLedger(selectedMonth));
        model.addAttribute("days", days);
        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("monthLabel", selectedMonth.getYear() + "년 " + selectedMonth.getMonthValue() + "월");
        model.addAttribute("previousMonth", selectedMonth.minusMonths(1).toString());
        model.addAttribute("nextMonth", selectedMonth.plusMonths(1).toString());
        model.addAttribute("totalStock", products.stream().mapToInt(ProductVo::getQuantity).sum());
        model.addAttribute("lowStockCount", products.stream().filter(p -> p.getQuantity() < 100).count());
        return "product_list";
    }
    
    // 등록 화면
    @GetMapping("/product/insert")
    public String insertForm(@RequestParam(required=false) String editCode, Model model) {
        model.addAttribute("products",productService.getProductList());
        if(editCode!=null&&!editCode.isBlank()) model.addAttribute("editProduct",productService.getProduct(editCode));
        return "product_insert";
    }
    @PostMapping("/product/update")
    public String updateProduct(ProductVo productVo,HttpSession session,RedirectAttributes redirect){try{productService.updateProduct(productVo,((UserVo)session.getAttribute("loginUser")).getUsername());redirect.addFlashAttribute("message","상품 정보가 수정되었습니다.");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/product/list";}
    @PostMapping("/product/delete")
    public String deleteProduct(@RequestParam String productCode,HttpSession session,RedirectAttributes redirect){try{productService.deleteProduct(productCode,((UserVo)session.getAttribute("loginUser")).getUsername());redirect.addFlashAttribute("message","상품이 삭제되었습니다. 재고·입출고 이력은 보존됩니다.");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/product/list";}

    @PostMapping("/product/insert")
    public String insert(ProductVo productVo, HttpSession session) {
        UserVo user = (UserVo) session.getAttribute("loginUser");
        productService.insertProduct(productVo, user.getUsername());
        return "redirect:/product/list";
    }

    @PostMapping("/order/product/create")
    public String quickProduct(ProductVo productVo, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productVo.setUseYn("Y");
            productService.insertProduct(productVo, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "상품이 등록되었습니다. 바로 수주에 사용할 수 있습니다.");
            redirectAttributes.addAttribute("newProduct", productVo.getProductCode());
        } catch (RuntimeException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/order/list";
    }

    @GetMapping("/stock/movement")
    public String movementForm(@RequestParam(required = false) String productCode,
            @RequestParam(required = false) String mode, Model model) {
        model.addAttribute("products", productService.getProductList());
        model.addAttribute("customers", productService.getCustomerList());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("selectedProductCode", productCode);
        model.addAttribute("selectedMode", mode == null ? "IN" : mode);
        return "stock_movement";
    }

    @PostMapping("/stock/movement")
    public String recordMovement(@RequestParam String productCode,
            @RequestParam String historyType, @RequestParam int quantity,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String memo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate movementDate,
            RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productService.recordMovement(productCode, historyType, quantity, customerId, memo,
                    movementDate, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "재고 처리가 완료되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stock/movement";
    }

    @GetMapping("/stock/history")
    public String stockHistory(@RequestParam(defaultValue="1") int page,Model model) {
        int size=20,total=productService.getStockHistoryCount(),totalPages=Math.max(1,(total+size-1)/size),current=Math.max(1,Math.min(page,totalPages));
        model.addAttribute("history", productService.getStockHistory(current-1,size));model.addAttribute("currentPage",current);model.addAttribute("totalPages",totalPages);model.addAttribute("totalCount",total);
        int start=Math.max(1,current-4),end=Math.min(totalPages,start+9);start=Math.max(1,end-9);model.addAttribute("pageNumbers",IntStream.rangeClosed(start,end).boxed().toList());
        return "stock_history";
    }
    @PostMapping("/stock/history/cancel") public String cancelHistory(@RequestParam Long historyId,HttpSession session,RedirectAttributes redirect){try{productService.cancelStockHistory(historyId,((UserVo)session.getAttribute("loginUser")).getUsername());redirect.addFlashAttribute("message","입출고 이력이 취소되고 재고가 원상복구되었습니다.");}catch(RuntimeException e){redirect.addFlashAttribute("error",e.getMessage());}return "redirect:/stock/history";}

    @GetMapping("/customer/list")
    public String customerList(@RequestParam(required = false) Long editId, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("customers", productService.getCustomerList());
        if (editId != null) {
            try {
                model.addAttribute("editCustomer", productService.getCustomer(editId));
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/customer/list";
            }
        }
        return "customer_list";
    }

    @PostMapping("/customer/insert")
    public String insertCustomer(CustomerVo customerVo, RedirectAttributes redirectAttributes, HttpSession session) {
        UserVo user = (UserVo) session.getAttribute("loginUser");
        productService.insertCustomer(customerVo, user.getUsername());
        redirectAttributes.addFlashAttribute("message", "거래처가 등록되었습니다.");
        return "redirect:/customer/list";
    }

    @PostMapping("/customer/update")
    public String updateCustomer(CustomerVo customerVo, RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productService.updateCustomer(customerVo, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "거래처 정보가 수정되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/list";
    }

    @PostMapping("/customer/delete")
    public String deleteCustomer(@RequestParam Long customerId, RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            UserVo user = (UserVo) session.getAttribute("loginUser");
            productService.deleteCustomer(customerId, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "거래처가 삭제되었습니다. 기존 입출고 기록은 유지됩니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/list";
    }
    
}
