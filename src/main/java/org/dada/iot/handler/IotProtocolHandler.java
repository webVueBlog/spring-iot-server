package org.dada.iot.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.dada.iot.event.MainEventProducer;
import org.dada.iot.session.TrafficStatistics;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 协议包解析
 
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class IotProtocolHandler extends MessageToMessageDecoder<ByteBuf> {


    /**
     * 主事件循环
     */
    private final MainEventProducer mainEventProducer;

    /**
     * 构造函数
     * @param mainEventProducer
     */
    public IotProtocolHandler(MainEventProducer mainEventProducer) {
        this.mainEventProducer = mainEventProducer;
    }

    /**
     * 解码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        try{
            // 计数器
            TrafficStatistics.addInPack(msg.readableBytes());//表示接收到的数据包大小
            // 把接收到的流转写成string字符串
            String message = msg.toString(CharsetUtil.UTF_8);
            log.debug(message);

            // 向队列发布服务
            mainEventProducer.onData(ctx.channel(), message);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
