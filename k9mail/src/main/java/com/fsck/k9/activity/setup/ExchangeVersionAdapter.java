package com.fsck.k9.activity.setup;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.fsck.k9.mail.ConnectionSecurity;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;


class ExchangeVersionAdapter extends ArrayAdapter<ExchangeVersionHolder> {
    public ExchangeVersionAdapter(Context context, int resource, ExchangeVersionHolder[] exchangeVersions) {
        super(context, resource, exchangeVersions);
    }

    public static ExchangeVersionAdapter get(Context context) {
        return get(context, ExchangeVersion.values());
    }

    public static ExchangeVersionAdapter get(Context context,
                                                ExchangeVersion[] items) {
        ExchangeVersionHolder[] holders = new ExchangeVersionHolder[items.length];
        for (int i = 0; i < items.length; i++) {
            holders[i] = new ExchangeVersionHolder(items[i], context.getResources());
        }
        ExchangeVersionAdapter securityTypesAdapter = new ExchangeVersionAdapter(context,
                android.R.layout.simple_spinner_item, holders);
        securityTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return securityTypesAdapter;
    }

    public int getExchangeVersionPosition(ExchangeVersion exchangeVersion) {
        for (int i=0; i<getCount(); i++) {
            if (getItem(i).exchangeVersion == exchangeVersion) {
                return i;
            }
        }
        return -1;
    }
}
