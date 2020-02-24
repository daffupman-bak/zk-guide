package io.daff.starter;

import org.apache.zookeeper.AsyncCallback;

/**
 * 监听删除事件
 *
 * @author daffupman
 * @since 2020/2/6
 */
public class DeleteCallBack implements AsyncCallback.VoidCallback {

    @Override
    public void processResult(int i, String path, Object ctx) {
        System.out.println("删除节点" + path);
        System.out.println((String)ctx);
    }
}
