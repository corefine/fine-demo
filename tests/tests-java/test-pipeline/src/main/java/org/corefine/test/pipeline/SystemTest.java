package org.corefine.test.pipeline;

/**
 * @author Fe by 2022/1/29 17:26
 */
public class SystemTest {

    public static void main(String[] args) {
        System.getProperties().forEach((k, v) -> {
            System.out.println(k + ":  " + v);
        });
    }
}
