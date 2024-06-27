package org.yk.price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.NextailException;
import org.yk.cart.CartItem;

import java.util.List;

/**
 * Class responsible to define conditions and actions for a given pricing rule.
 */
public class PricingRule {
    private static final Logger LOG = LoggerFactory.getLogger(PricingRule.class);
    private final List<PriceRuleCondition<?>> conditions;
    private final List<PriceRuleAction<?>> actions;

    public PricingRule() {
        this.conditions = null;
        this.actions = null;
    }

    public PricingRule(List<PriceRuleCondition<?>> conditions, List<PriceRuleAction<?>> actions) {
        this.conditions = conditions;
        this.actions = actions;
    }

    public List<PriceRuleCondition<?>> getConditions() {
        return conditions;
    }

    public List<PriceRuleAction<?>> getActions() {
        return actions;
    }


    /**
     * Defines a rule condition by constructing through a builder the right and left value and the conditional operator
     * to finally evaluate according to the condition type passed.
     *
     * @param <V> type of value to be compared with
     */
    public static class PriceRuleCondition<V> {
        private PriceRuleConditionType conditionType;
        private PriceRuleConditionOperator conditionOperator;
        private V conditionValue;

        public enum PriceRuleConditionType {
            CART_ITEM_QUANTITY_TOTAL,
            CART_ITEM_X_QUANTITY,
            CART_ITEM_CODE
        }

        public enum PriceRuleConditionOperator {
            EQUALS,
            NOT_EQUALS,
            GREATER_THAN,
            LESS_THAN,
            GREATER_THAN_EQUALS,
            LESS_THAN_EQUALS
        }

        private PriceRuleCondition() {
        }

        public PriceRuleConditionType getConditionType() {
            return conditionType;
        }

        public PriceRuleConditionOperator getConditionOperator() {
            return conditionOperator;
        }

        public V getConditionValue() {
            return conditionValue;
        }

