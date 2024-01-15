package cn.net.bhe.springmvcdemo;

import cn.net.bhe.mutil.StrUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class _Main {

    @GetMapping("/get")
    @ResponseBody
    public String get() {
        return StrUtils.HELLO_WORLD;
    }

}
