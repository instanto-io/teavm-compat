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
  public void changedSourcesRetainOriginalNoticesAndIdentifyModification() throws Exception {
    Path in = folder.newFolder("notices").toPath();
    Path out = folder.getRoot().toPath().resolve("adapted-notices");
    String notice = "/* Copyright Original Author. Licensed under Apache License 2.0. */";
    write(
        in,
        "p/Lib.java",
        notice
            + "\npackage p;\n"
            + "import jsinterop.annotations.JsType;\n"
            + "@JsType(isNative = true) interface Lib { String name(); }\n");
    String unchanged = "package p;\nclass Unchanged {}\n";
    write(in, "p/Unchanged.java", unchanged);
    SourceAdapter.adapt(in, out, false);
    String adapted = Files.readString(out.resolve("p/Lib.java"));
    assertTrue(adapted, adapted.startsWith(SourceAdapter.MODIFICATION_NOTICE));
    assertTrue(adapted, adapted.contains(notice));
    assertEquals(unchanged, Files.readString(out.resolve("p/Unchanged.java")));
  }

  @Test
  public void outputDirectorySymlinksCannotRedirectWrites() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path out = folder.newFolder("out").toPath();
    Path outside = folder.newFolder("outside").toPath();
    write(in, "p/Kept.java", "package p; class Kept {}\n");
    Path existing = write(outside, "Kept.java", "original");
    Files.createSymbolicLink(out.resolve("p"), outside);
    assertThrows(java.io.IOException.class, () -> SourceAdapter.adapt(in, out, false));
    assertEquals("original", Files.readString(existing));
  }

  @Test
  public void outputAliasingInputThroughASymlinkCannotDeleteSources() throws Exception {
    Path in = folder.newFolder("in").toPath();
    Path source = write(in, "Kept.java", "class Kept {}\n");
    Path alias = folder.getRoot().toPath().resolve("alias");
    Files.createSymbolicLink(alias, in);
    assertThrows(IllegalArgumentException.class, () -> SourceAdapter.adapt(in, alias, false));
    assertEquals("class Kept {}\n", Files.readString(source));
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
  public void genericNativeResultsUseTheObjectConversionBoundary() throws Exception {
    Path in = folder.newFolder("generic-results").toPath();
    Path out = folder.getRoot().toPath().resolve("adapted-results");
    write(
        in,
        "p/Lookup.java",
        """
        package p;
        import jsinterop.annotations.JsType;
        @JsType(isNative=true, namespace="", name="Map")
        public class Lookup<V> {
          public native V get(String key);
          public native <T> T read(String key);
        }
        """);
    SourceAdapter.adapt(in, out, false);
    String adapted = Files.readString(out.resolve("p/Lookup.java"));
    assertTrue(adapted, adapted.contains("public V get(String key)"));
    assertTrue(adapted, adapted.contains("return jsinterop.base.Js.cast($gwtBridge0(key));"));
    assertTrue(adapted, adapted.contains("Object $gwtBridge0(String key)"));
    assertTrue(adapted, adapted.contains("return jsinterop.base.Js.cast($gwtBridge1(key));"));
  }

  @Test
  public void callbackDeclarationsFeedTheSourceAdaptationPipeline() throws Exception {
    Path in = folder.newFolder("callbacks").toPath();
    Path out = folder.getRoot().toPath().resolve("adapted-callbacks");
    write(
        in,
        "p/Callback.java",
        "package p; import jsinterop.annotations.JsFunction; @JsFunction public interface"
            + " Callback<T> { Object call(T value); }");
    write(
        in,
        "p/Api.java",
        "package p; import jsinterop.annotations.JsType; import com.google.gwt.dom.client.Element;"
            + " @JsType(isNative=true) public class Api { public native void on(Callback<Element>"
            + " callback); public native void off(Callback<Element> callback); }");
    assertEquals(2, SourceAdapter.adapt(in, out, false));
    String adapted = Files.readString(out.resolve("p/Api.java"));
    assertTrue(adapted, adapted.contains("NativeCallbacks.remember"));
    assertTrue(adapted, adapted.contains("JavaScriptObject.of(a0)"));
    assertTrue(adapted, adapted.contains("off$callback"));
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
        """
        package p;
        class B {
          static native void call(Runnable r) /*-{ r.@java.lang.Runnable::run()(); }-*/;
        }
        """);
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
