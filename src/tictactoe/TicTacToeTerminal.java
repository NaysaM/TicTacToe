package tictactoe;

import java.sql.*;
import java.util.Scanner;
import java.sql.Connection;


    public class TicTacToeTerminal {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/tictactoedatabase";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "naysa";


        private static final String PLAYER_X = "\u001B[38;5;208mX\u001B[0m"; // Orange X

        private static final String PLAYER_O = "\u001B[30mO\u001B[0m"; // Black O



        private static final String EMPTY_CELL = " ";

        private static final String[] CELL_COLORS = {
                "\u001B[41m", "\u001B[42m", "\u001B[43m",
                "\u001B[44m", "\u001B[45m", "\u001B[46m",
                "\u001B[47m", "\u001B[100m", "\u001B[101m"
        }; // Different background colors

        private String[] board = new String[9];
        private String currentPlayer;
        private Connection connection;
        private int userId;
        private Scanner scanner;
        private boolean playAgainstBot;

        public TicTacToeTerminal() {
            connectToDatabase();
            scanner = new Scanner(System.in);
            currentPlayer = PLAYER_X;
            for (int i = 0; i < 9; i++) {
                board[i] = EMPTY_CELL;
            }
        }

        public static void main(String[] args) {
            TicTacToeTerminal game = new TicTacToeTerminal();
            game.loginOrRegister();
            game.chooseGameMode();
            game.playGame();
        }

        private void chooseGameMode() {
            System.out.println("Kies een spelmodus:");
            System.out.println("1. Speler tegen Speler");
            System.out.println("2. Speler tegen Bot");
            int choice = scanner.nextInt();
            playAgainstBot = (choice == 2);
        }

        private void playGame() {
            while (true) {
                printBoard();
                if (currentPlayer.equals(PLAYER_X) || !playAgainstBot) {
                    playerMove();
                } else {
                    botMove();
                }
                if (checkWin()) {
                    printBoard();
                    System.out.println("Speler " + (currentPlayer.equals(PLAYER_X) ? "X" : "O") + " wint!");
                    saveScore(1);
                    resetBoard();
                    showHighScores();
                } else if (isBoardFull()) {
                    printBoard();
                    System.out.println("Het is een gelijkspel!");
                    saveScore(0);
                    resetBoard();
                    showHighScores();
                } else {
                    currentPlayer = currentPlayer.equals(PLAYER_X) ? PLAYER_O : PLAYER_X;
                }
            }
        }

        private void printBoard() {
            for (int i = 0; i < 9; i++) {
                System.out.print(CELL_COLORS[i] + (board[i].equals(EMPTY_CELL) ? (i + 1) : board[i]) + "\u001B[0m ");
                if ((i + 1) % 3 == 0) {
                    System.out.println();
                } else {
                    System.out.print("| ");
                }
                if (i == 2 || i == 5) {
                    System.out.println("---------");
                }
            }
        }

        private void playerMove() {
            int move;
            while (true) {
                System.out.println("Speler " + (currentPlayer.equals(PLAYER_X) ? "X" : "O") + ", kies een vakje (1-9):");
                move = scanner.nextInt() - 1;
                if (move >= 0 && move < 9 && board[move].equals(EMPTY_CELL)) {
                    board[move] = currentPlayer;
                    break;
                } else {
                    System.out.println("Ongeldige zet, probeer opnieuw.");
                }
            }
        }

        private void botMove() {
            System.out.println("Bot (" + currentPlayer + ") maakt een zet:");
            for (int i = 0; i < 9; i++) {
                if (board[i].equals(EMPTY_CELL)) {
                    board[i] = currentPlayer;
                    break;
                }
            }
        }

        private boolean checkWin() {
            String[][] boardArr = new String[3][3];
            for (int i = 0; i < 9; i++) {
                boardArr[i / 3][i % 3] = board[i];
            }

            for (int i = 0; i < 3; i++) {
                if (boardArr[i][0].equals(boardArr[i][1]) && boardArr[i][1].equals(boardArr[i][2]) && !boardArr[i][0].equals(EMPTY_CELL)) return true;
                if (boardArr[0][i].equals(boardArr[1][i]) && boardArr[1][i].equals(boardArr[2][i]) && !boardArr[0][i].equals(EMPTY_CELL)) return true;
            }

            if (boardArr[0][0].equals(boardArr[1][1]) && boardArr[1][1].equals(boardArr[2][2]) && !boardArr[0][0].equals(EMPTY_CELL)) return true;
            if (boardArr[0][2].equals(boardArr[1][1]) && boardArr[1][1].equals(boardArr[2][0]) && !boardArr[0][2].equals(EMPTY_CELL)) return true;

            return false;
        }

        private boolean isBoardFull() {
            for (String cell : board) {
                if (cell.equals(EMPTY_CELL)) {
                    return false;
                }
            }
            return true;
        }

        private void resetBoard() {
            for (int i = 0; i < 9; i++) {
                board[i] = EMPTY_CELL;
            }
            currentPlayer = PLAYER_X;
        }

        private void loginOrRegister() {
            System.out.println("Kies een optie:");
            System.out.println("1. Log in");
            System.out.println("2. Maak een profiel aan");
            System.out.println("3. Bekijk Top Scores");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume the newline

            if (choice == 1) {
                login();
            } else if (choice == 2) {
                register();
            } else if (choice == 3) {
                showHighScores();
                loginOrRegister(); // Show options again after displaying high scores
            }
        }

        private void login() {
            System.out.print("Naam: ");
            String name = scanner.nextLine();
            System.out.print("Code: ");
            String code = scanner.nextLine();

            try {
                String query = "SELECT id FROM Users WHERE name = ? AND code = ?";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, code);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("id");
                    System.out.println("Inloggen succesvol!");
                } else {
                    System.out.println("Ongeldige naam of code, probeer opnieuw.");
                    login();
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void register() {
            System.out.print("Naam: ");
            String name = scanner.nextLine();
            System.out.print("Code: ");
            String code = scanner.nextLine();
            System.out.print("Geboortedatum (YYYY-MM-DD): ");
            String birthdate = scanner.nextLine();

            try {
                String query = "INSERT INTO Users (name, code, birthdate) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, code);
                stmt.setString(3, birthdate);
                stmt.executeUpdate();
                System.out.println("Profiel succesvol aangemaakt!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void saveScore(int win) {
            try {
                String query = "INSERT INTO Scores (user_id, score, completed_games) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE score = score + ?, completed_games = completed_games + 1";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setInt(2, win);
                stmt.setInt(3, 1);
                stmt.setInt(4, win);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void showHighScores() {
            try {
                String query = "SELECT u.name, s.score, s.completed_games FROM Scores s JOIN Users u ON s.user_id = u.id ORDER BY s.score DESC LIMIT 10";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                System.out.println("Top 10 Scores:");
                while (rs.next()) {
                    String name = rs.getString("name");
                    int score = rs.getInt("score");
                    int completedGames = rs.getInt("completed_games");
                    System.out.printf("%s - Score: %d, Voltooide Spellen: %d%n", name, score, completedGames);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void connectToDatabase() {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Verbonden met de database.");
            } catch (SQLException e) {
                System.out.println("Kon geen verbinding maken met de database");
                e.printStackTrace();
            }
        }

        private void closeConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Verbinding met de database gesloten.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

