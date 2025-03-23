package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InsufficientFundsTransaction implements Transaction {
    private final Integer timestamp;
    private final String message;

    @Override
    public void createTransaction() {

    }

    public InsufficientFundsTransaction(Integer timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode result = mapper.createObjectNode();

        result.put("timestamp", timestamp);
        result.put("description", message);

        return result;
    }

    @Override
    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTransactionType() {
        return "";
    }
}
