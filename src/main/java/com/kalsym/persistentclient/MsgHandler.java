package com.kalsym.persistentclient;

interface MsgHandler {

    public void handleMessage(String msg);

    public void exceptionError(String exception);
}
