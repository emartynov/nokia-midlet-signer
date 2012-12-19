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
    private String host;
    private String username;
    private String password;

    private List<SignTask> signTaskList = new ArrayList<SignTask>();

    @Override
    public void execute() throws BuildException {
        checkArgument(host, "Host");
        checkArgument(username, "Username");
        checkArgument(password, "Password");

        NokiaSigner signer = new NokiaSigner(host, username, password);

        checkAppsSize();

        for (SignTask signTask : signTaskList) {
            sign(signTask, signer);
        }
    }

    private void sign(SignTask signTask, NokiaSigner signer) {
        signTask.checkArguments();

        try {
            signTask.prepare();
        } catch (IOException e) {
            throw new BuildException("Sign preparation went wrong", e);
        }

        boolean needToSign = true;
        while (needToSign) {
            try {
                signer.sign(signTask.getJadFilename(), signTask.getJarFilename());
            } catch (IOException e) {
                needToSign = false;
            } catch (SigningException e) {
                needToSign = signTask.shouldRetry();
            }
        }
    }

    private void checkAppsSize() {
        if (signTaskList.size() == 0)
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

    public void addSign(SignTask signTask) {
        signTaskList.add(signTask);
    }
}
