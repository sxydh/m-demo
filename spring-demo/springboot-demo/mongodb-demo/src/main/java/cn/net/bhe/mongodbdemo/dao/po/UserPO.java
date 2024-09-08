package cn.net.bhe.mongodbdemo.dao.po;


import cn.net.bhe.mongodbdemo.dao.query.UserQuery;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collation = "user")
public class UserPO {

    public static UserPO of(UserQuery query) {
        UserPO po = new UserPO();
        BeanUtils.copyProperties(query, po);
        return po;
    }

    @Id
    private String id;
    private String name;
    private Integer age;

}
