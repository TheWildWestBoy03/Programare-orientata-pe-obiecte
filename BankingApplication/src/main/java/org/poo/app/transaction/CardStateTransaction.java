package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CardStateTransaction implements Transaction {
    private final Integer timestamp;
    private final String description;
    private final String cardNumber;
    private final String email;
    private final String IBAN;

    public CardStateTransaction(Integer timestamp, String description, String cardNumber, String email, String IBAN) {
        this.timestamp = timestamp;
        this.description = description;
        this.cardNumber = cardNumber;
        this.email = email;
        this.IBAN = IBAN;
    }

    @Override
    public void createTransaction() {

    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode result = mapper.createObjectNode();

        result.put("timestamp", timestamp);
        result.put("description", description);
        result.put("card", cardNumber);
        result.put("cardHolder", email);
        result.put("account", IBAN);

        return result;

    }

    @Override
    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTransactionType() {
        return "CardStateTransaction";
    }

    @Override
    public String getTransactionIBAN() {
        return IBAN;
    }
}
