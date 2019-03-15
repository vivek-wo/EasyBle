package com.vivek.wo.ble;

public interface FunctionProxy {

    /**
     * 执行方法
     *
     * @return
     */
    Object invoke();

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
    void listen(OnActionListener listener);

}
