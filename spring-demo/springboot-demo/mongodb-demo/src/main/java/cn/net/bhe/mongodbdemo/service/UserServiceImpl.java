package cn.net.bhe.mongodbdemo.service;

import cn.net.bhe.mongodbdemo.dao.UserRepository;
import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.dao.po.UserPO;
import cn.net.bhe.mongodbdemo.dao.query.UserQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public List<UserDTO> listUsers(UserQuery userQuery) {
        Example<UserPO> example = Example.of(
                UserPO.of(userQuery),
                ExampleMatcher.matching()
                        .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains()));
        List<UserPO> userPoList = userRepository.findAll(example);
        return userPoList.stream().map(UserDTO::of).collect(Collectors.toList());
    }

}
