package com.ebuddy.nokia.s40;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Eugen
 */
public class HttpHelperTest {
    private HttpHelper http;

    private DefaultHttpClient client = mock(DefaultHttpClient.class);
    private CredentialsProvider credentials = mock(CredentialsProvider.class);
    private ClientConnectionManager connections = mock(ClientConnectionManager.class);

    private HttpParams params = mock(HttpParams.class);

    @Before
    public void setUp() throws Exception {
        when(client.getCredentialsProvider()).thenReturn(credentials);
        when(client.getParams()).thenReturn(params);
        when(client.getConnectionManager()).thenReturn(connections);

        http = new HttpHelper(client);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void requestPageCorrectly() throws Exception {
        String data = "test";
        when(client.execute(any(HttpGet.class), any(ResponseHandler.class))).thenReturn(data);

        http.requestPage("test.com");

        verify(client).execute(any(HttpGet.class), any(ResponseHandler.class));
    }

    @Test
    public void setAuthCorrectly() {
        String url = "url";
        String user = "user";
        String password = "password";
        http.setAuth(url, user, password);

        verify(credentials).setCredentials(new AuthScope(url, 443), new UsernamePasswordCredentials(user, password));
    }

    @Test
    public void setCookieCorrectly() {
        verify(params).setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void  postFilesToNokia() throws IOException, URISyntaxException {
        String url = "url";
        String jad = "NapiExampleApp.jad";
        String jar = "NapiExampleApp.jar";

        http.postFilesToPage(url, getUploadFiles(jad, jar));

        ArgumentCaptor<HttpPost> post = ArgumentCaptor.forClass(HttpPost.class);
        verify(client).execute(post.capture(), any(ResponseHandler.class));

        HttpPost postValue = post.getValue();
        assertThat(postValue.getURI().toString()).isEqualTo(url);
        HttpEntity entity = postValue.getEntity();
        assertThat(entity).isInstanceOf(MultipartEntity.class);

        String data = getData(entity);
        assertThat(data).contains("Content-Disposition: form-data; name=\"" + NokiaSigner.JAD + "\"; filename=\"" + jad + "\"");
        assertThat(data).contains("Content-Disposition: form-data; name=\"" + NokiaSigner.JAR + "\"; filename=\"" + jar + "\"");
    }

    private UploadFile[] getUploadFiles(String jad, String jar) throws URISyntaxException {
        return new UploadFile[] {
                new UploadFile(NokiaSigner.JAD, findResourceFile(jad), NokiaSigner.APPLICATION_OCTET_STREAM),
                new UploadFile(NokiaSigner.JAR, findResourceFile(jar), NokiaSigner.APPLICATION_JAVA_ARCHIVE)};
    }

    private String findResourceFile(String filename) throws URISyntaxException {
        URL myTestURL = ClassLoader.getSystemResource(filename);
        return new File(myTestURL.toURI()).getAbsolutePath();
    }

    private String getData(HttpEntity entity) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream((int) entity.getContentLength());
        entity.writeTo(os);
        return new String(os.toByteArray());
    }

    @Test
    public void postStringCorrectly() throws IOException {
        String url = "url";
        String key = "key";
        String value = "value";

        http.postKeyValue(url, key, value);

        ArgumentCaptor<HttpPost> post = ArgumentCaptor.forClass(HttpPost.class);
        verify(client).execute(post.capture(), any(ResponseHandler.class));

        String sentData = getData(post.getValue().getEntity());
        assertThat(sentData).contains(key + "=" + value);
    }

    @Test
    public void shutdownCloseConnections() throws Exception {
        http.shutdown();

        verify(connections).shutdown();
    }
}
