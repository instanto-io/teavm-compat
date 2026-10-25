package io.instanto.widgets.processor;

import static org.junit.Assert.*;

import java.util.List;
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

  @Test(timeout = 5000)
  public void unterminatedDirectivesDoNotCauseQuadraticBacktracking() {
    String css = "@external ".repeat(20000);
    assertEquals(css, CssSupport.scope(css, "p-").css());
  }

  @Test
  public void urlsIgnoreCommentsAndStringsAndAllowQuotedParentheses() {
    String css =
        "/* url(unclosed */ a {content:'url(hidden)';background:url( 'a(b).png' );src:url(font.woff)}";
    assertEquals(
        List.of("a(b).png", "font.woff"),
        CssSupport.urls(css).stream().map(CssSupport.Url::value).toList());
  }

  @Test
  public void malformedAndUnterminatedUrlsRemainUnchanged() {
    assertTrue(CssSupport.urls("url( 'missing-close' ").isEmpty());
    assertTrue(CssSupport.urls("url( ").isEmpty());
    assertTrue(CssSupport.urls("url( )").isEmpty());
    assertTrue(CssSupport.urls("url('unterminated)").isEmpty());
  }

  @Test
  public void escapedQuotesAndUnterminatedLiteralsDoNotExposeCssCode() {
    String quoted = "'a\\'b'";
    for (boolean code : CssSupport.codePositions(quoted)) assertFalse(code);
    for (boolean code : CssSupport.codePositions("/* unfinished")) assertFalse(code);
    assertEquals(
        ".p-item {content:'unfinished", CssSupport.scope(".item {content:'unfinished", "p-").css());
  }
}
