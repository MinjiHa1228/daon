package com.example.daon.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.daon.mapper.ProductMapper;
import com.example.daon.vo.SalesOrderVo;
import lombok.RequiredArgsConstructor;
@Service @RequiredArgsConstructor
public class OrderService {
    private final ProductMapper mapper;
    private final AuthService authService;
    public List<SalesOrderVo> getOrders(){ return mapper.selectSalesOrderList(); }
    private SalesOrderVo get(Long id){ SalesOrderVo o=mapper.selectSalesOrderById(id); if(o==null) throw new IllegalArgumentException("주문을 찾을 수 없습니다."); return o; }
    @Transactional public void create(SalesOrderVo o,String user){
        if(o.getCustomerId()==null||o.getProductCode()==null||o.getProductCode().isBlank()) throw new IllegalArgumentException("거래처와 상품을 선택하세요.");
        if(o.getOrderedQty()==null||o.getOrderedQty()<=0) throw new IllegalArgumentException("주문수량은 1개 이상이어야 합니다.");
        if(o.getOrderDate()==null) o.setOrderDate(LocalDate.now());
        if(o.getDueDate()==null||o.getDueDate().isBefore(o.getOrderDate())) throw new IllegalArgumentException("납기일을 올바르게 입력하세요.");
        if(o.getUnitPrice()==null) o.setUnitPrice(BigDecimal.ZERO);
        if(o.getUnitPrice().signum()<0) throw new IllegalArgumentException("단가는 0원 이상이어야 합니다.");
        o.setOrderNo("DT-"+LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+UUID.randomUUID().toString().substring(0,6).toUpperCase());
        o.setTotalAmount(o.getUnitPrice().multiply(BigDecimal.valueOf(o.getOrderedQty()))); o.setCreatedBy(user);o.setUpdatedBy(user);
        mapper.insertSalesOrder(o); authService.audit(user,"ORDER_CREATE","SALES_ORDER",String.valueOf(o.getOrderId()),o.getOrderNo()+" / "+o.getOrderedQty()+"개");
    }
    @Transactional public void update(SalesOrderVo o,String user){
        SalesOrderVo before=get(o.getOrderId()); validateOrder(o); if(o.getProducedQty()==null)o.setProducedQty(before.getProducedQty());if(o.getDefectiveQty()==null)o.setDefectiveQty(before.getDefectiveQty());if(o.getShippedQty()==null)o.setShippedQty(before.getShippedQty());
        if(o.getProducedQty()<0||o.getDefectiveQty()<0||o.getShippedQty()<0||o.getProducedQty()>o.getOrderedQty()||o.getShippedQty()>o.getProducedQty())throw new IllegalArgumentException("생산·불량·출고 누적수량을 확인하세요.");o.setTotalAmount(o.getUnitPrice().multiply(BigDecimal.valueOf(o.getOrderedQty())));o.setUpdatedBy(user);
        if(mapper.updateSalesOrder(o)==0) throw new IllegalArgumentException("납품 완료 주문이거나 생산·출고 수량보다 주문수량이 작아 수정할 수 없습니다.");
        mapper.refreshSalesOrderStatus(o.getOrderId()); authService.audit(user,"ORDER_UPDATE","SALES_ORDER",String.valueOf(o.getOrderId()),before.getOrderNo()+" 수주 수정");
    }
    @Transactional public void cancel(Long id,String user){SalesOrderVo o=get(id);if(mapper.cancelSalesOrder(id,user)==0)throw new IllegalArgumentException("이미 출고된 주문은 삭제할 수 없습니다. 출고 정정 후 처리하세요.");authService.audit(user,"DELETE","SALES_ORDER",String.valueOf(id),o.getOrderNo()+" 수주 취소(이력 보존)");}
    private void validateOrder(SalesOrderVo o){
        if(o.getCustomerId()==null||o.getProductCode()==null||o.getProductCode().isBlank())throw new IllegalArgumentException("거래처와 상품을 선택하세요.");
        if(o.getOrderedQty()==null||o.getOrderedQty()<=0)throw new IllegalArgumentException("주문수량은 1개 이상이어야 합니다.");
        if(o.getOrderDate()==null||o.getDueDate()==null||o.getDueDate().isBefore(o.getOrderDate()))throw new IllegalArgumentException("주문일과 납기일을 확인하세요.");
        if(o.getUnitPrice()==null)o.setUnitPrice(BigDecimal.ZERO);if(o.getUnitPrice().signum()<0)throw new IllegalArgumentException("단가는 0원 이상이어야 합니다.");
    }
    @Transactional public void produce(Long id,int good,int defective,String user){
        SalesOrderVo o=get(id); if(good<0||defective<0||(good+defective)==0) throw new IllegalArgumentException("생산 또는 불량 수량을 입력하세요.");
        if(mapper.addProducedQuantity(id,good,defective,user)==0) throw new IllegalArgumentException("생산수량이 주문 잔량보다 많습니다.");
        mapper.refreshSalesOrderStatus(id); authService.audit(user,"PRODUCTION","SALES_ORDER",String.valueOf(id),o.getOrderNo()+" / 양품 "+good+" / 불량 "+defective);
    }
    @Transactional public void ship(Long id,int qty,String user){
        SalesOrderVo o=get(id); if(qty<=0) throw new IllegalArgumentException("출고수량은 1개 이상이어야 합니다.");
        if(mapper.addShippedQuantity(id,qty,user)==0) throw new IllegalArgumentException("생산 완료 수량보다 많이 출고할 수 없습니다.");
        mapper.refreshSalesOrderStatus(id); authService.audit(user,"SHIPMENT","SALES_ORDER",String.valueOf(id),o.getOrderNo()+" / 출고 "+qty);
    }
}
