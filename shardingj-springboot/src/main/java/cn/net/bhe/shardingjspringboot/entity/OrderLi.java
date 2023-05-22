package cn.net.bhe.shardingjspringboot.entity;

import lombok.Data;

import java.math.BigDecimal;

/* TODO */
@Data
public class OrderLi {

    private Long id;
    private Long orderId;
    private String goodsId;
    private BigDecimal quantity;

}
