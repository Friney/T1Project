package ru.t1.accountservice.api;

public final class Paths {
    public static final String CLIENTS = "/clients";
    public static final String CLIENTS_ID = "/clients/{clientId}";
    public static final String ACCOUNTS = CLIENTS_ID + "/accounts";
    public static final String ACCOUNTS_ID = "/accounts/{accountId}";
    public static final String TRANSACTIONS = ACCOUNTS_ID + "/transactions";

    private Paths() {
    }
}
