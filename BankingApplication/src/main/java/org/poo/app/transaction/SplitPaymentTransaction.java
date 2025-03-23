package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class SplitPaymentTransaction implements Transaction {
    private final Integer timestamp;
    private final String description;
    private final String currency;
    private final double amount;
    private final ArrayList<String> accounts;

    public SplitPaymentTransaction(Integer timestamp, String description, String currency, double amount, ArrayList<String> accounts) {
        this.timestamp = timestamp;
        this.description = description;
        this.currency = currency;
        this.amount = amount;
        this.accounts = accounts;
    }

    @Override
    public void createTransaction() {

    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode transaction = mapper.createObjectNode();
        transaction.put("timestamp", timestamp);
        transaction.put("description", description);
        transaction.put("currency", currency);
        transaction.put("amount", amount);

        ArrayNode accountsString = mapper.createArrayNode();

        for (String account : accounts) {
            accountsString.add(account);
        }

        transaction.put("involvedAccounts", accountsString);
        return transaction;
    }

    @Override
    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTransactionType() {
        return "SplitTransaction";
    }
}
