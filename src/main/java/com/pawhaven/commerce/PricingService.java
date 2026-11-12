package com.pawhaven.commerce;

import com.pawhaven.patterns.decorator.*;
import com.pawhaven.patterns.strategy.DiscountStrategy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private final Map<String, DiscountStrategy> discounts;

    public PricingService(List<DiscountStrategy> strategies) {
        discounts = strategies.stream().collect(Collectors.toUnmodifiableMap(DiscountStrategy::code, Function.identity()));
    }

    public PriceBreakdown quote(BigDecimal subtotal, String promoCode, boolean giftWrapping, BigDecimal donation) {
        String code = promoCode == null ? "" : promoCode.strip().toUpperCase(Locale.ROOT);
        DiscountStrategy strategy = discounts.get(code);
        if (strategy == null) throw new CommerceException("That promotional code is not valid.");
        CheckoutPriceComponent price = new BaseCheckoutPrice(subtotal, strategy.discount(subtotal));
        if (giftWrapping) price = new GiftWrappingDecorator(price);
        BigDecimal contribution = Money.donation(donation);
        if (contribution.signum() > 0) price = new ShelterDonationDecorator(price, contribution);
        return price.price();
    }
}
