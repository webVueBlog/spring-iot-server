package org.dada.iot.handler;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;
import org.dada.iot.protocol.ProtocolBase;
import org.dada.iot.session.TrafficStatistics;

/**
 * 出站编码器
 
 */
public class EncoderHandler extends ChannelOutboundHandlerAdapter {

    /**
     *
     * @param ctx               the {@link ChannelHandlerContext} for which the write operation is made
     * @param msg               the message to write
     * @param promise           the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg == null) {
            return;
        }

        if(msg instanceof ProtocolBase) {

            String json = new Gson().toJson(msg);
            ByteBuf byteBuf = ctx.alloc().buffer(json.length(), json.length());//表示分配一个初始容量为json.length()，最大容量为json.length()的ByteBuf

            byte[] bytes = json.getBytes(CharsetUtil.UTF_8);
            byteBuf.writeBytes(bytes);
            // 出站计数器
            TrafficStatistics.addOutPack(byteBuf.readableBytes());
            super.write(ctx,byteBuf,promise);
        }else{

            super.write(ctx,msg,promise);
        }


    }
}
