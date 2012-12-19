package com.ebuddy.nokia.s40.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.DataType;

import java.io.File;
import java.io.IOException;

/**
 * User: Eugen
 */
public class SignBundleType extends DataType {
    private String jadFilename;
    private String jarFilename;
    private boolean keepUnsignedJad;

    public void checkArguments() {
        if (jadFilename == null)
            throw new BuildException("'jadFilename' is mandatory for signing");

        if (jarFilename == null)
            throw new BuildException("'jarFilename' is mandatory for signing");
    }

    public void setJad(String jadFilename) {
        this.jadFilename = jadFilename;
    }

    public void setJar(String jarFilename) {
        this.jarFilename = jarFilename;
    }

    public void prepare() throws IOException {
        if (keepUnsignedJad)
            saveJad();
    }

    private void saveJad() throws IOException {
        File jadFile = new File(jadFilename);
        File savedJadFile = new File(getUnsignedJadFilename());

        Copy copyTask = new Copy();
        copyTask.setFile(jadFile);
        copyTask.setTofile(savedJadFile);

        copyTask.execute();
    }

    private String getUnsignedJadFilename() {
        int index = jadFilename.lastIndexOf(".jad");
        return jadFilename.substring(0, index) + "_unsigned.jad";
    }

    public void setKeepUnsignedJad(boolean keepUnsignedJad) {
        this.keepUnsignedJad = keepUnsignedJad;
    }

    public String getJarFilename() {
        return jarFilename;
    }

    public String getJadFilename() {
        return jadFilename;
    }
}
