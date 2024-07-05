package cn.net.bhe.mybatisplugindemo.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class Order {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Date orderDate;
    private String orderStatus;
    private String paymentMethod;
    private String shippingAddress;
    private String contactNumber;
    private String email;
    private String shippingCompany;
    private String trackingNumber;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private String invoiceTitle;
    private String taxNumber;
    private String notes;
    private Long promotionId;
    private Date createTime;
    private Date updateTime;
    private String rattr;
    private String rattr2;
    private String rattr3;
    private String rattr4;
    private String rattr5;
    private String rattr6;
    private String rattr7;
}
