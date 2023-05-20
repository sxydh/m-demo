package cn.net.bhe.stsorder.service;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stscommon.domain.Order;
import cn.net.bhe.stscommon.domain.Ret;
import cn.net.bhe.stsfeign.InvtFeignClient;
import cn.net.bhe.stsorder.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private InvtFeignClient invtFeignClient;

    @Override
    public Order add(Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        orderMapper.insert(order);
        order = orderMapper.selectByOrderId(order.getOrderId());

        Invt invt = new Invt();
        invt.setResId(order.getGoodsId());
        invt.setQuantity(order.getQuantity());
        Ret<Invt> ret = invtFeignClient.update(invt);
        Assert.isTrue(ret.getCode() == Ret.OK, ret.getMsg());
        invt = ret.getData();
        order.setInvt(invt);
        return order;
    }

}
