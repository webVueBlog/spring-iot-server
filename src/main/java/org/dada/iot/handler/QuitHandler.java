package org.dada.iot.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.dada.iot.session.SessionManager;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * 主要 处理退出业务,线程安全的可以进行贡献
 
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    SessionManager sessionManager;

    /**
     * 构造函数
     * @param sessionManager
     */
    public QuitHandler(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }

    /**
     * 会话退出时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("会话退出");
        cleanSession(ctx.channel());
    }

    /**
     * 解码失败时，异常客户端踢掉
     * @param ctx ChannelHandlerContext
     * @param cause Throwable
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress)ctx.channel().remoteAddress();//表示远程地址

            log.error("解码异常捕获:[{}:{}]",remoteAddress.getAddress().getHostAddress(),remoteAddress.getPort(), cause);
        } finally {
            ctx.channel().close();//关闭连接
        }
    }

    /**
     * 清空会话
     * @param channel
     */
    private void cleanSession(Channel channel){
        sessionManager.removeSession(channel);
    }
}
