package com.pawhaven.commerce;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Issues a local receipt reference for a simulated payment. */
@Service
public class DemoPaymentService {
    public UUID simulate(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) throw new CommerceException("A demo payment requires a valid total.");
        return UUID.randomUUID();
    }
}