        public boolean evalCondition(V value) {
            if (!value.getClass().isAssignableFrom(conditionValue.getClass())) {
                throw new NextailException("Eval condition: Value class " + value.getClass().getName()
                        + " can't be assigned from condition class " + conditionValue.getClass().getName());
            } else {
                LOG.debug("Eval condition: value class " + value.getClass().getName() + " can be assigned from condition class " + conditionValue.getClass().getName());
            }
            boolean canBeCompared = value instanceof Comparable && conditionValue instanceof Comparable;
            switch (getConditionOperator()) {
                case EQUALS -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) == 0;
                    }
                    return conditionValue.equals(value);
                }
                case GREATER_THAN_EQUALS -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) >= 0;
                    }
                    throw new NextailException("Invalid greater_than_equals operator for value of type " + value.getClass().getName()
                            + " and condition value of type " + value.getClass().getName());
                }
                case GREATER_THAN -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) > 0;
                    }
                    throw new NextailException("Invalid greater_than operator for value of type " + value.getClass().getName()
                            + " and condition value of type " + value.getClass().getName());
                }
                case LESS_THAN_EQUALS -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) <= 0;
                    }
                    throw new NextailException("Invalid less_than_equals operator for value of type " + value.getClass().getName()
                            + " and condition value of type " + value.getClass().getName());
                }
                case LESS_THAN -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) < 0;
                    }
                    throw new NextailException("Invalid less_than operator for value of type " + value.getClass().getName()
                            + " and condition value of type " + value.getClass().getName());
                }
                case NOT_EQUALS -> {
                    if (canBeCompared) {
                        return ((Comparable<V>) value).compareTo(conditionValue) != 0;
                    }
                    return !conditionValue.equals(value);
                }
            }
            return false;
        }

        public static class Builder<V> {
            private final PriceRuleCondition<V> priceRuleCondition = new PriceRuleCondition<>();

            public PriceRuleCondition.Builder<V> addConditionOperator(PriceRuleConditionOperator conditionOperator) {
                priceRuleCondition.conditionOperator = conditionOperator;
                return this;
            }

            public PriceRuleCondition.Builder<V> addConditionType(PriceRuleConditionType conditionType) {
                priceRuleCondition.conditionType = conditionType;
                return this;
            }

            public PriceRuleCondition.Builder<V> addConditionValue(V conditionValue) {
                priceRuleCondition.conditionValue = conditionValue;
                return this;
            }

            public PriceRuleCondition<V> build() {
                if (priceRuleCondition.conditionOperator == null) {
                    priceRuleCondition.conditionOperator = PriceRuleConditionOperator.EQUALS;
                }
                if (priceRuleCondition.conditionType == null) {
                    throw new NextailException("Missing condition type while building the pricing rule condition");
                }
                if (priceRuleCondition.conditionValue == null) {
                    throw new NextailException("Missing condition value while building the pricing rule condition");
                }
                return priceRuleCondition;
            }
        }

        @Override
        public String toString() {
            return "PriceRuleCondition{" +
                    "conditionType=" + conditionType +
                    ", conditionOperator=" + conditionOperator +
                    ", conditionValue=" + conditionValue +
                    '}';
        }
    }

    /**
     * Defines a rule action by constructing through a builder to apply to the checkout process,
     * based on the action type passed.
     *
     * @param <V>
     */
    public static class PriceRuleAction<V extends Number> {
        private PriceRuleActionType priceRuleActionType;
        private V value;

        public enum PriceRuleActionType {
            CART_ITEM_DISCOUNT_PERCENT,
            CART_ITEM_FIXED_PRICE
        }

        public PriceRuleActionType getPriceRuleActionType() {
            return priceRuleActionType;
        }

        public V getValue() {
            return value;
        }

        private PriceRuleAction() {
        }

        public static class Builder<V extends Number> {
            private final PriceRuleAction<V> priceRuleAction = new PriceRuleAction<>();

            public PriceRuleAction.Builder<V> addActionType(PriceRuleActionType actionType) {
                priceRuleAction.priceRuleActionType = actionType;
                return this;
            }

            public PriceRuleAction.Builder<V> addActionValue(V value) {
                priceRuleAction.value = value;
                return this;
            }

            public PriceRuleAction<V> build() {
                if (priceRuleAction.priceRuleActionType == null) {
                    throw new NextailException("Missing action type while building the pricing rule action");
                }
                if (priceRuleAction.value == null) {
                    throw new NextailException("Missing action value while building the pricing rule action");
                }
                return priceRuleAction;
            }
        }

        /**
         * Apply rule actions for a given list of cart items
         *
         * @param filteredCartItems The cart items filtered by the conditions evaluation
         */
        public void applyPricingRuleAction(List<CartItem> filteredCartItems) {
            // FIXME: possible incompatibilities between different actions when defined for the same pricing rule
            switch (this.getPriceRuleActionType()) {
                case CART_ITEM_FIXED_PRICE -> {
                    LOG.info("Applying CART_ITEM_FIXED_PRICE action rule [" + this.getValue() + "]");
                    if (this.getValue() != null) {
                        filteredCartItems.forEach(ci -> {
                            Double discount = ci.getPrice() - (this.getValue()).doubleValue();
                            ci.setDiscount(discount);
                            LOG.info("Applied CART_ITEM_FIXED_PRICE action rule discount = " + discount
                                    + " for cart item = " + ci.getCode());
                        });
                    } else {
                        throw new NextailException("Pricing rule action value is not a number");
                    }
                }
                case CART_ITEM_DISCOUNT_PERCENT -> {
                    LOG.info("Applying CART_ITEM_DISCOUNT_PERCENT action rule using discount percentage = " + this.getValue() + "%");
                    if (this.getValue() != null) {
                        filteredCartItems.forEach(ci -> {
                            Double discount = ci.getPrice() * (this.getValue().doubleValue() / 100);
                            LOG.info("Applied CART_ITEM_DISCOUNT_PERCENT action rule discount = " + discount
                                    + " for cart item = " + ci.getCode());
                            ci.setDiscount(ci.getDiscount() + discount);
                        });
                    } else {
                        throw new NextailException("Pricing rule action value is not a number");
                    }
                }
                default -> throw new NextailException("Invalid pricing rule action type");
            }
        }

        @Override
        public String toString() {
            return "PriceRuleAction{" +
                    "priceRuleActionType=" + priceRuleActionType +
                    ", value=" + value +
                    '}';
        }
    }
}
