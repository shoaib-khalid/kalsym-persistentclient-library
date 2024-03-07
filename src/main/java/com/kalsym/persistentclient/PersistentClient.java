package com.kalsym.persistentclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import org.slf4j.*;

/**
 *
 * @author Zeeshan Ali
 */
public class PersistentClient {

    static Logger logger = LoggerFactory.getLogger("com.kalsym.persistentclient");

    /**
     *
     */
    public boolean[] connectionStatus;

    /**
     *
     */
    public DataInputStream[] incomingStream;

    /**
     *
     */
    public DataOutputStream[] outgoingStream;

    /**
     *
     */
    public Socket[] clientsock = null;

    /**
     *
     */
    public int currentUseConnIndex = 0;

    /**
     *
     */
    public int connectionCount = 1;

    /**
     *
     */
    public int maxRequestRetryCount = 1;

    /**
     *
     */
    public String serverIp = "127.0.0.1";

    /**
     *
     */
    public int serverPort = -1;
    private static ProcessPersistentResponse handler = null;
    private int connectTimeoutMs = 5000;
    private int reconnectRetryIntervalMs = -1;
    private int readTimeoutMs = -1;

    /**
     *
     * @param ip
     * @param port
     */
    public PersistentClient(String ip, int port) {
        serverIp = ip;
        serverPort = port;
        logger.info(String.format("PersistentClient IP: %s and port: %s", ip, port));
    }

    //<editor-fold>
//    /**
//     *
//     * @param connectTimeoutMs
//     * @param connCount
//     * @param retryInterval It is used to retry to connect socket after specific
//     * interval in mili seconds
//     * @param maxRequestRetryCount It is number of retries to be done while
//     * sending request message to server it must not be 2 times greater than
//     * connCount e.g. if connCount is 10, maxRequestRetryCount must be less than
//     * or equal to 20
//     * @param readTimeoutMs
//     *
//     */
//    public void connectAllSocket(int connectTimeoutMs, int connCount,
//            int retryInterval, int maxRequestRetryCount, int readTimeoutMs) {
//        connectionStatus = new boolean[connCount];
//        this.connectTimeoutMs = connectTimeoutMs;
//        incomingStream = new DataInputStream[connCount];
//        outgoingStream = new DataOutputStream[connCount];
//        clientsock = new Socket[connCount];
//        setParameters(connCount, maxRequestRetryCount);
//        handler = new ProcessPersistentResponse();
//
//        for (int i = 0; i < connCount; i++) {
//            boolean connected = false;
//
//            while (!connected) {
//                connected = connectSpecificSocket(i, serverIp, serverPort, connectTimeoutMs, readTimeoutMs);
//                if (!connected) {
//                    try {
//                        Thread.sleep(retryInterval);
//                    } catch (Exception ex) {
//                        //LogProperties.WriteLog("Exception while sleep before retry connecting socket [" + i + "] " + ex);
//                    }
//                }
//            }
//        }
//
//        receiveAllConnection(connCount, handler, serverIp, serverPort, connectTimeoutMs);
//    }
    //   </editor-fold>
    /**
     *
     * @param connectTimeoutMs Connects this socket to the server with a
     * specified timeout value. A timeout of zero is interpreted as an infinite
     * timeout. The connection will then block until established or an error
     * occurs.
     * @param connCount
     * @param reconnectRetryIntervalMs It is used to retry to connect socket
     * after specific interval in millisecond
     * @param maxRequestRetryCount It is number of retries to be done while
     * sending request message to server it must not be 2 times greater than
     * connCount e.g connCount is 10, maxRequestRetryCount must be lesser or
     * equals to
     * @param readTimeoutMs It is specified in milliseconds. With this option
     * set to a non-zero timeout, a read() call on the InputStream associated
     * with all Connection(s) will block for only this amount of time. If the
     * timeout expires, a java.net.SocketTimeoutException is raised, though the
     * Socket is still valid. The option must be enabled prior to entering the
     * blocking operation to have effect. The timeout must be > 0. A timeout of
     * zero is interpreted as an infinite timeout.
     * @param monitorAllConnection
     * @throws InterruptedException
     */
    public void connectAllSocket(int connectTimeoutMs, int connCount, int reconnectRetryIntervalMs, int maxRequestRetryCount, int readTimeoutMs, boolean monitorAllConnection) throws InterruptedException {
        connectionCount = connCount;
        connectionStatus = new boolean[connCount];
        incomingStream = new DataInputStream[connCount];
        outgoingStream = new DataOutputStream[connCount];
        clientsock = new Socket[connCount];
        setParameters(connCount, maxRequestRetryCount);
        handler = new ProcessPersistentResponse();
        this.reconnectRetryIntervalMs = reconnectRetryIntervalMs;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;

        for (int i = 0; i < connCount; i++) {
            boolean connected = false;

            while (!connected) {
                connected = connectSpecificSocket(i, serverIp, serverPort, connectTimeoutMs, readTimeoutMs);
                if (!connected) {
                    try {
                        Thread.sleep(reconnectRetryIntervalMs);
                    } catch (Exception ex) {
                        logger.error(String.format("Exception while sleep before retry connecting socket [%s] ", i), ex);
                    }
                }
            }
        }
        receiveAllConnection(connCount, handler, serverIp, serverPort, connectTimeoutMs, reconnectRetryIntervalMs);
        if (monitorAllConnection) {
            this.monitorAllConnection();
        }
    }

