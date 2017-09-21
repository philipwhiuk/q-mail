package com.fsck.k9.mail.internet;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.fsck.k9.R;

public class SecureTransportState {
    private SecureTransportError error;
    private int reason;

    public SecureTransportState(SecureTransportError state) {
        this.error = state;
    }

    public SecureTransportState(SecureTransportError state,
            int reason) {
        error = state;
        this.reason = reason;
    }

    public SecureTransportError getErrorType() {
        return error;
    }

    public int getReasonResource() {
        return reason;
    }

    public enum SecureTransportError {
        UNKNOWN,
        SECURELY_ENCRYPTED,
        INSECURELY_ENCRYPTED,
        UNENCRYPTED;
    }
}
