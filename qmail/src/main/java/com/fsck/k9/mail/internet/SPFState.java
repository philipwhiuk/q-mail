package com.fsck.k9.mail.internet;


public class SPFState {
    private SPFError error;
    private String domain;

    public SPFState(SPFError state) {
        this.error = state;
    }

    public SPFState(SPFError state,
            String domain) {
        error = state;
        this.domain = domain;
    }

    public SPFError getErrorType() {
        return error;
    }

    public String getDomain() {
        return domain;
    }

    public enum SPFError {
        UNKNOWN,
        PASS,
        FAIL,
        NONE
    }
}
