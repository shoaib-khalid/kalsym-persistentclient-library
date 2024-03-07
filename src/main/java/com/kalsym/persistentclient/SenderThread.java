package com.kalsym.persistentclient;

import java.io.DataOutputStream;

/**
 *
 * @author taufik
 */
class SenderThread implements Runnable {

    DataOutputStream outgoingStream;
    String requestMsg;
    int connectionIndex;
    private final int connCount;
    private final int retryMaxCount;

    /**
     * This object is used to set error of the calling client, in the case of
     * any exception while connecting to server socket after the specified
     * number of retries
     */
    ClientWrapper client = null;
    private PersistentClient persistentClient = null;

    /**
     *
     * @param req
     * @param connCount
     * @param retryMaxCount
     * @param cli
     */
    public SenderThread(PersistentClient persisClient, String req, int connCount,
            int retryMaxCount, ClientWrapper cli) {
        this.requestMsg = req;
        persistentClient = persisClient;
        this.connectionIndex = persistentClient.currentUseConnIndex + 1;
        this.connCount = connCount;
        this.retryMaxCount = retryMaxCount;
        client = cli;
        if (this.connectionIndex >= this.connCount) {
            this.connectionIndex = 0;
        }
        persistentClient.currentUseConnIndex = this.connectionIndex;
    }

//    @Override
//    public void run() {
//        for (int x = 0; x < this.retryMaxCount; x++) {
//            try {
//                client.exceptionReason = "";
//                outgoingStream = persistentClient.outgoingStream[connectionIndex];
//                String strToSend = requestMsg + "\n";
//                byte[] outputBuf = strToSend.getBytes();
//                outgoingStream.write(outputBuf);
//                outgoingStream.flush();
//                //LogProperties.WriteLog("Sent msg using connection [" + connectionIndex + "]");
//                break;
//            } catch (Exception ex) {
//                //LogProperties.WriteLog("Could not send request to Server [" + connectionIndex + "] error: " + ex);
//                //LogProperties.WriteLog("Could not send request to Server [" + connectionIndex + "] error");
//                persistentClient.closeSilentlySpecificSocket(connectionIndex);
//                persistentClient.connectSpecificSocket(connectionIndex, persistentClient.serverIp, persistentClient.serverPort, persistentClient.connectTimeoutMs);
//                client.exceptionReason = ErrorCode.SocketError.getDescription();
//                //retry using other connection 
//                connectionIndex++;
//                if (connectionIndex >= this.connCount) {
//                    connectionIndex = 0;
//                }
//            }
//        }
//        if (!client.exceptionReason.equals("")) {
//            client.setResponse(ErrorCode.SocketError);
//        }
//        //LogProperties.WriteLog("Thread finish");
//    }
    @Override
    public void run() {
        for (int x = 0; x < this.retryMaxCount; x++) {
            //logger.debug("[" + refId + "] Attempt [" + x + "/" + this.retryMaxCount + "] [Connection:" + connectionIndex + "]");
            if (persistentClient.connectionStatus[connectionIndex] == false) {
                //logger.error("[" + refId + "] Persistent Connection Down! IP:" + ip + " Port:" + port + " ConnectionIndex:" + connectionIndex + "");
                client.exceptionReason = ErrorCode.SocketError.getDescription();
            } else {
                try {
                    client.exceptionReason = "";
                    outgoingStream = persistentClient.outgoingStream[connectionIndex];
                    String strToSend = requestMsg + "\n";
                    byte[] outputBuf = strToSend.getBytes();
                    outgoingStream.write(outputBuf);
                    outgoingStream.flush();
                    //logger.info("[" + refId + "] MO sent using Persistent Connection IP:" + ip + " Port:" + port + " ConnectionIndex:" + connectionIndex + " :" + requestMsg);
                    break;
                } catch (java.net.SocketException ex) {
                    //logger.error("[" + refId + "] SocketException. Could not send request to Server [" + connectionIndex + "] ", ex);
                    client.exceptionReason = ErrorCode.SocketError.getDescription();
                    //retry using other connection 
                    connectionIndex++;
                    if (connectionIndex >= this.connCount) {
                        connectionIndex = 0;
                    }
                } catch (Exception ex) {
                    //logger.error("[" + refId + "] Could not send request to Server [" + connectionIndex + "] error: ", ex);
                    client.exceptionReason = ErrorCode.SocketError.getDescription();
                    //retry using other connection 
                    connectionIndex++;
                    if (connectionIndex >= this.connCount) {
                        connectionIndex = 0;
                    }
                }
            }
        }
        //logger.debug("[" + refId + "] SenderThread finish");
        if (!client.exceptionReason.equals("")) {
            client.setResponse(ErrorCode.SocketError);
        }
    }

}
