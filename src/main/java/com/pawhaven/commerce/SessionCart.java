package com.pawhaven.commerce;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.util.WebUtils;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/** Session state contains only product IDs and quantities. Prices always come from the database. */
public class SessionCart implements Serializable {
    private static final String ATTRIBUTE = SessionCart.class.getName();
    private final Map<Long, Integer> quantities = new TreeMap<>();
    private String token = UUID.randomUUID().toString();

    public static SessionCart peek(HttpSession session) {
        return session == null ? null : (SessionCart) session.getAttribute(ATTRIBUTE);
    }

    public static SessionCart from(HttpSession session) {
        synchronized (WebUtils.getSessionMutex(session)) {
            SessionCart cart = (SessionCart) session.getAttribute(ATTRIBUTE);
            if (cart == null) {
                cart = new SessionCart();
                session.setAttribute(ATTRIBUTE, cart);
            }
            return cart;
        }
    }

    public synchronized int getItemCount() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public synchronized String getToken() { return token; }

    synchronized Map<Long, Integer> snapshot() {
        // Visit IDs in the same order at every checkout to avoid crossing product locks.
        return new TreeMap<>(quantities);
    }

    synchronized int quantity(long id) { return quantities.getOrDefault(id, 0); }

    synchronized void put(long id, int value) {
        quantities.put(id, value);
        rotateToken();
    }

    synchronized void remove(long id) {
        if (quantities.remove(id) != null) rotateToken();
    }

    public synchronized void clear() {
        quantities.clear();
        rotateToken();
    }

    private void rotateToken() { token = UUID.randomUUID().toString(); }
}
