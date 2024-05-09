package org.yk.nextail.checkout;

import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PriceRule;

import java.util.ArrayList;
import java.util.List;

public class Checkout {

    private final List<CartItem> cartItems;

    public Checkout(PriceRule pricesRule) {
        this.cartItems = new ArrayList<>();
    }

    public void scan(CartItem cartItem) {
        cartItems.add(cartItem);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

}
