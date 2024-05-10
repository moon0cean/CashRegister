package org.yk.nextail.checkout;

import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.List;

public class Checkout {

    private final List<CartItem> cartItems;

    private final PricingRule priceRule;

    public Checkout(PricingRule pricesRule) {
        this.cartItems = new ArrayList<>();
        this.priceRule = pricesRule;
    }

    public void scan(CartItem cartItem) {
        cartItems.add(cartItem);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public Double getCartTotal() {
        // FIXME: Apply price rules
        return cartItems.stream().mapToDouble(ci -> ci.getPrice()).sum();
    }

}
