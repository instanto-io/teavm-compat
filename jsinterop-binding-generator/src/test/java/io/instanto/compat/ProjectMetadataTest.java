package io.instanto.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import org.junit.Test;

public class ProjectMetadataTest {
  @Test
  public void elemental2VersionNamesItsUpstreamInput() throws Exception {
    var builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
    var xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
    var parent = builder.parse(Path.of("../pom.xml").toFile());
    var library = builder.parse(Path.of("../elemental2-compat/pom.xml").toFile());
    var bom = builder.parse(Path.of("../teavm-compat-bom/pom.xml").toFile());
    String upstream = xpath.evaluate("/project/properties/elemental2.version", parent);
    String version = xpath.evaluate("/project/version", library);
    assertEquals(upstream, version.replaceFirst("-SNAPSHOT$", ""));
    assertEquals(version, xpath.evaluate("/project/properties/elemental2.compat.version", parent));
    assertEquals(
        version,
        xpath.evaluate(
            "/project/dependencyManagement/dependencies/dependency[artifactId='elemental2-compat']/version",
            bom));
  }

  @Test
  public void publicBomManagesOnlyThisProjectsArtifacts() throws Exception {
    var builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
    var xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
    var bom = builder.parse(Path.of("../teavm-compat-bom/pom.xml").toFile());
    assertEquals("io.instanto", xpath.evaluate("/project/parent/groupId", bom));
    assertEquals("instanto-org-pom", xpath.evaluate("/project/parent/artifactId", bom));

    var reactor = builder.parse(Path.of("../pom.xml").toFile());
    var modules =
        (org.w3c.dom.NodeList)
            xpath.evaluate(
                "/project/modules/module", reactor, javax.xml.xpath.XPathConstants.NODESET);
    var ownArtifacts = new java.util.HashSet<String>();
    for (int i = 0; i < modules.getLength(); i++) {
      var module =
          builder.parse(Path.of("..", modules.item(i).getTextContent(), "pom.xml").toFile());
      ownArtifacts.add(xpath.evaluate("/project/artifactId", module));
    }

    var managed =
        (org.w3c.dom.NodeList)
            xpath.evaluate(
                "/project/dependencyManagement/dependencies/dependency",
                bom,
                javax.xml.xpath.XPathConstants.NODESET);
    for (int i = 0; i < managed.getLength(); i++) {
      String groupId = xpath.evaluate("groupId", managed.item(i));
      String artifactId = xpath.evaluate("artifactId", managed.item(i));
      assertEquals(artifactId, "io.instanto", groupId);
      assertTrue(
          artifactId + " is not built by this project", ownArtifacts.contains(artifactId));
    }
  }
}
