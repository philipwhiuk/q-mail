package com.fsck.k9.ui.messageview;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.fsck.k9.Account;
import com.fsck.k9.QMail;
import com.fsck.k9.R;
import com.fsck.k9.mail.internet.DKIMState;
import com.fsck.k9.mail.internet.SPFState;
import com.fsck.k9.mail.internet.SecureTransportState;
import com.fsck.k9.mailstore.CryptoResultAnnotation;
import com.fsck.k9.mailstore.CryptoResultAnnotation.CryptoProviderType;
import com.fsck.k9.mailstore.MessageViewInfo;
import com.fsck.k9.view.securityStatus.MessageCryptoDisplayStatus;
import com.fsck.k9.view.securityStatus.MessageDKIMDisplayStatus;
import com.fsck.k9.view.securityStatus.MessageSPFDisplayStatus;
import com.fsck.k9.view.securityStatus.MessageTransportSecurityDisplayStatus;
import timber.log.Timber;


public class MessageSecurityPresenter implements OnSecurityClickListener {
    public static final int REQUEST_CODE_UNKNOWN_KEY = 123;
    public static final int REQUEST_CODE_SECURITY_WARNING = 124;


    // injected state
    private final MessageSecurityMvpView messageSecurityMvpView;


    // persistent state
    private boolean overrideCryptoWarning;


    // transient state
    private CryptoResultAnnotation cryptoResultAnnotation;
    private SecureTransportState secureTransportState;
    private SPFState spfState;
    private DKIMState dkimState;
    private boolean reloadOnResumeWithoutRecreateFlag;


