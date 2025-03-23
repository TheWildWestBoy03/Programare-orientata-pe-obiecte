package org.poo.app.accounts;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class EconomyAccount extends Account {
    private double interestRate;

    public EconomyAccount(String type, String IBAN, Float balance, String currency, ArrayList<Card> accountCards, double interestRate) {
        super(type, IBAN, balance, currency, accountCards);
        this.interestRate = interestRate;
    }

    @Override
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }
}
