package com.licon.syncadaptertest.authentication;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "com.licon.syncadaptertest";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "licon-sync";

    /**
     * User data fields
     */
    public static final String USERDATA_USER_OBJ_ID = "userObjectId";   //Parse.com object id

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an licon-sync account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an licon-sync account";

    public static final ServerAuthenticate sServerAuthenticate = new ParseComServer();
}