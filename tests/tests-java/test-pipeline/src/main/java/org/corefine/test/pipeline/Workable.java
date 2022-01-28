package org.corefine.test.pipeline;

/**
 * @author Fe by 2022/1/18 09:20
 */
public interface Workable extends Runnable {

    default String getWorkName() {
        return getClass().getSimpleName();
    }
}
