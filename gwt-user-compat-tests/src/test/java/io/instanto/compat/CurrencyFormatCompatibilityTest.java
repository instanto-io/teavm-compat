package io.instanto.compat;

import static org.junit.Assert.*;

import org.gwtproject.i18n.client.NumberFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
public class CurrencyFormatCompatibilityTest {
  @Test
  public void currencyFormattingRoundTripsUsingItsNativePrecision() {
    for (String code : new String[] {"USD", "EUR", "GBP", "OMR"}) {
      NumberFormat format = NumberFormat.getCurrencyFormat(code);
      assertEquals(code, 1234.5, format.parse(format.format(1234.5)), 0.001);
      assertEquals(code, -1234.5, format.parse(format.format(-1234.5)), 0.001);
    }
    NumberFormat yen = NumberFormat.getCurrencyFormat("JPY");
    assertEquals(1235, yen.parse(yen.format(1234.6)), 0);
    NumberFormat rial = NumberFormat.getCurrencyFormat("OMR");
    assertEquals(1.234, rial.parse(rial.format(1.234)), 0.0001);
  }

  @Test
  public void malformedCurrencyInputIsRejected() {
    try {
      NumberFormat.getCurrencyFormat("US");
      fail("Invalid currency code accepted");
    } catch (IllegalArgumentException expected) {
    }
    try {
      NumberFormat.getCurrencyFormat("USD").parse("hello 12 dollars");
      fail("Invalid amount accepted");
    } catch (NumberFormatException expected) {
    }
  }
}
