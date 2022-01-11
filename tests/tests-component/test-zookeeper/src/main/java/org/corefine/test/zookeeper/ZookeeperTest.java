package org.corefine.test.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fe by 2021/12/9 18:57
 */
public class ZookeeperTest {

    public static class ServerInfo {
        private String serverName;
        private String ip;
        private String port;

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }
    }

    private final Pattern pattern = Pattern.compile("dubbo://([0-9.]+):([0-9]+)/[a-zA-Z.0-9]+\\?anyhost=true&application=([a-z-]+)&.*");

    private ServerInfo parseServerInfo(String dubboInfo) {
        try {
            dubboInfo = URLDecoder.decode(dubboInfo, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Matcher matcher = pattern.matcher(dubboInfo);
        if (matcher.find()) {
            ServerInfo info = new ServerInfo();
            info.setIp(matcher.group(1));
            info.setPort(matcher.group(2));
            info.setServerName(matcher.group(3));
            return info;
        }
        return null;
    }

    public void test() throws IOException, InterruptedException, KeeperException {
        String s = "zookeeper0-0.zookeeper.default.svc.cluster.local:2181," +
                "zookeeper1-0.zookeeper.default.svc.cluster.local:2181," +
                "zookeeper2-0.zookeeper.default.svc.cluster.local:2181";
        ZooKeeper zooKeeper = new ZooKeeper(s, 5000, null);
        Set<ServerInfo> infos = new HashSet<>();
        List<String> list = zooKeeper.getChildren("/dubbo", false);
        list.stream().filter(e -> e.startsWith("com.")).forEach(e -> {
            try {
                List<String> servers = zooKeeper.getChildren("/dubbo/" + e + "/providers", false);
                servers.forEach(d -> {
                    ServerInfo info = parseServerInfo(d);
                    if (info != null) {
                        infos.add(info);
                    }
                });
            } catch (KeeperException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        infos.stream().forEach(System.out::println);
        System.out.println(infos.size());
    }

    public void test2() {
        String s = "zookeeper0-0.zookeeper.default.svc.cluster.local:2181," +
                "zookeeper1-0.zookeeper.default.svc.cluster.local:2181," +
                "zookeeper2-0.zookeeper.default.svc.cluster.local:2181";
        ZookeeperHandler handler = new ZookeeperHandler();
        handler.setZookeeperServers(s);
        handler.init();
        String key = "aaaa";
        handler.mutexRun(key, () -> System.out.println("你好"));
        handler.mutexRun(key, () -> System.out.println("你好2"));
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        new ZookeeperTest().test2();
    }
}
