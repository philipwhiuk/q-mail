package com.fsck.k9.view.securityStatus;


import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.fsck.k9.R;
import com.fsck.k9.mail.internet.SPFState;


public enum MessageSPFDisplayStatus {
    LOADING (
            R.attr.openpgp_grey,
            R.drawable.status_lock,
            R.string.spf_msg_loading
    ),

    UNKNOWN (
            R.attr.openpgp_blue,
            R.drawable.status_signature_unverified_cutout,
            R.string.spf_msg_unknown
    ),

    FAIL (
            R.attr.openpgp_red,
            R.drawable.status_lock_disabled,
            R.string.spf_msg_fail
    ),

    PASS (
            R.attr.openpgp_green,
            R.drawable.status_signature_verified_cutout,
            R.string.spf_msg_pass
    ),

    NONE (
            R.attr.openpgp_grey,
            R.drawable.status_signature_unverified_cutout,
            R.string.spf_msg_none
    );

    @AttrRes public final int colorAttr;
    @DrawableRes public final int statusIconRes;
    @StringRes public final Integer textResTop;

    MessageSPFDisplayStatus(@AttrRes int colorAttr, @DrawableRes int statusIconRes, @StringRes int textResTop) {
        this.colorAttr = colorAttr;
        this.statusIconRes = statusIconRes;
        this.textResTop = textResTop;
    }

    @NonNull
    public static MessageSPFDisplayStatus fromResultAnnotation(SPFState spfState) {
        if (spfState == null) {
            return UNKNOWN;
        }

        switch (spfState.getErrorType()) {
            case PASS:
                return PASS;

            case FAIL:
                return FAIL;

            case NONE:
                return NONE;

            case UNKNOWN:
                return UNKNOWN;
        }
        throw new IllegalStateException("Unhandled case!");
    }
}
