package com.example.utrun.models;

public class MessageModel {

    private String msgId;
    private String senderId;
    private String message;

    private long timestamp;
    public MessageModel() {
    }
    public MessageModel(String msgId, String senderId, String message, long timestamp) {
        this.msgId = msgId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;


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
