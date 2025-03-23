package org.poo.app.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.app.accounts.Account;
import org.poo.app.accounts.Card;
import org.poo.app.transaction.Transaction;
import org.poo.app.utils.CommerciantSummary;

import java.util.ArrayList;

@Getter
@Setter
public abstract class User {
    private String userLastName;
    private String userType;
    @Getter
    private ArrayList<Transaction> transactions;

    public User(String userLastName, String userType) {
        this.userLastName = userLastName;
        this.userType = userType;
        this.transactions = new ArrayList<>();
    }

    public abstract ObjectNode printUser(ObjectMapper mapper);
    public abstract ArrayList<Account> getAccounts();
    public abstract String getUserAcoountIdentification();
    public abstract Card getCardByCardNumber(String cardNumber);
    public abstract Account getAccountByCardNumber(String cardNumber);
    public abstract void addAlias(String alias, String IBAN);
    public abstract ArrayList<CommerciantSummary> retrieveCommerciants();
    public abstract void setCommerciants(ArrayList<CommerciantSummary> commerciants);
}
