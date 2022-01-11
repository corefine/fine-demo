package org.corefine.test.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Fe by 2021/12/20 17:14
 */
public class ZookeeperHandler {
    public static final String ZK_LOCK_PATH = "/curator/mutex/";
    private String zookeeperServers;
    private CuratorFramework client;

    public String createLockKey(String type, int timer) {
        return type + (System.currentTimeMillis() / timer / 1000);
//        return type + "/" + DateUtil.format(new Date(System.currentTimeMillis() / timer / 1000 * timer * 1000), "yyyy-MM-dd/HH:mm:ss");
    }

    public void mutexRun(String lockKey, int timer, Runnable runnable) {
        this.mutexRun(createLockKey(lockKey, timer), runnable);
    }

    public void mutexRun(String lockKey, Runnable runnable) {
        try {
            getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ZK_LOCK_PATH + lockKey);
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException("执行异常", e);
        }
    }

    public void lockRun(String lockKey, long lockTimer, Runnable runnable) {
        InterProcessMutex lock = new InterProcessMutex(getClient(), ZK_LOCK_PATH + lockKey);
        try {
            if (lock.acquire(lockTimer, TimeUnit.SECONDS)) {
                runnable.run();
            }
        } catch (Exception e) {
            throw new RuntimeException("执行异常", e);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {}
        }
    }

    public List<String> list(String path) {
        try {
            return getClient().getChildren().forPath(path);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private CuratorFramework getClient() {
        //等待异步启动的client启动完成（加快启动时间)
        while (client == null) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {}
        }
        return client;
    }

    public void setZookeeperServers(String zookeeperServers) {
        this.zookeeperServers = zookeeperServers;
    }

    public void init() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperServers, new RetryNTimes(5, 5000));
        client.start();
        this.client = client;
    }
}
