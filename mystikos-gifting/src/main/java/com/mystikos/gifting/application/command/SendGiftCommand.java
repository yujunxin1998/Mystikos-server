package com.mystikos.gifting.application.command;

public record SendGiftCommand(
        Long patronId,
        Long companionId,
        Long giftId,
        int quantity
) {
}
