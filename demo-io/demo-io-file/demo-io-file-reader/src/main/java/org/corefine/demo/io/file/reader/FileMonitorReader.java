package org.corefine.demo.io.file.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用途：
 * 多线程文件监听（实时）
 * 功能：
 * 1.多个线程同时访问最新文件，并有一定cache
 * @author Fe by 2020/4/27 14:51
 */
public class FileMonitorReader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, ConnectInfo> connectMaps = new ConcurrentHashMap<>();
    private final BlockingQueue<SendMessageInfo> sendQueue = new ArrayBlockingQueue<>(1024 * 4);
    private boolean stopReadWork = true;
    private static final long WATCH_TIME = 200;
    private Integer cacheSize = 100;

    /**
     * 开始在线读取链接
     */
    public synchronized void createRead(MessageListener listener, String filepath) {
        ConnectInfo info = connectMaps.get(filepath);
        if (info != null) {
            for(String message : info.cache.readAll()) {
                if (message != null) {
                    sendMessage(message, listener, filepath);
                }
            }
        } else {
            info = new ConnectInfo();
            info.filepath = filepath;
            info.overFilepath = filepath + "_1";
            info.sessions = new CopyOnWriteArrayList<>();
            try {
                File file = new File(info.filepath);
                info.reader = new FileReadStream(file);
                info.cache = new CircleStringArray(cacheSize);
                if (file.length() > 10_0000) {
                    info.reader.skip(file.length() - 65536);
                }
            } catch (IOException e) {
                throw new RuntimeException("文件找不到：" + info.filepath, e);
            }
            connectMaps.put(filepath, info);
        }
        MessageInfo message = new MessageInfo();
        message.listener = listener;
        info.sessions.add(message);
        if (stopReadWork) {
            stopReadWork = false;
        }
    }

    /**
     * 关闭在线读取链接
     */
    public synchronized void closeRead(MessageListener listener, String filepath) {
        ConnectInfo info = connectMaps.get(filepath);
        if (info == null) {
            return;
        }
        MessageInfo message = new MessageInfo();
        message.listener = listener;
        info.sessions.remove(message);
        if (info.sessions.isEmpty()) {
            closePath(info);
        }
    }

    private void closePath(ConnectInfo info) {
        try {
            if (info.reader != null) {
                info.reader.close();
            }
            info.reader = null;
        } catch (IOException e) {
            logger.error("reader连接关闭异常", e);
        }
        connectMaps.remove(info.filepath);
        if (connectMaps.isEmpty()) {
            stopReadWork = true;
        }
    }

    /**
     * 读取所有数据
     */
    private void readAllData() {
        Collection<ConnectInfo> values = connectMaps.values();
        for (ConnectInfo info : values) {
            try {
                readData(info, info.reader);
            } catch (Exception e) {
                logger.error("读取数据异常:" + info.filepath, e);
                for (MessageInfo si : info.sessions) {
                    closeRead(si.listener, info.filepath);
                }
            }
        }
    }

    /**
     * 读取数据
     */
    private void readData(ConnectInfo connectInfo, FileReadStream reader) throws IOException {
        String data;
        reader.flush();
        while ((data = reader.readLine()) != null) {
            connectInfo.cache.add(data);
            for (MessageInfo info : connectInfo.sessions) {
                try {
                    sendQueue.put(new SendMessageInfo(info.listener, data));
                } catch (InterruptedException e) {
                    logger.error("等待发送消息异常", e);
                }
            }
        }
//        logger.info("{} 文件当前状态：{}", new File(connectInfo.overFilepath).getAbsolutePath(), new File(connectInfo.overFilepath).exists());
//        logger.info("sessions count: {}", connectInfo.sessions.size());
        if (!new File(connectInfo.overFilepath).exists()) {
            for (MessageInfo info : connectInfo.sessions) {
                info.listener.close();
                closeRead(info.listener, connectInfo.filepath);
                logger.info("close session");
            }
        }
    }

    public void init() {
        //实时读取任务
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (stopReadWork) {
                        Thread.sleep(WATCH_TIME);
                    } else {
                        long start = System.currentTimeMillis();
                        readAllData();
                        long sleep = WATCH_TIME + start - System.currentTimeMillis();
//                        System.out.println("sleep:" + sleep + "\t" + sendQueue.size());
                        if (sleep > 0) {
                            Thread.sleep(sleep);
                        }
                    }
                } catch (Throwable t) {
                    logger.error("读取数据异常", t);
                }
            }
        }, "SSH_PLAY_MESSAGE_READ");
        thread.setDaemon(true);
        thread.start();
        //消息发送任务
        thread = new Thread(() -> {
            while (true) {
                try {
                    SendMessageInfo info = sendQueue.take();
                    info.listener.onMessage(info.message);
                } catch (Throwable t) {
                    logger.error("发送数据异常", t);
                }
            }
        }, "SSH_PLAY_SEND");
        thread.setDaemon(true);
        thread.start();
    }

    private void sendMessage(String data, MessageListener listener, String filename) {
            synchronized (listener) {
                listener.onMessage(data);
            }
    }

    private static class ConnectInfo {
        String filepath;
        String overFilepath;
        List<MessageInfo> sessions;
        CircleStringArray cache;
        FileReadStream reader;
    }

    private static class MessageInfo {
        MessageListener listener;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageInfo that = (MessageInfo) o;
            return listener.equals(that.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener);
        }
    }

    private static class SendMessageInfo {
        final MessageListener listener;
        final String message;
        public SendMessageInfo(MessageListener listener, String message) {
            this.listener = listener;
            this.message = message;
        }
    }

    public interface MessageListener {
        void onMessage(String message);
        void close();
    }


    public static class CircleStringArray {
        private final Object lock = new Object();
        private final String[] array;
        private int index, totalCount;

        public CircleStringArray(int maxSize) {
            this.array = new String[maxSize];
        }

        public void add(String data) {
            synchronized (lock) {
                if (++totalCount < 0) {
                    totalCount = array.length + 1;
                }
                if (++index == array.length) {
                    index = 0;
                }
                array[index] = data;
            }
        }

        public String[] readAll() {
            synchronized (lock) {
                if (totalCount <= array.length) {
                    String[] results = new String[totalCount];
                    System.arraycopy(array, 0, results, 0, totalCount);
                    return results;
                } else {
                    String[] results = new String[array.length];
                    if (index == 0) {
                        System.arraycopy(array, 0, results, 0, array.length);
                        return results;
                    } else {
                        int len = array.length - index;
                        System.arraycopy(array, index, results, 0, len);
                        System.arraycopy(array, 0, results, len, index);
                        return results;
                    }
                }
            }
        }
    }

    public static class FileReadStream extends FileInputStream {
        private CircleByteArray array = new CircleByteArray(1024 * 64);


        public FileReadStream(File name) throws FileNotFoundException {
            super(name);
        }

        public String readLine() throws IOException {
            byte[] buffer = new byte[1024 * 64];
            int length;
            int readLen = super.available();
            if (readLen == 0) {
                return null;
            } else if (readLen > buffer.length) {
                readLen = buffer.length;
            }
            boolean noData = true;
            while ((length = super.read(buffer, 0, readLen)) != -1) {
                array.add(buffer, 0, length);
                if (noData) {
                    noData = false;
                }
            }
            if (!noData) {
                int size = array.size();
                byte[] newBuffer;
                if (size > buffer.length) {
                    newBuffer = new byte[size];
                } else {
                    newBuffer = buffer;
                }
                int readLength = array.read(newBuffer);
                return new String(newBuffer, 0, readLength);
            }
            return null;
        }

        public void flush() throws IOException {
            super.getFD().sync();
        }
    }


    public static class CircleByteArray {
        private byte[] data;
        private int startPoint, endPoint;

        public CircleByteArray() {
            this(64);
        }

        public CircleByteArray(int size) {
            data = new byte[size];
        }

        public int size() {
            if (startPoint == endPoint) {
                return 0;
            }
            return endPoint > startPoint ? (endPoint - startPoint) : (data.length - startPoint + endPoint);
        }

        public int read(byte[] result) {
            int size = size();
            if (size == 0) {
                return size;
            }
            int length = result.length;
            if (length > size) {
                length = size;
            }
            int newStartPoint = startPoint + length;
            if (newStartPoint > data.length) {
                int len = data.length - startPoint;
                System.arraycopy(data, startPoint, result, 0, len);
                startPoint = length - len;
                System.arraycopy(data, 0, result, len, startPoint);
            } else {
                System.arraycopy(data, startPoint, result, 0, length);
                startPoint = newStartPoint;
            }
//        System.out.println("read:\toldSize=" + size + "\tnewSize=" + size() + "\treadSize=" + result.length);
//        System.out.println("point:\tstart=" + startPoint + "\tend=" + endPoint + "\tdataLength=" + data.length);
            return length;
        }

        public void add(byte[] srcData, int start, int length) {
//        System.out.println("start point:\tstart=" + startPoint + "\tend=" + endPoint + "\tdataLength=" + data.length + "\tsrcLen=" + length);
            ensureCapacity(length + 1);
            int newEndPoint = endPoint + length;
            if (newEndPoint > data.length) {
                int len = data.length - endPoint;
                System.arraycopy(srcData, start, data, endPoint, len);
                endPoint = length - len;
                System.arraycopy(srcData, start + len, data, 0, endPoint);
            } else {
                System.arraycopy(srcData, start, data, endPoint, length);
                endPoint = newEndPoint;
            }
//        System.out.println("end   point:\tstart=" + startPoint + "\tend=" + endPoint + "\tdataLength=" + data.length);
        }

        private void ensureCapacity(int addLength) {
            int size = size();
//        System.out.println("add len:" + addLength + "\t" + size);
            int minCapacity = size + addLength;
            int oldCapacity = data.length;
            if (minCapacity > oldCapacity) {
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity < minCapacity) {
                    newCapacity = minCapacity;
                }
                byte[] newData = new byte[newCapacity];
                if (endPoint > startPoint) {
                    System.arraycopy(data, startPoint, newData, 0, size);
                    startPoint = 0;
                    endPoint = size;
                } else if (startPoint > endPoint) {
                    int len = data.length - startPoint;
                    System.arraycopy(data, startPoint, newData, 0, len);
                    System.arraycopy(data, 0, newData, len, size - len);
                    startPoint = 0;
                    endPoint = size;
                }
                data = newData;
            }
        }

        /**
         * 返回当前队列占用的空间
         */
        public int dataLength() {
            return data.length;
        }
    }

}
