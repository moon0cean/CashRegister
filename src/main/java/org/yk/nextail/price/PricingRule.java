package org.yk.nextail.price;

import java.util.List;

/**
 * Class responsible to define conditions and actions for a given pricing rule.
 */
public class PricingRule {
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
            PRODUCT_QUANTITY_IN_CHECKOUT,
            PRODUCT_CODE
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

        public static class Builder<V> {
            private PriceRuleCondition priceRuleCondition = new PriceRuleCondition<V>();

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

            public PriceRuleCondition build() {
                // FIXME: ensure a valid condition is created and use defaults if not (when possible)
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
     * Defines a rule action by constructing through a builder to apply to checkout based on
     * the action type passed.
     *
     * @param <V>
     */
    public static class PriceRuleAction<V> {
        private PriceRuleActionType priceRuleActionType;
        private V value;

        public enum PriceRuleActionType {
            PRODUCT_DISCOUNT_PERCENT,
            PRODUCT_FIXED_PRICE
        }

        private PriceRuleAction() {
        }

        public static class Builder<V> {
            PriceRuleAction priceRuleAction = new PriceRuleAction();

            public PriceRuleAction.Builder addActionType(PriceRuleActionType actionType) {
                priceRuleAction.priceRuleActionType = actionType;
                return this;
            }

            public PriceRuleAction.Builder addActionValue(V value) {
                priceRuleAction.value = value;
                return this;
            }

            public PriceRuleAction build() {
                // FIXME: ensure a valid action is created and use defaults if not (when possible)
                return priceRuleAction;
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
