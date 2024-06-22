package cn.net.bhe.quickstartdemo;

import cn.net.bhe.mutil.CpUtils;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class MonoDemo {

    public static void main(String[] args) {
        Mono<String> mono = Mono.defer(() -> Mono.just(CpUtils.ranChnCp()));
    }

}
