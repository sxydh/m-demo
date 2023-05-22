package cn.net.bhe.shardingjspringboot.controller;

import cn.net.bhe.shardingjspringboot.entity.Order;
import cn.net.bhe.shardingjspringboot.service.ShardingjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shardingj")
public class ShardingjController {

    @Autowired
    private ShardingjService shardingjService;

    @PostMapping("/insertOrder")
    public void insertOrder(@RequestBody Order order) {
        shardingjService.insertOrder(order);
    }

    @GetMapping("/selectOrderList")
    public List<Order> selectOrderList(Order order) {
        return shardingjService.selectOrderList(order);
    }

    @PutMapping("/updateOrder")
    public void updateOrder(@RequestBody Order order) {
        shardingjService.updateOrder(order);
    }

    @DeleteMapping("/deleteOrder")
    public void deleteOrder(Order order) {
        shardingjService.deleteOrder(order);
    }

}
