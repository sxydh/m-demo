package cn.net.bhe.mybatisqsdemo.mapper;


import cn.net.bhe.mybatisqsdemo.entity.Order;

import java.util.List;

public interface OrderMapper {

    List<Order> selectOrderList();

}
