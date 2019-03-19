package com.vivek.wo.ble;

public interface MethodProxy {

    /**
     * 执行方法
     *
     * @return
     */
    Object proxyInvoke(Object... args);

    /**
     * 执行方法回调
     *
     * @param args
     */
    void callback(int result, BluetoothException exception, Object... args);

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
}
