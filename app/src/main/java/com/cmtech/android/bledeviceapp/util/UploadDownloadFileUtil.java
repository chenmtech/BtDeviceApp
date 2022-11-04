package com.cmtech.android.bledeviceapp.util;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMIC_URL;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_DATA_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.app.ProgressDialog;
import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.vise.log.ViseLog;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件远程操作服务类
 */
public class UploadDownloadFileUtil {
    private static final int TIME_OUT = 10000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static final String FILE_SERVLET_URL = "File?";

    /**
     * 上传信号文件
     * @param context
     * @param type：文件类型字符串
     * @param file: 信号文件
     * @param cb：结束后的回调
     */
    public static void uploadFile(Context context, String type, File file, ICodeCallback cb) {
        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("上传");
        pBar.setMessage("正在上传，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        final int[] rtnCode = {RCODE_DATA_ERR};
        final String[] msg = {"上传错误"};
        Thread uploadThread = new Thread() {
            public void run() {
                String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; // 内容类型
                String RequestURL = KMIC_URL + FILE_SERVLET_URL;
                InputStream fis = null;
                DataOutputStream dos = null;
                try {
                    URL url = new URL(RequestURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true); // 允许输入流
                    conn.setDoOutput(true); // 允许输出流
                    conn.setUseCaches(false); // 不允许使用缓存
                    conn.setRequestMethod("POST"); // 请求方式
                    conn.setRequestProperty("Charset", CHARSET); // 设置编码
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                    if (file != null) {
                        OutputStream outputSteam = conn.getOutputStream();
                        dos = new DataOutputStream(outputSteam);
                        StringBuilder sb = new StringBuilder();
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);

                        /*sb.append("Content-Disposition: form-data; name=\"type\" ").append(type).append(LINE_END);
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());

                        sb = new StringBuilder();*/
                        String typeStr = "\""+type+"\"";
                        sb.append("Content-Disposition: form-data; name=").append(typeStr).append("; filename=\"").append(file.getName()).append("\"").append(LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset=" + CHARSET).append(LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());

                        fis = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        int process = 0;
                        int length = (int) file.length();
                        while ((len = fis.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                            process += len;
                            pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                        }
                        fis.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                                .getBytes();
                        dos.write(end_data);
                        dos.flush();
                        dos.close();
                        int res = conn.getResponseCode();
                        ViseLog.e(res);
                        if (res == 200) {
                            rtnCode[0] = RCODE_SUCCESS;
                            msg[0] = "上传成功";
                            //ViseLog.e(dealResponseResult(conn.getInputStream()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ViseLog.e(e);
                } finally {
                    if(fis!=null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(dos!= null) {
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.dismiss();
                            if(cb!=null) {
                                cb.onFinish(rtnCode[0], msg[0]);
                            }
                        }
                    });
                }
            }
        };
        uploadThread.start();
    }

    /**
     * 下载信号文件
     * @param context
     * @param type：文件类型字符串
     * @param fileName: 文件名字符串
     * @param toPath：下载后的文件保存路径
     * @param cb：结束后的回调
     */
    public static void downloadFile(Context context, String type, String fileName, File toPath, ICodeCallback cb) {
        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("下载");
        pBar.setMessage("正在下载，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        final int[] rtnCode = {RCODE_DATA_ERR};
        final String[] msg = {"下载失败"};
        Thread downloadThread = new Thread() {
            public void run() {
                Map<String, String> data = new HashMap<>();
                data.put("cmd", "download");
                data.put("type", type);
                data.put("fileName", fileName);
                String RequestURL = KMIC_URL + FILE_SERVLET_URL + HttpUtils.convertToString(data);
                ViseLog.e(RequestURL);
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    URL url = new URL(RequestURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(TIME_OUT);
                    con.setConnectTimeout(TIME_OUT);
                    con.setRequestProperty("Charset", CHARSET);
                    con.setRequestMethod("GET");
                    int res = con.getResponseCode();
                    ViseLog.e(res);
                    if (res == 200) {
                        int length = con.getContentLength();// 获取文件大小
                        is = con.getInputStream();
                        pBar.setMax(100); // 设置进度条的总长度
                        if (is != null) {
                            //将文件下载到指定路径中
                            File file = new File(toPath, fileName);
                            if(file.exists()) file.delete();
                            fos = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int len;
                            int process = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                process += len;
                                pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                            }
                            is.close();
                            rtnCode[0] = RCODE_SUCCESS;
                            msg[0] = "下载成功";
                        }
                        if (fos != null) {
                            fos.flush();
                            fos.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(is!= null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fos!=null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.dismiss();
                            if(cb!=null) {
                                cb.onFinish(rtnCode[0], msg[0]);
                            }
                        }
                    });
                }
            }
        };
        downloadThread.start();
    }

    /**
     * 判断远程文件是否存在
     * @param type：文件类型字符串
     * @param fileName：文件名
     * @return：文件是否存在
     */
    public static boolean isFileExist(String type, String fileName) {
        boolean[] success = {false};
        Thread findThread = new Thread() {
            public void run() {
                Map<String, String> data = new HashMap<>();
                data.put("cmd", "find");
                data.put("type", type);
                data.put("fileName", fileName);
                String RequestURL = KMIC_URL + FILE_SERVLET_URL + HttpUtils.convertToString(data);
                ViseLog.e(RequestURL);
                try {
                    URL url = new URL(RequestURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(TIME_OUT);
                    con.setConnectTimeout(TIME_OUT);
                    con.setRequestProperty("Charset", CHARSET);
                    con.setRequestMethod("GET");
                    int res = con.getResponseCode();
                    if (res == 200) {
                        success[0] = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        findThread.start();
        try {
            findThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return success[0];
    }

    private static String dealResponseResult(InputStream inputStream) {
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = byteArrayOutputStream.toString();
        return resultData;
    }

    /**
     * 下载信号文件
     * @param context
     * @param fileUrl: 文件URL
     * @param toFile：下载后的文件保存完整路径名，包括文件名
     * @param cb：结束后的回调
     */
    public static void downloadFile(Context context, String fileUrl, File toFile, ICodeCallback cb) {
        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("下载");
        pBar.setMessage("正在下载，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        final int[] rtnCode = {RCODE_DATA_ERR};
        final String[] msg = {"下载失败"};
        Thread downloadThread = new Thread() {
            public void run() {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    URL url = new URL(fileUrl);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(TIME_OUT);
                    con.setConnectTimeout(TIME_OUT);
                    con.setRequestProperty("Charset", CHARSET);
                    con.setRequestMethod("GET");
                    int res = con.getResponseCode();
                    ViseLog.e(res);
                    if (res == 200) {
                        int length = con.getContentLength();// 获取文件大小
                        is = con.getInputStream();
                        pBar.setMax(100); // 设置进度条的总长度
                        if (is != null) {
                            //将文件下载到指定路径中
                            if(toFile.exists()) toFile.delete();
                            fos = new FileOutputStream(toFile);
                            byte[] buf = new byte[1024];
                            int len;
                            int process = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                process += len;
                                pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                            }
                            is.close();
                            rtnCode[0] = RCODE_SUCCESS;
                            msg[0] = "下载成功";
                        }
                        if (fos != null) {
                            fos.flush();
                            fos.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(is!= null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fos!=null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.dismiss();
                            if(cb!=null) {
                                cb.onFinish(rtnCode[0], msg[0]);
                            }
                        }
                    });
                }
            }
        };
        downloadThread.start();
    }

}
