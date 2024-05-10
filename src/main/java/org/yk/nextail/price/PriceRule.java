package org.yk.nextail.price;

import java.util.List;

/**
 * Class responsible to define conditions and actions for a given pricing rule.
 */
public class PriceRule {
    private final List<PriceRuleCondition.Builder<?>> conditionBuilders;
    private final List<PriceRuleAction.Builder> actionBuilders;

    public PriceRule() {
        this.conditionBuilders = null;
        this.actionBuilders = null;
    }

    public PriceRule(List<PriceRuleCondition.Builder<?>> conditionBuilders, List<PriceRuleAction.Builder> actionBuilders) {
        this.conditionBuilders = conditionBuilders;
        this.actionBuilders = actionBuilders;
    }

    public static class PriceRuleCondition<V> {
        private PriceRuleConditionType conditionType;
        private PriceRuleConditionOperator conditionOperator;
        private V leftValue;
        private V rightValue;

        public enum PriceRuleConditionType {
            PRODUCT_QUANTITY,
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

            public PriceRuleCondition.Builder<V> addConditionLeftValue(V leftValue) {
                priceRuleCondition.leftValue = leftValue;
                return this;
            }

            public PriceRuleCondition.Builder<V> addConditionRightValue(V rightValue) {
                priceRuleCondition.rightValue = rightValue;
                return this;
            }

            public PriceRuleCondition build() {
                return priceRuleCondition;
            }
        }
    }

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
                return priceRuleAction;
            }
        }
    }
}
