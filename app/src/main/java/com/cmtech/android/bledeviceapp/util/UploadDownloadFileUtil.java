package com.cmtech.android.bledeviceapp.util;

import static com.cmtech.android.bledeviceapp.global.AppConstant.KMIC_URL;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_DOWNLOAD_ERR;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_UPLOAD_ERR;

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

public class UploadDownloadFileUtil {
    private static final int TIME_OUT = 10000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static final String FILE_SERVLET_URL = "File?";

    // 上传文件
    public static void uploadFile(Context context, String fileType, File file, ICodeCallback cb) {
        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("上传信号");
        pBar.setMessage("正在上传信号，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        final int[] rtnCode = {RETURN_CODE_UPLOAD_ERR};
        Thread uploadThread = new Thread() {
            public void run() {
                String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
                String PREFIX = "--", LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data"; // 内容类型
                String RequestURL = KMIC_URL + FILE_SERVLET_URL;
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
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
                            + BOUNDARY);
                    if (file != null) {
                        OutputStream outputSteam = conn.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(outputSteam);
                        StringBuilder sb = new StringBuilder();
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINE_END);

                        String type = "\""+fileType+"\"";
                        sb.append("Content-Disposition: form-data; name=").append(type).append("; filename=\"").append(file.getName()).append("\"").append(LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset=" + CHARSET).append(LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());

                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        int process = 0;
                        int length = (int) file.length();
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                            process += len;
                            pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                                .getBytes();
                        dos.write(end_data);
                        dos.flush();
                        int res = conn.getResponseCode();
                        ViseLog.e(res);
                        if (res == 200) {
                            rtnCode[0] = RETURN_CODE_SUCCESS;
                            //ViseLog.e(dealResponseResult(conn.getInputStream()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ViseLog.e(e);
                } finally {
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.dismiss();
                            if(cb!=null) {
                                cb.onFinish(rtnCode[0]);
                            }
                        }
                    });
                }
            }
        };
        uploadThread.start();
    }

    // 下载文件
    public static void downloadFile(Context context, String fileType, String fileName, File toPath, ICodeCallback cb) {
        ProgressDialog pBar = new ProgressDialog(context);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("下载信号");
        pBar.setMessage("正在下载信号，请稍候...");
        pBar.setProgress(0);
        pBar.show();
        final int[] rtnCode = {RETURN_CODE_DOWNLOAD_ERR};
        Thread downloadThread = new Thread() {
            public void run() {
                Map<String, String> data = new HashMap<>();
                data.put("cmd", "download");
                data.put("fileType", fileType);
                data.put("fileName", fileName);
                String RequestURL = KMIC_URL + FILE_SERVLET_URL + HttpUtils.convertToString(data);
                ViseLog.e(RequestURL);
                File file = null;
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
                        InputStream is = con.getInputStream();
                        pBar.setMax(100); // 设置进度条的总长度
                        FileOutputStream fos = null;
                        if (is != null) {
                            //将文件下载到指定路径中
                            file = new File(toPath, fileName);
                            if(file.exists()) file.delete();
                            fos = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int ch;
                            int process = 0;
                            while ((ch = is.read(buf)) != -1) {
                                fos.write(buf, 0, ch);
                                process += ch;
                                pBar.setProgress((int)(process*100.0/length)); // 实时更新进度了
                            }
                            rtnCode[0] = RETURN_CODE_SUCCESS;
                        }
                        if (fos != null) {
                            fos.flush();
                            fos.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ThreadUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pBar.dismiss();
                            if(cb!=null) {
                                cb.onFinish(rtnCode[0]);
                            }
                        }
                    });
                }
            }
        };
        downloadThread.start();
    }

    // 判断远程文件是否存在
    public static boolean isFileExist(String fileType, String fileName) {
        boolean[] success = {false};
        Thread findThread = new Thread() {
            public void run() {
                Map<String, String> data = new HashMap<>();
                data.put("cmd", "find");
                data.put("fileType", fileType);
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

}
