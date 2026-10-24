package io.instanto.widgets.processor;

import static org.junit.Assert.*;

import org.junit.Test;

public class CssSupportTest {
  @Test
  public void onlySelectorsChangeIncludingNestedRulesAndExternalNames() {
    String css =
        "@external external, plugin-*; /* heading */ @media screen { .button:hover {content:"
            + " '.button  text';background:url(x.button.png);} } .external,.plugin-item"
            + " {color:red;}";
    var styles = CssSupport.scope(css, "owner-");
    assertEquals("owner-button", styles.classes().get("button"));
    assertEquals("external", styles.classes().get("external"));
    assertEquals("plugin-item", styles.classes().get("plugin-item"));
    assertTrue(styles.css().contains(".owner-button:hover"));
    assertTrue(styles.css().contains("content: '.button  text'"));
    assertTrue(styles.css().contains("url(x.button.png)"));
    assertFalse(styles.css().contains("@external"));
  }

  @Test
  public void directivesInsideCssStringsAndCommentsRemainLiteral() {
    var scoped =
        CssSupport.scope(
            "/* @external button; */ .button {content:'@external button;';}", "scope-");
    assertEquals("scope-button", scoped.classes().get("button"));
    assertTrue(scoped.css().contains("/* @external button; */"));
    assertTrue(scoped.css().contains("content:'@external button;'"));
  }

  @Test
  public void qualifiedPrefixesDoNotCollideAcrossPackagesOrPunctuation() {
    assertNotEquals(SourceSupport.prefix("one.View.style"), SourceSupport.prefix("two.View.style"));
    assertNotEquals(SourceSupport.prefix("a.b_C.style"), SourceSupport.prefix("a_b.C.style"));
  }
}