    public MessageSecurityPresenter(Bundle savedInstanceState, MessageSecurityMvpView messageSecurityMvpView) {
        this.messageSecurityMvpView = messageSecurityMvpView;

        if (savedInstanceState != null) {
            overrideCryptoWarning = savedInstanceState.getBoolean("overrideCryptoWarning");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("overrideCryptoWarning", overrideCryptoWarning);
    }

    public void onResume() {
        if (reloadOnResumeWithoutRecreateFlag) {
            reloadOnResumeWithoutRecreateFlag = false;
            messageSecurityMvpView.restartMessageCryptoProcessing();
        }
    }

    public boolean maybeHandleShowMessage(MessageTopView messageView, Account account, MessageViewInfo messageViewInfo) {
        this.cryptoResultAnnotation = messageViewInfo.cryptoResultAnnotation;
        this.secureTransportState = messageViewInfo.secureTransportState;
        this.spfState = messageViewInfo.spfState;
        this.dkimState = messageViewInfo.dkimState;

        MessageCryptoDisplayStatus displayStatus =
                MessageCryptoDisplayStatus.fromResultAnnotation(messageViewInfo.cryptoResultAnnotation);
        if (displayStatus == MessageCryptoDisplayStatus.DISABLED) {
            return false;
        }

        boolean suppressSignOnlyMessages = !QMail.getOpenPgpSupportSignOnly();
        if (suppressSignOnlyMessages && displayStatus.isUnencryptedSigned()) {
            return false;
        }

        if (cryptoResultAnnotation.isOverrideSecurityWarning()) {
            overrideCryptoWarning = true;
        }

        messageView.getMessageHeaderView().setCryptoStatus(displayStatus);

        switch (displayStatus) {
            case UNENCRYPTED_SIGN_REVOKED:
            case ENCRYPTED_SIGN_REVOKED: {
                showMessageCryptoWarning(messageView, account, messageViewInfo,
                        R.string.messageview_crypto_warning_revoked);
                break;
            }
            case UNENCRYPTED_SIGN_EXPIRED:
            case ENCRYPTED_SIGN_EXPIRED: {
                showMessageCryptoWarning(messageView, account, messageViewInfo,
                        R.string.messageview_crypto_warning_expired);
                break;
            }
            case UNENCRYPTED_SIGN_INSECURE:
            case ENCRYPTED_SIGN_INSECURE: {
                showMessageCryptoWarning(messageView, account, messageViewInfo,
                        R.string.messageview_crypto_warning_insecure);
                break;
            }
            case UNENCRYPTED_SIGN_ERROR:
            case ENCRYPTED_SIGN_ERROR: {
                showMessageCryptoWarning(messageView, account, messageViewInfo,
                        R.string.messageview_crypto_warning_error);
                break;
            }
            case ENCRYPTED_UNSIGNED: {
                showMessageCryptoWarning(messageView, account, messageViewInfo,
                        R.string.messageview_crypto_warning_unsigned);
                break;
            }

            case CANCELLED: {
                Drawable providerIcon = getOpenPgpApiProviderIcon(messageView.getContext());
                messageView.showMessageCryptoCancelledView(messageViewInfo, providerIcon);
                break;
            }

            case INCOMPLETE_ENCRYPTED: {
                Drawable providerIcon = getOpenPgpApiProviderIcon(messageView.getContext());
                messageView.showMessageEncryptedButIncomplete(messageViewInfo, providerIcon);
                break;
            }

            case ENCRYPTED_ERROR:
            case ENCRYPTED_INSECURE:
            case UNSUPPORTED_ENCRYPTED: {
                Drawable providerIcon = getOpenPgpApiProviderIcon(messageView.getContext());
                if (messageViewInfo.cryptoResultAnnotation.hasReplacementData()) {
                    showMessageCryptoWarning(messageView, account, messageViewInfo,
                            R.string.messageview_crypto_warning_insecure);
                } else {
                    messageView.showMessageCryptoErrorView(messageViewInfo, providerIcon);
                }
                break;
            }

            case ENCRYPTED_NO_PROVIDER: {
                messageView.showCryptoProviderNotConfigured(messageViewInfo, cryptoResultAnnotation.getProviderType());
                break;
            }

            case INCOMPLETE_SIGNED:
            case UNSUPPORTED_SIGNED:
            default: {
                messageView.showMessage(account, messageViewInfo);
                break;
            }

            case LOADING: {
                throw new IllegalStateException("Displaying message while in loading state!");
            }
        }

        return true;
    }

    private void showMessageCryptoWarning(MessageTopView messageView, Account account,
            MessageViewInfo messageViewInfo, @StringRes int warningStringRes) {
        if (overrideCryptoWarning) {
            messageView.showMessage(account, messageViewInfo);
            return;
        }
        Drawable providerIcon = getOpenPgpApiProviderIcon(messageView.getContext());
        boolean showDetailButton = cryptoResultAnnotation.hasOpenPgpInsecureWarningPendingIntent();
        messageView.showMessageCryptoWarning(messageViewInfo, providerIcon, warningStringRes, showDetailButton);
    }

    @Override
    public void onSecurityClick() {
        MessageCryptoDisplayStatus displayStatus;
        if (cryptoResultAnnotation == null) {
            displayStatus = MessageCryptoDisplayStatus.DISABLED;
        } else {
            displayStatus =
                    MessageCryptoDisplayStatus.fromResultAnnotation(cryptoResultAnnotation);
        }
        MessageTransportSecurityDisplayStatus transportDisplayStatus =
                MessageTransportSecurityDisplayStatus.fromResultAnnotation(secureTransportState);
        MessageSPFDisplayStatus spfDisplayStatus =
                MessageSPFDisplayStatus.fromResultAnnotation(spfState);
        MessageDKIMDisplayStatus dkimDisplayStatus =
                MessageDKIMDisplayStatus.fromResultAnnotation(dkimState);
        switch (displayStatus) {
            case LOADING:
                // no need to do anything, there is a progress bar...
                break;
            case UNENCRYPTED_SIGN_UNKNOWN:
                launchPendingIntent(cryptoResultAnnotation);
                break;
            default:
                displaySecurityInfoDialog(displayStatus, transportDisplayStatus, spfDisplayStatus, dkimDisplayStatus);
                break;
        }
    }

    @SuppressWarnings("UnusedParameters") // for consistency with Activity.onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_UNKNOWN_KEY) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            messageSecurityMvpView.restartMessageCryptoProcessing();
        } else if (requestCode == REQUEST_CODE_SECURITY_WARNING) {
            if (overrideCryptoWarning || resultCode != Activity.RESULT_OK) {
                return;
            }

            overrideCryptoWarning = true;
            messageSecurityMvpView.redisplayMessage();
        } else {
            throw new IllegalStateException("got an activity result that wasn't meant for us. this is a bug!");
        }
    }

    private void displaySecurityInfoDialog(MessageCryptoDisplayStatus displayStatus,
            MessageTransportSecurityDisplayStatus transportSecurityDisplayStatus,
            MessageSPFDisplayStatus spfDisplayStatus,
            MessageDKIMDisplayStatus dkimDisplayStatus) {
        messageSecurityMvpView.showSecurityInfoDialog(
                displayStatus, transportSecurityDisplayStatus,
                spfDisplayStatus, dkimDisplayStatus,
                cryptoResultAnnotation != null && cryptoResultAnnotation.hasOpenPgpInsecureWarningPendingIntent());
    }

    private void launchPendingIntent(CryptoResultAnnotation cryptoResultAnnotation) {
        try {
            PendingIntent pendingIntent = cryptoResultAnnotation.getOpenPgpPendingIntent();
            if (pendingIntent != null) {
                messageSecurityMvpView.startPendingIntentForCryptoPresenter(
                        pendingIntent.getIntentSender(), REQUEST_CODE_UNKNOWN_KEY, null, 0, 0, 0);
            }
        } catch (IntentSender.SendIntentException e) {
            Timber.e(e, "SendIntentException");
        }
    }

    public void onClickShowCryptoKey() {
        try {
            PendingIntent pendingIntent = cryptoResultAnnotation.getOpenPgpSigningKeyIntentIfAny();
            if (pendingIntent != null) {
                messageSecurityMvpView.startPendingIntentForCryptoPresenter(
                        pendingIntent.getIntentSender(), null, null, 0, 0, 0);
            }
        } catch (IntentSender.SendIntentException e) {
            Timber.e(e, "SendIntentException");
        }
    }

    public void onClickRetryCryptoOperation() {
        messageSecurityMvpView.restartMessageCryptoProcessing();
    }

    public void onClickShowMessageOverrideWarning() {
        overrideCryptoWarning = true;
        messageSecurityMvpView.redisplayMessage();
    }

    public void onClickShowCryptoWarningDetails() {
        try {
            PendingIntent pendingIntent = cryptoResultAnnotation.getOpenPgpInsecureWarningPendingIntent();
            if (pendingIntent != null) {
                messageSecurityMvpView.startPendingIntentForCryptoPresenter(
                        pendingIntent.getIntentSender(), REQUEST_CODE_SECURITY_WARNING, null, 0, 0, 0);
            }
        } catch (IntentSender.SendIntentException e) {
            Timber.e(e, "SendIntentException");
        }
    }

    public Parcelable getDecryptionResultForReply() {
        if (cryptoResultAnnotation != null && cryptoResultAnnotation.isOpenPgpResult()) {
            return cryptoResultAnnotation.getOpenPgpDecryptionResult();
        }
        return null;
    }

    @Nullable
    private static Drawable getOpenPgpApiProviderIcon(Context context) {
        try {
            String openPgpProvider = QMail.getOpenPgpProvider();
            if (QMail.NO_OPENPGP_PROVIDER.equals(openPgpProvider)) {
                return null;
            }
            return context.getPackageManager().getApplicationIcon(openPgpProvider);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public void onClickConfigureProvider(CryptoProviderType cryptoProviderType) {
        reloadOnResumeWithoutRecreateFlag = true;
        messageSecurityMvpView.showCryptoConfigDialog(cryptoProviderType);
    }

    public interface MessageSecurityMvpView {
        void redisplayMessage();
        void restartMessageCryptoProcessing();

        void startPendingIntentForCryptoPresenter(IntentSender si, Integer requestCode, Intent fillIntent,
                int flagsMask, int flagValues, int extraFlags) throws IntentSender.SendIntentException;

        void showSecurityInfoDialog(MessageCryptoDisplayStatus displayStatus,
                MessageTransportSecurityDisplayStatus transportSecurityDisplayStatus,
                MessageSPFDisplayStatus spfDisplayStatus,
                MessageDKIMDisplayStatus dkimDisplayStatus,
                boolean hasSecurityWarning);
        void showCryptoConfigDialog(CryptoProviderType cryptoProviderType);
    }
}
