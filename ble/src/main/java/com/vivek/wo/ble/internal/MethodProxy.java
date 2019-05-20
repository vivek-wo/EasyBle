package com.vivek.wo.ble.internal;

public interface MethodProxy {

    /**
     * 设置执行方法回调监听
     *
     * @param listener
     */
    MethodProxy listen(OnActionListener listener);

    /**
     * 设置超时时间
     *
     * @param timeout 毫秒
     * @return
     */
    MethodProxy timeout(long timeout);

    /**
     * 设置请求参数
     *
     * @param args
     * @return
     */
    MethodProxy parameterArgs(Object... args);

    /**
     * 执行当前方法
     *
     * @return
     */
    MethodProxy invoke();

    /**
     * 在队列中执行当前方法
     *
     * @return
     */
    MethodProxy invokeInQueue();
}
