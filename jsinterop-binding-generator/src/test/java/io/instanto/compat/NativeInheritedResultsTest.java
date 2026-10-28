package io.instanto.compat;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class NativeInheritedResultsTest {
  @Test
  public void inheritedGenericResultIsImplementedOnceWithoutLosingOverloads() {
    String parent =
        """
        package p;
        import jsinterop.annotations.JsType;
        @JsType(isNative=true) public interface View<K,V> { V get(K key); }
        """;
    String child =
        """
        package p;
        import jsinterop.annotations.JsType;
        @JsType(isNative=true) public class Map<K,V> implements View<K,V> {
          public native V get(K key);
          public native V get(String key);
        }
        """;
    BindingPipeline pipeline = new BindingPipeline(List.of(parent, child), false);
    String adaptedParent = pipeline.transform(parent);
    String adaptedChild = pipeline.transform(child);
    assertTrue(adaptedParent, adaptedParent.contains("default V get(K key)"));
    assertTrue(adaptedParent, adaptedParent.contains("Js.cast("));
    assertTrue(adaptedParent, adaptedParent.contains("interface $NativeResult0<K, V>"));
    assertFalse(adaptedChild, adaptedChild.contains("get(K key)"));
    assertTrue(adaptedChild, adaptedChild.contains("get(String key)"));
    assertTrue(adaptedChild, adaptedChild.contains("implements View<K, V>"));
  }
}
