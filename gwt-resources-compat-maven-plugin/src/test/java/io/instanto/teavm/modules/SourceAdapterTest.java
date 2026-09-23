package io.instanto.teavm.modules;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SourceAdapterTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private Path write(Path root, String relative, String text) throws Exception {
    Path file = root.resolve(relative);
    Files.createDirectories(file.getParent());
    Files.writeString(file, text);
    return file;
  }

  @Test
  public void jsInteropBecomesTeaVmJso() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path out = folder.getRoot().toPath().resolve("out");
    write(
        in,
        "p/Lib.java",
        "package p;\nimport jsinterop.annotations.JsType;\n"
            + "@JsType(isNative = true)\ninterface Lib { String name(); }\n");
    assertEquals(1, SourceAdapter.adapt(in, out, false));
    String adapted = Files.readString(out.resolve("p/Lib.java"));
    assertTrue(adapted, adapted.contains("extends JSObject"));
    assertFalse(adapted, adapted.contains("@JsType"));
  }

  @Test
  public void legacyJsniBecomesJsBodyOnlyWhenAsked() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path out = folder.getRoot().toPath().resolve("out");
    String jsni =
        "package p;\nclass A {\n  static native boolean ready() /*-{ return !!$wnd.x; }-*/;\n}\n";
    write(in, "p/A.java", jsni);
    SourceAdapter.adapt(in, out, false);
    assertEquals(jsni, Files.readString(out.resolve("p/A.java")));
    SourceAdapter.adapt(in, out, true);
    String adapted = Files.readString(out.resolve("p/A.java"));
    assertTrue(adapted, adapted.contains("@JSBody"));
    assertTrue(adapted, adapted.contains("window.x"));
  }

  @Test
  public void javaCallbacksInJsniFailNamingTheFile() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path out = folder.getRoot().toPath().resolve("out");
    write(
        in,
        "p/B.java",
        "package p;\nclass B {\n  static native void call(Runnable r) /*-{ r.@java.lang.Runnable::run()(); }-*/;\n}\n");
    try {
      SourceAdapter.adapt(in, out, true);
      fail("expected the Java callback to be rejected");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage(), expected.getMessage().startsWith("p/B.java"));
    }
  }

  @Test
  public void staleOutputIsRemovedAndOtherFilesKept() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path out = folder.newFolder("out").toPath();
    write(in, "p/Kept.java", "package p;\nclass Kept {}\n");
    write(out, "p/Gone.java", "package p;\nclass Gone {}\n");
    write(out, "notes.txt", "kept");
    SourceAdapter.adapt(in, out, false);
    assertTrue(Files.exists(out.resolve("p/Kept.java")));
    assertFalse(Files.exists(out.resolve("p/Gone.java")));
    assertTrue(Files.exists(out.resolve("notes.txt")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nestedRootsAreRejected() throws Exception {
    Path in = folder.newFolder("in").toPath();
    SourceAdapter.adapt(in, in.resolve("out"), false);
  }
}
