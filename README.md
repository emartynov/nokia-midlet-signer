# MIDlet Signer for Nokia R&amp;D certificate (nokia-midlet-signer)
===================

## Briefs

MIDlet Signer with Nokia R&amp;D certificate

The project automates R&amp;D  signing process for MIDlet

Please contact Nokia to get details about access to this signing

## Usage

### Ant task

* Download (or build) and add task definition to your ant project

        <taskdef resource="nokia-sign-defs.xml" classpath="ant/nokia-sign-ant-task-0.9-jar-with-dependencies.jar"/>

* Add signing task to your project

        <target name="sign">
            <nokiaSign host="<signing host>" username="<your username>" password="<your password>" retrycount="3">
                <bundle jad="${basedir}\NapiExampleApp.jad" jar="${basedir}\NapiExampleApp.jar" keepunsignedjad="true"/>
            </nokiaSign>
        </target>

Please take a look to `example` folder

### Raw Java