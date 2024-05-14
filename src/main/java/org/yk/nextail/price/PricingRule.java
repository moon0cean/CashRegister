package org.yk.nextail.price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yk.nextail.NextailException;

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
            boolean canBeCompared = false;
            if (value instanceof Comparable && conditionValue instanceof Comparable) {
                canBeCompared = true;
            }
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
