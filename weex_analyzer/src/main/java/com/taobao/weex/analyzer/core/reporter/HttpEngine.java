package com.taobao.weex.analyzer.core.reporter;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.taobao.weex.utils.WXLogUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */

final class HttpEngine {

    private static final String TAG = "HttpEngine";

    public static class Response {
        int code;
        String message;

        @Override
        public String toString() {
            return "Response{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    public static class Request {
        String method;
        String url;
        Map<String, String> headers;
        String jsonData;
        Object rawData;
    }

    public interface OnHttpCompletedListener {
        void onHttpComplete(Response response);
    }

    public static void asyncRequest(@NonNull final Request request, @NonNull final OnHttpCompletedListener listener) {
        new AsyncTask<Request,Void,Response>(){

            @Override
            protected Response doInBackground(Request... params) {
                if(request.rawData != null && request.jsonData == null) {
                    try {
                        request.jsonData = JSON.toJSONString(request.rawData);
                    }catch (Exception e) {
                        request.jsonData = "{}";
                    }
                }
                WXLogUtils.d("HttpDataReporter",request.jsonData);
                return syncRequest(params[0]);
            }

            @Override
            protected void onPostExecute(Response response) {
                listener.onHttpComplete(response);
            }
        }.execute(request);
    }


    @NonNull
    public static Response syncRequest(@NonNull Request request) {
        Response response = new Response();

        HttpURLConnection connection = null;
        String url = request.url;
        if (TextUtils.isEmpty(url)) {
            response.code = -1;
            response.message = "bad http request";
            return response;
        }

        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            String method = request.method;
            if (TextUtils.isEmpty(method)) {
                method = "GET";
            }
            connection.setRequestMethod(method);

            if (request.headers != null) {
                for (Map.Entry<String, String> header : request.headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            connection.setRequestProperty(Const.HDR_USER_AGENT,Const.DEFAULT_USER_AGENT);
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);

            if (request.jsonData != null) {
                connection.setRequestProperty(Const.HDR_CONTENT_TYPE, Const.APP_JSON);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                byte[] postData = request.jsonData.getBytes();

                // only compress if the new body is smaller than uncompressed body
                if (postData.length > Const.MIN_COMPRESSED_ADVANTAGE) {
                    byte[] compressedBody = gzip(postData);
                    if (compressedBody != null &&
                            postData.length - compressedBody.length > Const.MIN_COMPRESSED_ADVANTAGE) {
                        postData = compressedBody;
                        connection.setRequestProperty(Const.HDR_CONTENT_ENCODING, "gzip");
                    }
                }

                connection.setFixedLengthStreamingMode(postData.length);
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.write(postData);
                dos.flush();
            } else {
                connection.connect();
            }

            int responseCode = connection.getResponseCode();
            response.code = responseCode;

            String responseMsg = null;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                if (inputStream != null) {
                    responseMsg = readStream(inputStream);
                }
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    responseMsg = readStream(errorStream);
                }
            }
            response.message = responseMsg;

            return response;
        } catch (Exception e) {
            WXLogUtils.e(TAG, e.getMessage());
            response.code = -1;
            response.message = e.getMessage();
            return response;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(@NonNull InputStream stream) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            baos.write(buffer,0,count);
        }
        return new String(baos.toByteArray(),Const.UTF8);
    }

    private static byte[] gzip(byte[] input) {
        GZIPOutputStream gzipOS = null;
        try {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            gzipOS = new GZIPOutputStream(byteArrayOS);
            gzipOS.write(input);
            gzipOS.flush();
            gzipOS.close();
            gzipOS = null;
            return byteArrayOS.toByteArray();
        } catch (Exception e) {
            return null;
        } finally {
            if (gzipOS != null) {
                try {
                    gzipOS.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    private static class Const {
        static final String DEFAULT_USER_AGENT = "com.taobao.weex.analyzer/1.0";
        static final String APP_FORM = "application/x-www-form-urlencoded";
        static final String APP_JSON = "application/json";
        static final String APP_BINARY = "application/octet-stream";
        static final String TEXT_PLAIN = "text/plain";
        static final String HDR_CONTENT_TYPE = "Content-Type";
        static final String HDR_CONTENT_ENCODING = "Content-Encoding";
        static final String HDR_ACCEPT_ENCODING = "Accept-Encoding";
        static final String HDR_ACCEPT = "Accept";
        static final String HDR_USER_AGENT = "User-Agent";
        static final String UTF8 = "utf-8";

        static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
        static final Class BYTE_ARRAY_CLASS = EMPTY_BYTE_ARRAY.getClass();
        /**
         * Minimal number of bytes the compressed content must be smaller than uncompressed
         */
        static final int MIN_COMPRESSED_ADVANTAGE = 80;
    }

}
