package Server;

public class User {
    int userId;
    String username;
    String password;
    double balance;

    User(int userId, String username, String password, double balance) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.balance = balance;
    }
}
