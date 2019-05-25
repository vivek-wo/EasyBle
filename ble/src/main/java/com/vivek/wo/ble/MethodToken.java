package com.vivek.wo.ble;

public interface MethodToken {

    /**
     * 设置执行方法回调监听
     *
     * @param listener
     */
    MethodToken listen(OnActionListener listener);

    /**
     * 设置超时时间
     *
     * @param timeout 毫秒
     * @return
     */
    MethodToken timeout(long timeout);

    /**
     * 设置请求参数
     *
     * @param args
     * @return
     */
    MethodToken parameterArgs(Object... args);

    /**
     * 执行当前方法
     *
     * @return
     */
    MethodToken invoke();

    /**
     * 在队列中执行当前方法
     *
     * @return
     */
    MethodToken invokeInQueue();
}