    /**
     *
     * @param i
     * @param IP
     * @param port
     * @param timeoutSoMs
     * @param readTimeoutMs
     * @return
     */
    protected boolean connectSpecificSocket(int i, String IP, int port, int timeoutSoMs, int readTimeoutMs) {

        SocketAddress sockaddr = new InetSocketAddress(IP, port);
        clientsock[i] = new Socket();

        try {
            clientsock[i].setSoLinger(true, 0);
            clientsock[i].setKeepAlive(true);
            clientsock[i].setSoTimeout(readTimeoutMs);
            clientsock[i].connect(sockaddr, timeoutSoMs);

            outgoingStream[i] = new DataOutputStream(clientsock[i].getOutputStream());
            incomingStream[i] = new DataInputStream(clientsock[i].getInputStream());

            connectionStatus[i] = true;
            logger.info(String.format("IP:PORT [%s:%s], Socket [%s] Connected!", IP, port, i));
        } catch (Exception ex) {
//            logger.error(String.format("Exception while connectiong socket [%s] ", i), ex);
            logger.error(String.format("Exception while connectiong, IP:PORT [%s:%s], Socket [%s] ", IP, port, i), ex);
            connectionStatus[i] = false;
        }
        return connectionStatus[i];
    }

    /**
     *
     * @param i
     */
    protected void closeSilentlySpecificSocket(int i) {
        if (clientsock[i] != null) {
            try {
                clientsock[i].close();
            } catch (IOException ex) {
                logger.error(String.format("Exception while closing silently socket [%s] ", i), ex);
            }
        }
    }

    /**
     * Monitors all sockets in connection
     *
     * @param connCount
     * @param reconnectRetryIntervalMs
     * @param ip
     * @param port
     * @param timeoutMs
     */
    private void monitorAllConnection() {
        for (int t = 0; t < this.connectionCount; t++) {
            MonitoringThread monitor = new MonitoringThread(this, t, this.reconnectRetryIntervalMs, this.serverIp, this.serverPort, this.connectTimeoutMs, this.readTimeoutMs);
            monitor.start();
        }
    }

    /**
     *
     * @param connCount
     * @param handler
     * @param IP
     * @param port
     * @param timeoutMs
     */
    private void receiveAllConnection(int connCount, MsgHandler handler,
            String IP, int port, int timeoutMs, int retryIntervalMs) {
        for (int t = 0; t < connCount; t++) {
            ReceiverThread receiver = new ReceiverThread(this, t, handler, IP, port, timeoutMs, retryIntervalMs);
            receiver.start();
        }
    }

    /**
     *
     * @param connCount
     * @param maxRetry
     */
    private void setParameters(int connCount, int maxRetry) {
        connectionCount = connCount;
        maxRequestRetryCount = maxRetry;
    }

    /**
     *
     * @param msg
     * @param client
     */
    protected void sendMessage(String msg, ClientWrapper client) {
        SenderThread sender = new SenderThread(this, msg, connectionCount, maxRequestRetryCount, client);
        Thread thread = new Thread(sender);
        thread.start();
    }

//    /**
//     *
//     * @param timeoutConnectMs
//     * @param connectionCount
//     * @param retryInterval
//     */
//    public void connectAllSocket(int timeoutConnectMs, int connectionCount, int retryInterval) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}
