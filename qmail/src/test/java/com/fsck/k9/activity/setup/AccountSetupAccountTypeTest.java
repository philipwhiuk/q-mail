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
import com.fsck.k9.mail.store.RemoteStore;
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
public class AccountSetupAccountTypeTest {

    private ActivityController<AccountSetupAccountType> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();
    private Preferences preferences = Preferences.getPreferences(
            shadowApplication.getApplicationContext());
    private Account account;

    @Before
    public void setup() {
        Utils.deleteExistingAccounts(preferences);
        account = Preferences.getPreferences(
                shadowApplication.getApplicationContext()).newAccount();
        String storeUri = ImapStore.createStoreUri(new ServerSettings(
                Type.IMAP, "hotmail.com",
                Type.IMAP.defaultPort, ConnectionSecurity.NONE,
                AuthType.PLAIN, "test",
                "password",
                null));
        account.setStoreUri(storeUri);
        account.setEmail("test@hotmail.com");

        Intent intent = new Intent();
        intent.putExtra("account", account.getUuid());
        controller = Robolectric.buildActivity(AccountSetupAccountType.class)
                .withIntent(intent)
                .create().resume();
    }

    @Test
    public void showsImapButton() throws Exception {
        AccountSetupAccountType activity = controller.get();

        assertNotNull(activity.findViewById(R.id.imap));
        assertTrue(activity.findViewById(R.id.imap).isEnabled());
    }

    @Test
    public void showsPop3Button() throws Exception {
        AccountSetupAccountType activity = controller.get();

        assertNotNull(activity.findViewById(R.id.pop));
        assertTrue(activity.findViewById(R.id.pop).isEnabled());
    }

    @Test
    public void showsWebDavButton() throws Exception {
        AccountSetupAccountType activity = controller.get();

        assertNotNull(activity.findViewById(R.id.webdav));
        assertTrue(activity.findViewById(R.id.webdav).isEnabled());
    }

    @Test
    public void showsEwsButton() throws Exception {
        AccountSetupAccountType activity = controller.get();

        assertNotNull(activity.findViewById(R.id.ews));
        assertTrue(activity.findViewById(R.id.ews).isEnabled());
    }

    @Test
    public void clickingImapStartsSetupAccountIncoming() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.imap).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupIncoming.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingImapSetsAccountTypeIMAP() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.imap).performClick();

        assertEquals(Type.IMAP, RemoteStore.decodeStoreUri(account.getStoreUri()).type);
    }

    @Test
    public void clickingPop3StartsSetupAccountIncoming() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.pop).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupIncoming.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingPopSetsAccountTypePOP3() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.pop).performClick();

        assertEquals(Type.POP3, RemoteStore.decodeStoreUri(account.getStoreUri()).type);
    }

    @Test
    public void clickingWebDavStartsSetupAccountIncoming() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.webdav).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupIncoming.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingWebDavSetsAccountTypeWebDAV() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.webdav).performClick();

        assertEquals(Type.WebDAV, RemoteStore.decodeStoreUri(account.getStoreUri()).type);
    }

    @Test
    public void clickingEwsStartsSetupAccountIncoming() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.ews).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupIncoming.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingExchangeSetsAccountTypeEWS() throws Exception {
        AccountSetupAccountType activity = controller.get();

        activity.findViewById(R.id.ews).performClick();

        assertEquals(Type.EWS, RemoteStore.decodeStoreUri(account.getStoreUri()).type);
    }
}
