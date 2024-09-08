package cn.net.bhe.mongodbdemo.service;

import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;

import java.util.List;

public interface UserService {

    List<UserDTO> listUsers(UserDTO dto);

    void addUser(UserDTO dto);

}
