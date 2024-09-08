package cn.net.bhe.mongodbdemo.web;

import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping
    public List<UserDTO> listUsers(UserDTO dto) {
        return userService.listUsers(dto);
    }

    @PostMapping
    public void addUser(UserDTO dto) {
        userService.addUser(dto);
    }

}
