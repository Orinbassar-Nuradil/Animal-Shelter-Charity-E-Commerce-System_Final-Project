package com.pawhaven.web.commerce;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Formats whole tenge compactly while preserving tiyn when a discount includes them. */
@Component("kzt")
public class Kzt {
    public String format(BigDecimal amount) {
        NumberFormat number = NumberFormat.getNumberInstance(Locale.ENGLISH);
        number.setMaximumFractionDigits(2);
        return number.format(amount) + " ₸";
    }
}
