package org.poo.app.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.app.accounts.Account;
import org.poo.app.accounts.Card;
import org.poo.app.transaction.Transaction;
import org.poo.app.utils.CommerciantSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;

@Getter
@Setter
public class Client extends User{
    private String userFirstName;
    private String userEmail;
    private ArrayList<Account> accounts;
    private ArrayList<CommerciantSummary> commerciants;

    private LinkedHashMap<String, String> accountAliases;
    public Client(String userFirstName, String userLastName, String userEmail, String userType) {
        super(userLastName, userType);
        this.userFirstName = userFirstName;
        this.userEmail = userEmail;
        this.accounts = new ArrayList<>();
        this.accountAliases = new LinkedHashMap<>();
        this.commerciants = new ArrayList<>();
    }

    @Override
    public ObjectNode printUser(ObjectMapper mapper) {
        ObjectNode userObject = mapper.createObjectNode();

        userObject.put("firstName", userFirstName);
        userObject.put("lastName", super.getUserLastName());
        userObject.put("email", userEmail);

        ArrayNode userAccountsArrayNode = mapper.createArrayNode();
        for (Account account : this.getAccounts()) {
            ObjectNode accountObject = mapper.createObjectNode();

            accountObject.put("IBAN", account.getIBAN());
            accountObject.put("balance", account.getBalance());
            accountObject.put("currency", account.getCurrency());
            accountObject.put("type", account.getType());

            ArrayNode cardsArrayNode = mapper.createArrayNode();

            for (Card card : account.getAccountCards()) {
                ObjectNode cardObject = mapper.createObjectNode();
                cardObject.put("cardNumber", card.getCardNumber());

                if (account.getStatus().equals("blocked")) {
                    card.setStatus("frozen");
                }
                cardObject.put("status", card.getStatus());

                cardsArrayNode.add(cardObject);
            }

            accountObject.put("cards", cardsArrayNode);
            userAccountsArrayNode.add(accountObject);
        }

        userObject.put("accounts", userAccountsArrayNode);

        return userObject;
    }

    @Override
    public String getUserAcoountIdentification() {
        return userEmail;
    }

    @Override
    public Card getCardByCardNumber(String cardNumber) {
        for (Account account : accounts) {
            for (Card card : account.getAccountCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    return card;
                }
            }
        }
        return null;
    }

    @Override
    public Account getAccountByCardNumber(String cardNumber) {
        for (Account account : accounts) {
            for (Card card : account.getAccountCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    return account;
                }
            }
        }

        return null;
    }

    @Override
    public ArrayList<Transaction> getTransactions() {
        return super.getTransactions();
    }

    public void addAlias(String alias, String IBAN) {
        accountAliases.put(alias, IBAN);
    }

    public ArrayList<CommerciantSummary> retrieveCommerciants() {
        return commerciants;
    }
}
