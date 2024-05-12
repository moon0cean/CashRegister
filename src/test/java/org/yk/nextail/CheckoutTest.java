package org.yk.nextail;

import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.cart.CartItem;
import org.yk.nextail.checkout.Checkout;
import org.yk.nextail.price.PricingRule;

import java.util.ArrayList;
import java.util.List;

import static org.yk.nextail.CheckoutTest.PredefinedCartItems.PANTS;
import static org.yk.nextail.CheckoutTest.PredefinedCartItems.TSHIRT;
import static org.yk.nextail.CheckoutTest.PredefinedCartItems.VOUCHER;

public class CheckoutTest {
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutTest.class);
    private final static List<PricingRule> priceRulesList = new ArrayList<>();
    private final static List<List<CartItem>> checkoutItemList = new ArrayList<>();
    protected enum PredefinedCartItems {
        // TODO: Consider using a Factory here
        VOUCHER,
        TSHIRT,
        PANTS;

        public CartItem getCartItem() {
            if (this.equals(VOUCHER)) {
                return new CartItem("VOUCHER", "Gift Card", 5.00, "€");
            } else if (this.equals(TSHIRT)) {
                return new CartItem("TSHIRT", "Summer T-Shirt", 20.00, "€");
            } else if (this.equals(PANTS)) {
                return new CartItem("PANTS", "Summer Pants", 7.50, "€");
            }
            return null;
        }
    }

    @BeforeClass
    public static void init() {
        LOG.info("Initializing Nextail Cash-Register Checkout test data");
        createPriceRules();
        populateCheckout();
    }


    private static void createPriceRules() {
        // Unit price 19€ for TSHIRT items when cart has >= 3 cart items of that type
        List<PricingRule.PriceRuleCondition<?>> conditions = List.of(
                new PricingRule.PriceRuleCondition.Builder<String>()
                        .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                        .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_CODE)
                        .addConditionValue("TSHIRT")
                        .build(),
                new PricingRule.PriceRuleCondition.Builder<Integer>()
                        .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.GREATER_THAN_EQUALS)
                        .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_QUANTITY_IN_CHECKOUT)
                        .addConditionValue(3)
                        .build()
        );
        List<PricingRule.PriceRuleAction<?>> actions = List.of(
                new PricingRule.PriceRuleAction.Builder()
                        .addActionType(PricingRule.PriceRuleAction.PriceRuleActionType.PRODUCT_FIXED_PRICE)
                        .addActionValue(19.00)
                        .build()
        );
        priceRulesList.add(new PricingRule(conditions, actions));

        // 2-for-1 pricing rule for VOUCHER
        conditions = List.of(
                new PricingRule.PriceRuleCondition.Builder<String>()
                        .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                        .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.PRODUCT_CODE)
                        .addConditionValue("VOUCHER")
                        .build()
        );
        actions = List.of(
                new PricingRule.PriceRuleAction.Builder()
                        .addActionType(PricingRule.PriceRuleAction.PriceRuleActionType.PRODUCT_DISCOUNT_PERCENT)
                        .addActionValue(50)
                        .build()
        );
        priceRulesList.add(new PricingRule(conditions, actions));
    }

    private static void populateCheckout() {
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), PANTS.getCartItem())); // Example 1
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), VOUCHER.getCartItem())); // Example 2
        checkoutItemList.add(List.of(TSHIRT.getCartItem(), TSHIRT.getCartItem(), TSHIRT.getCartItem(),
                VOUCHER.getCartItem(), TSHIRT.getCartItem())); // Example 3
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), VOUCHER.getCartItem(),
                VOUCHER.getCartItem(), PANTS.getCartItem(), TSHIRT.getCartItem(), TSHIRT.getCartItem())); // Example 4
    }

    @Test
    public void usingExample1PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        LOG.info("Example 1 init");
        final Checkout checkoutExample1 = new Checkout(priceRulesList);
        checkoutItemList.get(0).forEach(coi -> checkoutExample1.scan(coi));
        MatcherAssert.assertThat("Example 1: cart total amount expected 32.5€; cart total = " + checkoutExample1.getCartTotal(),
                checkoutExample1.getCartTotal().compareTo(32.5) == 0);
        LOG.info("Example 1 end");
    }

    @Test
    public void usingExample2PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        LOG.info("Example 2 init");
        final Checkout checkoutExample2 = new Checkout(priceRulesList);
        checkoutItemList.get(1).forEach(coi -> checkoutExample2.scan(coi));
        MatcherAssert.assertThat("Example 2: total amount expected 25.00€; cart total = " + checkoutExample2.getCartTotal(),
                checkoutExample2.getCartTotal().compareTo(25.00) == 0);
        LOG.info("Example 2 end");
    }

    @Test
    public void usingExample3PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        LOG.info("Example 3 init");
        final Checkout checkoutExample3 = new Checkout(priceRulesList);
        checkoutItemList.get(2).forEach(coi -> checkoutExample3.scan(coi));
        MatcherAssert.assertThat("Example 3: total amount expected 81.00€; cart total = " + checkoutExample3.getCartTotal(),
                checkoutExample3.getCartTotal().compareTo(81.00) == 0);
        LOG.info("Example 3 end");
    }

    @Test
    public void usingExample4PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        LOG.info("Example 4 init");
        final Checkout checkoutExample4 = new Checkout(priceRulesList);
        checkoutItemList.get(3).forEach(coi -> checkoutExample4.scan(coi));
        MatcherAssert.assertThat("Example 4: total amount expected 74.50€; cart total = " + checkoutExample4.getCartTotal(),
                checkoutExample4.getCartTotal().compareTo(74.5) == 0);
        LOG.info("Example 4 end");
    }

}
