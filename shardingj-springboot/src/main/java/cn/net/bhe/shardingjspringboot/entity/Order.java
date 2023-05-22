package cn.net.bhe.shardingjspringboot.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/* TODO */
@Data
public class Order {

    private Long id;
    private Date orderDate;
    private List<OrderLi> orderLiList;

}
