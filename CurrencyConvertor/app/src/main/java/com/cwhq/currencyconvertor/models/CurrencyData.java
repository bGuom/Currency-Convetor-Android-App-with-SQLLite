package com.cwhq.currencyconvertor.models;

public class CurrencyData {
    private String Name;
    private double Rate;
    private double Value;

    public CurrencyData(String name, double rate, double value) {
        Name = name;
        Rate = rate;
        Value = value;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getRate() {
        return Rate;
    }

    public void setRate(double rate) {
        Rate = rate;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }
}
