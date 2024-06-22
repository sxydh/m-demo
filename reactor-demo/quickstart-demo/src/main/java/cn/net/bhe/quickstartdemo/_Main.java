package cn.net.bhe.quickstartdemo;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class _Main {

    public static void main(String[] args) {
// 创建一个包含数字1到5的 Flux
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5)
                .subscribeOn(Schedulers.parallel()); // 在并行线程中执行

        // 订阅 Flux 并定义处理数据的逻辑
        flux.subscribe(
                // onNext 指定对每个元素的处理逻辑
                item -> System.out.println("Received: " + item),
                // onError 指定处理错误的逻辑
                error -> System.err.println("Error: " + error),
                // onComplete 指定 Flux 完成时的处理逻辑
                () -> System.out.println("Flux completed!")
        );

        // 防止主线程提前结束
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
