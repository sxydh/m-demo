package cn.net.bhe.mybatisplugindemo;

import cn.net.bhe.mybatisplugindemo.entity.Order;
import cn.net.bhe.mybatisplugindemo.mapper.OrderMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

public class _MybatisPluginApp {

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
