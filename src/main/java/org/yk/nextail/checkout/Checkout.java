package org.yk.nextail.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Checkout {
    private static final Logger LOG = LoggerFactory.getLogger(Checkout.class);
    private final List<CartItem> cartItems;
    private final List<PricingRule> pricingRules;

    // TODO: Likely to be redesigned in order to not have to pass pricing rules at this stage
    public Checkout(List<PricingRule> pricingRules) {
        this.cartItems = new ArrayList<>();
        this.pricingRules = pricingRules;
    }

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
        List<CartItem> filteredCartItems = cartItems;

        // 1. Firstly, evaluate PRODUCT_CODE condition when present in order to trim down the stream
        Optional<PricingRule.PriceRuleCondition<?>> productCodeConditionOptional = pricingRules.stream()
                .flatMap(pr -> pr.getConditions().stream())
                .filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_CODE))
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
        Optional<PricingRule.PriceRuleCondition<?>> quantityInCheckoutConditionOptional = pricingRules.stream()
                .flatMap(pr -> pr.getConditions().stream())
                .filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_QUANTITY_IN_CHECKOUT))
                .findFirst();
        if (!filteredCartItems.isEmpty() && quantityInCheckoutConditionOptional.isPresent()) {
            LOG.info("Evaluating PRODUCT_QUANTITY_IN_CHECKOUT condition...");
            PricingRule.PriceRuleCondition<Integer> quantityInCheckoutCondition = (PricingRule.PriceRuleCondition<Integer>) quantityInCheckoutConditionOptional.get();

            // FIXME: Honor condition operator
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
            applyPricingRuleActions(filteredCartItems);
        }
    }

    private void applyPricingRuleActions(List<CartItem> cartItems) {
        pricingRules.stream().flatMap(pr -> pr.getActions().stream()).forEach(pra -> {
            switch (pra.getPriceRuleActionType()) {
                case PRODUCT_FIXED_PRICE -> {
                    LOG.info("Applying PRODUCT_FIXED_PRICE action rule [" + pra.getValue() + "]");
                    if (pra.getValue() instanceof Number) {
                        cartItems.forEach(c -> c.setDiscount(c.getPrice().doubleValue() - (pra.getValue()).doubleValue()));
                    } else {
                        // TODO: Handle error
                    }
                }
                case PRODUCT_DISCOUNT_PERCENT -> {
                    LOG.info("Applying PRODUCT_DISCOUNT_PERCENT action rule [" + pra.getValue() + "]");
                    // TODO: Not yet implemented
                }
                default -> {
                    // TODO: Handle error
                }
            }
        });
    }
}