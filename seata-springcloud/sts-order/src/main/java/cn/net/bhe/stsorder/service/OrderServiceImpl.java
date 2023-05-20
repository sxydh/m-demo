package cn.net.bhe.stsorder.service;

import cn.net.bhe.stscommon.domain.Invt;
import cn.net.bhe.stscommon.domain.Order;
import cn.net.bhe.stscommon.domain.Ret;
import cn.net.bhe.stsfeign.InvtFeignClient;
import cn.net.bhe.stsorder.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private InvtFeignClient invtFeignClient;

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional
    @Override
    public Order add(Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        orderMapper.insert(order);
        order = orderMapper.selectByOrderId(order.getOrderId());

        Invt invt = new Invt();
        invt.setResId(order.getGoodsId());
        invt.setQuantity(order.getQuantity());
        Ret<Invt> ret = invtFeignClient.update(invt);
        order.setInvt(ret.getData());
        return order;
    }

}
