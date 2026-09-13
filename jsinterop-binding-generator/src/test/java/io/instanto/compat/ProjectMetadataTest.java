package io.instanto.compat;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import org.junit.Test;

public class ProjectMetadataTest {
  @Test
  public void publicBomDoesNotExportBuildDependencyPins() throws Exception {
    var document =
        javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(Path.of("../teavm-compat-bom/pom.xml").toFile());
    assertEquals(0, document.getElementsByTagName("parent").getLength());
    var dependencies = document.getElementsByTagName("dependency");
    assertEquals(8, dependencies.getLength());
    for (int i = 0; i < dependencies.getLength(); i++) {
      var dependency = (org.w3c.dom.Element) dependencies.item(i);
      assertEquals(
          "io.instanto", dependency.getElementsByTagName("groupId").item(0).getTextContent());
    }
  }
}
