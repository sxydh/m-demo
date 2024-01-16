package cn.net.bhe.mybatisqsdemo;

import cn.net.bhe.mybatisqsdemo.entity.Order;
import cn.net.bhe.mybatisqsdemo.mapper.OrderMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

public class _Main {

    public static void main(String[] args) throws Exception {
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml")) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
                List<Order> orderList = orderMapper.selectOrderList();
                System.out.println(orderList.size());
            }
        }
    }

}
