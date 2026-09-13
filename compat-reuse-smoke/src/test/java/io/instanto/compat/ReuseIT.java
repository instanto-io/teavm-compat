package io.instanto.compat;

import static org.junit.Assert.*;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.FilePayload;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ReuseIT {
  @Parameterized.Parameters(name = "{0}")
  public static Object[] browsers() {
    return new Object[] {"chromium", "firefox", "webkit"};
  }

  private final String engine;

  public ReuseIT(String engine) {
    this.engine = engine;
  }

  @Test
  public void nativeApisWorkWithoutWidgets() throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    byte[] script = Files.readAllBytes(Path.of("target/site/showcase.js"));
    byte[] html =
        "<!doctype html><meta charset='utf-8'><body><script src='/showcase.js'></script><script>main();</script>"
            .getBytes(StandardCharsets.UTF_8);
    server.createContext(
        "/",
        exchange -> {
          boolean js = exchange.getRequestURI().getPath().equals("/showcase.js");
          byte[] bytes = js ? script : html;
          exchange
              .getResponseHeaders()
              .set("Content-Type", js ? "application/javascript" : "text/html; charset=utf-8");
          exchange.sendResponseHeaders(200, bytes.length);
          try (var output = exchange.getResponseBody()) {
            output.write(bytes);
          }
        });
    server.start();
    try (var playwright = Playwright.create()) {
      BrowserType type =
          switch (engine) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
          };
      try (var browser = type.launch(new BrowserType.LaunchOptions().setHeadless(true))) {
        var page = browser.newPage();
        List<String> errors = new ArrayList<>();
        page.onPageError(errors::add);
        page.navigate("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        page.waitForFunction("document.body.getAttribute('data-reuse') === 'ready'");
        for (String[] expected :
            new String[][] {
              {"data-storage", "passed"},
              {"data-date-parse", "passed"},
              {"data-intl", "passed"},
              {"data-spanish", "enero"},
              {"data-promise", "PROMISE-VALUE"},
              {"data-rejection", "rejected-value"},
              {"data-blob", "blob-value"}
            }) {
          page.waitForFunction(
              "p => document.querySelector('#browser-apis').getAttribute(p[0]) === p[1]",
              Arrays.asList(expected));
        }
        assertEquals("#4466cc", page.locator("svg rect").getAttribute("fill"));
        page.locator("#native-upload")
            .setInputFiles(
                new FilePayload(
                    "probe.txt", "text/plain", "uploaded text".getBytes(StandardCharsets.UTF_8)));
        page.waitForFunction(
            "document.querySelector('#browser-apis').getAttribute('data-file') === 'uploaded text'");
        page.locator("#push-history").click();
        assertTrue(page.url().endsWith("#detail"));
        page.goBack();
        page.waitForFunction(
            "document.querySelector('#browser-apis').getAttribute('data-history') === 'back'");
        assertTrue(errors.toString(), errors.isEmpty());
      }
    } finally {
      server.stop(0);
    }
  }
}
