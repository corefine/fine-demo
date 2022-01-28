package org.corefine.test.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fe by 2022/1/18 09:22
 */
public abstract class Worker implements Workable {
    private final String workName;
    protected final Logger logger;

    protected Worker(String workName) {
        this.workName = workName;
        logger = LoggerFactory.getLogger(workName);
    }

    public final String getWorkName() {
        return workName;
    }
}
