package cn.net.bhe.mybatisplugindemo.mapper;


import cn.net.bhe.mybatisplugindemo.entity.Order;

import java.util.List;

public interface OrderMapper {

    List<Order> selectOrderList();

}
