package cn.net.bhe.quickstartdemo;

import cn.net.bhe.mutil.CpUtils;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Log4j2
public class _FluxApp {

    public static void main(String[] args) {
        /* 序列生产者 */
        Flux<String> flux = Flux.generate(synchronousSink -> {
            String value = CpUtils.ranChnCp();
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
                            log.info("map: {}", map);
                            return map.toString();
                        }));

        /* 序列消费者 */
        flux.subscribe(
                value -> log.info("consumer: {}", value),
                error -> log.error("errorConsumer: {}", error.getLocalizedMessage()),
                () -> log.info("completeConsumer"));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            log.error(e);
        }
    }

}
