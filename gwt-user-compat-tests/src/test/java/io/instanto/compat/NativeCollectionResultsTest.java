package io.instanto.compat;

import static org.junit.Assert.*;

import elemental2.core.JsArray;
import elemental2.core.JsMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class NativeCollectionResultsTest {
  @Test
  public void absentGenericValuesBecomeNullAndStoredObjectsKeepIdentity() {
    JsMap<Object, JsArray<Object>> map = new JsMap<>();
    Object key = new Object();
    assertNull(map.get(key));
    JsArray<Object> values = new JsArray<>();
    Object value = new Object();
    values.push(value);
    map.set(key, values);
    assertSame(values, map.get(key));
    assertSame(value, map.get(key).getAt(0));
    var iterator = map.values();
    var first = iterator.next();
    assertFalse(first.isDone());
    assertSame(values, first.getValue());
    var end = iterator.next();
    assertTrue(end.isDone());
    assertNull(end.getValue());
    map.delete(key);
    assertNull(map.get(key));
    JsMap<String, String> strings = new JsMap<>();
    strings.set("key", "value");
    assertEquals("value", strings.get("key"));
    assertNull(strings.get("missing"));
    JsArray<String> names = new JsArray<>();
    names.push("first", "second");
    assertEquals("second", names.find((name, index, array) -> name.equals("second")));
    assertNull(names.find((name, index, array) -> false));
    assertEquals("second", names.pop());
  }
}
