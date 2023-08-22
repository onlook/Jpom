package org.dromara.jpom.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.dromara.jpom.transport.properties.NettyProperties;
import org.dromara.jpom.transport.netty.service.ChannelServiceManager;
import org.dromara.jpom.transport.netty.service.NettyCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * Netty客户端
 *
 * @author Hong
 * @since 2023/08/22
 */
public abstract class NettyClient implements CommandLineRunner, Closeable, ChannelClient {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    private final EventLoopGroup group = new NioEventLoopGroup();

    private NettyProperties nettyProperties;

    private List<ChannelInitializerClient> supports;

    @Override
    public void run(String... args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        doConnect(bootstrap, group);
    }

    @Autowired
    public void setNettyProperties(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
    }

    @Autowired
    public void setSupports(List<ChannelInitializerClient> supports) {
        this.supports = supports;
    }

    @Override
    public void close() throws IOException {
        try {
            group.shutdownGracefully().sync();
            log.info("关闭Netty");
        } catch (Exception e) {
            log.error("关闭Netty失败");
        }
    }

    @Override
    public void doConnect(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        Optional<ChannelInitializerClient> channelSupport = supports.stream().filter(it -> it.support(nettyProperties.getTcp())).findFirst();
        if (channelSupport.isPresent()) {
            ChannelInitializerClient client = channelSupport.get();
            client.setChannelClient(this);
            bootstrap.handler(client);
            bootstrap.remoteAddress(nettyProperties.getHost(), nettyProperties.getPort());
            connect(bootstrap);
        } else {
            log.info("Tcp方式错误，仅支持websocket/socket");
            throw new RuntimeException("Tcp方式错误，仅支持websocket/socket");
        }
    }

    public void connect(Bootstrap bootstrap) {
        bootstrap.connect().addListener((ChannelFuture futureListener) -> {
            ChannelServiceManager.setChannelService(NettyCustomer.INSTANCE);
            final EventLoop eventLoop = futureListener.channel().eventLoop();
            if (futureListener.cause() != null) {
                log.error("Failed to connect to server" + futureListener.cause() + " -> We will try connect after 10s");
                futureListener.channel().eventLoop().schedule(() -> doConnect(new Bootstrap(), eventLoop), 10, TimeUnit.SECONDS);
            } else {
                NettyCustomer.add(futureListener.channel());
                log.info("启动 {} client，连接server成功, host: {}, port: {}", nettyProperties.getTcp(), nettyProperties.getHost(), nettyProperties.getPort());
            }
        });
    }
}
