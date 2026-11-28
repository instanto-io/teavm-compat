package io.instanto.compat;

import static org.junit.Assert.*;

import io.instanto.compat.boxed.BoxedParameters;
import io.instanto.compat.boxed.BoxedProbe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

/**
 * Boxed numbers and booleans in native JsInterop declarations reach JavaScript as primitives, as
 * they do under GWT. The declarations are adapted from GWT source by the binding generator.
 */
@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class BoxedNativeValuesTest {
  @Before
  public void defineProbe() {
    install();
  }

  @Test
  public void boxedArgumentsArriveAsPrimitives() {
    BoxedProbe probe = new BoxedProbe();
    probe.take(0.03);
    assertEquals("number", lastType(probe));
    assertEquals(0.03, lastNumber(probe), 0);
    probe.takeNumber(Integer.valueOf(7));
    assertEquals("number", lastType(probe));
    assertEquals(7, lastNumber(probe), 0);
    probe.takeBoolean(Boolean.TRUE);
    assertEquals("boolean", lastType(probe));
    probe.take(null);
    assertEquals("object", lastType(probe));
    assertTrue(lastIsNull(probe));
    assertEquals("number", BoxedProbe.typeOf(2.5));
  }

  @Test
  public void boxedResultsComeBackAsJavaBoxes() {
    BoxedProbe probe = new BoxedProbe();
    Double ratio = probe.ratio();
    assertEquals(Double.valueOf(0.5), ratio);
    Number count = probe.count();
    assertEquals(3, count.intValue());
    assertEquals(Boolean.TRUE, probe.visible());
    assertNull(probe.missing());
  }

  @Test
  public void boxedPropertiesAreStoredAsPrimitives() {
    BoxedProbe probe = new BoxedProbe();
    probe.setSize(0.03);
    assertEquals("number", propertyType(probe, "size"));
    assertEquals(Double.valueOf(0.03), probe.getSize());

    BoxedParameters parameters = newObject();
    assertNull(parameters.getSize());
    parameters.setSize(0.03);
    parameters.setTransparent(true);
    assertEquals("number", propertyType(parameters, "size"));
    assertEquals("boolean", propertyType(parameters, "transparent"));
    assertEquals(0.03, parameters.getSize().doubleValue(), 0);
    assertEquals(Boolean.TRUE, parameters.getTransparent());
  }

  @JSBody(
      script =
          "globalThis.BoxedProbe = function() {};"
              + "BoxedProbe.typeOf = function(v) { return typeof v; };"
              + "BoxedProbe.prototype.take = function(v) { this.last = v; };"
              + "BoxedProbe.prototype.takeNumber = BoxedProbe.prototype.take;"
              + "BoxedProbe.prototype.takeBoolean = BoxedProbe.prototype.take;"
              + "BoxedProbe.prototype.ratio = function() { return 0.5; };"
              + "BoxedProbe.prototype.count = function() { return 3; };"
              + "BoxedProbe.prototype.visible = function() { return true; };"
              + "BoxedProbe.prototype.missing = function() { return undefined; };")
  private static native void install();

  @JSBody(params = "probe", script = "return typeof probe.last;")
  private static native String lastType(BoxedProbe probe);

  @JSBody(params = "probe", script = "return probe.last;")
  private static native double lastNumber(BoxedProbe probe);

  @JSBody(params = "probe", script = "return probe.last === null;")
  private static native boolean lastIsNull(BoxedProbe probe);

  @JSBody(
      params = {"target", "name"},
      script = "return typeof target[name];")
  private static native String propertyType(JSObject target, String name);

  @JSBody(script = "return {};")
  private static native BoxedParameters newObject();
}
