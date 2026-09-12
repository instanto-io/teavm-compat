package io.instanto.bootstrap.testing.contracts;

final class ContractAssertions {
  private ContractAssertions() {}

  static void equal(Object expected, Object actual, String message) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
    }
  }

  static void same(Object expected, Object actual, String message) {
    if (expected != actual) {
      throw new AssertionError(message + ": expected the same instance");
    }
  }

  static void isTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
