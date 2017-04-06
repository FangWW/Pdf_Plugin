package com.pdf_plugin;

import android.os.Handler;
import android.os.Message;

import com.pdf_plugin.factory.ThreadPoolExecutorProxyFactrory;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by admin on 2016/5/4.
 */
public class DownLoadManager {

    private static final int STATE_SUCCESS = 1;
    private static final int STATE_DOWNLOAD = 2;
    private static final int STATE_ERROR = 3;

    static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            Data data = (Data) msg.obj;
            CallBack callBack = data.callBack;
            switch (what) {
                case STATE_SUCCESS:
                    callBack.onResponse(data.response);
                    break;
                case STATE_DOWNLOAD:
                    callBack.onDownLoad(data.progress, data.max);
                    break;
                case STATE_ERROR:
                    callBack.onError(data.exception);
                    File file = new File(data.response);
                    if (file.exists()) {
                        file.delete();
                    }
                    break;

            }
        }
    };

    public static abstract class CallBack {
        public abstract void onResponse(String response);

        public abstract void onError(Exception e);

        public void onDownLoad(int progress, int max) {

        }
    }

    /**
     * 异步下载文件
     *
     * @param urlPath  下载地址
     * @param filePath 下载文件所在的绝对路径
     * @param callBack 回调
     */
    public static void downloadFileAsny(final String urlPath, final String filePath, final CallBack callBack) {
        ThreadPoolExecutorProxyFactrory.getThreadPoolExecutorProxy().execute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                InputStream is = null;
                FileOutputStream fos = null;
                BufferedInputStream bis = null;
                Data data = new Data();
                data.callBack = callBack;
                data.response = filePath;
                try {
                    url = new URL(urlPath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.setConnectTimeout(5000);
                    //获取到文件的大小
                    is = conn.getInputStream();
                    int contentLength = conn.getContentLength();
                    data.max = contentLength;
                    long l = System.currentTimeMillis();

                    File file = new File(filePath + "update" + l);
                    fos = new FileOutputStream(file);
                    bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    int total = 0;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        total += len;
                        data.progress = total;
                        //获取当前下载量
                        Message msg = Message.obtain();
                        msg.what = STATE_DOWNLOAD;
                        msg.obj = data;
                        mHandler.sendMessage(msg);
                    }
                    copy(file.getAbsolutePath(), filePath, true);
                    Message msg = Message.obtain();
                    msg.what = STATE_SUCCESS;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg = Message.obtain();
                    msg.what = STATE_ERROR;
                    data.exception = e;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (bis != null) {
                            bis.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    static class Data {
        public int progress;
        public int max;
        public CallBack callBack;
        public String response;
        public Exception exception;
    }

    /**
     * 改名
     */
    public static boolean copy(String src, String des, boolean delete) {
        File file = new File(src);
        if (!file.exists()) {
            return false;
        }
        File desFile = new File(des);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            int count = -1;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                out.flush();
            }
        } catch (Exception e) {
            return false;
        } finally {
            close(in);
            close(out);
        }
        if (delete) {
            file.delete();
        }
        return true;
    }

    /**
     * 关闭流
     */
    public static boolean close(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
            }
        }
        return true;
    }
}
