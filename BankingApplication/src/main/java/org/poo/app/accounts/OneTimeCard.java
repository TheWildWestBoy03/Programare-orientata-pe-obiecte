package org.poo.app.accounts;

public class OneTimeCard extends Card {
    boolean approvement;
    public OneTimeCard(String cardNumber, String status, String type) {
        super(cardNumber, status, type);
        approvement = true;
    }

    public void doTransactionViaCreditCard() {

    }
}
