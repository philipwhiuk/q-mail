package com.fsck.k9.activity.setup;


import android.content.Intent;

import com.fsck.k9.Account;
import com.fsck.k9.K9RobolectricTestRunner;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.Utils;
import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ServerSettings.Type;
import com.fsck.k9.mail.store.imap.ImapStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(K9RobolectricTestRunner.class)
public class AccountSetupIncomingTest {

    private ActivityController<AccountSetupIncoming> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();
    private Preferences preferences = Preferences.getPreferences(
            shadowApplication.getApplicationContext());

    @Before
    public void setup() {
        Utils.deleteExistingAccounts(preferences);
        Account account = Preferences.getPreferences(
                shadowApplication.getApplicationContext()).newAccount();
        String storeUri = ImapStore.createStoreUri(new ServerSettings(
                Type.IMAP, "hotmail.com",
                Type.IMAP.defaultPort, ConnectionSecurity.NONE,
                AuthType.PLAIN, "test",
                "password",
                null));
        account.setStoreUri(storeUri);
        Intent intent = new Intent();
        intent.putExtra("account", account.getUuid());
        controller = Robolectric.buildActivity(AccountSetupIncoming.class)
                .withIntent(intent)
                .create().resume();
    }

    @Test
    public void nextButtonIsEnabled() throws Exception {
        AccountSetupIncoming activity = controller.get();

        assertTrue(activity.findViewById(R.id.next).isEnabled());
    }

    @Test
    public void clickingNextStartsCheckSettings() throws Exception {
        AccountSetupIncoming activity = controller.get();

        activity.findViewById(R.id.next).performClick();

        Intent expectedIntent = new Intent(activity,
                AccountSetupCheckSettings.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
