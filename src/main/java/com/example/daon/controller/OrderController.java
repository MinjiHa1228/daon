package com.example.daon.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.daon.mapper.ProductMapper;
import com.example.daon.service.OrderService;
import com.example.daon.vo.SalesOrderVo;
import com.example.daon.vo.UserVo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
@Controller @RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService; private final ProductMapper mapper;
    @GetMapping("/order/list") public String list(@RequestParam(required=false) String newProduct,@RequestParam(required=false) Long editId, Model model){
        var orders=orderService.getOrders(); model.addAttribute("orders",orders); model.addAttribute("customers",mapper.selectCustomerList()); model.addAttribute("products",mapper.selectProductList());
        model.addAttribute("newProduct",newProduct); if(editId!=null)model.addAttribute("editOrder",orders.stream().filter(o->o.getOrderId().equals(editId)).findFirst().orElse(null)); model.addAttribute("openCount",orders.stream().filter(o->!"SHIPPED".equals(o.getOrderStatus())&&!"CANCELLED".equals(o.getOrderStatus())).count());
        model.addAttribute("dueCount",orders.stream().filter(o->!"SHIPPED".equals(o.getOrderStatus())&&!o.getDueDate().isAfter(java.time.LocalDate.now().plusDays(3))).count());
        return "order_list";
    }
    @PostMapping("/order/create") public String create(SalesOrderVo order,HttpSession s,RedirectAttributes r){ try{orderService.create(order,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","수주가 등록되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/order/list";}
    @PostMapping("/order/update") public String update(SalesOrderVo order,HttpSession s,RedirectAttributes r){try{orderService.update(order,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","수주가 수정되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/order/list";}
    @PostMapping("/order/delete") public String delete(@RequestParam Long orderId,HttpSession s,RedirectAttributes r){try{orderService.cancel(orderId,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","수주가 삭제(취소)되었습니다. 이력은 보존됩니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/order/list";}
    @PostMapping("/order/produce") public String produce(@RequestParam Long orderId,@RequestParam int goodQty,@RequestParam(defaultValue="0") int defectiveQty,HttpSession s,RedirectAttributes r){try{orderService.produce(orderId,goodQty,defectiveQty,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","생산 실적이 반영되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/order/list";}
    @PostMapping("/order/ship") public String ship(@RequestParam Long orderId,@RequestParam int shipmentQty,HttpSession s,RedirectAttributes r){try{orderService.ship(orderId,shipmentQty,((UserVo)s.getAttribute("loginUser")).getUsername());r.addFlashAttribute("message","출고가 반영되었습니다.");}catch(RuntimeException e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/order/list";}
}
