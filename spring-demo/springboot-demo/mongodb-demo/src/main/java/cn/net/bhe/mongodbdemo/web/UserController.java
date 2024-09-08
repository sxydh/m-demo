package cn.net.bhe.mongodbdemo.web;

import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.service.UserService;
import cn.net.bhe.mutil.NmUtils;
import cn.net.bhe.mutil.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Scanner;

@RestController
@Slf4j
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

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String line = scanner.nextLine();
                    if ("1".equals(line)) {
                        UserDTO userDTO = new UserDTO();
                        userDTO.setId(System.currentTimeMillis() + "");
                        userDTO.setName(NmUtils.randomName());
                        userDTO.setAge(NumUtils.ranInt());
                        this.addUser(userDTO);
                    } else if ("2".equals(line)) {
                        UserDTO userDTO = new UserDTO();
                        List<UserDTO> dtoList = this.listUsers(userDTO);
                        System.out.println(dtoList);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).start();
    }

}
