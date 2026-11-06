package com.example.daon.mapper;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.daon.vo.CustomerVo;
import com.example.daon.vo.ProductVo;
import com.example.daon.vo.StockHistoryVo;
import com.example.daon.vo.UserVo;
import com.example.daon.vo.AuditLogVo;
import com.example.daon.vo.EmployeeVo;
import com.example.daon.vo.EmployeeLeaveVo;
import com.example.daon.vo.EmployeePayrollVo;
import com.example.daon.vo.EmployeeWorkAllowanceVo;
import com.example.daon.vo.SalesOrderVo;
@Mapper
public interface ProductMapper {

    List<ProductVo> selectProductList();
    ProductVo selectProductByCode(String productCode);

    void insertProduct(ProductVo productVo);
    int updateProduct(ProductVo productVo);
    int softDeleteProduct(@Param("productCode") String productCode);

	void insertStockIfNotExists(String productCode);

	int updateStock(@Param("productCode") String productCode, @Param("changeQty") int changeQty);

	int selectStock(String productCode);

	void insertHistory(@Param("productCode") String productCode,
			@Param("historyType") String historyType,
			@Param("quantity") int quantity,
			@Param("balanceAfter") int balanceAfter,
			@Param("customerId") Long customerId,
			@Param("memo") String memo,
			@Param("createdAt") LocalDateTime createdAt,
			@Param("createdBy") String createdBy);

	List<StockHistoryVo> selectStockHistory(@Param("offset") int offset,@Param("limit") int limit);
	int countStockHistory();
	StockHistoryVo selectStockHistoryById(Long historyId);
	int markStockHistoryCancelled(@Param("historyId") Long historyId,@Param("cancelledBy") String cancelledBy);

	List<StockHistoryVo> selectStockHistoryFrom(@Param("start") LocalDateTime start);

	List<CustomerVo> selectCustomerList();
	CustomerVo selectCustomerById(Long customerId);

	void insertCustomer(CustomerVo customerVo);
	int updateCustomer(CustomerVo customerVo);
	int deleteCustomer(@Param("customerId") Long customerId, @Param("updatedBy") String updatedBy);

	int countUsers();
	UserVo selectUserByUsername(String username);
	List<UserVo> selectUserList();
	void insertUser(UserVo userVo);
	int updateUser(@Param("username") String username,@Param("displayName") String displayName,@Param("role") String role,@Param("passwordHash") String passwordHash);
	int softDeleteUser(String username);
	int countActiveAdmins();
	void insertAudit(@Param("username") String username, @Param("actionType") String actionType,
			@Param("targetType") String targetType, @Param("targetId") String targetId,
			@Param("details") String details);
	List<AuditLogVo> selectAuditList(@Param("offset") int offset, @Param("limit") int limit);
	int countAuditLogs();
	int deleteExpiredAuditLogs();
	List<EmployeeVo> selectEmployeeList();
	EmployeeVo selectEmployeeById(Long employeeId);
	void insertEmployee(EmployeeVo employeeVo);
	int updateEmployee(EmployeeVo employeeVo);
	int softDeleteEmployee(@Param("employeeId") Long employeeId, @Param("updatedBy") String updatedBy);
	void insertEmployeeLeave(EmployeeLeaveVo leaveVo);
	List<EmployeeLeaveVo> selectEmployeeLeaveList(Long employeeId);
	int deleteEmployeeLeave(@Param("leaveHistoryId") Long leaveHistoryId,@Param("employeeId") Long employeeId);
	List<EmployeePayrollVo> selectEmployeePayrollList(Long employeeId);
	void upsertEmployeePayroll(EmployeePayrollVo payrollVo);
	int deleteEmployeePayroll(@Param("payrollId") Long payrollId,@Param("employeeId") Long employeeId);
	List<EmployeeWorkAllowanceVo> selectEmployeeWorkAllowanceList(@Param("employeeId") Long employeeId, @Param("workMonth") String workMonth);
	BigDecimal selectEmployeeWorkAllowanceTotal(@Param("employeeId") Long employeeId, @Param("workMonth") String workMonth);
	void upsertEmployeeWorkAllowance(EmployeeWorkAllowanceVo workVo);
	int deleteEmployeeWorkAllowance(@Param("employeeId") Long employeeId,@Param("workDate") java.time.LocalDate workDate);
	List<SalesOrderVo> selectSalesOrderList();
	SalesOrderVo selectSalesOrderById(Long orderId);
	void insertSalesOrder(SalesOrderVo orderVo);
	int addProducedQuantity(@Param("orderId") Long orderId, @Param("goodQty") int goodQty, @Param("defectiveQty") int defectiveQty, @Param("updatedBy") String updatedBy);
	int addShippedQuantity(@Param("orderId") Long orderId, @Param("shipmentQty") int shipmentQty, @Param("updatedBy") String updatedBy);
	void refreshSalesOrderStatus(Long orderId);
	int updateSalesOrder(SalesOrderVo orderVo);
	int cancelSalesOrder(@Param("orderId") Long orderId,@Param("updatedBy") String updatedBy);
}
