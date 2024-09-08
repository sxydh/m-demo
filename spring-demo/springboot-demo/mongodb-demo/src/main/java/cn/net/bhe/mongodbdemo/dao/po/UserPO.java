package cn.net.bhe.mongodbdemo.dao.po;


import cn.net.bhe.mongodbdemo.dao.dto.UserDTO;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collation = "user")
public class UserPO {

    public static UserPO of(UserDTO dto) {
        UserPO po = new UserPO();
        BeanUtils.copyProperties(dto, po);
        return po;
    }

    @Id
    private String id;
    private String name;
    private Integer age;

}
