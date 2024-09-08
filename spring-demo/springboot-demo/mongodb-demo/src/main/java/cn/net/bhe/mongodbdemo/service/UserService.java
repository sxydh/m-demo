package cn.net.bhe.mongodbdemo.service;

import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.dao.query.UserQuery;

import java.util.List;

public interface UserService {

    List<UserDTO> listUsers(UserQuery userQuery);

}
