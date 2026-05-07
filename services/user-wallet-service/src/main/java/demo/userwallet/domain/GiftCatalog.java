package demo.userwallet.domain;

import java.math.BigDecimal;
import java.util.Map;

public final class GiftCatalog {
  private GiftCatalog() {}

  // MVP: static catalog. Prices are decimal amounts with scale=2.
  public static final Map<String, BigDecimal> PRICES =
      Map.of(
          "ROSE", Money.of("1.00"),
          "COFFEE", Money.of("5.00"),
          "DIAMOND", Money.of("20.00"));

  public static BigDecimal price(String giftType) {
    BigDecimal price = PRICES.get(giftType);
    if (price == null) {
      throw new IllegalArgumentException("Unknown giftType: " + giftType);
    }
    return price;
  }
}

