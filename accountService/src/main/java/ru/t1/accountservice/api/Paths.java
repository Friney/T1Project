package ru.t1.accountservice.api;

public final class Paths {
    public static final String API = "/api";
    public static final String V1 = "/v1";
    public static final String AUTH_V1 = API + V1 + "/auth";
    public static final String CLIENTS_V1 = API + V1 + "/clients";
    public static final String CLIENTS_ID_V1 = API + V1 + "/clients/{clientId}";
    public static final String ACCOUNTS_V1 = CLIENTS_ID_V1 + "/accounts";
    public static final String ACCOUNTS_V1_WITHOUT_CLIENT = API + V1 + "/accounts";
    public static final String ACCOUNTS_ID_V1 = ACCOUNTS_V1_WITHOUT_CLIENT + "/{accountId}";
    public static final String TRANSACTIONS_V1 = ACCOUNTS_ID_V1 + "/transactions";

    private Paths() {
    }
}
