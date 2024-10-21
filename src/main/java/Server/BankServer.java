package Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class BankServer {
    private static Map<String, User> userBalances = new HashMap<>();
    private static Set<String> loggedInUsers = new HashSet<>();
    private static BankServerGUI gui;

    public static void main(String[] args) {
        loadUserData();
        gui = new BankServerGUI();

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
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine(); // Đọc username từ client
                String password = in.readLine(); // Đọc password từ client
                String clientIp = socket.getInetAddress().getHostAddress();

                synchronized (loggedInUsers) {
                    // Kiểm tra nếu user đã đăng nhập
                    if (loggedInUsers.contains(username)) {
                        out.println("User already logged in");

                        // Cập nhật lại GUI nếu người dùng đã đăng nhập
                        gui.updateUserList(username, clientIp); // Luôn cập nhật GUI để hiển thị người dùng
                        return; // Không làm gì thêm khi user đã đăng nhập
                    } else if (validateUser(username, password)) {
                        loggedInUsers.add(username); // Thêm user vào danh sách loggedInUsers
                        gui.updateUserList(username, clientIp); // Cập nhật giao diện với IP của user
                        out.println("Login successful");
                    } else {
                        out.println("Invalid username or password");
                        socket.close();
                        return;
                    }
                }

                // Xử lý các lệnh khác từ client (ví dụ: chuyển tiền)
                String command;
                while ((command = in.readLine()) != null) {
                    if (command.startsWith("TRANSFER")) {
                        String[] parts = command.split(" ");
                        String toUser = parts[1];
                        double amount = Double.parseDouble(parts[2]);

                        synchronized (userBalances) {
                            if (!userBalances.containsKey(toUser)) {
                                out.println("Invalid user: " + toUser);
                                continue;
                            }

                            if (userBalances.get(username).balance < amount) {
                                out.println("Insufficient funds");
                                continue;
                            }

                            boolean success = performTransfer(username, toUser, amount);
                            if (success) {
                                userBalances.get(username).balance -= amount;
                                userBalances.get(toUser).balance += amount;
                                gui.addTransaction(username, toUser, amount);
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
                    // Đóng socket và cập nhật GUI khi user thoát
                    String clientIp = socket.getInetAddress().getHostAddress();
                    synchronized (loggedInUsers) {
                        loggedInUsers.remove(username); // Xóa user khỏi danh sách loggedInUsers
                        gui.removeUser(username, clientIp); // Cập nhật GUI
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Kiểm tra xem người dùng đã có trong GUI hay chưa
        private boolean isUserInGUI(String username) {
            for (int i = 0; i < gui.getUserListModel().size(); i++) {
                if (gui.getUserListModel().getElementAt(i).contains(username)) {
                    return true;
                }
            }
            return false;
        }

        private boolean performTransfer(String fromUser, String toUser, double amount) {
            String updateUserBalanceSQL = "UPDATE Users SET balance = balance + ? WHERE user_id = ?";
            String insertTransactionSQL = "INSERT INTO Transactions (from_user, to_user, amount, transaction_date) VALUES (?, ?, ?, ?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement updateFromUser = connection.prepareStatement(updateUserBalanceSQL);
                 PreparedStatement updateToUser = connection.prepareStatement(updateUserBalanceSQL);
                 PreparedStatement insertTransaction = connection.prepareStatement(insertTransactionSQL)) {

                connection.setAutoCommit(false);

                int fromUserId = userBalances.get(fromUser).userId;
                int toUserId = userBalances.get(toUser).userId;

                updateFromUser.setDouble(1, -amount);
                updateFromUser.setInt(2, fromUserId);
                int rowsAffectedFrom = updateFromUser.executeUpdate();

                updateToUser.setDouble(1, amount);
                updateToUser.setInt(2, toUserId);
                int rowsAffectedTo = updateToUser.executeUpdate();

                insertTransaction.setInt(1, fromUserId);
                insertTransaction.setInt(2, toUserId);
                insertTransaction.setDouble(3, amount);
                insertTransaction.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                int rowsAffectedTransaction = insertTransaction.executeUpdate();

                if (rowsAffectedFrom > 0 && rowsAffectedTo > 0 && rowsAffectedTransaction > 0) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
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

    static class BankServerGUI {
        private JFrame frame;
        private JTable transactionTable;
        private DefaultListModel<String> userListModel;

        public BankServerGUI() {
            frame = new JFrame("Bank Server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400);
            frame.setLayout(new BorderLayout());

            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BorderLayout());
            frame.add(leftPanel, BorderLayout.WEST);

            // JList to display logged-in users
            userListModel = new DefaultListModel<>();
            JList<String> userList = new JList<>(userListModel);
            JScrollPane userScrollPane = new JScrollPane(userList);
            leftPanel.add(userScrollPane, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BorderLayout());
            frame.add(rightPanel, BorderLayout.CENTER);

            // Table for transaction information
            transactionTable = new JTable(new DefaultTableModel(new Object[]{"From User", "To User", "Amount"}, 0));
            JScrollPane scrollPane = new JScrollPane(transactionTable);
            rightPanel.add(scrollPane, BorderLayout.CENTER);

            frame.setVisible(true);
        }

        // Update the user list with the username and IP
        public void updateUserList(String username, String ip) {
            userListModel.addElement(username + " (" + ip + ")");
        }

        // Remove user from the list
        public void removeUser(String username, String ip) {
            userListModel.removeElement(username + " (" + ip + ")");
        }

        // Add transaction to the table
        public void addTransaction(String fromUser, String toUser, double amount) {
            DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
            model.addRow(new Object[]{fromUser, toUser, amount});
        }
        public DefaultListModel<String> getUserListModel() {
            return userListModel;
        }
    }
}
