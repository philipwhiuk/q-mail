package com.fsck.k9.view.securityStatus;


import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.fsck.k9.R;
import com.fsck.k9.mail.internet.DKIMState;
import com.fsck.k9.mail.internet.SecureTransportState;


public enum MessageDKIMDisplayStatus {
    LOADING (
            R.attr.openpgp_grey,
            R.drawable.status_lock,
            R.string.dkim_msg_loading
    ),

    UNKNOWN (
            R.attr.openpgp_blue,
            R.drawable.status_lock,
            R.string.dkim_msg_unknown
    ),

    FAIL (
            R.attr.openpgp_red,
            R.drawable.status_lock_disabled,
            R.string.dkim_msg_fail
    ),

    PASS (
            R.attr.openpgp_green,
            R.drawable.status_lock,
            R.string.dkim_msg_pass
    ),

    NONE (
            R.attr.openpgp_grey,
            R.drawable.status_lock,
            R.string.dkim_msg_none
    );

    @AttrRes public final int colorAttr;
    @DrawableRes public final int statusIconRes;
    @StringRes public final Integer textResTop;

    MessageDKIMDisplayStatus(@AttrRes int colorAttr, @DrawableRes int statusIconRes, @StringRes int textResTop) {
        this.colorAttr = colorAttr;
        this.statusIconRes = statusIconRes;
        this.textResTop = textResTop;
    }

    @NonNull
    public static MessageDKIMDisplayStatus fromResultAnnotation(DKIMState dkimState) {
        if (dkimState == null) {
            return UNKNOWN;
        }

        switch (dkimState.getErrorType()) {
            case PASS:
                return PASS;

            case FAIL:
                return FAIL;

            case UNKNOWN:
                return UNKNOWN;

            case NONE:
                return NONE;
        }
        throw new IllegalStateException("Unhandled case!");
    }
}
