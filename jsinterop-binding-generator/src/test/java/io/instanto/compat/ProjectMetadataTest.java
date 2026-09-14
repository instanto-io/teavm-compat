package io.instanto.compat;

import static org.junit.Assert.assertEquals;

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
  public void publicBomDoesNotExportBuildDependencyPins() throws Exception {
    var document =
        javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(Path.of("../teavm-compat-bom/pom.xml").toFile());
    var parent = (org.w3c.dom.Element) document.getElementsByTagName("parent").item(0);
    assertEquals("io.instanto", parent.getElementsByTagName("groupId").item(0).getTextContent());
    assertEquals(
        "instanto-org-pom", parent.getElementsByTagName("artifactId").item(0).getTextContent());
    var dependencies = document.getElementsByTagName("dependency");
    assertEquals(8, dependencies.getLength());
    for (int i = 0; i < dependencies.getLength(); i++) {
      var dependency = (org.w3c.dom.Element) dependencies.item(i);
      assertEquals(
          "io.instanto", dependency.getElementsByTagName("groupId").item(0).getTextContent());
    }
  }
}
