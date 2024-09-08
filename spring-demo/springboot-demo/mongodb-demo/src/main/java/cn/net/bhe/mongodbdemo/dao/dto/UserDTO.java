package cn.net.bhe.mongodbdemo.dao.dto;

import cn.net.bhe.mongodbdemo.dao.po.UserPO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    public static UserDTO of(UserPO po) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }

    private String id;
    private String name;
    private Integer age;

}
