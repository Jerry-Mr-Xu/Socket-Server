package com.jerry.server_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * 聊天室服务端
 * <p>
 * Created by xujierui on 2018/3/16.
 */

public class ChattingRoomService extends Service {
    private static final String TAG = ChattingRoomService.class.getSimpleName();

    private boolean isServiceDestroyed = false;
    private String[] preDefinedMessage = {
            "你好啊，哈哈哈",
            "请问你叫什么名字啊",
            "今天天气不错啊"
    };

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        // 开一个线程用以监听端口
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            while (!isServiceDestroyed) {
                try {
                    // 监听8688端口的客户端连接（会阻塞线程）
                    final Socket clientSocket = serverSocket.accept();
                    Log.i(TAG, "a client connected");

                    // 开启一个线程用以监听客户端发送的消息
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(clientSocket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        // 用于接收客户端消息
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        // 用于向客户端发送消息
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        writer.println("欢迎来到聊天室");

        while (!isServiceDestroyed) {
            // 获取客户端发送的消息（会阻塞线程）
            String clientMessage = reader.readLine();
            Log.i(TAG, "message from client: " + clientMessage);
            // 如果客户端发送了null则该客户端已经断开连接
            if (clientMessage == null) {
                break;
            }

            int i = new Random().nextInt(preDefinedMessage.length);
            String toClientMessage = preDefinedMessage[i];
            writer.println(toClientMessage);
            Log.i(TAG, "send to client: " + toClientMessage);
        }

        Log.i(TAG, "client socket closed");
        writer.close();
        reader.close();
        client.close();
    }
}
