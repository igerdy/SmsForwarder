package com.idormy.sms.forwarder.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.idormy.sms.forwarder.SenderActivity.NOTIFY;

public class SenderBarkMsg {

    static String TAG = "SenderBarkMsg";

    public static void sendMsg(final Handler handError, String barkServer, String from, String content, String phoneNumber) throws Exception {
        Log.i(TAG, "sendMsg barkServer:" + barkServer + " from:" + from + " content:" + content + " phoneNumber:" + phoneNumber);

        if (barkServer == null || barkServer.isEmpty()) {
            return;
        }

        content += "\n[" + phoneNumber + "]";
        barkServer += URLEncoder.encode(from, "UTF-8");
        barkServer += "/" + URLEncoder.encode(content, "UTF-8");
        barkServer += "?isArchive=1"; //自动保存
        int isCode = content.indexOf("验证码");
        int isPassword = content.indexOf("动态密码");
        if (isCode != -1 || isPassword != -1) {
            Pattern p = Pattern.compile("(\\d{4,6})");
            Matcher m = p.matcher(content);
            if (m.find()) {
                System.out.println(m.group());
                barkServer += "&automaticallyCopy=1&copy=" + m.group();
            }
        }

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(barkServer).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.d(TAG, "onFailure：" + e.getMessage());

                if (handError != null) {
                    Message msg = new Message();
                    msg.what = NOTIFY;
                    Bundle bundle = new Bundle();
                    bundle.putString("DATA", "发送失败：" + e.getMessage());
                    msg.setData(bundle);
                    handError.sendMessage(msg);
                }

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                Log.d(TAG, "Code：" + response.code() + responseStr);

                if (handError != null) {
                    Message msg = new Message();
                    msg.what = NOTIFY;
                    Bundle bundle = new Bundle();
                    bundle.putString("DATA", "发送状态：" + responseStr);
                    msg.setData(bundle);
                    handError.sendMessage(msg);
                    Log.d(TAG, "Response：" + response.code() + responseStr);
                }

            }
        });
    }

}
