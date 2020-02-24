package io.daff.starter;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zk会话恢复
 *
 * @author daffupman
 * @since 2020/2/5
 */
public class ZKConnectSessionWatcher implements Watcher {

    final static Logger log = LoggerFactory.getLogger(ZKConnectSessionWatcher.class);

    public static final String zkServerPath = "192.168.35.65:2181";
    public static final Integer timeout = 5000;

    public static void main(String[] args) throws Exception {

        ZooKeeper zk = new ZooKeeper(zkServerPath, timeout, new ZKConnectSessionWatcher());

        // 获取会话id
        long sessionId = zk.getSessionId();
        log.warn("当前会话id为：{}", "0x" + Long.toHexString(sessionId));
        byte[] sessionPassword = zk.getSessionPasswd();
        log.warn("当前会话密码为：{}", new String(sessionPassword));

        log.warn("客户端开始连接zookeeper服务器...");
        log.warn("连接状态：{}", zk.getState());
        Thread.sleep(1000);
        log.warn("连接状态：{}", zk.getState());

        Thread.sleep(200);

        // 开始会话重连
        log.warn("开始会话重连...");
        ZooKeeper zkSession = new ZooKeeper(zkServerPath,
                timeout,
                new ZKConnectSessionWatcher(),
                sessionId,
                sessionPassword);
        log.warn("重新连接状态zkSession：{}", zkSession.getState());
        Thread.sleep(1000);
        log.warn("重新连接状态zkSession：{}", zkSession.getState());
    }

    @Override
    public void process(WatchedEvent event) {
        log.warn("接受到watch通知：{}", event);
    }
}
