package com.fsck.k9.ui.crypto;


import android.content.Context;

import org.openintents.smime.ISMimeService2;
import org.openintents.smime.util.SMimeApi;


public class SMimeApiFactory {
    SMimeApi createSMimeApi(Context context, ISMimeService2 service) {
        return new SMimeApi(context, service);
    }
}
