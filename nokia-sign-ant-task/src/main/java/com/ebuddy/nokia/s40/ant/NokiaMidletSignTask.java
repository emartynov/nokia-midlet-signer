package com.ebuddy.nokia.s40.ant;

import com.ebuddy.nokia.s40.NokiaSigner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Eugen
 */
public class NokiaMidletSignTask extends Task {
    private String host;
    private String username;
    private String password;

    private int retryCount = 1;

    private List<SignBundleType> signBundleTypeList = new ArrayList<SignBundleType>();

    @Override
    public void execute() throws BuildException {
        checkArgument(host, "Host");
        checkArgument(username, "Username");
        checkArgument(password, "Password");

        NokiaSigner signer = new NokiaSigner(host, username, password);

        checkAppsSize();

        for (SignBundleType signBundleType : signBundleTypeList) {
            sign(signBundleType, signer);
        }
    }

    private void sign(SignBundleType signBundleType, NokiaSigner signer) {
        int attemptCount = 0;

        signBundleType.checkArguments();

        try {
            signBundleType.prepare();
        } catch (IOException e) {
            throw new BuildException("Sign preparation went wrong", e);
        }

        while (attemptCount < retryCount) {
            try {
                log("Signing: " + signBundleType.getJadFilename());

                signer.sign(signBundleType.getJadFilename(), signBundleType.getJarFilename());
            } catch (Exception ignored) {

            } finally {
                attemptCount++;
            }
        }
    }

    private void checkAppsSize() {
        if (signBundleTypeList.size() == 0)
            throw new BuildException("Please provide at least one application for sign");
    }

    private void checkArgument(String argument, final String argumentName) {
        if (argument == null)
            throw new BuildException("'" + argumentName + "' is mandatory for signing");
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addBundle(SignBundleType bundle) {
        signBundleTypeList.add(bundle);
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}