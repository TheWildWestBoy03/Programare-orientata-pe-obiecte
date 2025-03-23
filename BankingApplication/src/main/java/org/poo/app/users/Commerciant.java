package org.poo.app.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.app.accounts.Account;
import org.poo.app.accounts.Card;
import org.poo.app.transaction.Transaction;
import org.poo.app.utils.CommerciantSummary;

import java.lang.reflect.Array;
import java.util.ArrayList;

@Setter
@Getter
public class Commerciant extends User {
    private String description;
    private Integer id;
    private String name;
    private ArrayList<String> commerciants;
    private ArrayList<Account> accounts;

    public Commerciant(String description, Integer id, ArrayList<String> commerciants, String name, String userType) {
        super(name, userType);
        this.description = description;
        this.id = id;
        this.commerciants = commerciants;
    }

    @Override
    public ObjectNode printUser(ObjectMapper mapper) {
        return null;
    }

    @Override
    public String getUserAcoountIdentification() {
        return name;
    }

    @Override
    public Card getCardByCardNumber(String cardNumber) {
        return null;
    }

    @Override
    public Account getAccountByCardNumber(String cardNumber) {
        return null;
    }

    @Override
    public void addAlias(String alias, String IBAN) {

    }

    @Override
    public ArrayList<CommerciantSummary> retrieveCommerciants() {
        return null;
    }

    @Override
    public void setCommerciants(ArrayList<CommerciantSummary> commerciants) {

    }

    public ArrayList<Transaction> getTransactions() {
        return super.getTransactions();
    }
}
