import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (Exception e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean userExists(String username) {
        try {
            String userFilePath = "users.txt";
            File file = new File(userFilePath);

            if (!file.exists()) {
                return false;
            }

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(username + ":")) {
                    return true;
                }
            }

            scanner.close();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean checkPassword(String username, String password) {
        try {
            String userFilePath = "users.txt";
            File file = new File(userFilePath);

            if (!file.exists()) {
                return false;
            }

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(username + ":" + password)) {
                    return true;
                }
            }

            scanner.close();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void loginUser() {
        try {
            Socket socket = new Socket("127.0.0.1", 8000);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();

            if (userExists(username)) {
                System.out.println("Enter your password: ");
                String password = scanner.nextLine();

                if (checkPassword(username, password)) {
                    System.out.println("You logged in successfully!");
                    Client client = new Client(socket, username);
                    client.listenForMessage();
                    client.sendMessage();
                } else {
                    System.out.println("Invalid credentials. Exiting...");
                }
            } else {
                System.out.println("User not found. Exiting...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean registerUser(String username, String password, Socket socket) {
        try {
            String userFilePath = "users.txt";
            File file = new File(userFilePath);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(username + ":" + password + System.lineSeparator());
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void signUpUser() {
        try {
            Socket socket = new Socket("127.0.0.1", 8000);
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your new username: ");
            String username = scanner.nextLine();

            if (!userExists(username)) {
                System.out.println("Enter your new password: ");
                String password = scanner.nextLine();

                if (registerUser(username, password, socket)) {
                    System.out.println("User registered successfully. You can now log in.");
                    loginUser();
                } else {
                    System.out.println("Registration failed. Exiting...");
                }
            } else {
                System.out.println("Username already exists. Exiting...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press 0 to login or 1 to sign up: ");
        int choice = scanner.nextInt();

        if (choice == 0) {
            loginUser();
        } else if (choice == 1) {
            signUpUser();
        } else {
            System.out.println("Invalid choice. Exiting...");
        }
    }
}
