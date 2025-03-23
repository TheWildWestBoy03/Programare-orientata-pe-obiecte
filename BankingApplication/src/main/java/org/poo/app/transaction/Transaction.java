package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Transaction {
    public void createTransaction();
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper);
    public Integer getTimestamp();
    public String getTransactionType();
    public String getTransactionIBAN();
}
