# MIDlet Signer for Nokia R&amp;D certificate [![Build Status](https://travis-ci.org/emartynov/nokia-midlet-signer.png?branch=master)](https://travis-ci.org/emartynov/nokia-midlet-signer)

## Briefs

MIDlet Signer with Nokia R&amp;D certificate

The project automates R&amp;D  signing process for MIDlet

Please contact Nokia to get details about access to this signing

## Usage

### Ant task

* Download (or build) ant task jar and add task definition to your ant project:

        <taskdef resource="nokia-sign-defs.xml" classpath="ant/nokia-sign-ant-task-0.9-jar-with-dependencies.jar"/>

* Add signing task to your project:

        <target name="sign">
            <nokiaSign host="<signing host>" username="<your username>" password="<your password>" retrycount="3">
                <bundle jad="${basedir}\NapiExampleApp.jad" jar="${basedir}\NapiExampleApp.jar" keepunsignedjad="true"/>
            </nokiaSign>
        </target>

Please take a look to [`example`][1] folder

### Raw Java

* Download (or build) engine jar. Use jar with dependencies if you don't have `org.apache.HttpClient` in your classpath
* Run jar file with parameters:
        
        java -jar nokia-sign-engine-0.9-jar-with-dependencies.jar -h <host> -u <username> -p <password> -jad <jad> -jar <jar>

## TODO
* Think about multitasking (using one signer but several simultaneous signing or maybe create several signers in thread pool)
* Create maven plugin for signing automation

  [1]: https://github.com/emartynov/nokia-midlet-signer/tree/master/example
