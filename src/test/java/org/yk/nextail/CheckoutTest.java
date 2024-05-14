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
    private final static List<PricingRule> defaultPricingRules = new ArrayList<>();
    private final static List<PricingRule> extraPricingRules = new ArrayList<>();
    private final static List<List<CartItem>> checkoutItemList = new ArrayList<>();

    protected enum PredefinedPricingRules {
        THREE_OR_MORE_TSHIRT_19,
        TWO_FOR_ONE_VOUCHER,
        THREE_FOR_ONE_VOUCHER;

        public PricingRule getPricingRule() {
            List<PricingRule.PriceRuleCondition<?>> conditions = null;
            List<PricingRule.PriceRuleAction<?>> actions = null;
            if (this.equals(THREE_OR_MORE_TSHIRT_19)) {
                // Unit price 19€ for TSHIRT items when cart has >= 3 cart items of that type
                conditions = List.of(
                        new PricingRule.PriceRuleCondition.Builder<String>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_CODE)
                                .addConditionValue("TSHIRT")
                                .build(),
                        new PricingRule.PriceRuleCondition.Builder<Integer>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.GREATER_THAN_EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_QUANTITY_TOTAL)
                                .addConditionValue(3)
                                .build()
                );
                actions = List.of(
                        new PricingRule.PriceRuleAction.Builder()
                                .addActionType(PricingRule.PriceRuleAction.PriceRuleActionType.CART_ITEM_FIXED_PRICE)
                                .addActionValue(19.00)
                                .build()
                );
            } else if (this.equals(TWO_FOR_ONE_VOUCHER)) {
                // 2-for-1 pricing rule for VOUCHER
                conditions = List.of(
                        new PricingRule.PriceRuleCondition.Builder<String>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_CODE)
                                .addConditionValue("VOUCHER")
                                .build(),
                        new PricingRule.PriceRuleCondition.Builder<Integer>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_X_QUANTITY)
                                .addConditionValue(2)
                                .build()
                );
                actions = List.of(
                        new PricingRule.PriceRuleAction.Builder()
                                .addActionType(PricingRule.PriceRuleAction.PriceRuleActionType.CART_ITEM_DISCOUNT_PERCENT)
                                .addActionValue(100)
                                .build()
                );
            } else if (this.equals(THREE_FOR_ONE_VOUCHER)) {
                // 3-for-1 pricing rule for VOUCHER
                conditions = List.of(
                        new PricingRule.PriceRuleCondition.Builder<String>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_CODE)
                                .addConditionValue("VOUCHER")
                                .build(),
                        new PricingRule.PriceRuleCondition.Builder<Integer>()
                                .addConditionOperator(PricingRule.PriceRuleCondition.PriceRuleConditionOperator.EQUALS)
                                .addConditionType(PricingRule.PriceRuleCondition.PriceRuleConditionType.CART_ITEM_X_QUANTITY)
                                .addConditionValue(3)
                                .build()
                );
                actions = List.of(
                        new PricingRule.PriceRuleAction.Builder()
                                .addActionType(PricingRule.PriceRuleAction.PriceRuleActionType.CART_ITEM_DISCOUNT_PERCENT)
                                .addActionValue(100)
                                .build()
                );
            }
            return new PricingRule(conditions, actions);
        }

    }

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
        createDefaultPriceRules();
        createExtraPriceRules();
        populateCheckout();
    }


    private static void createDefaultPriceRules() {
        defaultPricingRules.add(PredefinedPricingRules.THREE_OR_MORE_TSHIRT_19.getPricingRule());
        defaultPricingRules.add(PredefinedPricingRules.TWO_FOR_ONE_VOUCHER.getPricingRule());
    }

    private static void createExtraPriceRules() {
        extraPricingRules.add(PredefinedPricingRules.THREE_OR_MORE_TSHIRT_19.getPricingRule());
        extraPricingRules.add(PredefinedPricingRules.THREE_FOR_ONE_VOUCHER.getPricingRule());
    }

    private static void populateCheckout() {
        // Example 1
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), PANTS.getCartItem()));
        // Example 2
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), VOUCHER.getCartItem()));
        // Example 3
        checkoutItemList.add(List.of(TSHIRT.getCartItem(), TSHIRT.getCartItem(), TSHIRT.getCartItem(),
                VOUCHER.getCartItem(), TSHIRT.getCartItem()));
        // Example 4
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), VOUCHER.getCartItem(),
                VOUCHER.getCartItem(), PANTS.getCartItem(), TSHIRT.getCartItem(), TSHIRT.getCartItem()));
        // Example 5
        checkoutItemList.add(List.of(VOUCHER.getCartItem(), TSHIRT.getCartItem(), VOUCHER.getCartItem(),
                VOUCHER.getCartItem(), PANTS.getCartItem(), TSHIRT.getCartItem(), TSHIRT.getCartItem()));
    }

    @Test
    public void usingExample1PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValue_withDefaultPricingRulesApplied() {
        LOG.info("Example 1 using default pricing rules began");
        final Checkout checkoutExample1 = new Checkout(defaultPricingRules);
        checkoutItemList.get(0).forEach(coi -> checkoutExample1.scan(coi));
        MatcherAssert.assertThat("Example 1: cart total amount expected 32.5€; cart total = " + checkoutExample1.getCartTotal(),
                checkoutExample1.getCartTotal().compareTo(32.5) == 0);
        LOG.info("Example 1 using default pricing rules ended");
    }

    @Test
    public void usingExample2PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValue_withDefaultPricingRulesApplied() {
        LOG.info("Example 2 using default pricing rules began");
        final Checkout checkoutExample2 = new Checkout(defaultPricingRules);
        checkoutItemList.get(1).forEach(coi -> checkoutExample2.scan(coi));
        MatcherAssert.assertThat("Example 2: cart total amount expected 25.00€; cart total = " + checkoutExample2.getCartTotal(),
                checkoutExample2.getCartTotal().compareTo(25.00) == 0);
        LOG.info("Example 2 using default pricing rules ended");
    }

    @Test
    public void usingExample3PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValue_withDefaultPricingRulesApplied() {
        LOG.info("Example 3 using default pricing rules began");
        final Checkout checkoutExample3 = new Checkout(defaultPricingRules);
        checkoutItemList.get(2).forEach(coi -> checkoutExample3.scan(coi));
        MatcherAssert.assertThat("Example 3: cart total amount expected 81.00€; cart total = " + checkoutExample3.getCartTotal(),
                checkoutExample3.getCartTotal().compareTo(81.00) == 0);
        LOG.info("Example 3 using default pricing rules ended");
    }

    @Test
    public void usingExample4PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValue_withDefaultPricingRulesApplied() {
        LOG.info("Example 4 using default pricing rules began");
        final Checkout checkoutExample4 = new Checkout(defaultPricingRules);
        checkoutItemList.get(3).forEach(coi -> checkoutExample4.scan(coi));
        MatcherAssert.assertThat("Example 4: cart total amount expected 74.50€; cart total = " + checkoutExample4.getCartTotal(),
                checkoutExample4.getCartTotal().compareTo(74.5) == 0);
        LOG.info("Example 4 using default pricing rules ended");
    }

    @Test
    public void usingExample5PredefinedCartItems_thenScanThemSequentiallyInCheckout_totalMustMatchExpectedValue_withExtraPricingRulesApplied() {
        LOG.info("Example 5 using extra pricing rules began");
        final Checkout checkoutExample5 = new Checkout(extraPricingRules);
        checkoutItemList.get(4).forEach(coi -> checkoutExample5.scan(coi));
        MatcherAssert.assertThat("Example 5: cart total amount expected 69.50€; cart total = " + checkoutExample5.getCartTotal(),
                checkoutExample5.getCartTotal().compareTo(69.5) == 0);
        LOG.info("Example 5 using extra pricing rules ended");
    }

}
