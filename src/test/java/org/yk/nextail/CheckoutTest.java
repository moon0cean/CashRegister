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

public class CheckoutTest {
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutTest.class);
    private static List<CartItem> cartItemList = new ArrayList<>();
    private static List<PricingRule> priceRulesList = new ArrayList<>();
    private static List<List<CartItem>> checkoutItemList = new ArrayList<>();

    @BeforeClass
    public static void init() {
        LOG.info("Initializing Nextail Cash-Register Checkout test data");
        createCartItems();
        createPriceRules();
        populateCheckout();
    }

    private static void createCartItems() {
        cartItemList.add(new CartItem("VOUCHER", "Gift Card", 5.00, "€"));
        cartItemList.add(new CartItem("TSHIRT", "Summer T-Shirt", 20.00, "€"));
        cartItemList.add(new CartItem("PANTS", "Summer Pants", 7.50, "€"));
    }

    private static void createPriceRules() {
        // 2-for-1 pricing rule for VOUCHER
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

        // Unit price 19€ for TSHIRT items when cart has >= 3 cart items of that type
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
        checkoutItemList.add(List.of(cartItemList.get(0), cartItemList.get(1), cartItemList.get(2))); // Example 1
        checkoutItemList.add(List.of(cartItemList.get(0), cartItemList.get(1), cartItemList.get(0))); // Example 2
        checkoutItemList.add(List.of(cartItemList.get(1), cartItemList.get(1), cartItemList.get(1),
                cartItemList.get(0), cartItemList.get(1))); // Example 3
        checkoutItemList.add(List.of(cartItemList.get(0), cartItemList.get(1), cartItemList.get(0),
                cartItemList.get(0), cartItemList.get(2), cartItemList.get(1), cartItemList.get(1))); // Example 4
    }

    @Test
    public void usingExample1PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        final Checkout checkoutExample1 = new Checkout(priceRulesList);
        checkoutItemList.get(0).forEach(coi -> checkoutExample1.scan(coi));
        MatcherAssert.assertThat("Example 1 total amount expected 32.5€", checkoutExample1.getCartTotal().compareTo(32.5) == 0);
    }

    @Test
    public void usingExample2PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        final Checkout checkoutExample2 = new Checkout(priceRulesList);
        checkoutItemList.get(1).forEach(coi -> checkoutExample2.scan(coi));
        MatcherAssert.assertThat("Example 2 total amount expected 25.00€", checkoutExample2.getCartTotal().compareTo(25.00) == 0);
    }

    @Test
    public void usingExample3PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        final Checkout checkoutExample3 = new Checkout(priceRulesList);
        checkoutItemList.get(2).forEach(coi -> checkoutExample3.scan(coi));
        MatcherAssert.assertThat("Example 3 total amount expected 81.00€", checkoutExample3.getCartTotal().compareTo(81.00) == 0);
    }

    @Test
    public void usingExample4PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValueWithPricingRulesApplied() {
        final Checkout checkoutExample4 = new Checkout(priceRulesList);
        checkoutItemList.get(3).forEach(coi -> checkoutExample4.scan(coi));
        MatcherAssert.assertThat("Example 4 total amount expected 74.50€", checkoutExample4.getCartTotal().compareTo(74.5) == 0);
    }

}
