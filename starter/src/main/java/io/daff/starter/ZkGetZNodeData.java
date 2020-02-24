package io.daff.starter;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 获取节点数据
 *
 * @author daffupman
 * @since 2020/2/6
 */
public class ZkGetZNodeData implements Watcher {
    private ZooKeeper zookeeper = null;

    public static final String zkServerPath = "192.168.35.65:2181";
    public static final Integer timeout = 5000;
    private static Stat stat = new Stat();

    public ZkGetZNodeData() {}

    public ZkGetZNodeData(String connectString) {
        try {
            zookeeper = new ZooKeeper(connectString, timeout, new ZkGetZNodeData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CountDownLatch countDown = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        ZkGetZNodeData zkServer = new ZkGetZNodeData(zkServerPath);

        /*
         参数：
         path：节点路径
         watch：true或者false，注册一个watch事件
         stat：状态
         */
        byte[] resByte = zkServer.getZookeeper().getData("/imooc", true, stat);
        String result = new String(resByte);
        System.out.println("当前值:" + result);
        countDown.await();
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if(event.getType() == EventType.NodeDataChanged){
                ZkGetZNodeData zkServer = new ZkGetZNodeData(zkServerPath);
                byte[] resByte = zkServer.getZookeeper().getData("/imooc", false, stat);
                String result = new String(resByte);
                System.out.println("更改后的值:" + result);
                System.out.println("版本号变化dversion：" + stat.getVersion());
                countDown.countDown();
            } else if(event.getType() == EventType.NodeCreated) {

            } else if(event.getType() == EventType.NodeChildrenChanged) {

            } else if(event.getType() == EventType.NodeDeleted) {

            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }
    public void setZookeeper(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }
}
