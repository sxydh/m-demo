package cn.net.bhe.jdkdemo.serializedemo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer age;

}
