package org.yk.nextail.price;

/**
 *
 */
public class PriceRule {
    private final PriceRuleCondition condition;
    private final PriceRuleAction action;

    public PriceRule() {
        this.condition = null;
        this.action = null;
    }

    public PriceRule(PriceRuleCondition condition, PriceRuleAction action) {
        this.condition = condition;
        this.action = action;
    }

    public static class PriceRuleCondition<V> {
        private PriceRuleConditionType conditionType;
        private PriceRuleConditionOperator conditionOperator;
        private V leftValue;
        private V rightValue;

        public enum PriceRuleConditionType {
            PRODUCT_QUANTITY
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

            public PriceRuleCondition addConditionOperator(PriceRuleConditionOperator conditionOperator) {
                priceRuleCondition.conditionOperator = conditionOperator;
                return priceRuleCondition;
            }

            public PriceRuleCondition<V> addConditionType(PriceRuleConditionType conditionType) {
                priceRuleCondition.conditionType = conditionType;
                return priceRuleCondition;
            }

            public PriceRuleCondition<V> addConditionLeftValue(V leftValue) {
                priceRuleCondition.leftValue = leftValue;
                return priceRuleCondition;
            }

            public PriceRuleCondition<V> addConditionRightValue(V rightValue) {
                priceRuleCondition.rightValue = rightValue;
                return priceRuleCondition;
            }
        }
    }

    public static class PriceRuleAction {
        private PriceRuleActionType priceRuleActionType;

        public enum PriceRuleActionType {
            PRODUCT_DISCOUNT_PERCENT,
            PRODUCT_PRICE
        }

        private PriceRuleAction() {
        }

        public static class Builder {
            PriceRuleAction priceRuleAction = new PriceRuleAction();

            PriceRuleAction addActionType(PriceRuleActionType actionType) {
                priceRuleAction.priceRuleActionType = actionType;
                return priceRuleAction;
            }
        }
    }
}
