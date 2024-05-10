package org.yk.nextail.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.cart.CartItem;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.Arrays;
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
        cartItems.add(cartItem);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public Double getCartTotal() {
        if (evaluatePricingRuleConditions()) {
            // FIXME: Apply pricing rules actions
            applyPricingRuleActions();
        }
        return cartItems.stream().mapToDouble(ci -> ci.getPrice()).sum();
    }

    /**
     * Evaluates pricing rule conditions against current cart items.
     * NOTE: currently there's a limitation for composed conditions, which are being evaluated using AND operator
     *
     * @return
     */
    private boolean evaluatePricingRuleConditions() {
        Arrays.stream(PricingRule.PriceRuleCondition.PriceRuleConditionType.values()).forEach(prc -> {
//            LOG.info("PRC: " + prc);
        });

        Optional<PricingRule.PriceRuleCondition<?>> productCodeConditionOptional = pricingRules.stream()
                .flatMap(pr -> pr.getConditions().stream())
                .filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_CODE))
                .findFirst();

        List<CartItem> filteredCartItems = cartItems;

        // 1. Apply first PRODUCT_CODE condition when present
        if (productCodeConditionOptional.isPresent()) {
            LOG.info("Found PRODUCT_CODE condition, evaluating...");
            PricingRule.PriceRuleCondition<?> productCodeCondition = productCodeConditionOptional.get();
            LOG.info(productCodeCondition.toString());

            // FIXME: Honor condition operator
            filteredCartItems = cartItems.stream()
                    .filter(ci -> productCodeCondition.getConditionValue().equals(ci.getCode()))
                    .collect(Collectors.toList());

        }

        Optional<PricingRule.PriceRuleCondition<?>> quantityInCheckoutConditionOptional = pricingRules.stream()
                .flatMap(pr -> pr.getConditions().stream())
                .filter(c -> c.getConditionType().equals(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_QUANTITY_IN_CHECKOUT))
                .findFirst();
        if (quantityInCheckoutConditionOptional.isPresent()) {
            LOG.info("Found PRODUCT_QUANTITY_IN_CHECKOUT condition, evaluating...");
            PricingRule.PriceRuleCondition<?> quantityInCheckoutCondition = quantityInCheckoutConditionOptional.get();
            if ((Integer) quantityInCheckoutCondition.getConditionValue() == filteredCartItems.size()) {
                return true;
            }
            LOG.info(quantityInCheckoutCondition.toString());
        }

//        pricingRules.stream().filter(pr -> pr.getConditions().stream().filter()
        return false;
    }

    private void applyPricingRuleActions() {

    }

}