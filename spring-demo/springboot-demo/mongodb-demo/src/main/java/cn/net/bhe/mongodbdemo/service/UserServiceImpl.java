package cn.net.bhe.mongodbdemo.service;

import cn.net.bhe.mongodbdemo.dao.UserRepository;
import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import cn.net.bhe.mongodbdemo.dao.po.UserPO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public List<UserDTO> listUsers(UserDTO dto) {
        Example<UserPO> example = Example.of(
                UserPO.of(dto),
                ExampleMatcher.matching()
                        .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains()));
        List<UserPO> userPoList = userRepository.findAll(example);
        return userPoList.stream().map(UserDTO::of).collect(Collectors.toList());
    }

    @Override
    public void addUser(UserDTO dto) {
        UserPO userPO = UserPO.of(dto);
        userRepository.save(userPO);
    }

}
