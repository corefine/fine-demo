package org.corefine.test.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;

import java.util.concurrent.TimeUnit;

/**
 * @author Fe by 2021/12/20 16:59
 */
public class LockTest {


    /** Zookeeper info */
    private static final String ZK_ADDRESS = "zookeeper0-0.zookeeper.default.svc.cluster.local:2181,zookeeper1-0.zookeeper.default.svc.cluster.local:2181,zookeeper2-0.zookeeper.default.svc.cluster.local:2181";
    private static final String ZK_LOCK_PATH = "/zktest";

    public static void main(String[] args) throws Exception {
        // 1.Connect to zk
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS, new RetryNTimes(1, 5000)
        );
        client.start();
        System.out.println("zk client start successfully!");
        System.out.println(client.getChildren().forPath("/dubbo"));

        Thread t1 = new Thread(() -> {
            doWithLock(client);
        }, "t1");
        Thread t2 = new Thread(() -> {
            doWithLock(client);
        }, "t2");

        t1.start();
        t2.start();
    }

    private static void doWithLock(CuratorFramework client) {
        InterProcessMutex lock = new InterProcessMutex(client, ZK_LOCK_PATH);
        try {
            if (lock.acquire(2, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName() + " hold lock");
                Thread.sleep(8000L);
                System.out.println(Thread.currentThread().getName() + " release lock");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
