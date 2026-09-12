package io.instanto.compat;

import static org.junit.Assert.*;

import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Pattern;
import org.junit.Test;

public class SourceIntegrityTest {
  @Test
  public void extractedInputsMatchTheirPinnedHashes() throws Exception {
    Path root = Path.of("..");
    String lock = Files.readString(root.resolve("upstream/provenance.json"));
    var entries =
        Pattern.compile("\"([^\"]+)\": \"([0-9a-f]{64})\"")
            .matcher(lock.substring(lock.indexOf("\"sha256\": {")));
    int checked = 0;
    while (entries.find()) {
      assertEquals(
          entries.group(1),
          entries.group(2),
          HexFormat.of()
              .formatHex(
                  MessageDigest.getInstance("SHA-256")
                      .digest(Files.readAllBytes(root.resolve(entries.group(1))))));
      checked++;
    }
    assertTrue("Input lock must cover extracted sources and bindings", checked > 20);
  }

  @Test
  public void publicBomDoesNotExportBuildDependencyPins() throws Exception {
    var document =
        javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(Path.of("../teavm-compat-bom/pom.xml").toFile());
    assertEquals(0, document.getElementsByTagName("parent").getLength());
    var dependencies = document.getElementsByTagName("dependency");
    assertEquals(6, dependencies.getLength());
    for (int i = 0; i < dependencies.getLength(); i++) {
      var dependency = (org.w3c.dom.Element) dependencies.item(i);
      assertEquals(
          "io.instanto", dependency.getElementsByTagName("groupId").item(0).getTextContent());
    }
  }
}
