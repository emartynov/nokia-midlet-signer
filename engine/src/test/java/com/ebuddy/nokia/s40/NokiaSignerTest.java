package com.ebuddy.nokia.s40;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test for simple NokiaSigner.
 */
public class NokiaSignerTest {

    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String JAD = "test.jad";
    private static final String JAR = "test.jar";
    private static final String HOST = "host";

    private NokiaSigner signer;
    private HttpHelper http = mock(HttpHelper.class);

    @Before
    public void setUp() {
        signer = new NokiaSigner(HOST, USER, PASSWORD, http);
    }

    @Test
    public void authenticationSetCorrectly() {
        verify(http).setAuth(HOST, USER, PASSWORD);
    }

    @Test
    public void closeResourcesAfter() throws Exception {
        signer.sign(JAD, JAR);

        verify(http).shutdown();
    }

    @Test
    public void requestedLoginPage() throws Exception {
        signer.sign(JAD, JAR);

        verify(http).requestPage(signer.getSignURL());
    }

    @Test
    public void uploadFilesForSigning() throws Exception {
        signer.sign(JAD, JAR);

        ArgumentCaptor<UploadFile[]> captor = ArgumentCaptor.forClass(UploadFile[].class);
        verify(http).postFilesToPage(eq(signer.getSignURL()), captor.capture());

        UploadFile[] data = captor.getValue();
        assertThat(data.length).isEqualTo(2);
        checkFile(data[0], NokiaSigner.JAD, NokiaSigner.APPLICATION_OCTET_STREAM);
        checkFile(data[1], NokiaSigner.JAR, NokiaSigner.APPLICATION_JAVA_ARCHIVE);
    }

    private void checkFile(UploadFile file, String name, String mimeType) {
        assertThat(file.getName()).isEqualTo(name);
        assertThat(file.getMimeType()).isEqualTo(mimeType);
    }

    @Test
    public void setPermissions() throws Exception {
        signer.sign(JAD, JAR);

        verify(http).postKeyValue(signer.getSignURL(), NokiaSigner.SUBMIT, NokiaSigner.KEEP_PERMISSIONS);
    }

    @Test
    public void setDomain() throws Exception {
        signer.sign(JAD, JAR);

        verify(http).postKeyValue(signer.getSignURL(), NokiaSigner.DOMAIN, NokiaSigner.MANUFACTURER_DOMAIN);
    }

    @Test
    public void requestAndDownload() throws Exception {
        signer.sign(JAD, JAR);

        verify(http).requestAndSaveFile(anyString(), eq(JAD));
    }
}
