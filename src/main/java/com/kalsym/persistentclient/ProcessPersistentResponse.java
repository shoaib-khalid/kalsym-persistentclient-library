package com.kalsym.persistentclient;

/**
 *
 * @author zeeshan
 */
class ProcessPersistentResponse implements MsgHandler {

    @Override
    public void handleMessage(String response) {
        //LogProperties.WriteLog("Persistent response from server:" + response);
        String refId = extractRefId(response);

        //LogProperties.WriteLog("Getting request from hashMap against ID: " + refId);
        ClientWrapper pClient = ClientWrapper.responseMap.get(refId);
        if (pClient == null) {
            //LogProperties.WriteLog("Couldn't map response: " + response);
        } else {
            response = extractResponseMsg(response);
            pClient.setResponse(ErrorCode.Success, response);
            //LogProperties.WriteLog("Response from persistent server:" + response);
        }
    }

    /**
     *
     * @param response
     * @return
     */
    private String extractRefId(String response) {
        try {
            return response.substring(0, response.indexOf("|"));
        } catch (Exception exp) {
            //LogProperties.WriteLog("Couldn't find refId in : " + response);
        }
        return "";
    }

    /**
     * Extracts message from requestMsg, request Msg must be in format:
     * refId|Msg
     *
     * @param requestMsg
     * @return
     */
    private String extractResponseMsg(String responseMsg) {
        try {
            return responseMsg.substring(responseMsg.indexOf("|") + 1);
        } catch (Exception exp) {
            //LogProperties.WriteLog("Couldn't find message in : " + responseMsg);
        }
        return "";
    }

    @Override
    public void exceptionError(String exception) {
        //LogProperties.WriteLog("Exception occur:" + exception);
    }
}
