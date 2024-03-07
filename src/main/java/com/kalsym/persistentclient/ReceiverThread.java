package com.kalsym.persistentclient;

import java.io.DataInputStream;

/**
 *
 * @author taufik
 */
class ReceiverThread extends Thread implements Runnable {

    DataInputStream incomingStream;
    int connectionIndex;
    private final MsgHandler handler;
    private final String IP;
    private final int port;
    private final int timeoutMs;
    private final int retryIntervalMs;
    private PersistentClient persistentClient = null;

    public ReceiverThread(PersistentClient persistClient, int connectionIndex,
            MsgHandler handler, String IP, int port, int timeoutMs, int retryIntervalMs) {
        this.connectionIndex = connectionIndex;
        this.handler = handler;
        this.IP = IP;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.retryIntervalMs = retryIntervalMs;
        persistentClient = persistClient;
    }

//    @Override
//    public void run() {
//        int token;
//        String replyMsg = "";
//        while (true) {
//            incomingStream = persistentClient.incomingStream[connectionIndex];
//            try {
//                while ((token = incomingStream.read()) != -1) {
//                    char ch = (char) token;
//                    if (ch == '\n') {
//                        //complete one msg
//                        processResponse(replyMsg);
//                        replyMsg = "";
//                    } else {
//                        replyMsg = replyMsg + (char) token;
//                    }
//                }
//            } catch (Exception ex) {
//                //LogProperties.WriteLog("Exception in receiver thread [" + connectionIndex + "] " + ex);
//                persistentClient.closeSilentlySpecificSocket(connectionIndex);
//                persistentClient.connectSpecificSocket(connectionIndex, this.IP, this.port, this.timeoutMs,this.retryIntervalMs);
//            }
//        }
//    }
    @Override
    public void run() {
        int token;
        String replyMsg = "";
        while (true) {
            if (persistentClient.connectionStatus[connectionIndex] == false) {
                //logger.error("ReceiverThread stop reading. Persistent Connection is down! IP:"+IP+" Port:"+port+" ConnectionIndex:" + connectionIndex + "");
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (Exception ex2) {
                    //logger.error("[IP:"+IP+" Port:"+port+"] Could not sleep in ReceiverThread [" + connectionIndex + "] error: ", ex2);
                }
            } else {
                try {
                    incomingStream = persistentClient.incomingStream[connectionIndex];
                    while ((token = incomingStream.read()) != -1) {
                        char ch = (char) token;
                        if (ch == '\n') {
                            //complete one msg
                            processResponse(replyMsg);
                            replyMsg = "";
                        } else {
                            replyMsg = replyMsg + (char) token;
                        }
                    }
                    replyMsg = "";
                } catch (java.net.SocketException ex) {
                    //logger.error("[IP:"+IP+" Port:"+port+"] SocketException in receiver thread [" + connectionIndex + "] ", ex);
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (Exception ex2) {
                        //logger.error("[IP:"+IP+" Port:"+port+"] Could not sleep in ReceiverThread [" + connectionIndex + "] error: ", ex2);
                    }
                    //break;
                } catch (Exception ex) {
                    //logger.error("[IP:"+IP+" Port:"+port+"] Exception in receiver thread [" + connectionIndex + "] ", ex);
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (Exception ex2) {
                        //logger.error("[IP:"+IP+" Port:"+port+"] Could not sleep in ReceiverThread [" + connectionIndex + "] error: ", ex2);
                    }
                    //break;
                }
            }
        }
    }

    private void processResponse(String resp) {
        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / mb;
        long maxMemory = runtime.maxMemory() / mb;
        //LogProperties.WriteLog("Received something! Memory Usage:[" + memoryUsed + "/" + maxMemory + "]");
        handler.handleMessage(resp);
    }
}
