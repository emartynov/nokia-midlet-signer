package com.ebuddy.nokia.s40;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class NokiaSigner {
    static final String JAD = "jad";
    static final String JAR = "jar";

    static final String URL_SIGN_IN_SUFFIX = "MIDletSigningServlet/";
    static final String URL_SIGNED_MIDLET_SUFFIX = "TEMP_MIDLETSIGNING/";

    static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    static final String APPLICATION_JAVA_ARCHIVE = "application/java-archive";
    static final String SUBMIT = "submit";
    static final String KEEP_PERMISSIONS = "keep Permissions";
    static final String DOMAIN = "domain";
    static final String MANUFACTURER_DOMAIN = "Manufacturer domain";


    private HttpHelper http;
    private String host;

    public NokiaSigner(String host, String username, String password) {
        this(host, username, password, new HttpHelper());
    }

    NokiaSigner(String host, String username, String password, HttpHelper http) {
        this.http = http;

        this.host = host;
        http.setAuth(this.host, username, password);
    }

    public void sign(String jadFile, String jarFile) throws IOException, SigningException {
        try {
            login();
            uploadFiles(jadFile, jarFile);
            postPermissions();
            choseDomain();
            downloadAndSaveJad(jadFile);
        } catch (IOException e) {
            System.out.println(e.toString());
            throw e;
        } catch (SigningException e) {
            System.out.println(e.toString());
            throw e;
        }
    }

    public void shutdown() {
        http.shutdown();
    }

    private void downloadAndSaveJad(String jadFileName) throws IOException, SigningException {
        http.requestAndSaveFile(makeDownloadURL(getFileName(jadFileName)), jadFileName);
    }

    private String getFileName(String path) {
        int index = path.lastIndexOf(File.separatorChar);
        if (index < 0) return path;
        return path.substring(index + 1);
    }

    private String makeDownloadURL(String filename) {
        return getDownloadURL() + getCookiePart() + "/" + filename;
    }

    private String getDownloadURL() {
        return "https://" + host + "/" + URL_SIGNED_MIDLET_SUFFIX;
    }

    String getSignURL() {
        return "https://" + host + "/" + URL_SIGN_IN_SUFFIX;
    }

    private String getCookiePart() {
        return http.getCookie();
    }

    private void choseDomain() throws IOException {
        http.postKeyValue(getSignURL(), DOMAIN, MANUFACTURER_DOMAIN);
    }

    private void postPermissions() throws IOException {
        http.postKeyValue(getSignURL(), SUBMIT, KEEP_PERMISSIONS);
    }

    private void uploadFiles(String jadFile, String jarFile) throws IOException {
        http.postFilesToPage(getSignURL(), prepareUploadFiles(jadFile, jarFile));
    }

    private UploadFile[] prepareUploadFiles(String jadFile, String jarFile) {
        return new UploadFile[] {
                new UploadFile(JAD, jadFile, APPLICATION_OCTET_STREAM),
                new UploadFile(JAR, jarFile, APPLICATION_JAVA_ARCHIVE)};
    }

    private void login() throws IOException {
        http.requestPage(getSignURL());
    }

    public static void main(String[] args) throws Exception {
        String host = args[0];
        String username = args[1];
        String password = args[2];

        NokiaSigner signer = new NokiaSigner(host, username, password);


        String jadFileName = args[3];
        String jarFileName = args[4];

        signer.sign(jadFileName, jarFileName);

        signer.shutdown();
    }
}
