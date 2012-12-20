package com.ebuddy.nokia.s40.ant;

import com.ebuddy.nokia.s40.NokiaSigner;
import com.ebuddy.nokia.s40.SigningException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * User: Eugen
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NokiaMidletSignTask.class, SignBundleType.class})
public class NokiaSignBehaviourTest {
    private static final String HOST = "host";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String JAD = "a.jad";
    private static final String JAR = "a.jar";

    private NokiaSigner signer = mock(NokiaSigner.class);
    private Copy copy = mock(Copy.class);

    private NokiaMidletSignTask task;
    private SignBundleType bundle;

    @Before
    public void setUp() throws Exception {
        createTaskAndConfigure();

        whenNew(NokiaSigner.class).withArguments(HOST, USER, PASSWORD).thenReturn(signer);
        whenNew(Copy.class).withAnyArguments().thenReturn(copy);
    }

    private void createTaskAndConfigure() {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);
        task.setPassword(PASSWORD);
        task.setRetryCount(5);

        bundle = new SignBundleType();
        bundle.setJad(JAD);
        bundle.setJar(JAR);
        task.addBundle(bundle);
    }

    @Test(expected = BuildException.class)
    public void throwExceptionWhenUnconfiguredHost() throws Exception {
        task = new NokiaMidletSignTask();

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionWhenUnconfiguredUser() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionWhenUnconfiguredPassword() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionWhenNoRetryInvalid() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);
        task.setPassword(PASSWORD);

        task.setRetryCount(-1);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionWhenNoBundle() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);
        task.setPassword(PASSWORD);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionBundleUnconfiguredJad() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);
        task.setPassword(PASSWORD);

        SignBundleType bundle = new SignBundleType();
        task.addBundle(bundle);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void throwExceptionBundleUnconfiguredJar() throws Exception {
        task = new NokiaMidletSignTask();
        task.setHost(HOST);
        task.setUsername(USER);
        task.setPassword(PASSWORD);

        SignBundleType bundle = new SignBundleType();
        bundle.setJad(JAD);
        task.addBundle(bundle);

        task.execute();
    }

    @Test
    public void noExceptionWhenConfiguredAndRetrySetCorrectly() throws Exception {
        task.setRetryCount(0);

        task.execute();

        assertThat(task.retryCount).isEqualTo(1);
    }

    @Test
    public void saveUnsignedFileWhenConfigured() throws Exception {
        bundle.setKeepUnsignedJad(true);

        task.execute();

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(copy).setFile(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getName()).isEqualTo(JAD);

        verify(copy).setTofile(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getName()).isEqualTo("a_unsigned.jad");

        verify(copy).execute();
    }

    @Test
    public void callsSign() throws Exception {
        task.execute();

        verify(signer).sign(JAD, JAR);
    }

    @Test(expected = BuildException.class)
    public void noRetryIfIOException() throws Exception {
        doThrow(new IOException()).when(signer).sign(JAD, JAR);

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void failsWhenAllAttemptsFailed() throws Exception {
        int retryCount = 3;
        task.setRetryCount(retryCount);
        doThrow(new SigningException("Test")).when(signer).sign(JAD, JAR);

        task.execute();
    }

    @Test
    public void retriesIfSigningException() throws Exception {
        int retryCount = 9;
        task.setRetryCount(retryCount);
        doThrow(new SigningException("Test")).when(signer).sign(JAD, JAR);

        try {
            task.execute();
        } catch (BuildException ignored) {
        }

        verify(signer, times(retryCount)).sign(JAD, JAR);
    }

    @Test
    public void retriesLessTriesIfOneAttemptSuccess() throws Exception {
        int retryCount = 5;
        task.setRetryCount(retryCount);

        doThrow(new SigningException("Test")).doNothing().when(signer).sign(JAD, JAR);

        task.execute();

        verify(signer, times(2)).sign(JAD, JAR);
    }

    @Test
    public void whenSigningFinishedShutdownIsCalled() throws Exception {
        task.execute();

        verify(signer).shutdown();
    }
}
