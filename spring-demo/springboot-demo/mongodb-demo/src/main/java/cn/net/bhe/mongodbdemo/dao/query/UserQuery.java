package cn.net.bhe.mongodbdemo.dao.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

}
