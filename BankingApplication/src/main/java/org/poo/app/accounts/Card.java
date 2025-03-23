package org.poo.app.accounts;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Card {
    private String cardNumber;
    private String status;
    private String type;
    public Card(String cardNumber, String status, String type) {
        this.cardNumber = cardNumber;
        this.status = status;
        this.type = type;
    }

    public void doTransactionViaCreditCard() {

    }
}
