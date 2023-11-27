package com.example.utrun.models;



public class MessageModel   {

    private String msgId;
    private String senderId;
    private String message;

    private long timestamp;
    private String ReceiverReadMessage;
    private boolean CurrentReadMessage;
    public MessageModel() {
    }
    public MessageModel(String msgId, String senderId, String message, long timestamp,String ReceiverReadMessage, boolean CurrentReadMessage) {
        this.msgId = msgId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.ReceiverReadMessage = ReceiverReadMessage;
        this.CurrentReadMessage = CurrentReadMessage;


    }

    public String getReceiverReadMessage() {
        return ReceiverReadMessage;
    }

    public void setReceiverReadMessage(String receiverReadMessage) {
        ReceiverReadMessage = receiverReadMessage;
    }

    public boolean getCurrentReadMessage() {
        return CurrentReadMessage;
    }

    public void setCurrentReadMessage(boolean currentReadMessage) {
        CurrentReadMessage = currentReadMessage;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
