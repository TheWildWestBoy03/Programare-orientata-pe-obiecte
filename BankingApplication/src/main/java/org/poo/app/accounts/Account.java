package org.poo.app.accounts;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Account {
    private String IBAN;
    private double balance;
    private String currency;
    private ArrayList<Card> accountCards;
    private String type;
    private Double minBalance;
    private String status;

    public Account(String type, String IBAN, Float balance, String currency, ArrayList<Card> accountCards) {
        this.IBAN = IBAN;
        this.balance = balance;
        this.currency = currency;
        this.accountCards = accountCards;
        this.type = type;
        this.minBalance = 0.0;
        this.status = "active";
    }

    public void setInterestRate(double interestRate) {
        ;
    }

    public double getInterestRate() {
        return 0.0;
    }
}
