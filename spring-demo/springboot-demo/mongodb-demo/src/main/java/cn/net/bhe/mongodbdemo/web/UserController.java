package cn.net.bhe.mongodbdemo.web;

import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.dao.query.UserQuery;
import cn.net.bhe.mongodbdemo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    public List<UserDTO> listUsers(UserQuery userQuery) {
        return userService.listUsers(userQuery);
    }

}
