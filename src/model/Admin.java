package model;

public class Admin extends User {
    public Admin(String userID, String username, String password, String firstName, String lastName, String createdAt, String updatedAt){
        super(userID, username, password, firstName, lastName, createdAt, updatedAt);
    }
}