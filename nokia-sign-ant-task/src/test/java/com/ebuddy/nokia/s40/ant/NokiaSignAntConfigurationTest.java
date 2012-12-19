package com.ebuddy.nokia.s40.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: Eugen
 */
public class NokiaSignAntConfigurationTest {
    private Project project;
    private Document document;
    private NokiaMidletSignTask task;

    @Before
    public void setUp() throws URISyntaxException, ParserConfigurationException, IOException, SAXException {
        File antFile = new File(findResourceFile("build.xml"));
        project = new Project();
        project.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        helper.parse(project, antFile);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        document = dBuilder.parse(antFile);
        document.getDocumentElement().normalize();

        task = getNokiaMidletSignTask();
    }

    @Test
    public void checkLoadNokiaSignTaskFromXML() throws Exception {
        Element signTaskElement = getElementByName("nokiaSign");

        assertThat(task.host).isEqualTo(signTaskElement.getAttribute("host"));
        assertThat(task.username).isEqualTo(signTaskElement.getAttribute("username"));
        assertThat(task.password).isEqualTo(signTaskElement.getAttribute("password"));
        assertThat(task.retryCount).isEqualTo(Integer.parseInt(signTaskElement.getAttribute("retrycount")));
    }

    @Test
    public void checkLoadBundleDataFromXML() throws Exception {
        Element signTaskElement = getElementByName("bundle");

        List<SignBundleType> bundles = task.getBundlesList();

        assertThat(bundles).hasSize(1);

        SignBundleType bundle = bundles.get(0);

        assertThat(bundle.getJadFilename()).endsWith(getFileName(signTaskElement.getAttribute("jad")));
        assertThat(bundle.getJarFilename()).endsWith(getFileName(signTaskElement.getAttribute("jar")));
        assertThat(bundle.keepUnsignedJad).isEqualTo(Boolean.parseBoolean(signTaskElement.getAttribute("keepunsignedjad")));
    }

    private String getFileName(String attribute) {
        return attribute.substring(attribute.lastIndexOf('\\') + 1);
    }

    private NokiaMidletSignTask getNokiaMidletSignTask() {
        Hashtable targets = project.getTargets();
        Target target = (Target) targets.get(getTargetName());
        Task[] tasks = target.getTasks();

        UnknownElement defTask = (UnknownElement) tasks[0];
        NokiaMidletSignTask task = new NokiaMidletSignTask();
        task.setProject(project);
        defTask.configure(task);

        return task;
    }

    private Element getElementByName(String tagName) {
        NodeList list = document.getElementsByTagName(tagName);

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }

        return null;
    }

    private String getTargetName() {
        NodeList list = document.getElementsByTagName("target");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                return element.getAttribute("name");
            }
        }

        return "sign";
    }

    private String findResourceFile(String filename) throws URISyntaxException {
        URL myTestURL = ClassLoader.getSystemResource(filename);
        return new File(myTestURL.toURI()).getAbsolutePath();
    }
}
