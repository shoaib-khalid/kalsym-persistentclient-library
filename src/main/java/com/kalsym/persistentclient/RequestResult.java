package com.kalsym.persistentclient;

/**
 *
 * @author zeeshan
 */
public class RequestResult {

    private final ErrorCode errorCode;
    private String response = "";

    public RequestResult(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public RequestResult(ErrorCode errorCode, String response) {
        this.errorCode = errorCode;
        this.response = response;
    }

    /**
     * @return the errorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

}
