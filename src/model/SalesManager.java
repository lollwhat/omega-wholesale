package model;

public class SalesManager extends User{
    protected String userID;
    protected String username;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String createdAt;
    protected String updatedAt;

    public SalesManager(String userID, String username, String password, String firstName, String lastName, String createdAt, String updatedAt){
        super(userID, username, password, firstName, lastName, createdAt, updatedAt);
    }
}
