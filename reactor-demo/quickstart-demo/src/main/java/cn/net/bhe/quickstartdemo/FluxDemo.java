package cn.net.bhe.quickstartdemo;

import cn.net.bhe.mutil.CpUtils;
import cn.net.bhe.mutil.NumUtils;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Log4j2
public class FluxDemo {

    public static void main(String[] args) throws Exception {
        Flux<Integer> flux2 = Flux.generate(synchronousSink -> {
            Integer value = NumUtils.ranInt();
            log.info("Produce -> {}", value);
            synchronousSink.next(value);
            sleep(1000);
        });

        flux2 = Flux.just(1, 2, 3, 4);

        flux2.map(value -> value + 1)
                .map(value -> value + 1)
                .map(value -> value + 1)
                .flatMap(value -> Flux.just(value + 1))
                .subscribe(System.out::println);
        Thread.currentThread().join();


        /* 序列生产者 */
        Flux<String> flux = Flux.generate(synchronousSink -> {
            String value = CpUtils.ranChnCp();
            log.info("Produce -> {}", value);
            synchronousSink.next(value);
            sleep(10);
        });

        /* 序列操作符 */
        flux = flux.flatMap(value ->
                // 异步实现
                Flux.just(value).subscribeOn(Schedulers.parallel())
                        // 操作符实现
                        .map(pvalue -> {
                            sleep(15000);
                            Map<String, Object> map = Map.of(pvalue, pvalue.length());
                            log.info("Map -> {}", map);
                            return map.toString();
                        }));

        /* 序列消费者 */
        flux.subscribe(
                value -> log.info("Consume -> {}", value),
                error -> log.error("Error -> {}", error.getLocalizedMessage()),
                () -> log.info("Complete"));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

}
