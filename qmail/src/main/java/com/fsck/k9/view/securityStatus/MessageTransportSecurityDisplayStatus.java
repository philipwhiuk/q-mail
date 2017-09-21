package com.fsck.k9.view.securityStatus;


import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.fsck.k9.R;
import com.fsck.k9.mail.internet.SecureTransportState;


public enum MessageTransportSecurityDisplayStatus {
    LOADING (
            R.attr.openpgp_grey,
            R.drawable.status_lock,
            R.string.transport_crypto_msg_loading
    ),

    UNKNOWN (
            R.attr.openpgp_blue,
            R.drawable.status_lock,
            R.string.transport_crypto_msg_unknown
    ),

    UNENCRYPTED (
            R.attr.openpgp_grey,
            R.drawable.status_lock_disabled,
            R.string.transport_crypto_msg_unencrypted
    ),

    ENCRYPTED (
            R.attr.openpgp_green,
            R.drawable.status_lock,
            R.string.transport_crypto_msg_encrypted
    ),

    ENCRYPTED_INSECURE (
            R.attr.openpgp_orange,
            R.drawable.status_lock,
            R.string.transport_crypto_msg_insecure
    );

    @AttrRes public final int colorAttr;
    @DrawableRes public final int statusIconRes;
    @StringRes public final Integer textResTop;

    MessageTransportSecurityDisplayStatus(@AttrRes int colorAttr, @DrawableRes int statusIconRes, @StringRes int textResTop) {
        this.colorAttr = colorAttr;
        this.statusIconRes = statusIconRes;
        this.textResTop = textResTop;
    }

    @NonNull
    public static MessageTransportSecurityDisplayStatus fromResultAnnotation(SecureTransportState secureTransportState) {
        if (secureTransportState == null) {
            return UNKNOWN;
        }

        switch (secureTransportState.getErrorType()) {
            case SECURELY_ENCRYPTED:
                return ENCRYPTED;

            case INSECURELY_ENCRYPTED:
                return ENCRYPTED_INSECURE;

            case UNKNOWN:
                return UNKNOWN;

            case UNENCRYPTED:
                return UNENCRYPTED;
        }
        throw new IllegalStateException("Unhandled case!");
    }
}
