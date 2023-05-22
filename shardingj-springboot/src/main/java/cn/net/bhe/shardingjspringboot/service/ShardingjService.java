package cn.net.bhe.shardingjspringboot.service;

import cn.net.bhe.shardingjspringboot.entity.Order;

import java.util.List;

public interface ShardingjService {

    void insertOrder(Order order);

    List<Order> selectOrderList(Order order);

    void updateOrder(Order order);

    void deleteOrder(Order order);

}
