package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CreatedAccountTransaction implements Transaction {
    private Integer timestamp;
    private String description;

    public CreatedAccountTransaction(Integer timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    @Override
    public void createTransaction() {

    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode newObject = mapper.createObjectNode();

        newObject.put("timestamp", timestamp);
        newObject.put("description", description);

        return newObject;
    }

    @Override
    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTransactionType() {
        return "CreatedAccountTransaction";
    }
}
