package org.example.rest;

public class Response {
    private int status;
    private String description;
    private Object data;

    public Response(int status, String description, Object data) {
        this.status = status;
        this.description = description;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
