package com.fsck.k9.activity.setup;


import android.content.Intent;
import android.widget.EditText;

import com.fsck.k9.Account;
import com.fsck.k9.K9RobolectricTestRunner;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.Accounts;
import com.fsck.k9.activity.Utils;
import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ServerSettings.Type;
import com.fsck.k9.mail.TransportUris;
import com.fsck.k9.mail.store.imap.ImapStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(K9RobolectricTestRunner.class)
public class AccountSetupBasicsTest {

    private ActivityController<AccountSetupBasics> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();
    private Preferences preferences = Preferences.getPreferences(
            shadowApplication.getApplicationContext());

    @Before
    public void setup() {
        Utils.deleteExistingAccounts(preferences);
        controller = Robolectric.buildActivity( AccountSetupBasics.class ).create().resume();
    }

    @Test
    public void showsNextButton() throws Exception {
        AccountSetupBasics activity = controller.get();

        assertNotNull(activity.findViewById(R.id.next));
        assertTrue(activity.findViewById(R.id.next).isEnabled());
    }

    @Test
    public void showsManualSetupButton() throws Exception {
        AccountSetupBasics activity = controller.get();

        assertNotNull(activity.findViewById(R.id.manual_setup));
        assertTrue(activity.findViewById(R.id.manual_setup).isEnabled());
    }

    @Test
    public void clickingManualSetupStartsSetupAccountType() throws Exception {
        AccountSetupBasics activity = controller.get();

        activity.findViewById(R.id.manual_setup).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupAccountType.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingManualSetupCreatesAccountWithUrisAndEmail() throws Exception {
        AccountSetupBasics activity = controller.get();

        ((EditText) activity.findViewById(R.id.account_email)).setText("test@hotmail.com");
        ((EditText) activity.findViewById(R.id.account_password)).setText("password");
        activity.findViewById(R.id.manual_setup).performClick();

        assertEquals(1, preferences.getAccounts().size());
        Account account = preferences.getAccounts().get(0);
        assertNotNull(preferences.getAccounts().get(0));

        String expectedStoreUri = ImapStore.createStoreUri(new ServerSettings(
                Type.IMAP, "mail.hotmail.com",
                -1, ConnectionSecurity.SSL_TLS_REQUIRED,
                AuthType.PLAIN, "test@hotmail.com",
                "password",
                null));
        String expectedTransportUri = TransportUris.createTransportUri(new ServerSettings(
                Type.SMTP, "mail.hotmail.com",
                -1, ConnectionSecurity.SSL_TLS_REQUIRED,
                AuthType.PLAIN, "test@hotmail.com",
                "password",
                null));
        String expectedEmail = "test@hotmail.com";

        assertEquals(expectedStoreUri, account.getStoreUri());
        assertEquals(expectedTransportUri, account.getTransportUri());
        assertEquals(expectedEmail, account.getEmail());
    }

    @Test
    public void clickingNextWithNoDetailsStartsSetupAccountType() throws Exception {
        AccountSetupBasics activity = controller.get();

        activity.findViewById(R.id.next).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupAccountType.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingNextWithDetailsForKnownProviderStartsSetupCheckSettings() throws Exception {
        AccountSetupBasics activity = controller.get();
        ((EditText) activity.findViewById(R.id.account_email)).setText("test@hotmail.com");

        activity.findViewById(R.id.next).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupCheckSettings.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
