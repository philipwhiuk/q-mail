package com.fsck.k9.mailstore;


import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsck.k9.mail.internet.MimeBodyPart;

import org.openintents.openpgp.OpenPgpDecryptionResult;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.smime.SMimeDecryptionResult;
import org.openintents.smime.SMimeError;
import org.openintents.smime.SMimeSignatureResult;


public final class CryptoResultAnnotation {
    @NonNull private final CryptoError errorType;
    private final MimeBodyPart replacementData;

    private final OpenPgpDecryptionResult openPgpDecryptionResult;
    private final OpenPgpSignatureResult openPgpSignatureResult;
    private final OpenPgpError openPgpError;
    private final PendingIntent openPgpPendingIntent;
    private final PendingIntent openPgpInsecureWarningPendingIntent;

    private final SMimeDecryptionResult sMimeDecryptionResult;
    private final SMimeSignatureResult sMimeSignatureResult;
    private final SMimeError sMimeError;
    private final PendingIntent sMimePendingIntent;
    private final PendingIntent sMimeInsecureWarningPendingIntent;
    private final boolean overrideCryptoWarning;

    private final CryptoResultAnnotation encapsulatedResult;

    private CryptoResultAnnotation(@NonNull CryptoError errorType, MimeBodyPart replacementData,
            OpenPgpDecryptionResult openPgpDecryptionResult,
            OpenPgpSignatureResult openPgpSignatureResult,
            PendingIntent openPgpPendingIntent,
            PendingIntent openPgpInsecureWarningPendingIntent,
            OpenPgpError openPgpError,
            boolean overrideCryptoWarning) {
        this.errorType = errorType;
        this.replacementData = replacementData;

        this.openPgpDecryptionResult = openPgpDecryptionResult;
        this.openPgpSignatureResult = openPgpSignatureResult;
        this.openPgpPendingIntent = openPgpPendingIntent;
        this.openPgpError = openPgpError;
        this.openPgpInsecureWarningPendingIntent = openPgpInsecureWarningPendingIntent;
        this.sMimeDecryptionResult = null;
        this.sMimeSignatureResult = null;
        this.sMimePendingIntent = null;
        this.sMimeError = null;
        this.sMimeInsecureWarningPendingIntent = null;
        this.overrideCryptoWarning = overrideCryptoWarning;

        this.encapsulatedResult = null;
    }

    private CryptoResultAnnotation(@NonNull CryptoError errorType, MimeBodyPart replacementData,
            SMimeDecryptionResult sMimeDecryptionResult,
            SMimeSignatureResult sMimeSignatureResult,
            PendingIntent sMimePendingIntent,
            PendingIntent sMimeInsecureWarningPendingIntent,
            SMimeError sMimeError,
            boolean overrideCryptoWarning) {
        this.errorType = errorType;
        this.replacementData = replacementData;

        this.openPgpDecryptionResult = null;
        this.openPgpSignatureResult = null;
        this.openPgpPendingIntent = null;
        this.openPgpError = null;
        this.openPgpInsecureWarningPendingIntent = null;
        this.sMimeDecryptionResult = sMimeDecryptionResult;
        this.sMimeSignatureResult = sMimeSignatureResult;
        this.sMimePendingIntent = sMimePendingIntent;
        this.sMimeError = sMimeError;
        this.sMimeInsecureWarningPendingIntent = sMimeInsecureWarningPendingIntent;
        this.overrideCryptoWarning = overrideCryptoWarning;

        this.encapsulatedResult = null;
    }

    private CryptoResultAnnotation(CryptoResultAnnotation annotation, CryptoResultAnnotation encapsulatedResult) {
        if (annotation.encapsulatedResult != null) {
            throw new AssertionError("cannot replace an encapsulated result, this is a bug!");
        }

        this.errorType = annotation.errorType;
        this.replacementData = annotation.replacementData;

        this.openPgpDecryptionResult = annotation.openPgpDecryptionResult;
        this.openPgpSignatureResult = annotation.openPgpSignatureResult;
        this.openPgpPendingIntent = annotation.openPgpPendingIntent;
        this.openPgpInsecureWarningPendingIntent = annotation.openPgpInsecureWarningPendingIntent;
        this.openPgpError = annotation.openPgpError;
        this.sMimeDecryptionResult = annotation.sMimeDecryptionResult;
        this.sMimeSignatureResult = annotation.sMimeSignatureResult;
        this.sMimePendingIntent = annotation.sMimePendingIntent;
        this.sMimeInsecureWarningPendingIntent = annotation.sMimeInsecureWarningPendingIntent;
        this.sMimeError = annotation.sMimeError;
        this.overrideCryptoWarning = annotation.overrideCryptoWarning;

        this.encapsulatedResult = encapsulatedResult;
    }

    public static CryptoResultAnnotation createErrorAnnotation(CryptoError error, MimeBodyPart replacementData) {
        if (error == CryptoError.OPENPGP_OK || error == CryptoError.SMIME_OK) {
            throw new AssertionError("CryptoError must be actual error state!");
        }
        return new CryptoResultAnnotation(error, replacementData, (OpenPgpDecryptionResult) null, null,
                null, null, null, false);
    }

    public static CryptoResultAnnotation createOpenPgpResultAnnotation(OpenPgpDecryptionResult decryptionResult,
            OpenPgpSignatureResult signatureResult, PendingIntent pendingIntent,
            PendingIntent insecureWarningPendingIntent, MimeBodyPart replacementPart,
            boolean overrideCryptoWarning) {
        return new CryptoResultAnnotation(CryptoError.OPENPGP_OK, replacementPart,
                decryptionResult, signatureResult, pendingIntent, insecureWarningPendingIntent, null,
                overrideCryptoWarning);
    }

