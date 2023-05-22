package cn.net.bhe.shardingjspringboot.mapper;

import cn.net.bhe.shardingjspringboot.entity.Order;
import cn.net.bhe.shardingjspringboot.entity.OrderLi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/* TODO */
@Mapper
public interface ShardingjMapper {

    int insertOrder(Order order);

    int insertOrderLi(OrderLi orderLi);

    List<Order> selectOrderList(Order order);

    int updateOrder(Order order);

    int deleteOrder(Order order);

    int deleteOrderLiByOrderId(@Param("orderId") Long orderId);

}
