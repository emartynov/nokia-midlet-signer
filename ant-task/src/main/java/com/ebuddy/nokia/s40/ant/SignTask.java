package com.ebuddy.nokia.s40.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.DataType;

import java.io.File;
import java.io.IOException;

/**
 * User: Eugen
 */
public class SignTask extends DataType {
    private String jadFileName;
    private String jarFileName;
    private boolean saveUnsignedJad;
    private int attemptCount;
    private int numberOfRetries;

    public void checkArguments() {
        if (jadFileName == null)
            throw new BuildException("'jadFilename' is mandatory for signing");

        if (jarFileName == null)
            throw new BuildException("'jarFilename' is mandatory for signing");
    }

    public void setJadFileName(String jadFileName) {
        this.jadFileName = jadFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public void prepare() throws IOException {
        if (saveUnsignedJad)
            saveJad();
    }

    private void saveJad() throws IOException {
        File jadFile = new File(jadFileName);
        File savedJadFile = new File(getUnsignedJadFilename());

        Copy copyTask = new Copy();
        copyTask.setFile(jadFile);
        copyTask.setTofile(savedJadFile);

        copyTask.execute();
    }

    private String getUnsignedJadFilename() {
        int index = jadFileName.lastIndexOf(".jad");
        return jadFileName.substring(0, index) + "_unsigned.jad";
    }

    public void setSaveUnsignedJad(boolean saveUnsignedJad) {
        this.saveUnsignedJad = saveUnsignedJad;
    }

    public String getJarFilename() {
        return jarFileName;
    }

    public String getJadFilename() {
        return jadFileName;
    }

    public boolean shouldRetry() {
        attemptCount++;
        return attemptCount < numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }
}
