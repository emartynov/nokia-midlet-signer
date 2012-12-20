package com.ebuddy.nokia.s40.ant;

import com.ebuddy.nokia.s40.NokiaSigner;
import com.ebuddy.nokia.s40.SigningException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Eugen
 */
public class NokiaMidletSignTask extends Task {
    String host;
    String username;
    String password;

    int retryCount = 1;

    private List<SignBundleType> signBundleTypeList = new ArrayList<SignBundleType>();

    @Override
    public void execute() throws BuildException {
        checkArgument(host, "Host");
        checkArgument(username, "Username");
        checkArgument(password, "Password");

        checkRetryCount();

        NokiaSigner signer = new NokiaSigner(host, username, password);

        checkAppsSize();

        for (SignBundleType signBundleType : signBundleTypeList) {
            sign(signBundleType, signer);
        }

        signer.shutdown();
    }

    private void checkRetryCount() {
        if (retryCount == 0)
            retryCount = 1;

        if (retryCount < 0)
            throw new BuildException("Invalid retry count " + retryCount);
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

                log("Successfully signed!");
                break;
            } catch (SigningException e) {
                attemptCount++;
            } catch (IOException e) {
                throw new BuildException("Sign can't sign", e);
            }
        }

        if (attemptCount == retryCount)
            throw new BuildException("Tried " + retryCount + " retries to sign but failed");
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

    List<SignBundleType> getBundlesList() {
        return signBundleTypeList;
    }
}