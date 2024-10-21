package Server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class BankServer {
    private static Map<String, User> userBalances = new HashMap<>();
    private static Set<String> loggedInUsers = new HashSet<>();

    public static void main(String[] args) {
        loadUserData();

        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server started on port 1234");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUserData() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT user_id, username, password, balance FROM Users")) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                double balance = resultSet.getDouble("balance");
                userBalances.put(username, new User(userId, username, password, balance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String username = in.readLine();
                String password = in.readLine();

                synchronized (loggedInUsers) {
                    if (loggedInUsers.contains(username)) {
                        out.println("User already logged in");
                        socket.close();
                        return;
                    } else if (validateUser(username, password)) {
                        loggedInUsers.add(username);
                        out.println("Login successful");
                    } else {
                        out.println("Invalid username or password");
                        socket.close();
                        return;
                    }
                }

                String command;
                while ((command = in.readLine()) != null) {
                    if (command.startsWith("TRANSFER")) {
                        String[] parts = command.split(" ");
                        String toUser = parts[1];
                        double amount = Double.parseDouble(parts[2]);

                        synchronized (userBalances) {
                            // Check if 'toUser' exists
                            if (!userBalances.containsKey(toUser)) {
                                out.println("Invalid user: " + toUser);
                                continue; // Stop further processing for this command
                            }

                            // Check if 'fromUser' has sufficient funds
                            if (userBalances.get(username).balance < amount) {
                                out.println("Insufficient funds");
                                continue; // Stop further processing for this command
                            }

                            // Perform the transfer and database update
                            boolean success = performTransfer(username, toUser, amount);

                            if (success) {
                                // Reflect the updated balances in the local map
                                userBalances.get(username).balance -= amount;
                                userBalances.get(toUser).balance += amount;

                                out.println("Transfer successful");
                            } else {
                                out.println("Transfer failed");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (loggedInUsers) {
                    loggedInUsers.remove(socket);
                }
            }
        }

        private boolean performTransfer(String fromUser, String toUser, double amount) {
            String updateUserBalanceSQL = "UPDATE Users SET balance = balance + ? WHERE user_id = ?";
            String insertTransactionSQL = "INSERT INTO Transactions (from_user, to_user, amount, transaction_date) VALUES (?, ?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement updateFromUser = connection.prepareStatement(updateUserBalanceSQL);
                 PreparedStatement updateToUser = connection.prepareStatement(updateUserBalanceSQL);
                 PreparedStatement insertTransaction = connection.prepareStatement(insertTransactionSQL)) {

                connection.setAutoCommit(false); // Enable transaction management

                // Retrieve the user IDs for fromUser and toUser
                int fromUserId = userBalances.get(fromUser).userId;
                int toUserId = userBalances.get(toUser).userId;

                // Deduct amount from 'fromUser'
                updateFromUser.setDouble(1, -amount);  // Deducting amount
                updateFromUser.setInt(2, fromUserId);  // Use user_id for fromUser
                int rowsAffectedFrom = updateFromUser.executeUpdate();

                // Add amount to 'toUser'
                updateToUser.setDouble(1, amount);  // Adding amount
                updateToUser.setInt(2, toUserId);  // Use user_id for toUser
                int rowsAffectedTo = updateToUser.executeUpdate();

                // Log transaction using correct user IDs for fromUser and toUser
                insertTransaction.setInt(1, fromUserId);  // Log from_user as fromUserId
                insertTransaction.setInt(2, toUserId);  // Log to_user as toUserId
                insertTransaction.setDouble(3, amount);  // Log the amount
                insertTransaction.setTimestamp(4, new Timestamp(System.currentTimeMillis()));  // Log current timestamp
                int rowsAffectedTransaction = insertTransaction.executeUpdate();

                if (rowsAffectedFrom > 0 && rowsAffectedTo > 0 && rowsAffectedTransaction > 0) {
                    connection.commit();  // Commit the transaction
                    return true;
                } else {
                    connection.rollback();  // Rollback if any operation fails
                    System.out.println("Transfer failed: Transaction rollback.");
                    return false;
                }

            } catch (SQLException e) {
                System.err.println("SQL Exception during transfer: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        private boolean validateUser(String username, String password) {
            User user = userBalances.get(username);
            return user != null && user.password.equals(password);
        }
    }

    static class User {
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

}
