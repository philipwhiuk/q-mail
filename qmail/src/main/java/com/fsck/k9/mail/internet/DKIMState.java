package com.fsck.k9.mail.internet;


public class DKIMState {
    private DKIMError error;
    private String domain;

    public DKIMState(DKIMError state) {
        this.error = state;
    }

    public DKIMState(DKIMError state,
            String domain) {
        error = state;
        this.domain = domain;
    }

    public DKIMError getErrorType() {
        return error;
    }

    public String getDomain() {
        return domain;
    }

    public enum DKIMError {
        UNKNOWN,
        PASS,
        FAIL,
        NONE
    }
}
