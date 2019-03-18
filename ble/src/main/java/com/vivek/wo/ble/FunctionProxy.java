package com.vivek.wo.ble;

public interface FunctionProxy {

    /**
     * 执行方法
     *
     * @return
     */
    Object invoke(Object... args);

    /**
     * 执行方法回调
     *
     * @param args
     */
    void callback(Object... args);

    /**
     * 设置执行方法回调监听
     *
     * @param listener
     */
    FunctionProxy listen(OnActionListener listener);

    /**
     * 设置超时时间
     *
     * @param timeout
     * @return
     */
    FunctionProxy timeout(int timeout);

}
