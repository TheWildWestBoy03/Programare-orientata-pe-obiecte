package org.poo.app.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SendMoneyTransaction implements Transaction {
    private Integer timestamp;
    private String description;
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String type;
    private String currency;

    public SendMoneyTransaction(Integer timestamp, String description,
                                    String senderIBAN, String receiverIBAN,
                                    double amount, String currency) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.type = "sent";
        this.currency = currency;
    }

    @Override
    public void createTransaction() {

    }

    @Override
    public ObjectNode createTransactionObjectNode(ObjectMapper mapper) {
        ObjectNode newObject = mapper.createObjectNode();

        newObject.put("timestamp", timestamp);
        newObject.put("description", description);
        newObject.put("senderIBAN", senderIBAN);
        newObject.put("receiverIBAN", receiverIBAN);
        newObject.put("amount", amount + " " + currency);
        newObject.put("transferType", type);

        return newObject;
    }

    @Override
    public Integer getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTransactionType() {
        return "SendMoneyTransaction";
    }
}
