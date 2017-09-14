package com.fsck.k9.activity.setup;

import android.content.res.Resources;

import com.fsck.k9.R;
import com.fsck.k9.mail.ConnectionSecurity;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;


class ExchangeVersionHolder {
    final ExchangeVersion exchangeVersion;
    private final Resources resources;

    public ExchangeVersionHolder(ExchangeVersion exchangeVersion, Resources resources) {
        this.exchangeVersion = exchangeVersion;
        this.resources = resources;
    }

    public String toString() {
        final int resourceId = resourceId();
        if (resourceId == 0) {
            return exchangeVersion.name();
        } else {
            return resources.getString(resourceId);
        }
    }

    private int resourceId() {
        switch (exchangeVersion) {
            case Exchange2007_SP1: return R.string.account_setup_incoming_exchange_version_2007sp1_label;
            case Exchange2010: return R.string.account_setup_incoming_exchange_version_2010_label;
            case Exchange2010_SP1: return R.string.account_setup_incoming_exchange_version_2010sp1_label;
            case Exchange2010_SP2: return R.string.account_setup_incoming_exchange_version_2010sp2_label;
            default: return 0;
        }
    }
}
