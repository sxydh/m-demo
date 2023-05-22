package cn.net.bhe.shardingjspringboot.service;

import cn.net.bhe.shardingjspringboot.entity.Order;
import cn.net.bhe.shardingjspringboot.entity.OrderLi;
import cn.net.bhe.shardingjspringboot.mapper.ShardingjMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShardingjServiceImpl implements ShardingjService {

    @Autowired
    private ShardingjMapper shardingjMapper;

    @Override
    public void insertOrder(Order order) {
        shardingjMapper.insertOrder(order);
        insertOrderLi(order);
    }

    private void insertOrderLi(Order order) {
        if (order.getOrderLiList() != null) {
            shardingjMapper.deleteOrderLiByOrderId(order.getId());
            for (OrderLi orderLi : order.getOrderLiList()) {
                orderLi.setOrderId(order.getId());
                shardingjMapper.insertOrderLi(orderLi);
            }
        }
    }

    @Override
    public List<Order> selectOrderList(Order order) {
        return shardingjMapper.selectOrderList(order);
    }

    @Override
    public void updateOrder(Order order) {
        shardingjMapper.updateOrder(order);
        insertOrderLi(order);
    }

    @Override
    public void deleteOrder(Order order) {
        shardingjMapper.deleteOrder(order);
        shardingjMapper.deleteOrderLiByOrderId(order.getId());
    }

}
