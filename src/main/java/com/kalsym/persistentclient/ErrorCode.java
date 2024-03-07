package com.kalsym.persistentclient;

/**
 * All the Error Codes returned by this module
 *
 * Format: NAME(CODE,"Description"),
 *
 * @author Ali Khan
 */
public enum ErrorCode {

    // Custom Error Codes
    SocketError(500, "socket connect error"),
    Success(501, "Success"),
    NoResponse(502, "No Response, Timeout or ThreadInterupted");

    private final int code;
    private final String description;

    ErrorCode(int numVal, String desc) {
        this.code = numVal;
        this.description = desc;
    }

    /**
     * Gets the numeric value of the error code
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the detailed description of the error code
     *
     * @return
     */
    public String getDescription() {
        return description;
    }
}
