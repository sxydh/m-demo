package cn.net.bhe.stsorder.mapper;

import cn.net.bhe.stscommon.domain.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    @Insert(" insert into sts_order(order_id, goods_id, quantity) values(#{orderId}, #{goodsId}, #{quantity}) ")
    void insert(Order order);

    @Select(" select t.order_id, t.goods_id, t.quantity from sts_order t where t.order_id = #{orderId} ")
    Order selectByOrderId(String orderId);

}
