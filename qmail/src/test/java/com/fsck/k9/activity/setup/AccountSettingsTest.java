package com.fsck.k9.activity.setup;


import android.content.Intent;
import android.os.Bundle;

import com.fsck.k9.Account;
import com.fsck.k9.K9RobolectricTestRunner;
import com.fsck.k9.Preferences;
import com.fsck.k9.activity.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertNotNull;


@RunWith(K9RobolectricTestRunner.class)
public class AccountSettingsTest {

    private ActivityController<AccountSettings> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();
    private Preferences preferences = Preferences.getPreferences(shadowApplication.getApplicationContext());

    @Before
    public void setup() {
        Utils.deleteExistingAccounts(preferences);
        Account account = Preferences.getPreferences(shadowApplication.getApplicationContext()).newAccount();
        account.setStoreUri("imap://PLAIN:test:password/1|@hotmail.com:143");
        Intent intent = new Intent();
        intent.putExtra("account", account.getUuid());
        controller = Robolectric.buildActivity( AccountSettings.class ).withIntent(intent)
                .create().resume();
    }

    @Test
    public void notNull() throws Exception {
        AccountSettings activity = controller.get();

        assertNotNull(activity);
    }
}
