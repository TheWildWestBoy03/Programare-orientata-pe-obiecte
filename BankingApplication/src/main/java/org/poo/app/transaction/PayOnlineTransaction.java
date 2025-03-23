package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayOnlineTransaction implements Transaction {
    private Integer timestamp;
    private String description;
    private double amount;
    private String commerciant;
    private String currency;
    private String IBAN;

    public PayOnlineTransaction(String IBAN, Integer timestamp, String description, double amount,
                                    String commerciant, String currency) {
        this.timestamp = timestamp;
        this.description = description;
        this.amount = amount;
        this.commerciant = commerciant;
        this.currency = currency;
        this.IBAN = IBAN;
    }

    @Override
    public void createTransaction() {

    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode newObject = mapper.createObjectNode();

        newObject.put("timestamp", timestamp);
        newObject.put("description", description);
        newObject.put("amount", amount);
        newObject.put("commerciant", commerciant);

        return newObject;
    }

    @Override
    public String getTransactionType() {
        return "PayOnlineTransaction";
    }

    @Override
    public String getTransactionIBAN() {
        return IBAN;
    }
}
