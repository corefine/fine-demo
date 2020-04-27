package org.corefine.demo.io.file.reader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 用途：
 * 按行保存内容，实现快速读取指定行。
 * 功能：
 * 1.实现顺序读取
 * 2.实现指定时间读取
 * 参数：
 * 存储内容：记录的时间，记录长度，记录内容
 * 实现：
 * 读取时先读取出所有的时间和index存储在list中，并将时间放入TreeMap
 * 原理：
 * RandomAccessFile支持指定位置读取，TreeMap支持时间读取
 * 缺点：
 * 未实现写入功能
 *
 * @author Fe by 2020/4/27 11:52
 */
public class FileIndexReader {

    public ConnectInfo open(String filepath) throws IOException {
        ConnectInfo info = new ConnectInfo();
        info.setFilepath(filepath);
        info.setReader(new RandomAccessFile(filepath, "r"));
        List<Record> list = new ArrayList<>();
        TreeMap<Long, Integer> timeIndex = new TreeMap<>();
        info.setDatas(list);
        info.setTimeIndex(timeIndex);
        readIndex(info, list, timeIndex);
        return info;
    }

    public void close(ConnectInfo info) throws IOException {
        if (info != null && info.reader() != null) {
            info.reader().close();
            info.setReader(null);
        }
    }

    public void readData(ConnectInfo info, List<Record> list) throws IOException {
        RandomAccessFile reader = info.reader();
        byte[] buffer = new byte[1024 * 64];    //使用64K读取，硬盘默认读取4K
        int index;
        for (Record record : list) {
            reader.seek(record.getPosition());
            index = reader.read(buffer, 0, record.getLength());
            record.setData(new String(buffer, 0, index, StandardCharsets.UTF_8));
        }
    }

    public List<Record> readDataByTime(ConnectInfo info, long time, int length) throws IOException {
        Map.Entry<Long, Integer> index = info.timeIndex().ceilingEntry(time);
        if (index == null) {
            return new ArrayList<>();
        } else {
            return readDataById(info, index.getValue(), length);
        }
    }

    public List<Record> readDataById(ConnectInfo info, int id, int length) throws IOException {
        List<Record> datas = info.datas();
        if (id >= datas.size() || length <= 0) {
            return new ArrayList<>();
        } else {
            int end = id + length;
            if (end > datas.size()) {
                end = datas.size();
            }
            List<Record> result = datas.subList(id, end);
            readData(info, result);
            return result;
        }
    }

    private void readIndex(ConnectInfo info, List<Record> list, TreeMap<Long, Integer> timeIndex) throws IOException {
        RandomAccessFile reader = info.reader();
        long length = info.reader().length();
        int position = 0;
        byte[] binfo = new byte[12];
        long currentTime = 0;
        while (position < length) {
            position += 12;
            int index = reader.read(binfo);
            if (index != 12) {
                throw new IOException("文件读取异常");
            }
            int s = bytesToInt(binfo, 0);   //秒
            int m = bytesToInt(binfo, 4);   //毫秒
            int l = bytesToInt(binfo, 8);   //长度
            long t = s * 1000L + m / 1000;           //time
            Record record = new Record();
            record.setId(list.size());
            record.setLength(l);
            record.setPosition(position);
            record.setTimestamp(t);
            list.add(record);
            position += l;
            reader.seek(position);
            if (currentTime != t) {
                timeIndex.put(t, record.getId());
                currentTime = t;
            }
        }
    }

    /**
     * 时间读取实现
     */
    private int bytesToInt(byte[] ary, int offset) {
        int value;
        value = ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000));
        return value;
    }

    public static class ConnectInfo {
        private String filepath;
        private RandomAccessFile reader;
        private List<Record> datas;
        private TreeMap<Long, Integer> timeIndex;
        private long position;

        public String filepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public RandomAccessFile reader() {
            return reader;
        }

        public void setReader(RandomAccessFile reader) {
            this.reader = reader;
        }

        public List<Record> datas() {
            return datas;
        }

        public void setDatas(List<Record> datas) {
            this.datas = datas;
        }

        public long position() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }

        public TreeMap<Long, Integer> timeIndex() {
            return timeIndex;
        }

        public void setTimeIndex(TreeMap<Long, Integer> timeIndex) {
            this.timeIndex = timeIndex;
        }

        protected void finalize() throws Throwable {
            if (reader != null) {
                reader.close();
            }
            reader = null;
        }
    }

    public static class Record {
        private int id;
        private int length;
        private long timestamp;
        private long position;
        private String data;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public long getPosition() {
            return position;
        }

        public void setPosition(long position) {
            this.position = position;
        }
    }

}