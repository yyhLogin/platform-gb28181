package com.yyh.media.callback;

/**
 * @author: yyh
 * @date: 2022-02-23 14:55
 * @description: RequestMessage 请求信息定义
 **/
public class RequestMessage {

    private String id;

    private String key;

    private Object data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
