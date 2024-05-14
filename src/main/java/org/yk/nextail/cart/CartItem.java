package org.yk.nextail.cart;

public class CartItem {

    private final String code;
    private final String name;
    private final Double price;
    private final String currency;
    private Double discount = 0.00;

    public CartItem(String code, String name, Double price, String currency) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}
