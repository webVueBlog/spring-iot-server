package org.dada.iot.handler;

import lombok.extern.slf4j.Slf4j;
import org.dada.iot.session.SessionManager;
import org.dada.iot.event.MainEventProducer;
import org.dada.iot.session.TrafficStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 服务程序状态信息打印
 *
 
 */
@Slf4j
@Component
public class StatusPringHandler implements Runnable {

    /**
     * 会话管理
     */
    @Autowired
    SessionManager sessionManager;

    @Autowired
    MainEventProducer mainEventProducer;//主事件队列生产者,用于获取主事件队列大小

    @Override
    public void run() {
        log.info(
                "\n"+
                "+---------+---------+------------+-----------+-------------+---------------+-------------+---------------+\n" +
                "| session | channel | main queue | log queue |   in pack   |    in byte    |   out pack  |    out byte   |\n" +
                "+---------+---------+------------+-----------+-------------+---------------+-------------+---------------+\n"+
                "| "+String.format("%7d",sessionManager.getSessionCount())+" | "+String.format("%7d",sessionManager.getChannelCount())+" |    "+String.format("%7d",mainEventProducer.getRingBufferSize())+" |   "+String.format("%7d",0)+" |     " +
                        String.format("%7d", TrafficStatistics.getInPack())+" |     "+String.format("%9d", TrafficStatistics.getInByte())+" |     "+String.format("%7d", TrafficStatistics.getOutPack())+" |     "+String.format("%9d", TrafficStatistics.getOutByte())+" |\n"+
                "+---------+---------+------------+-----------+-------------+---------------+-------------+---------------+\n");
    }
}
