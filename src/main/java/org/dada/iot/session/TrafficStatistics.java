package org.dada.iot.session;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流量统计
 * 包含进出站的包和大小
 
 */
public class TrafficStatistics {
    /**
     * 进站数据包数量
     */
    static AtomicInteger inPack = new AtomicInteger(0);

    static AtomicInteger outPack = new AtomicInteger(0);
    /**
     * 进站数据包大小
     */
    static AtomicLong inByte = new AtomicLong(0);

    static AtomicLong outByte = new AtomicLong(0);

    /**
     * 增加进站数据包
     * @param byteSize 增加的数据包大小
     */
    public static void addInPack(long byteSize){
        inPack.getAndIncrement();//表示增加一个数据包
        inByte.getAndAdd(byteSize);//表示增加数据包大小
    }

    /**
     * 增加出站数据包
     * @param byteSize 增加的数据包大小
     */
    public static void addOutPack(long byteSize){
        outPack.getAndIncrement();
        outByte.getAndAdd(byteSize);
    }

    public static int getInPack(){
        return inPack.get();//表示获取数据包数量
    }

    public static int getOutPack(){
        return outPack.get();//表示获取数据包数量
    }

    public static long getInByte(){
        return inByte.get();//表示获取数据包大小
    }

    public static long getOutByte(){
        return outByte.get();//表示获取数据包大小
    }
}
