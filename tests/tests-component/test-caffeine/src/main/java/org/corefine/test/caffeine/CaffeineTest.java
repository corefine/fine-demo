package org.corefine.test.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;

/**
 * @author Fe by 2022/1/13 09:11
 */
public class CaffeineTest {

    public static record User(String userName, String nickName, int age) {

        protected void finalize() throws Throwable {
            System.out.println("destroy: " + userName);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LoadingCache<String, User> cache = Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofSeconds(15))
                .refreshAfterWrite(Duration.ofSeconds(5))
                .recordStats()
                .evictionListener((key, value, cause) -> {
                    System.out.println(key + "@" + value + "#" + cause);
                })
                .build(CaffeineTest::createExpensiveGraph);
        for (int i = 0; i < 10000; i++) {
            cache.get("张" + (i + 11));
            cache.get("张" + (i + 9));
            cache.get("张" + (i + 7));
            cache.get("张" + (i + 5));
            cache.get("张" + (i + 3));
            cache.get("张" + i);
            System.out.println(cache.stats() + "  " + cache.estimatedSize());
//            System.out.println("get:" + cache.get("张" + i));
//            System.out.println("get:" + cache.get("张" + (i + 3)));
            Thread.sleep(1000);
        }
    }

    public static User createExpensiveGraph(String key) {
//        System.out.println("init: " + key);
        return new User(key, key + "@nick", key.hashCode());
    }
}
