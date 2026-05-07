package demo.userwallet.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
  private Money() {}

  public static final int SCALE = 2;

  public static BigDecimal of(String s) {
    return new BigDecimal(s).setScale(SCALE, RoundingMode.UNNECESSARY);
  }

  public static BigDecimal normalize(BigDecimal v) {
    if (v == null) {
      return null;
    }
    return v.setScale(SCALE, RoundingMode.HALF_UP);
  }
}

