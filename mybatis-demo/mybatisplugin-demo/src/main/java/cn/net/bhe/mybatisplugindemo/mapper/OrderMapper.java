package cn.net.bhe.mybatisplugindemo.mapper;


import cn.net.bhe.mybatisplugindemo.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    List<Order> selectOrderList(@Param("limit") int limit);

}
