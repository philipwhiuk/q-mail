package com.fsck.k9.ui.crypto;


import java.io.InputStream;
import java.io.OutputStream;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fsck.k9.QMail;
import com.fsck.k9.autocrypt.AutocryptOperations;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.BodyPart;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.internet.MimeBodyPart;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.TextBody;
import com.fsck.k9.mailstore.CryptoResultAnnotation;
import com.fsck.k9.mailstore.CryptoResultAnnotation.CryptoError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.OpenPgpDecryptionResult;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpApi.IOpenPgpCallback;
import org.openintents.openpgp.util.OpenPgpApi.IOpenPgpSinkResultCallback;
import org.openintents.openpgp.util.OpenPgpApi.OpenPgpDataSink;
import org.openintents.openpgp.util.OpenPgpApi.OpenPgpDataSource;
import org.openintents.smime.ISMimeService2;
import org.openintents.smime.SMimeDecryptionResult;
import org.openintents.smime.SMimeSignatureResult;
import org.openintents.smime.util.SMimeApi;
import org.openintents.smime.util.SMimeApi.ISMimeSinkResultCallback;
import org.openintents.smime.util.SMimeApi.SMimeDataSink;
import org.openintents.smime.util.SMimeApi.SMimeDataSource;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.fsck.k9.QMail.NO_OPENPGP_PROVIDER;
import static com.fsck.k9.QMail.NO_SMIME_PROVIDER;
import static com.fsck.k9.message.TestMessageConstructionUtils.bodypart;
import static com.fsck.k9.message.TestMessageConstructionUtils.messageFromBody;
import static com.fsck.k9.message.TestMessageConstructionUtils.multipart;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class MessageCryptoHelperTest {
    private MessageCryptoHelper messageCryptoHelper;
    private MessageCryptoCallback messageCryptoCallback;
    private Intent capturedApiIntent;
    private AutocryptOperations autocryptOperations;

    private OpenPgpApiFactory openPgpApiFactory;
    private OpenPgpApi openPgpApi;
    private IOpenPgpSinkResultCallback capturedOpenPgpCallback;

    private SMimeApiFactory sMimeApiFactory;
    private SMimeApi sMimeApi;
    private ISMimeSinkResultCallback capturedSMimeCallback;


    @Before
    public void setUp() throws Exception {
        autocryptOperations = mock(AutocryptOperations.class);
    }

    @After
    public void cleanUp() {
        QMail.setOpenPgpProvider(NO_OPENPGP_PROVIDER);
        QMail.setSMimeProvider(NO_SMIME_PROVIDER);
    }

    private void setUpWithOpenPgpApi() {
        createOpenPgpMocks();
        createCryptoObjects();
    }

    private void setUpWithSMimeApi() {
        createSMimeMocks();
        createCryptoObjects();
    }

    private void setUpWithBoth() {
        createOpenPgpMocks();
        createSMimeMocks();
        createCryptoObjects();
    }

    private void createOpenPgpMocks() {
        openPgpApi = mock(OpenPgpApi.class);
        QMail.setOpenPgpProvider("org.example.dummy");
        openPgpApiFactory = mock(OpenPgpApiFactory.class);
        when(openPgpApiFactory.createOpenPgpApi(any(Context.class), any(IOpenPgpService2.class)))
                .thenReturn(openPgpApi);
    }

    private void createSMimeMocks() {
        sMimeApi = mock(SMimeApi.class);
        sMimeApiFactory = mock(SMimeApiFactory.class);
        QMail.setSMimeProvider("org.example.dummy");
        when(sMimeApiFactory.createSMimeApi(any(Context.class), any(ISMimeService2.class))).thenReturn(sMimeApi);
    }

    private void createCryptoObjects() {
        messageCryptoHelper = new MessageCryptoHelper(RuntimeEnvironment.application,
                openPgpApiFactory, sMimeApiFactory,
                autocryptOperations);
        messageCryptoCallback = mock(MessageCryptoCallback.class);
    }
    @Test
    public void textPlain_withOpenPgpApi_checksForAutocryptHeader() throws Exception {
        setUpWithOpenPgpApi();
        MimeMessage message = new MimeMessage();
        message.setUid("msguid");
        message.setHeader("Content-Type", "text/plain");

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        ArgumentCaptor<MessageCryptoAnnotations> captor = ArgumentCaptor.forClass(MessageCryptoAnnotations.class);
        verify(messageCryptoCallback).onCryptoOperationsFinished(captor.capture());
        MessageCryptoAnnotations annotations = captor.getValue();
        assertTrue(annotations.isEmpty());
        verifyNoMoreInteractions(messageCryptoCallback);

        verify(autocryptOperations).hasAutocryptHeader(message);
        verifyNoMoreInteractions(autocryptOperations);
    }

    @Test
    public void textPlainWithAutocrypt_withOpenPgpApi() throws Exception {
        setUpWithOpenPgpApi();
        MimeMessage message = new MimeMessage();
        message.setUid("msguid");
        message.setHeader("Content-Type", "text/plain");

        when(autocryptOperations.hasAutocryptHeader(message)).thenReturn(true);
        when(autocryptOperations.addAutocryptPeerUpdateToIntentIfPresent(same(message), any(Intent.class))).thenReturn(true);


        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);


        ArgumentCaptor<MessageCryptoAnnotations> captor = ArgumentCaptor.forClass(MessageCryptoAnnotations.class);
        verify(messageCryptoCallback).onCryptoOperationsFinished(captor.capture());
        MessageCryptoAnnotations annotations = captor.getValue();
        assertTrue(annotations.isEmpty());
        verifyNoMoreInteractions(messageCryptoCallback);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(autocryptOperations).addAutocryptPeerUpdateToIntentIfPresent(same(message), intentCaptor.capture());
        verify(openPgpApi).executeApiAsync(same(intentCaptor.getValue()), same((InputStream) null),
                same((OutputStream) null), any(IOpenPgpCallback.class));
    }

    @Test
    public void multipartSignedOpenPgp__withNullBody_withOpenPgpApi__shouldReturnSignedIncomplete() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        bodypart("text/plain"),
                        bodypart("application/pgp-signature")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, true);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.OPENPGP_SIGNED_BUT_INCOMPLETE, null,
                null, null, null, null, null, null);
    }

    @Test
    public void multipartEncryptedOpenPgp__withNullBody_withOpenPgpApi__shouldReturnEncryptedIncomplete() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/pgp-encrypted\"",
                        bodypart("application/pgp-encrypted"),
                        bodypart("application/octet-stream")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, false);

        assertPartAnnotationHasState(
                message, messageCryptoCallback, CryptoError.OPENPGP_ENCRYPTED_BUT_INCOMPLETE,
                null,
                null, null, null,
                null, null, null);
    }

    @Test
    public void multipartEncrypted__withUnknownProtocol_withOpenPgpApi__shouldReturnEncryptedUnsupported() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/bad-protocol\"",
                        bodypart("application/bad-protocol", "content"),
                        bodypart("application/octet-stream", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, false);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.ENCRYPTED_BUT_UNSUPPORTED,
                null,
                null,
                null, null,
                null,
                null, null);
    }

    @Test
    public void multipartSigned__withUnknownProtocol_withOpenPgpApi__shouldReturnSignedUnsupported() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/bad-protocol\"",
                        bodypart("text/plain", "content"),
                        bodypart("application/bad-protocol", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, true);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.SIGNED_BUT_UNSUPPORTED, null,
                null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedOpenPgp_withOpenPgpApi__shouldCallOpenPgpApiAsync() throws Exception {
        setUpWithOpenPgpApi();
        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        signedBodyPart,
                        bodypart("application/pgp-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        OutputStream outputStream = mock(OutputStream.class);


        processOpenPgpSignedMessageAndCaptureMocks(message, signedBodyPart, outputStream);


        assertEquals(OpenPgpApi.ACTION_DECRYPT_VERIFY, capturedApiIntent.getAction());
        assertEquals("test@example.org", capturedApiIntent.getStringExtra(OpenPgpApi.EXTRA_SENDER_ADDRESS));

        verify(autocryptOperations).addAutocryptPeerUpdateToIntentIfPresent(message, capturedApiIntent);
        verifyNoMoreInteractions(autocryptOperations);
    }

    @Test
    public void multipartSignedOpenPgp_withOpenPgpApi__withSignOnlyDisabled__shouldReturnNothing() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        bodypart("text/plain", "content"),
                        bodypart("application/pgp-signature", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, false);

        assertReturnsWithNoCryptoAnnotations(messageCryptoCallback);
    }

    @Test
    public void multipartSignedOpenPgp_withOpenPgpApi__withSignOnlyDisabledAndNullBody__shouldReturnNothing() throws Exception {
        setUpWithOpenPgpApi();
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        bodypart("text/plain"),
                        bodypart("application/pgp-signature")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, false);

        assertReturnsWithNoCryptoAnnotations(messageCryptoCallback);
    }

    @Test
    public void multipartEncryptedOpenPgp_withOpenPgpApi__shouldCallOpenPgpApiAsync() throws Exception {
        setUpWithOpenPgpApi();
        Body encryptedBody = spy(new TextBody("encrypted data"));
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/pgp-encrypted\"",
                        bodypart("application/pgp-encrypted", "content"),
                        bodypart("application/octet-stream", encryptedBody)
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        OutputStream outputStream = mock(OutputStream.class);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_SUCCESS);
        OpenPgpDecryptionResult decryptionResult = mock(OpenPgpDecryptionResult.class);
        resultIntent.putExtra(OpenPgpApi.RESULT_DECRYPTION, decryptionResult);
        OpenPgpSignatureResult signatureResult = mock(OpenPgpSignatureResult.class);
        resultIntent.putExtra(OpenPgpApi.RESULT_SIGNATURE, signatureResult);
        PendingIntent pendingIntent = mock(PendingIntent.class);
        resultIntent.putExtra(OpenPgpApi.RESULT_INTENT, pendingIntent);


        processOpenPgpEncryptedMessageAndCaptureMocks(message, encryptedBody, outputStream);

        MimeBodyPart decryptedPart = new MimeBodyPart(new TextBody("text"));
        capturedOpenPgpCallback.onReturn(resultIntent, decryptedPart);


        assertEquals(OpenPgpApi.ACTION_DECRYPT_VERIFY, capturedApiIntent.getAction());
        assertEquals("test@example.org", capturedApiIntent.getStringExtra(OpenPgpApi.EXTRA_SENDER_ADDRESS));
        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.OPENPGP_OK, decryptedPart,
                decryptionResult, signatureResult, pendingIntent, null, null, null);
        verify(autocryptOperations).addAutocryptPeerUpdateToIntentIfPresent(message, capturedApiIntent);
        verifyNoMoreInteractions(autocryptOperations);
    }

    private void processOpenPgpEncryptedMessageAndCaptureMocks(Message message, Body encryptedBody, OutputStream outputStream)
            throws Exception {
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<OpenPgpDataSource> dataSourceCaptor = ArgumentCaptor.forClass(OpenPgpDataSource.class);
        ArgumentCaptor<IOpenPgpSinkResultCallback> callbackCaptor = ArgumentCaptor.forClass(IOpenPgpSinkResultCallback.class);
        verify(openPgpApi).executeApiAsync(intentCaptor.capture(), dataSourceCaptor.capture(),
                any(OpenPgpDataSink.class), callbackCaptor.capture());

        capturedApiIntent = intentCaptor.getValue();
        capturedOpenPgpCallback = callbackCaptor.getValue();

        OpenPgpDataSource dataSource = dataSourceCaptor.getValue();
        dataSource.writeTo(outputStream);
        verify(encryptedBody).writeTo(outputStream);
    }

    private void processOpenPgpSignedMessageAndCaptureMocks(Message message, BodyPart signedBodyPart,
            OutputStream outputStream) throws Exception {
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback, null, null, true);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<OpenPgpDataSource> dataSourceCaptor = ArgumentCaptor.forClass(OpenPgpDataSource.class);
        ArgumentCaptor<IOpenPgpSinkResultCallback> callbackCaptor = ArgumentCaptor.forClass(IOpenPgpSinkResultCallback.class);
        verify(openPgpApi).executeApiAsync(intentCaptor.capture(), dataSourceCaptor.capture(),
                callbackCaptor.capture());

        capturedApiIntent = intentCaptor.getValue();
        capturedOpenPgpCallback = callbackCaptor.getValue();

        OpenPgpDataSource dataSource = dataSourceCaptor.getValue();
        dataSource.writeTo(outputStream);
        verify(signedBodyPart).writeTo(outputStream);
    }

    @Test
    public void textPlain_withSMimeApi() throws Exception {
        setUpWithSMimeApi();

        MimeMessage message = new MimeMessage();
        message.setUid("msguid");
        message.setHeader("Content-Type", "text/plain");

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        ArgumentCaptor<MessageCryptoAnnotations> captor = ArgumentCaptor.forClass(MessageCryptoAnnotations.class);
        verify(messageCryptoCallback).onCryptoOperationsFinished(captor.capture());
        MessageCryptoAnnotations annotations = captor.getValue();
        assertTrue(annotations.isEmpty());
        verifyNoMoreInteractions(messageCryptoCallback);
    }

    @Test
    public void multipartEncryptedUnknownProtocol_withSMimeApi__shouldReturnEncryptedUnsupported() throws Exception {
        setUpWithSMimeApi();

        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/bad-protocol\"",
                        bodypart("application/bad-protocol", "content"),
                        bodypart("application/octet-stream", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.ENCRYPTED_BUT_UNSUPPORTED,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedUnknownProtocol_withSMimeApi__shouldReturnSignedUnsupported() throws Exception {
        setUpWithSMimeApi();

        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/bad-protocol\"",
                        bodypart("text/plain", "content"),
                        bodypart("application/bad-protocol", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, true);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.SIGNED_BUT_UNSUPPORTED,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedUnknownProtocol_withSMimeApi_withSignOnlyDisabled__shouldReturnNothing() throws Exception {
        setUpWithSMimeApi();

        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/bad-protocol\"",
                        bodypart("text/plain", "content"),
                        bodypart("application/bad-protocol", "content")
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertReturnsWithNoCryptoAnnotations(messageCryptoCallback);
    }


    @Test
    public void multipartEncryptedSMime_withOpenPgpApi__shouldReturnEncryptedNoProvider() throws Exception {
        setUpWithOpenPgpApi();
        Body encryptedBody = spy(new TextBody("encrypted data"));
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/pkcs7-mime\"",
                        bodypart("application/pkcs7-mime", "content"),
                        bodypart("application/octet-stream", encryptedBody)
                )
        );

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.SMIME_ENCRYPTED_NO_PROVIDER,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedSMime_withOpenPgpApi_withSignOnlyDisabled__shouldReturnNothing() throws Exception {
        setUpWithOpenPgpApi();

        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pkcs7-signature\"",
                        signedBodyPart,
                        bodypart("application/pkcs7-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertReturnsWithNoCryptoAnnotations(messageCryptoCallback);
    }

    @Test
    public void multipartSignedSMime_withOpenPgpApi_withSignOnly__shouldReturnSignedNoProvider() throws Exception {
        setUpWithOpenPgpApi();

        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pkcs7-signature\"",
                        signedBodyPart,
                        bodypart("application/pkcs7-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, true);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.SMIME_SIGNED_NO_PROVIDER,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartEncryptedOpenPgp_withSMimeApi__shouldReturnEncryptedNoProvider() throws Exception {
        setUpWithSMimeApi();

        Body encryptedBody = spy(new TextBody("encrypted data"));
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/pgp-encrypted\"",
                        bodypart("application/pgp-encrypted", "content"),
                        bodypart("application/octet-stream", encryptedBody)
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.OPENPGP_ENCRYPTED_NO_PROVIDER,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedOpenPgp_withSMimeApi_withSignOnlyDisabled__shouldReturnNothing() throws Exception {
        setUpWithSMimeApi();
        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        signedBodyPart,
                        bodypart("application/pgp-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        assertReturnsWithNoCryptoAnnotations(messageCryptoCallback);
    }

    @Test
    public void multipartSignedOpenPgp_withSMimeApi_withSignOnlyEnabled__shouldReturnSignedNoProvider() throws Exception {
        setUpWithSMimeApi();
        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pgp-signature\"",
                        signedBodyPart,
                        bodypart("application/pgp-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        MessageCryptoCallback messageCryptoCallback = mock(MessageCryptoCallback.class);
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, true);

        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.OPENPGP_SIGNED_NO_PROVIDER,
                null, null,
                null, null,
                null, null, null);
    }

    @Test
    public void multipartSignedSMime_withSMimeApi__shouldCallSMimeApiAsync() throws Exception {
        setUpWithSMimeApi();
        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pkcs7-signature\"",
                        signedBodyPart,
                        bodypart("application/pkcs7-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        OutputStream outputStream = mock(OutputStream.class);


        processSMimeSignedMessageAndCaptureMocks(message, signedBodyPart, outputStream);

        assertEquals(SMimeApi.ACTION_DECRYPT_VERIFY, capturedApiIntent.getAction());
        assertEquals("test@example.org", capturedApiIntent.getStringExtra(SMimeApi.EXTRA_SENDER_ADDRESS));
    }

    @Test
    public void multipartSignedSMime_withSMimeApi_withSignOnlyDisabled__shouldCallSMimeApiAsync() throws Exception {
        setUpWithSMimeApi();
        BodyPart signedBodyPart = spy(bodypart("text/plain", "content"));
        Message message = messageFromBody(
                multipart("signed", "protocol=\"application/pkcs7-signature\"",
                        signedBodyPart,
                        bodypart("application/pkcs7-signature", "content")
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        OutputStream outputStream = mock(OutputStream.class);


        processSMimeSignedMessageAndCaptureMocks(message, signedBodyPart, outputStream);

        assertEquals(SMimeApi.ACTION_DECRYPT_VERIFY, capturedApiIntent.getAction());
        assertEquals("test@example.org", capturedApiIntent.getStringExtra(SMimeApi.EXTRA_SENDER_ADDRESS));
    }

    @Test
    public void multipartEncryptedSMime_withSMimeApi__shouldCallSMimeApiAsync() throws Exception {
        setUpWithSMimeApi();
        Body encryptedBody = spy(new TextBody("encrypted data"));
        Message message = messageFromBody(
                multipart("encrypted", "protocol=\"application/pkcs7-mime\"",
                        bodypart("application/pkcs7-mime", "content"),
                        bodypart("application/octet-stream", encryptedBody)
                )
        );
        message.setFrom(Address.parse("Test <test@example.org>")[0]);

        OutputStream outputStream = mock(OutputStream.class);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(SMimeApi.RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
        SMimeDecryptionResult decryptionResult = mock(SMimeDecryptionResult.class);
        resultIntent.putExtra(SMimeApi.RESULT_DECRYPTION, decryptionResult);
        SMimeSignatureResult signatureResult = mock(SMimeSignatureResult.class);
        resultIntent.putExtra(SMimeApi.RESULT_SIGNATURE, signatureResult);
        PendingIntent pendingIntent = mock(PendingIntent.class);
        resultIntent.putExtra(SMimeApi.RESULT_INTENT, pendingIntent);


        processSMimeEncryptedMessageAndCaptureMocks(message, encryptedBody, outputStream);

        MimeBodyPart decryptedPart = new MimeBodyPart(new TextBody("text"));
        capturedSMimeCallback.onReturn(resultIntent, decryptedPart);


        assertEquals(SMimeApi.ACTION_DECRYPT_VERIFY, capturedApiIntent.getAction());
        assertEquals("test@example.org", capturedApiIntent.getStringExtra(SMimeApi.EXTRA_SENDER_ADDRESS));
        assertPartAnnotationHasState(message, messageCryptoCallback, CryptoError.SMIME_OK, decryptedPart,
                null, null, null,
                decryptionResult, signatureResult, pendingIntent);
    }

    private void processSMimeEncryptedMessageAndCaptureMocks(
            Message message, Body encryptedBody, OutputStream outputStream)
            throws Exception {
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, false);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<SMimeDataSource> dataSourceCaptor = ArgumentCaptor.forClass(SMimeDataSource.class);
        ArgumentCaptor<ISMimeSinkResultCallback> callbackCaptor = ArgumentCaptor.forClass(
                ISMimeSinkResultCallback.class);
        verify(sMimeApi).executeApiAsync(intentCaptor.capture(), dataSourceCaptor.capture(),
                any(SMimeDataSink.class), callbackCaptor.capture());

        capturedApiIntent = intentCaptor.getValue();
        capturedSMimeCallback = callbackCaptor.getValue();

        SMimeDataSource dataSource = dataSourceCaptor.getValue();
        dataSource.writeTo(outputStream);
        verify(encryptedBody).writeTo(outputStream);
    }

    private void processSMimeSignedMessageAndCaptureMocks(Message message, BodyPart signedBodyPart,
            OutputStream outputStream) throws Exception {
        messageCryptoHelper.asyncStartOrResumeProcessingMessage(message, messageCryptoCallback,
                null, null, true);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<SMimeDataSource> dataSourceCaptor = ArgumentCaptor.forClass(SMimeDataSource.class);
        ArgumentCaptor<ISMimeSinkResultCallback> callbackCaptor = ArgumentCaptor.forClass(ISMimeSinkResultCallback.class);
        verify(sMimeApi).executeApiAsync(intentCaptor.capture(), dataSourceCaptor.capture(),
                callbackCaptor.capture());

        capturedApiIntent = intentCaptor.getValue();
        capturedSMimeCallback = callbackCaptor.getValue();

        SMimeDataSource dataSource = dataSourceCaptor.getValue();
        dataSource.writeTo(outputStream);
        verify(signedBodyPart).writeTo(outputStream);
    }

    private void assertReturnsWithNoCryptoAnnotations(MessageCryptoCallback messageCryptoCallback) {
        ArgumentCaptor<MessageCryptoAnnotations> captor = ArgumentCaptor.forClass(MessageCryptoAnnotations.class);
        verify(messageCryptoCallback).onCryptoOperationsFinished(captor.capture());
        verifyNoMoreInteractions(messageCryptoCallback);

        MessageCryptoAnnotations annotations = captor.getValue();
        assertTrue(annotations.isEmpty());
    }

    private void assertPartAnnotationHasState(Message message, MessageCryptoCallback messageCryptoCallback,
            CryptoError cryptoErrorState, MimeBodyPart replacementPart, OpenPgpDecryptionResult openPgpDecryptionResult,
            OpenPgpSignatureResult openPgpSignatureResult, PendingIntent openPgpPendingIntent,
            SMimeDecryptionResult sMimeDecryptionResult,
            SMimeSignatureResult sMimeSignatureResult, PendingIntent sMimePendingIntent) {
        ArgumentCaptor<MessageCryptoAnnotations> captor = ArgumentCaptor.forClass(MessageCryptoAnnotations.class);
        verify(messageCryptoCallback).onCryptoOperationsFinished(captor.capture());
        MessageCryptoAnnotations annotations = captor.getValue();
        CryptoResultAnnotation cryptoResultAnnotation = annotations.get(message);
        assertEquals(cryptoErrorState, cryptoResultAnnotation.getErrorType());
        if (replacementPart != null) {
            assertSame(replacementPart, cryptoResultAnnotation.getReplacementData());
        }
        assertSame(openPgpDecryptionResult, cryptoResultAnnotation.getOpenPgpDecryptionResult());
        assertSame(openPgpSignatureResult, cryptoResultAnnotation.getOpenPgpSignatureResult());
        assertSame(openPgpPendingIntent, cryptoResultAnnotation.getOpenPgpPendingIntent());
        assertSame(sMimeDecryptionResult, cryptoResultAnnotation.getSMimeDecryptionResult());
        assertSame(sMimeSignatureResult, cryptoResultAnnotation.getSMimeSignatureResult());
        assertSame(sMimePendingIntent, cryptoResultAnnotation.getSMimePendingIntent());
        verifyNoMoreInteractions(messageCryptoCallback);
    }

}