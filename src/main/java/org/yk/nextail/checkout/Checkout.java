package org.yk.nextail.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class representing the current state of the checkout process,
 * handling cart items operations (currently only scan)
 */
public class Checkout {
    private static final Logger LOG = LoggerFactory.getLogger(Checkout.class);
    private final List<CartItem> cartItems;
    private final List<PricingRule> pricingRules;

    // FIXME: Likely to be redesigned in order to not have to pass pricing rules at this stage
    public Checkout(List<PricingRule> pricingRules) {
        this.cartItems = new ArrayList<>();
        this.pricingRules = pricingRules;
    }

    /**
     * Scans a new cart item and adds it to the checkout process,
     * then evaluates and applies pricing rules
     *
     * @param cartItem The cart item to be added into Checkout
     */
    public synchronized void scan(CartItem cartItem) {
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
            if (pr.getConditions() == null || (pr.getConditions() == null && pr.getActions() == null)) {
                return;
            }

            // 1. Firstly, evaluate CART_ITEM_CODE condition when present in order to trim down the stream
            Optional<PricingRule.PriceRuleCondition<?>> productCodeConditionOptional =
                    pr.getConditions().stream().filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_CODE))
                            .findFirst();
            if (productCodeConditionOptional.isPresent()) {
                LOG.info("Evaluating CART_ITEM_CODE condition...");
                PricingRule.PriceRuleCondition<String> productCodeCondition = (PricingRule.PriceRuleCondition<String>) productCodeConditionOptional.get();


                filteredCartItems = cartItems.stream()
                        .filter(ci -> productCodeCondition.evalCondition(ci.getCode()))
                        .collect(Collectors.toList());
                if (!filteredCartItems.isEmpty()) {
                    LOG.info("Found " + filteredCartItems.size() + " cart item occurrence/s for CART_ITEM_CODE condition ["
                            + productCodeCondition.getConditionOperator().name()
                            + " " + productCodeCondition.getConditionValue()
                            + "]");
                }
            }

            // 2. Secondly, evaluate CART_ITEM_QUANTITY_TOTAL condition when present,
            // using all cart items OR the ones filtered by previous conditions
            Optional<PricingRule.PriceRuleCondition<?>> cartItemQuantityTotalConditionOptional =
                    pr.getConditions().stream().filter(c ->
                                    c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_QUANTITY_TOTAL))
                            .findFirst();
            if (!filteredCartItems.isEmpty() && cartItemQuantityTotalConditionOptional.isPresent()) {
                LOG.info("Evaluating CART_ITEM_QUANTITY_TOTAL condition...");
                PricingRule.PriceRuleCondition<Integer> cartItemQuantityTotalCondition =
                        (PricingRule.PriceRuleCondition<Integer>) cartItemQuantityTotalConditionOptional.get();

                if (cartItemQuantityTotalCondition.evalCondition(filteredCartItems.size())) {
                    LOG.info("Found " + filteredCartItems.size() + " cart item occurrence/s for CART_ITEM_QUANTITY_TOTAL condition ["
                            + cartItemQuantityTotalCondition.getConditionOperator().name()
                            + " " + cartItemQuantityTotalCondition.getConditionValue()
                            + "]");
                } else {
                    filteredCartItems = new ArrayList<>();
                }
            }

            // 3. Thirdly, evaluate CART_ITEM_X_QUANTITY condition when present,
            // using all cart items OR the ones filtered by previous conditions
            Optional<PricingRule.PriceRuleCondition<?>> cartItemXQuantityConditionOptional =
                    pr.getConditions().stream().filter(c ->
                                    c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_X_QUANTITY))
                            .findFirst();
            if (!filteredCartItems.isEmpty() && cartItemXQuantityConditionOptional.isPresent()) {
                LOG.info("Evaluating CART_ITEM_X_QUANTITY condition...");
                PricingRule.PriceRuleCondition<Integer> cartItemXQuantityCondition =
                        (PricingRule.PriceRuleCondition<Integer>) cartItemXQuantityConditionOptional.get();

                Integer cartItemXQuantity = Math.round(
                        (filteredCartItems.size() / cartItemXQuantityCondition.getConditionValue())
                );
                LOG.debug("Cart item X quantity = " + cartItemXQuantity);
                if (cartItemXQuantity > 0) {
                    Integer cartItemSize = cartItemXQuantityCondition.getConditionValue() - cartItemXQuantity;
                    LOG.info("Found " + cartItemSize
                            + " cart item occurrence/s for CART_ITEM_X_QUANTITY condition ["
                            + cartItemXQuantityCondition.getConditionOperator().name()
                            + " " + cartItemXQuantityCondition.getConditionValue()
                            + "]");

                    filteredCartItems = filteredCartItems.subList(0, cartItemSize);
                    LOG.debug("Cart items filtered size = " + filteredCartItems.size());
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
     * @param filteredCartItems        The cart items filtered by the conditions evaluation
     * @param priceRuleActions list of actions to apply
     */
    private void applyPricingRuleActions(List<CartItem> filteredCartItems, List<PricingRule.PriceRuleAction<?>> priceRuleActions) {
        // FIXME: possible incompatibilities between different actions when defined for the same pricing rule
        priceRuleActions.forEach(pra -> {
            switch (pra.getPriceRuleActionType()) {
                case CART_ITEM_FIXED_PRICE -> {
                    LOG.info("Applying CART_ITEM_FIXED_PRICE action rule [" + pra.getValue() + "]");
                    if (pra.getValue() instanceof Number) {
                        filteredCartItems.forEach(ci -> {
                            Double discount = ci.getPrice().doubleValue() - (pra.getValue()).doubleValue();
                            ci.setDiscount(ci.getDiscount() + discount);
                            LOG.info("Applied CART_ITEM_FIXED_PRICE action rule discount = " + discount
                                    + " for cart item = " + ci.getCode());
                        });
                    } else {
                        // TODO: Handle error
                    }
                }
                case CART_ITEM_DISCOUNT_PERCENT -> {
                    LOG.info("Applying CART_ITEM_DISCOUNT_PERCENT action rule using discount percentage = " + pra.getValue() + "%");
                    if (pra.getValue() instanceof Number) {
                        filteredCartItems.forEach(ci -> {
                            Double discount = (ci.getPrice().doubleValue()) * (pra.getValue().doubleValue() / 100);
                            LOG.info("Applied CART_ITEM_DISCOUNT_PERCENT action rule discount = " + discount
                                    + " for cart item = " + ci.getCode());
                            ci.setDiscount(ci.getDiscount() + discount);
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