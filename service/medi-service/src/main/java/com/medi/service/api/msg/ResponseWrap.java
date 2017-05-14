package com.medi.service.api.msg;

import lombok.Data;

@Data
public class ResponseWrap<T> {
    private String ID;
    private String error;
    private T response;

    public String getID() {
        return ID;
    }

    public ResponseWrap setID(String ID) {
        this.ID = ID;
        return this;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public ResponseWrap setError(String error) {
        this.error = error;
        return this;
    }

    public ResponseWrap setResponse(T response) {
        this.response = response;
        return this;
    }
}
