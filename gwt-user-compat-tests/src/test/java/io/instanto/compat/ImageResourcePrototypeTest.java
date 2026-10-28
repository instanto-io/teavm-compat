package io.instanto.compat;

import static org.junit.Assert.*;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.shared.UriUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.*;

@RunWith(TeaVMTestRunner.class)
public class ImageResourcePrototypeTest {
  @Test
  public void preservesSpriteMarkerAndMetadata() {
    var uri = UriUtils.fromSafeConstant("images/sprites.png");
    ImageResource image =
        new ImageResourcePrototype.Bundle("icon", uri, 12, 24, 32, 18, false, true);
    assertTrue(image instanceof ImageResourcePrototype.Bundle);
    assertSame(uri, image.getSafeUri());
    assertEquals("images/sprites.png", image.getURL());
    assertEquals("icon", image.getName());
    assertEquals(12, image.getLeft());
    assertEquals(24, image.getTop());
    assertEquals(32, image.getWidth());
    assertEquals(18, image.getHeight());
    assertFalse(image.isAnimated());
    assertTrue(((ImageResourcePrototype) image).isLossy());
    ImageResource plain = new ImageResourcePrototype("plain", uri, 0, 0, 6, 7, true, false);
    assertFalse(plain instanceof ImageResourcePrototype.Bundle);
    assertTrue(plain.isAnimated());
    assertFalse(((ImageResourcePrototype) plain).isLossy());
  }
}
