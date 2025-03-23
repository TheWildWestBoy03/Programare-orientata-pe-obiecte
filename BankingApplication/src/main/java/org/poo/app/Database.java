package org.poo.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.jshell.execution.Util;
import lombok.Getter;
import lombok.Setter;
import org.poo.app.accounts.Account;
import org.poo.app.accounts.Card;
import org.poo.app.accounts.EconomyAccount;
import org.poo.app.accounts.OneTimeCard;
import org.poo.app.transaction.*;
import org.poo.app.users.Client;
import org.poo.app.users.Commerciant;
import org.poo.app.users.User;
import org.poo.app.utils.CommerciantSummary;
import org.poo.app.utils.ExchangeMoneyTypes;
import org.poo.fileio.*;
import org.poo.utils.Utils;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Optional;

@Setter
@Getter
public class Database {
    private ArrayList<User> users;
    private LinkedHashMap<ExchangeMoneyTypes, Double> exchangeDatabase;
    @Getter
    private static Database databaseInstance;
    private ObjectMapper mapper;
    private ArrayNode resultsArray;

    private User getCommerciantByName(String name) {
        return users.stream().filter(user -> user.getUserType().equals("commerciant")
                    && user.getUserLastName().equals(name)).findFirst().orElse(null);
    }

    private Account getAccountByName(String IBAN) {
        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(IBAN)) {
                    return account;
                }
            }
        }

        return null;
    }

    private User returnUserByEmail(String email) {
        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }

            if (user.getUserAcoountIdentification().equals(email)) {
                return user;
            }
        }

        return null;
    }

    private User getUserByIBAN(String IBAN) {
        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(IBAN)) {
                    return user;
                }
            }
        }

        return null;
    }

    private Double returnRateBySourceAndDestination(String sourceMoney, String destinationMoney) {
        for (ExchangeMoneyTypes key : exchangeDatabase.keySet()) {
            if (key.getSourceMoney().equals(sourceMoney) && key.getTargetMoney().equals(destinationMoney)) {
                return exchangeDatabase.get(key);
            }
        }

        return 0.0;
    }

    private ExchangeMoneyTypes returnExchangeDictKey(String sourceMoney, String destinationMoney) {
        for (ExchangeMoneyTypes key : exchangeDatabase.keySet()) {
            if (key.getSourceMoney().equals(sourceMoney) && key.getTargetMoney().equals(destinationMoney)) {
                return key;
            }
        }

        return null;
    }

    private ExchangeMoneyTypes returnExchangeMoneyByTarget(String sourceMoney) {
        for (ExchangeMoneyTypes key : exchangeDatabase.keySet()) {
            if (key.getTargetMoney().equals(sourceMoney)) {
                return key;
            }
        }

        return null;
    }

    private void createExchangeMoneyDictionary(ObjectInput inputData) {
        for (ExchangeInput exchange : inputData.getExchangeRates()) {
            String sourceMoney = exchange.getFrom();
            String targetMoney = exchange.getTo();
            double exchangeAmount = exchange.getRate();

            ExchangeMoneyTypes newExchangeRelation = new ExchangeMoneyTypes(sourceMoney, targetMoney);
            ExchangeMoneyTypes newReverseExchangeRelation = new ExchangeMoneyTypes(targetMoney, sourceMoney);

            if (returnExchangeMoneyByTarget(sourceMoney) == null && returnExchangeMoneyByTarget(targetMoney) == null) {
                // nu mai avem alte relatii de adaugat
            } else if (returnExchangeMoneyByTarget(targetMoney) == null) {
                LinkedHashMap<ExchangeMoneyTypes, Double> newExchangeDatabase =
                        addCliqueRelations(sourceMoney, targetMoney, exchangeAmount);

                exchangeDatabase.putAll(newExchangeDatabase);
            } else if (returnExchangeMoneyByTarget(sourceMoney) == null) {
                LinkedHashMap<ExchangeMoneyTypes, Double> newExchangeDatabase =
                        addCliqueRelations(targetMoney, sourceMoney, exchangeAmount);
                exchangeDatabase.putAll(newExchangeDatabase);
            }

            exchangeDatabase.put(newExchangeRelation, exchangeAmount);
            exchangeDatabase.put(newReverseExchangeRelation, (double) 1.0 / exchangeAmount);
        }
    }

    private LinkedHashMap<ExchangeMoneyTypes, Double> addCliqueRelations(String sourceMoney, String targetMoney, double exchangeAmount) {
        LinkedHashMap<ExchangeMoneyTypes, Double> newExchangeDatabase = new LinkedHashMap<>();
        for (ExchangeMoneyTypes key : exchangeDatabase.keySet()) {
            if (key.getTargetMoney().equals(sourceMoney)) {
                newExchangeDatabase.put(new ExchangeMoneyTypes(key.getSourceMoney(), targetMoney), exchangeAmount * exchangeDatabase.get(key));
                newExchangeDatabase.put(new ExchangeMoneyTypes(targetMoney, key.getSourceMoney()),
                            (double) 1.0 / exchangeAmount / exchangeDatabase.get(key));
            }
        }
        return newExchangeDatabase;
    }

    private void createUsers(ObjectInput inputData) {
        for (UserInput user : inputData.getUsers()) {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            String email = user.getEmail();

            users.add(new Client(firstName, lastName, email, "client"));
        }
    }

    private Database(ObjectInput inputData) {
        mapper = new ObjectMapper();
        users = new ArrayList<>();
        exchangeDatabase = new LinkedHashMap<>();
        resultsArray = mapper.createArrayNode();
        mapper = new ObjectMapper();

        createUsers(inputData);
        createExchangeMoneyDictionary(inputData);

        for (CommandInput command : inputData.getCommands()) {
            String commandName = command.getCommand();

            if (commandName.equals("printUsers")) {
                printUsers(command);
            } else if (commandName.equals("addAccount")) {
                addAccount(command);
            } else if (commandName.equals("addFunds")) {
                addFunds(command);
            } else if (commandName.equals("createCard") || commandName.startsWith("createOneTimeCard")) {
                createUserCard(command);
            } else if (commandName.startsWith("deleteAccount")) {
                deleteAccount(command);
            } else if (commandName.startsWith("deleteCard")) {
                deleteUserCard(command);
            } else if (commandName.startsWith("setMinBalance")) {
                setMinBalance(command);
            } else if (commandName.startsWith("checkCardStatus")) {
                checkCardStatus(command);
            } else if (commandName.startsWith("payOnline")) {
                ObjectNode result = payOnline(command);
                if (result != null) {
                    resultsArray.add(result);
                }
            } else if (commandName.startsWith("sendMoney")) {
                sendMoney(command);
            } else if (commandName.startsWith("setAlias")) {

            } else if (commandName.startsWith("splitPayment")) {
                splitPayment(command);
            } else if (commandName.startsWith("addInterest")) {
                addInterest(command);
            } else if (commandName.startsWith("changeInterestRate")) {
                changeInterestRate(command);
            } else if (commandName.startsWith("report")) {
                ObjectNode result = report(command);

                if (result != null) {
                    resultsArray.add(result);
                }
            } else if (commandName.startsWith("spendingsReport")) {
                ObjectNode result = spendingReport(command);

                if (result != null) {
                    resultsArray.add(result);
                }
            } else if (commandName.startsWith("printTransaction")) {
                printTransaction(command);
            }
        }
    }

    public void printUsers(CommandInput command) {
        ObjectNode result = mapper.createObjectNode();
        result.put("command", command.getCommand());
        ArrayNode userArrayNode = mapper.createArrayNode();

        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }
            userArrayNode.add(user.printUser(mapper));
        }

        result.put("output", userArrayNode);
        result.put("timestamp", command.getTimestamp());
        resultsArray.add(result);
    }

    public void addAccount(CommandInput command) {
        String accountType = command.getAccountType();
        String userEmail = command.getEmail();
        String accountCurrency = command.getCurrency();
        double interestRate = 0.0;

        if (accountType.equals("savings")) {
            interestRate = command.getInterestRate();
        }

        for (User user : users) {
            if (user.getUserType().startsWith("commerciant")) {
                continue;
            }
            if (user.getUserAcoountIdentification().startsWith(userEmail)) {
                String accountIBAN = Utils.generateIBAN();

                if (accountType.equals("savings")){
                    user.getAccounts().add(new EconomyAccount(accountType, accountIBAN, 0.0F, accountCurrency, new ArrayList<>(), interestRate));
                } else {
                    user.getAccounts().add(new Account(accountType, accountIBAN, 0.0F, accountCurrency, new ArrayList<>()));
                }
                Transaction newTransaction = new CreatedAccountTransaction
                        (command.getTimestamp(), "New account created");

                user.getTransactions().add(newTransaction);
            }
        }
    }

    public void deleteAccount(CommandInput command) {
        ObjectNode result = mapper.createObjectNode();
        result.put("command", command.getCommand());
        result.put("command", command.getCommand());

        String email = command.getEmail();
        String IBAN = command.getAccount();

        Account accountToDelete = null;
        for (User user : users) {
            if (user.getUserAcoountIdentification().startsWith(email)) {
                for (Account account : user.getAccounts()) {
                    if (account.getBalance() == 0) {
                        accountToDelete = account;
                        break;
                    }
                }

                if (accountToDelete != null) {
                    user.getAccounts().remove(accountToDelete);
                    break;
                }
            }
        }

        ObjectNode deletingResult = mapper.createObjectNode();
        if (accountToDelete != null) {
            deletingResult.put("success", "Account deleted");
            deletingResult.put("timestamp", command.getTimestamp());
        } else {
            deletingResult.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
            deletingResult.put("timestamp", command.getTimestamp());
        }

        result.put("output", deletingResult);
        result.put("timestamp", command.getTimestamp());

        resultsArray.add(result);
    }

    public void createUserCard(CommandInput command) {
        String accountIBAN = command.getAccount();
        String email = command.getEmail();
        String commandName = command.getCommand();

        Account newAccountCard = null;
        User requestingUser = null;
        for(User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }
            if (user.getUserAcoountIdentification().startsWith(email)) {
                for (Account account : user.getAccounts()) {
                    if (account.getIBAN().equals(accountIBAN)) {
                        newAccountCard = account;
                        requestingUser = user;
                        break;
                    }
                }
            }
        }

        if (newAccountCard != null) {
            String cardNumber = Utils.generateCardNumber();
            if (commandName.equals("createCard")) {
                ArrayList<Card> newCards = newAccountCard.getAccountCards();
                newCards.add(new Card(cardNumber, "active", "normal"));
                newAccountCard.setAccountCards(newCards);
            } else {
                ArrayList<Card> newCards = newAccountCard.getAccountCards();
                newCards.add(new OneTimeCard(cardNumber, "active", "oneTime"));
                newAccountCard.setAccountCards(newCards);
            }

            requestingUser.getTransactions().add(new CardStateTransaction(command.getTimestamp(),
                        "New card created", cardNumber, requestingUser.getUserAcoountIdentification(),
                                                        accountIBAN));
        }
    }

    public void addFunds(CommandInput command) {
        double funds = command.getAmount();
        String IBAN = command.getAccount();

        for (User user : users) {
            if (user.getUserType().startsWith("commerciant")) {
                continue;
            }
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().startsWith(IBAN)) {
                    account.setBalance((double) (account.getBalance() + funds * 1.0F));
                }
            }
        }
    }

    public void deleteUserCard(CommandInput command) {
        String cardNumber = command.getCardNumber();
        for (User user : users) {
            if (user.getUserType().startsWith("commerciant")) {
                continue;
            }
            for (Account account : user.getAccounts()) {
                Card cardToDelete = null;
                for (Card card : account.getAccountCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        cardToDelete = card;
                        break;
                    }
                }

                if (cardToDelete != null) {
                    account.getAccountCards().remove(cardToDelete);
                    user.getTransactions().add(new CardStateTransaction(command.getTimestamp(),
                            "The card has been destroyed", cardNumber, user.getUserAcoountIdentification(),
                            account.getIBAN()));
                    break;
                }
            }
        }
    }

    public void printTransaction(CommandInput commandInput) {
        ObjectNode printTransactionObjectNode = mapper.createObjectNode();
        printTransactionObjectNode.put("command", commandInput.getCommand());
        String email = commandInput.getEmail();
        Integer timestamp = commandInput.getTimestamp();

        User requestedUser = null;
        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }

            if (user.getUserAcoountIdentification().equals(email)) {
                requestedUser = user;
                break;
            }
        }

        if (requestedUser == null) {
            return;
        }

        ArrayNode transactionsArray = mapper.createArrayNode();
        for (Transaction transaction : requestedUser.getTransactions()) {
            transactionsArray.add(transaction.createTransactionObjectNode(mapper));
        }

        printTransactionObjectNode.put("output", transactionsArray);
        printTransactionObjectNode.put("timestamp", timestamp);

        resultsArray.add(printTransactionObjectNode);
    }

    public void setMinBalance(CommandInput commandInput) {
        double minBalance = commandInput.getMinBalance();
        String IBAN = commandInput.getAccount();

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(IBAN)) {
                    account.setMinBalance(minBalance);
                    break;
                }
            }
        }
    }

    public void checkCardStatus(CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        Integer timestamp = commandInput.getTimestamp();

        ObjectNode checkCardStatusObjectNode = mapper.createObjectNode();
        checkCardStatusObjectNode.put("command", commandInput.getCommand());

        boolean cardFound = false;
        Account savedAccount = null;
        User savedUser = null;
        Card savedCard = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getAccountCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        cardFound = true;
                        savedAccount = account;
                        savedUser = user;
                        savedCard = card;
                    }
                }
            }
        }

        if (savedAccount != null) {
            if (savedAccount.getStatus().equals("blocked")) {
                return;
            }
        }

        ObjectNode outputObjectNode = mapper.createObjectNode();
        if (!cardFound) {
            outputObjectNode.put("timestamp", timestamp);
            outputObjectNode.put("description", "Card not found");
        }

        checkCardStatusObjectNode.put("output", outputObjectNode);
        checkCardStatusObjectNode.put("timestamp", timestamp);

        if (!cardFound) {
            resultsArray.add(checkCardStatusObjectNode);
        } else {
            if (savedAccount.getBalance() <= savedAccount.getMinBalance()) {
                String attentionMessage = "You have reached the minimum amount of funds, the card will be frozen";
                savedUser.getTransactions().add(new InsufficientFundsTransaction(commandInput.getTimestamp(),
                        attentionMessage));
                savedCard.setStatus("frozen");
            }
        }
    }

    public ObjectNode payOnline(CommandInput commandInput) {
        String commandName = commandInput.getCommand();
        String cardNumber = commandInput.getCardNumber();
        double amount = commandInput.getAmount();
        String currency = commandInput.getCurrency();
        Integer timestanp = commandInput.getTimestamp();
        String description = commandInput.getDescription();
        String commerciant = commandInput.getCommerciant();
        String email = commandInput.getEmail();

        ArrayList<User> commercials = new ArrayList<>();
        for (User user : users) {
            if (user.getUserType().startsWith("commerciant")) {
                continue;
            }

            if (user.getUserAcoountIdentification().startsWith(email)) {
                Card card = user.getCardByCardNumber(cardNumber);
                if (card != null) {
                    User theDestinationCommerciant = getCommerciantByName(commerciant);
                    if (theDestinationCommerciant == null) {
                        theDestinationCommerciant = new Commerciant(null, null, new ArrayList<>(), commerciant, "commerciant");
                        commercials.add(theDestinationCommerciant);
                    }

                    Account sourceAccount = user.getAccountByCardNumber(cardNumber);
                    User sourceUser = getUserByIBAN(sourceAccount.getIBAN());

                    if (sourceUser == null) {
                        return null;
                    }
                    ExchangeMoneyTypes commerciantToUserAccount = returnExchangeDictKey(currency, sourceAccount.getCurrency());

                    double rate = 1.0F;
                    if (commerciantToUserAccount != null) {
                        rate = exchangeDatabase.get(commerciantToUserAccount);
                    }

                    double rest = sourceAccount.getBalance() - amount * rate;

                    if (card.getStatus().equals("frozen")) {
                        sourceUser.getTransactions().add(new InsufficientFundsTransaction(commandInput.getTimestamp(),
                                "The card is frozen"));
                    } else if (0 > rest) {
                        sourceUser.getTransactions().add(new InsufficientFundsTransaction(commandInput.getTimestamp(),
                                "Insufficient funds"));
                    } else {
                        sourceUser.getTransactions().add(new PayOnlineTransaction(commandInput.getTimestamp(), "Card payment", amount * rate,
                                commerciant, sourceAccount.getCurrency()));;
                        sourceAccount.setBalance(rest);

                        ArrayList<CommerciantSummary> commerciants = sourceUser.retrieveCommerciants();

                        CommerciantSummary correctCommerciant = null;
                        for (CommerciantSummary commerciantSummary : commerciants) {
                            if (commerciantSummary.getCommerciantName().equals(commerciant)) {
                                commerciantSummary.setAmount(commerciantSummary.getAmount() + amount);
                                correctCommerciant = commerciantSummary;
                                break;
                            }
                        }

                        if (correctCommerciant == null) {
                            correctCommerciant = new CommerciantSummary(commerciant, amount);
                            commerciants.add(correctCommerciant);
                        }

                        commerciants = new ArrayList<>(commerciants.stream()
                                .sorted(Comparator.comparing(CommerciantSummary::getCommerciantName))
                                .toList());

                        sourceUser.setCommerciants(commerciants);

                        if (sourceAccount.getBalance() <= sourceAccount.getMinBalance()) {
                            String attentionMessage = "You have reached the minimum amount of funds, the card will be frozen";
                            sourceUser.getTransactions().add(new InsufficientFundsTransaction(commandInput.getTimestamp(),
                                    attentionMessage));
                            card.setStatus("frozen");
                        }
                    }

                    return null;
                }
            }
        }

        ObjectNode errorCaseNode = mapper.createObjectNode();
        errorCaseNode.put("command", commandName);

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("timestamp", timestanp);
        outputNode.put("description", "Card not found");

        errorCaseNode.set("output", outputNode);
        errorCaseNode.put("timestamp", timestanp);
        users.addAll(commercials);

        return errorCaseNode;
    }

    public void sendMoney(CommandInput commandInput) {
        String sourceIBAN = commandInput.getAccount();
        double amount = commandInput.getAmount();
        String receiverIBAN = commandInput.getReceiver();
        Integer timestamp = commandInput.getTimestamp();
        String description = commandInput.getDescription();
        String email = commandInput.getEmail();
        Account sourceAccount = getAccountByName(sourceIBAN);
        Account destinationAccount = getAccountByName(receiverIBAN);

        boolean accountSafe = false;

        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }

            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(sourceIBAN)) {
                    if (email.equals(user.getUserAcoountIdentification())) {
                        accountSafe = true;
                        break;
                    }
                }
            }

            if (accountSafe) {
                break;
            }
        }

        if (!accountSafe) {
            sourceAccount = null;
        }

        if (destinationAccount == null || sourceAccount == null) {
            return;
        }

        double exchangeRate = returnRateBySourceAndDestination(sourceAccount.getCurrency(), destinationAccount.getCurrency());

        if (sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            exchangeRate = 1.0;
        }
        double balanceAfterTransaction = sourceAccount.getBalance() - amount;

        User sourceUser = getUserByIBAN(sourceIBAN);
        User destinationUser = getUserByIBAN(receiverIBAN);

        if (sourceUser == null || destinationUser == null) {
            return;
        }

        if (balanceAfterTransaction < 0) {
            sourceUser.getTransactions().add(new InsufficientFundsTransaction(commandInput.getTimestamp(),
                    "Insufficient funds"));
            return;
        }

        sourceAccount.setBalance(balanceAfterTransaction);

        destinationAccount.setBalance(destinationAccount.getBalance() + amount * exchangeRate);

        sourceUser.getTransactions().add(new SendMoneyTransaction(timestamp, description, sourceIBAN,
                receiverIBAN, amount, sourceAccount.getCurrency()));
        destinationUser.getTransactions().add(new ReceivedMoneyTransaction(timestamp, description, sourceIBAN,
                receiverIBAN, amount * exchangeRate, destinationAccount.getCurrency()));
    }

    public void setAlias(CommandInput commandInput) {
        String commandName = commandInput.getCommand();
        String alias = commandInput.getAlias();
        int timestamp = commandInput.getTimestamp();
        String email = commandInput.getEmail();
        String IBAN = commandInput.getAccount();

        User questioningUser = returnUserByEmail(email);

        if (questioningUser == null) {
            return;
        }
        questioningUser.addAlias(alias, IBAN);
    }

    public void splitPayment(CommandInput commandInput) {
        ArrayList<String> accounts = (ArrayList<String>) commandInput.getAccounts();
        double totalSum = commandInput.getAmount();
        int timestamp = commandInput.getTimestamp();
        String paymentCurrency = commandInput.getCurrency();

        ArrayList<User> impliedUsers = new ArrayList<>();
        ArrayList<Account> impliedAccounts = new ArrayList<>();

        for (String stringAccount : accounts) {
            if (getUserByIBAN(stringAccount) == null) {
                return;
            }
            impliedUsers.add(getUserByIBAN(stringAccount));

            if (getAccountByName(stringAccount) == null) {
                return;
            }

            impliedAccounts.add(getAccountByName(stringAccount));
        }

        if (impliedUsers.isEmpty()) {
            return;
        }

        boolean notEnoughSupply = false;
        for (Account account : impliedAccounts) {
            if (account == null) {
                continue;
            }
            double exchange_rate = returnRateBySourceAndDestination(paymentCurrency, account.getCurrency());
            if (paymentCurrency.equals(account.getCurrency())) {
                exchange_rate = 1.0;
            }

            double rest = account.getBalance() - totalSum / impliedUsers.size() * exchange_rate;

            if (rest < 0) {
                notEnoughSupply = true;
                break;
            }
        }

        if (notEnoughSupply) {
            for (User user : impliedUsers) {
                if (user == null) {
                    continue;
                }
                user.getTransactions().add(new InsufficientFundsTransaction(timestamp, "Insufficient funds"));
            }
        } else {
            for (Account account : impliedAccounts) {
                double exchange_rate = returnRateBySourceAndDestination(paymentCurrency, account.getCurrency());
                if (paymentCurrency.equals(account.getCurrency())) {
                    exchange_rate = 1.0;
                }

                account.setBalance(account.getBalance() - totalSum / impliedUsers.size() * exchange_rate);
            }

            for (User user : impliedUsers) {
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                DecimalFormat df = new DecimalFormat("#.00", symbols);
                user.getTransactions().add(new SplitPaymentTransaction(timestamp,
                        "Split payment of " + df.format(totalSum) + " " + paymentCurrency, paymentCurrency,
                        totalSum / impliedUsers.size(), accounts));
            }
        }
    }

    public void addInterest(CommandInput commandInput) {
        String requestedIBAN = commandInput.getAccount();
        double newInterestRate = commandInput.getInterestRate();

        Account requestedAccount = getAccountByName(requestedIBAN);
        if (requestedAccount == null) {
            return;
        }

        if (requestedAccount.getType().equals("savings")) {
            requestedAccount.setInterestRate(requestedAccount.getInterestRate() + newInterestRate);
        }
    }

    public void changeInterestRate(CommandInput commandInput) {
        String requestedIBAN = commandInput.getAccount();
        double newInterestRate = commandInput.getInterestRate();

        Account requestedAccount = getAccountByName(requestedIBAN);
        if (requestedAccount == null) {
            return;
        }

        if (requestedAccount.getType().equals("savings")) {
            requestedAccount.setInterestRate(newInterestRate);
        }
    }

    public ObjectNode report(CommandInput commandInput) {
        String commandName = commandInput.getCommand();
        Integer startTimestamp = commandInput.getStartTimestamp();
        Integer endTimestamp = commandInput.getEndTimestamp();
        String requestedIBAN = commandInput.getAccount();

        User requestedUser = getUserByIBAN(requestedIBAN);
        Account requestedAccount = getAccountByName(requestedIBAN);

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("command", commandName);

        ObjectNode accountNode = mapper.createObjectNode();
        if (requestedUser == null || requestedAccount == null) {
            accountNode.put("timestamp", commandInput.getTimestamp());
            accountNode.put("description", "Account not found");
        } else {
            accountNode.put("IBAN", requestedIBAN);
            accountNode.put("balance", requestedAccount.getBalance());
            accountNode.put("currency", requestedAccount.getCurrency());

            ArrayNode transactionsArray = mapper.createArrayNode();

            for (Transaction transaction : requestedUser.getTransactions()) {
                if (transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                    transactionsArray.add(transaction.createTransactionObjectNode(mapper));
                }
            }

            accountNode.put("transactions", transactionsArray);
        }
        reportNode.put("output", accountNode);

        reportNode.put("timestamp", commandInput.getTimestamp());
        return reportNode;
    }

    public ObjectNode spendingReport(CommandInput commandInput) {
        String commandName = commandInput.getCommand();
        ObjectNode spendingReportObject = mapper.createObjectNode();

        Integer startTimestamp = commandInput.getStartTimestamp();
        Integer endTimestamp = commandInput.getEndTimestamp();
        String requestedIBAN = commandInput.getAccount();
        User requestedUser = getUserByIBAN(requestedIBAN);
        Account requestedAccount = getAccountByName(requestedIBAN);

        spendingReportObject.put("command", commandName);
        ObjectNode resultNode = mapper.createObjectNode();
        String errorMessage = "";

        if (requestedAccount == null) {
            errorMessage = "Account not found";
        } else if (requestedUser == null) {
            errorMessage = "User not found";
        } else if (requestedAccount.getType().equals("savings")) {
            errorMessage = "This kind of report is not supported for a saving account";
        }

        if (!errorMessage.isEmpty()) {
            resultNode.put("error", errorMessage);
        } else {
            // no errors found, normal spendingReport creation
            ArrayNode transactionsArray = mapper.createArrayNode();
            resultNode.put("IBAN", requestedAccount.getIBAN());
            resultNode.put("balance", requestedAccount.getBalance());
            resultNode.put("currency", requestedAccount.getCurrency());

            for (Transaction transaction : requestedUser.getTransactions()) {
                if (transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp
                                        && transaction.getTransactionType().startsWith("PayOnline")) {
                    transactionsArray.add(transaction.createTransactionObjectNode(mapper));
                }
            }

            resultNode.put("transactions", transactionsArray);
            ArrayNode accountsArray = mapper.createArrayNode();

            for (CommerciantSummary commerciantSummary : requestedUser.retrieveCommerciants()) {
                ObjectNode commerciantNode = mapper.createObjectNode();
                commerciantNode.put("commerciant", commerciantSummary.getCommerciantName());
                commerciantNode.put("total", commerciantSummary.getAmount());

                accountsArray.add(commerciantNode);
            }

            resultNode.put("commerciants", accountsArray);
        }

        spendingReportObject.put("output", resultNode);
        spendingReportObject.put("timestamp", commandInput.getTimestamp());

        return spendingReportObject;
    }

    public void clearResources() {
        for (User user : users) {
            if (user.getUserType().equals("commerciant")) {
                continue;
            }
            for (Account account : user.getAccounts()) {
                account.getAccountCards().clear();
            }

            user.getAccounts().clear();
        }
        users.clear();
        exchangeDatabase.clear();
        databaseInstance = null;
    }

    public static Database getDatabaseInstance(ObjectInput inputData) {
        if (Database.databaseInstance == null) {
            Database.databaseInstance = new Database(inputData);
        }

        return Database.databaseInstance;
    }

    public static void setDatabaseInstance(Database databaseInstance) {
        Database.databaseInstance = databaseInstance;
    }
}
