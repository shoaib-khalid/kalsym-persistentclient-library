package com.kalsym.persistentclient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zeeshan
 */
public class ClientWrapper {

    private final CountDownLatch responseLatch = new CountDownLatch(1);

    public static ConcurrentHashMap<String, ClientWrapper> responseMap = new ConcurrentHashMap<String, ClientWrapper>();

    private int timeout;
    private String refId = "";
    private String requestMessage = "";
    public String exceptionReason = "";

    RequestResult requestResponse = null;
    PersistentClient persistentClient = null;

    /**
     *
     * @param ref ReferenceId has to be unique
     * @param request
     * @param persisClient
     */
    public ClientWrapper(String ref, String request,
            PersistentClient persisClient) {
        refId = ref;
        requestMessage = request;
        persistentClient = persisClient;
    }

    /**
     * Sends Sync request to Persistent Server, Waits indefinitely for response
     *
     * @return
     */
    public RequestResult sendPersistentRequest() {
        return sendPersistentRequest(Long.MAX_VALUE);
    }

    public RequestResult sendPersistentRequest(long timeout) {
        if (timeout > 0L) {
            responseMap.put(refId, this);
        } else {
            //LogProperties.writeLog("Cannot add in responseMap, timeout: " + timeout);
        }
        persistentClient.sendMessage(refId + "|" + requestMessage, this);
        try {
            responseLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            //LogProperties.writeLog("Error in awaiting response: " + ex);
            setResponse(ErrorCode.NoResponse);
        }
        responseMap.remove(refId);
        return requestResponse;
    }

    /**
     * @param responseCode
     * @param responseString
     */
    protected void setResponse(ErrorCode responseCode, String responseString) {
        if ("".equals(responseString)) {
            requestResponse = new RequestResult(responseCode);
        } else {
            requestResponse = new RequestResult(responseCode, responseString);
        }
        try {
            ClientWrapper client = ClientWrapper.responseMap.get(refId);
            if (null != client) {
                ClientWrapper.responseMap.remove(refId);
            }
        } catch (Exception ex) {
            //LogProperties.writeLog(KalsymLogLevel.ERROR, "Exception in setResponse function");
            //throw new Exception(ex);
        }

        this.responseLatch.countDown();
    }

    /**
     * Used when there is no responseString, i.e. when some problem in sending
     *
     * @param responseCode
     */
    protected void setResponse(ErrorCode responseCode) {
        setResponse(responseCode, "");
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