    public static CryptoResultAnnotation createOpenPgpCanceledAnnotation() {
        return new CryptoResultAnnotation(CryptoError.OPENPGP_UI_CANCELED, null, (OpenPgpDecryptionResult) null, null, null, null, null, false);
    }

    public static CryptoResultAnnotation createOpenPgpSignatureErrorAnnotation(
            OpenPgpError error, MimeBodyPart replacementData) {
        return new CryptoResultAnnotation(
                CryptoError.OPENPGP_SIGNED_API_ERROR, replacementData, null, null, null, null, error, false);
    }

    public static CryptoResultAnnotation createOpenPgpEncryptionErrorAnnotation(OpenPgpError error) {
        return new CryptoResultAnnotation(
                CryptoError.OPENPGP_ENCRYPTED_API_ERROR, null, null, null, null, null, error, false);
    }

    public static CryptoResultAnnotation createSMimeResultAnnotation(SMimeDecryptionResult decryptionResult,
            SMimeSignatureResult signatureResult, PendingIntent pendingIntent,
            PendingIntent insecureWarningPendingIntent, MimeBodyPart replacementPart,
            boolean overrideCryptoWarning) {
        return new CryptoResultAnnotation(CryptoError.SMIME_OK, replacementPart,
                decryptionResult, signatureResult, pendingIntent, insecureWarningPendingIntent, null,
                overrideCryptoWarning);
    }

    public static CryptoResultAnnotation createSMimeCanceledAnnotation() {
        return new CryptoResultAnnotation(CryptoError.SMIME_UI_CANCELED, null, (SMimeDecryptionResult) null, null, null, null, null, false);
    }

    public static CryptoResultAnnotation createSMimeSignatureErrorAnnotation(
            SMimeError error, MimeBodyPart replacementData) {
        return new CryptoResultAnnotation(
                CryptoError.SMIME_SIGNED_API_ERROR, replacementData, null, null, null, null, error, false);
    }

    public static CryptoResultAnnotation createSMimeEncryptionErrorAnnotation(SMimeError error) {
        return new CryptoResultAnnotation(
                CryptoError.SMIME_ENCRYPTED_API_ERROR, null, null, null, null, null, error, false);
    }

    public boolean isOpenPgpResult() {
        return openPgpDecryptionResult != null && openPgpSignatureResult != null;
    }

    public boolean hasSignatureResult() {
        return openPgpSignatureResult != null &&
                openPgpSignatureResult.getResult() != OpenPgpSignatureResult.RESULT_NO_SIGNATURE;
    }

    @Nullable
    public OpenPgpDecryptionResult getOpenPgpDecryptionResult() {
        return openPgpDecryptionResult;
    }

    @Nullable
    public OpenPgpSignatureResult getOpenPgpSignatureResult() {
        return openPgpSignatureResult;
    }

    @Nullable
    public PendingIntent getOpenPgpSigningKeyIntentIfAny() {
        if (hasSignatureResult()) {
            return getOpenPgpPendingIntent();
        }
        if (encapsulatedResult != null && encapsulatedResult.hasSignatureResult()) {
            return encapsulatedResult.getOpenPgpPendingIntent();
        }
        return null;
    }

    @Nullable
    public PendingIntent getOpenPgpPendingIntent() {
        return openPgpPendingIntent;
    }

    public boolean hasOpenPgpInsecureWarningPendingIntent() {
        return openPgpInsecureWarningPendingIntent != null;
    }

    @Nullable
    public PendingIntent getOpenPgpInsecureWarningPendingIntent() {
        return openPgpInsecureWarningPendingIntent;
    }

    @Nullable
    public OpenPgpError getOpenPgpError() {
        return openPgpError;
    }

    @NonNull
    public CryptoError getErrorType() {
        return errorType;
    }

    public boolean hasReplacementData() {
        return replacementData != null;
    }

    @Nullable
    public MimeBodyPart getReplacementData() {
        return replacementData;
    }

    public boolean isOverrideSecurityWarning() {
        return overrideCryptoWarning;
    }

    @NonNull
    public CryptoResultAnnotation withEncapsulatedResult(CryptoResultAnnotation resultAnnotation) {
        return new CryptoResultAnnotation(this, resultAnnotation);
    }

    public boolean hasEncapsulatedResult() {
        return encapsulatedResult != null;
    }

    public CryptoResultAnnotation getEncapsulatedResult() {
        return encapsulatedResult;
    }

    public CryptoProviderType getProviderType() {
        return CryptoProviderType.OPENPGP;
    }

    public enum CryptoProviderType {
        OPENPGP,
        SMIME
    }

    public enum CryptoError {
        OPENPGP_OK,
        OPENPGP_UI_CANCELED,
        OPENPGP_SIGNED_API_ERROR,
        OPENPGP_ENCRYPTED_API_ERROR,
        OPENPGP_SIGNED_BUT_INCOMPLETE,
        OPENPGP_ENCRYPTED_BUT_INCOMPLETE,
        SIGNED_BUT_UNSUPPORTED,
        ENCRYPTED_BUT_UNSUPPORTED,
        OPENPGP_ENCRYPTED_NO_PROVIDER,
        OPENPGP_SIGNED_NO_PROVIDER,
        SMIME_OK,
        SMIME_UI_CANCELED,
        SMIME_SIGNED_API_ERROR,
        SMIME_ENCRYPTED_API_ERROR,
        SMIME_SIGNED_BUT_INCOMPLETE,
        SMIME_ENCRYPTED_BUT_INCOMPLETE,
        SMIME_ENCRYPTED_NO_PROVIDER,
        SMIME_SIGNED_NO_PROVIDER,
        ENCRYPTED_NO_PROVIDER
    }
}
