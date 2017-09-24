package com.fsck.k9.service;


import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;


class Utils {
    static void startServicePossiblyInForeground(Context context, Intent i) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            System.out.println(i);
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }
}
