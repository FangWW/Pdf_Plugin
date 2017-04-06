package com.pdf_plugin.factory;


public class ThreadPoolExecutorProxyFactrory {
    static ThreadPoolExecutorProxy mNormalThreadPoolExecutorProxy;
    static ThreadPoolExecutorProxy mDownLoadThreadPoolExecutorProxy;

    /**
     * 得到普通的线程池代理
     *
     * @return
     */
    public static ThreadPoolExecutorProxy getThreadPoolExecutorProxy() {
        if (mNormalThreadPoolExecutorProxy == null) {
            synchronized (ThreadPoolExecutorProxyFactrory.class) {
                if (mNormalThreadPoolExecutorProxy == null) {
                    mNormalThreadPoolExecutorProxy = new ThreadPoolExecutorProxy(5, 5, 5000);
                }
            }
        }
        return mNormalThreadPoolExecutorProxy;
    }

    /**
     * 得到下载的线程池代理
     *
     * @return
     */
    public static ThreadPoolExecutorProxy getDownLoadThreadPoolExecutorProxy() {
        if (mDownLoadThreadPoolExecutorProxy == null) {
            synchronized (ThreadPoolExecutorProxyFactrory.class) {
                if (mDownLoadThreadPoolExecutorProxy == null) {
                    mDownLoadThreadPoolExecutorProxy = new ThreadPoolExecutorProxy(3, 3, 5000);
                }
            }
        }
        return mDownLoadThreadPoolExecutorProxy;
    }


}
