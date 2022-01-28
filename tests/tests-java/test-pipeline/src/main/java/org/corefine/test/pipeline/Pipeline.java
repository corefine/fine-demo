package org.corefine.test.pipeline;

/**
 * @author Fe by 2022/1/18 11:37
 */
public interface Pipeline {
    int CONTINUE = 1, BREAK = -1;

    int execute(Workable work);
}
