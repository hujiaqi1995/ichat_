package com.xdu.ichat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hujiaqi
 * @create 2020/6/27
 */

@Getter
@Setter
@Component
@ConfigurationProperties("websocket.connetor.server")
public class WebsocketServerConfig {
    public int port;
    public int portForDr;
    public boolean useEpoll;
    public boolean useMemPool;
    public boolean useDirectBuffer;
    public int bossThreads;
    public int workerThreads;
    public int userThreads;
    public int connTimeoutMills;
    public int soLinger;
    public int backlog;
    public boolean reuseAddr;
    public int sendBuff;
    public int recvBuff;
    public int readIdleSecond;
    public int writeIdleSecond;
    public int allIdleSecond;
    public int idleTimes;
}
