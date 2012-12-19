package com.ebuddy.nokia.s40;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: Eugen
 */
public class HttpHelper {
    private final DefaultHttpClient client;
    private BasicCookieStore cookieStore;

    public HttpHelper() {
        this(new DefaultHttpClient());
    }

    protected HttpHelper(DefaultHttpClient client) {
        this.client = client;

        client.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    }

    public void requestPage(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);

        httpGet.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        client.execute(httpGet, new BasicResponseHandler());
    }

    public void shutdown() {
        client.getConnectionManager().shutdown();
    }

    public void setAuth(String scopeURL, String username, String password) {
        client.getCredentialsProvider().setCredentials(new AuthScope(scopeURL, 443),
                new UsernamePasswordCredentials(username, password));
    }

    public void postFilesToPage(String url, UploadFile[] files) throws IOException {
        HttpPost post = new HttpPost(url);

        post.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        post.setEntity(createUploadEntity(files));

        client.execute(post, new BasicResponseHandler());
    }

    private MultipartEntity createUploadEntity(UploadFile[] files) {
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (UploadFile file : files) {
            addFile(file, entity);
        }

        return entity;
    }

    private void addFile(UploadFile uploadFile, MultipartEntity entity) {
        File file = new File(uploadFile.getFilename());
        entity.addPart(uploadFile.getName(), new FileBody(file, uploadFile.getMimeType()));
    }

    public void postKeyValue(String url, String name, String value) throws IOException {
        HttpPost post = new HttpPost(url);

        post.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        post.setEntity(new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair(name, value))));

        client.execute(post, new BasicResponseHandler());
    }

    public void requestAndSaveFile(String url, String fileName) throws IOException, SigningException {
        HttpGet httpGet = new HttpGet(url);

        httpGet.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        HttpEntity entity = null;

        try {
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                throw new SigningException(response.getStatusLine().toString());
            entity = response.getEntity();

            writeFile(fileName, entity);
        } finally {
            if (entity != null)
                EntityUtils.consumeQuietly(entity);
        }
    }

    private void writeFile(String fileName, HttpEntity entity) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File(fileName));
            entity.writeTo(os);
        } finally {
            if (os != null)
                try {
                    os.flush();
                    os.close();
                } catch (IOException ignored) {
                }
        }
    }

    public String getCookie() {
        return client.getCookieStore().getCookies().get(0).getValue();
    }
}
