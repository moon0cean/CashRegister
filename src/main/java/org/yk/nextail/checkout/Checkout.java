package org.yk.nextail.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Checkout {
    private static final Logger LOG = LoggerFactory.getLogger(Checkout.class);
    private final List<CartItem> cartItems;
    private final List<PricingRule> pricingRules;

    // TODO: Likely to be redesigned in order to not have to pass pricing rules at this stage
    public Checkout(List<PricingRule> pricingRules) {
        this.cartItems = new ArrayList<>();
        this.pricingRules = pricingRules;
    }

    /**
     * Scans a new cart item and adds it to the checkout process, then evaluates and applies pricing rules
     *
     * @param cartItem The cart item to be added into Checkout
     */
    public void scan(CartItem cartItem) {
        // FIXME: consider concurrency
        LOG.info("Scanning new cart item [" + cartItem.getCode() + "]");
        cartItems.add(cartItem);
        evaluateAndApplyPricingRules();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public Double getCartTotal() {
        return cartItems.stream().mapToDouble(ci -> ci.getPrice() - ((ci.getDiscount() != null) ? ci.getDiscount() : 0.00)).sum();
    }

    /**
     * Evaluates pricing rule conditions against current cart items.
     * NOTE: currently there's a limitation for composed conditions,
     * which are always being evaluated using AND operator
     *
     * @return
     */
    private void evaluateAndApplyPricingRules() {
        pricingRules.stream().forEach(pr -> {
            List<CartItem> filteredCartItems = cartItems;

            // 1. Firstly, evaluate PRODUCT_CODE condition when present in order to trim down the stream
            Optional<PricingRule.PriceRuleCondition<?>> productCodeConditionOptional =
                    pr.getConditions().stream().filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_CODE))
                            .findFirst();
            if (productCodeConditionOptional.isPresent()) {
                LOG.info("Evaluating PRODUCT_CODE condition...");
                PricingRule.PriceRuleCondition<String> productCodeCondition = (PricingRule.PriceRuleCondition<String>) productCodeConditionOptional.get();


                filteredCartItems = cartItems.stream()
                        .filter(ci -> productCodeCondition.evalCondition(ci.getCode()))
                        .collect(Collectors.toList());
                if (!filteredCartItems.isEmpty()) {
                    LOG.info("Found " + filteredCartItems.size() + " cart item occurrence/s for PRODUCT_CODE condition ["
                            + productCodeCondition.getConditionOperator().name()
                            + " " + productCodeCondition.getConditionValue()
                            + "]");
                }
            }

            // 2. Secondly, evaluate PRODUCT_QUANTITY_IN_CHECKOUT condition when present,
            // using all cart items OR the ones filtered by the previous condition
            Optional<PricingRule.PriceRuleCondition<?>> quantityInCheckoutConditionOptional =
                    pr.getConditions().stream().filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_QUANTITY_TOTAL))
                            .findFirst();
            if (!filteredCartItems.isEmpty() && quantityInCheckoutConditionOptional.isPresent()) {
                LOG.info("Evaluating PRODUCT_QUANTITY_IN_CHECKOUT condition...");
                PricingRule.PriceRuleCondition<Integer> quantityInCheckoutCondition = (PricingRule.PriceRuleCondition<Integer>) quantityInCheckoutConditionOptional.get();

                if (quantityInCheckoutCondition.evalCondition(filteredCartItems.size())) {
                    LOG.info("Found " + filteredCartItems.size() + " cart item occurrence/s for PRODUCT_QUANTITY_IN_CHECKOUT condition ["
                            + quantityInCheckoutCondition.getConditionOperator().name()
                            + " " + quantityInCheckoutCondition.getConditionValue()
                            + "]");
                } else {
                    filteredCartItems = new ArrayList<>();
                }
            }

            if (!filteredCartItems.isEmpty()) {
                applyPricingRuleActions(filteredCartItems, pr.getActions());
            }
        });
    }

    /**
     * Apply rule actions for a given list of cart items
     *
     * @param cartItems        The cart items filtered by the conditions evaluation
     * @param priceRuleActions list of actions to apply
     */
    private void applyPricingRuleActions(List<CartItem> cartItems, List<PricingRule.PriceRuleAction<?>> priceRuleActions) {
        priceRuleActions.forEach(pra -> {
            switch (pra.getPriceRuleActionType()) {
                case CART_ITEM_FIXED_PRICE -> {
                    LOG.info("Applying CART_ITEM_FIXED_PRICE action rule [" + pra.getValue() + "]");
                    if (pra.getValue() instanceof Number) {
                        cartItems.forEach(ci -> {
                            Double discount = ci.getPrice().doubleValue() - (pra.getValue()).doubleValue();
                            ci.setDiscount(discount);
                            LOG.info("Applied CART_ITEM_FIXED_PRICE action rule discount = " + discount
                                    + " for cart item = " + ci.getCode());
                        });
                    } else {
                        // TODO: Handle error
                    }
                }
                case CART_ITEM_X_QUANTITY_DISCOUNT_PERCENT -> {
                    LOG.info("Applying CART_ITEM_X_QUANTITY_DISCOUNT_PERCENT action rule for quantity = " + pra.getQuantity()
                            + " and discount percent = " + pra.getValue() + "%");
                    if (pra.getValue() instanceof Number && (pra.getQuantity() != null && pra.getQuantity() > 0)) {
                        IntStream.range(0, cartItems.size() - 1).forEach(i -> {
                            if (i % pra.getQuantity() == 0) {
                                CartItem ci = cartItems.get(i);
                                Double discount = (ci.getPrice().doubleValue() * pra.getQuantity()) * (pra.getValue().doubleValue() / 100);
                                if (discount > ci.getPrice()) { // Never apply a discount greater than the price of the item
                                    discount = ci.getPrice();
                                }
                                LOG.info("Applied CART_ITEM_X_QUANTITY_DISCOUNT_PERCENT action rule discount = " + discount
                                        + " for cart item = " + ci.getCode());
                                ci.setDiscount(discount);
                            }
                        });
                    } else {
                        // TODO: Handle error
                    }
                }
                default -> {
                    // TODO: Handle error
                }
            }
        });
    }
}