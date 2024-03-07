package com.kalsym.persistentclient;

import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author taufik
 *
 */
class MonitoringThread extends Thread implements Runnable {

    static Logger logger = LoggerFactory.getLogger("com.kalsym.persistentclient");

    DataOutputStream outgoingStream;
    String requestMsg;
    public boolean connected = true;
    int connectionIndex;
    private final int interval;
    private final String IP;
    private final int port;
    private final int timeoutMs;
    private final int waitTimeoutMs;
    private PersistentClient persistentClient = null;

    /**
     *
     * @param persistClient
     * @param connectionIndex
     * @param interval
     * @param IP
     * @param port
     * @param timeoutMs TODO: remove persistentClient object in this constructor
     */
    public MonitoringThread(PersistentClient persistClient, int connectionIndex, int interval, String IP, int port, int timeoutMs, int waitTimeoutMs) {
        this.connectionIndex = connectionIndex;
        this.interval = interval;
        this.IP = IP;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.waitTimeoutMs = waitTimeoutMs;
        persistentClient = persistClient;
    }

    @Override
    public void run() {
        while (true) {
            outgoingStream = persistentClient.outgoingStream[connectionIndex];
            try {
                String strToSend = "Test_Link_Msg" + "\n";
                byte[] outputBuf = strToSend.getBytes();
                outgoingStream.write(outputBuf);
                outgoingStream.flush();
                logger.debug(String.format("Sent TEST msg in [%s]", connectionIndex));
            } catch (Exception ex) {
                logger.error(String.format("Could not send request to monitor thread [%s] error: ", connectionIndex), ex);
                connected = false;
                persistentClient.closeSilentlySpecificSocket(connectionIndex);
                persistentClient.connectSpecificSocket(connectionIndex, this.IP, this.port, this.timeoutMs, this.waitTimeoutMs);
            }
            try {
                Thread.sleep(interval);
            } catch (Exception ex) {
                logger.error(String.format("Could not sleep in monitor thread [%s] error ", connectionIndex), ex);
            }
        }
    }
}
