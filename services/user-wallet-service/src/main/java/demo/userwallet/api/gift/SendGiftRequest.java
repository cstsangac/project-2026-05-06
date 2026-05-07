package demo.userwallet.api.gift;

import jakarta.validation.constraints.NotBlank;

public record SendGiftRequest(@NotBlank String giftType, String clientRequestId) {}

