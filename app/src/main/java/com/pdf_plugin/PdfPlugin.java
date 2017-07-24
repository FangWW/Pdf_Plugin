package com.pdf_plugin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.morgoo.ApkItem;
import com.morgoo.droidplugin.pm.PluginManager;

import java.io.File;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

/**
 * pdf插件处理
 *
 * @Author FangJW
 * @Date 3/31/17
 */
public class PdfPlugin {
    /**
     * 插件下载路径   测试插件在项目根目录  上传到服务器
     */
//    String url = "https://pro-app-qn.fir.im/2060312a0b27ff01b2215380b32c5e1fbc638e15.apk?attname=sample-debug.apk_2.0.1.apk&e=1500887849&token=LOvmia8oXF4xnLh0IdH05XMYpH6ENHNpARlmPc-T:LuBQXiMIBzHldyWehRW6XrZOLVg=";
    String url = "https://raw.githubusercontent.com/FangWW/Pdf_Plugin/master/pdf_plug.apk";
    //        String url = "https://raw.githubusercontent.com/FangWW/Pdf_Plugin/master/pdf_plug_2.0.1.apk";
    private Activity mContext;
    private static PdfPlugin mPdfPlug;
    private ProgressDialog mProgressDialog;
    private String mPath = "";
    private View decorView;

    public PdfPlugin(Activity context) {
        setContext(context);
        //下载框
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("正在下载pdf插件");
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 单例
     *
     * @param context
     * @param path
     * @return
     */
    public static PdfPlugin getInstant(Activity context, String path) {
        if (mPdfPlug == null) {
            mPdfPlug = new PdfPlugin(context);
        } else {
            mPdfPlug.setContext(context);
        }
        mPdfPlug.setPath(path);
        return mPdfPlug;
    }

    /**
     * 下载检查插件
     */
    public void downLoadPlugin() {
        try {
            String fileDir = mContext.getCacheDir().getPath();
            String fileName = getFileName(url);
            File file = new File(fileDir, fileName);
            if (file.exists()) {
                loadApk(file);
                return;
            }
            mProgressDialog.show();
            DownLoadManager.downloadFileAsny(url, file.getAbsolutePath(), new DownLoadManager.CallBack() {

                @Override
                public void onResponse(String response) {
                    File apk = new File(response);
                    if (apk.exists()) {
                        loadApk(apk);
                        return;
                    }
                }

                @Override
                public void onError(Exception e) {
                    mProgressDialog.dismiss();
                    openDefPdf();
                }

                @Override
                public void onDownLoad(int progress, int max) {
                    super.onDownLoad(progress, max);
                    mProgressDialog.setMax(max);
                    mProgressDialog.setProgress(progress);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            openDefPdf();
        }
    }

    /**
     * 寻求手机上安装的其他pdf工具
     */
    private void openDefPdf() {
        decorView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(mContext, "调用第三方打开中", Toast.LENGTH_SHORT).show();
                    openPdfFile(mContext, mPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "您的机器不支持pdf文件", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 启动插件打开
     *
     * @param apk
     */
    private void loadApk(File apk) {
        try {
            PackageManager pm = mContext.getPackageManager();
            final PackageInfo info = pm.getPackageArchiveInfo(apk.getPath(), 0);
            if (info != null) {
                mProgressDialog.setMessage("正在检查pdf插件中...");
                final ApkItem item = new ApkItem(mContext, info, apk.getPath());
                if (item.installing) {
                    mProgressDialog.dismiss();
                    return;
                }
                if (!PluginManager.getInstance().isConnected()) {
                    Toast.makeText(mContext, "插件服务正在初始化，请稍后再试。。。", Toast.LENGTH_SHORT).show();
                    openDefPdf();
                }
                final PackageInfo packageInfo = PluginManager.getInstance().getPackageInfo(item.packageInfo.packageName, 0);
                if (packageInfo != null && String.valueOf(packageInfo.versionName).equals(item.versionName)) {
                    mProgressDialog.dismiss();
                    openPdf(item);
//                            Toast.makeText(mContext, "已经安装了，不能再安装", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressDialog.setMessage("正在安装pdf插件中...");
                    new Thread() {
                        @Override
                        public void run() {
                            if (packageInfo != null) {//卸载更新
                                if (!PluginManager.getInstance().isConnected()) {
                                    Toast.makeText(mContext, "服务未连接", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    try {
                                        PluginManager.getInstance().deletePackage(item.packageInfo.packageName, 0);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        openDefPdf();
                                    }
                                }
                            }
                            doInstall(item);
                        }
                    }.start();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            openDefPdf();
//                try {//失败后再次尝试安装插件
//                    PluginManager.getInstance().installPackage(item.apkfile, 0);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
        }
    }

    /**
     * 安装插件
     *
     * @param item
     */
    private synchronized void doInstall(final ApkItem item) {
        try {
            item.installing = true;
            final int re = PluginManager.getInstance().installPackage(item.apkfile, 0);
            item.installing = false;
            decorView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mProgressDialog.dismiss();
                        switch (re) {
                            case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                                Toast.makeText(mContext, "安装失败，文件请求的权限太多", Toast.LENGTH_SHORT).show();
                                openDefPdf();
                                break;
                            case INSTALL_FAILED_NOT_SUPPORT_ABI:
                                Toast.makeText(mContext, "宿主不支持插件的abi环境，可能宿主运行时为64位，但插件只支持32位", Toast.LENGTH_SHORT).show();
                                openDefPdf();
                                break;
                            case INSTALL_SUCCEEDED:
                                Toast.makeText(mContext, "安装完成", Toast.LENGTH_SHORT).show();
                                openPdf(item);
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        openDefPdf();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            openDefPdf();
        }
    }

    private void openPdf(ApkItem item) {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(item.packageInfo.packageName);
        intent.putExtra("key", mPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) mContext).startActivity(intent);
    }

    /**
     * 去除url路径最后的文件名
     *
     * @param path url路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


    public void setPath(String path) {
        mPath = path;
    }


    public void setContext(Activity context) {
        try {
            mContext = context;
            decorView = ((Activity) mContext).getWindow().getDecorView();
        } catch (Exception e) {
            e.printStackTrace();
            openDefPdf();
        }
    }

    /**
     * android获取一个用于打开PDF文件的intent
     *
     * @param path 要打开的文件的绝对路径
     * @return
     */
    public Intent getPdfFileIntent(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);//Intent.ACTION_VIEW = "android.intent.action.VIEW"
        intent.addCategory(Intent.CATEGORY_DEFAULT);//Intent.CATEGORY_DEFAULT = "android.intent.category.DEFAULT"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri contentUri = FileProvider.getUriForFile(mContext, "cn.kooki.app.chezhen.fileProvider", new File(path));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/pdf");

        } else {
            Uri uri = Uri.fromFile(new File(path));
            intent.setDataAndType(uri, "application/pdf");
        }
        return intent;
    }

    /**
     * 打开pdf文件
     *
     * @param path
     */
    public void openPdfFile(Activity activity, String path) {
        Intent intent = getPdfFileIntent(path);
        activity.startActivity(intent);
    }
}
