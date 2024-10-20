import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Main extends JFrame {
    private JButton[][] buttons = new JButton[3][3];
    private Socket playerXSocket;
    private Socket playerOSocket;
    private boolean isMyTurn = true;  // Serwer zaczyna jako gracz X
    private DataOutputStream playerXOutput;
    private DataInputStream playerXInput;
    private DataOutputStream playerOOutput;
    private DataInputStream playerOInput;

    public Main() {
        setTitle("Kółko i Krzyżyk - Serwer (Gracz X)");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 3));

        initializeButtons();

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                System.out.println("Serwer uruchomiony. Oczekiwanie na graczy...");

                // Oczekiwanie na połączenie gracza X
                playerXSocket = serverSocket.accept();
                System.out.println("Gracz X połączony.");

                // Inicjalizacja strumieni dla gracza X
                playerXInput = new DataInputStream(playerXSocket.getInputStream());
                playerXOutput = new DataOutputStream(playerXSocket.getOutputStream());

                // Oczekiwanie na połączenie gracza O
                playerOSocket = serverSocket.accept();
                System.out.println("Gracz O połączony.");

                // Inicjalizacja strumieni dla gracza O
                playerOInput = new DataInputStream(playerOSocket.getInputStream());
                playerOOutput = new DataOutputStream(playerOSocket.getOutputStream());

                // Wysłanie informacji startowej do graczy
                playerXOutput.writeUTF("START X");
                playerOOutput.writeUTF("START O");
/*
                // Odbieranie ruchów od gracza O i przekazywanie ich do gracza X
                while (true) {
                    String moveFromO = playerOInput.readUTF();  // Ruch od gracza O
                    //processMove(moveFromO, "O");  // Przetwarzanie ruchu gracza O
                }

 */
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initializeButtons() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new JButton("");
                buttons[row][col].setFont(new Font("Arial", Font.PLAIN, 60));
                buttons[row][col].addActionListener(new ButtonClickListener(row, col));
                add(buttons[row][col]);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public void actionPerformed(ActionEvent e) {
            if (!buttons[row][col].getText().equals("") || !isMyTurn) return;

            // Aktualizujemy planszę dla gracza X
            buttons[row][col].setText("X");
            isMyTurn = false;

            try {
                // Wysyłanie ruchu gracza X do klienta (gracza O)
                if (playerXOutput != null) {
                    playerXOutput.writeUTF(row + "," + col);  // Wysłanie ruchu do klienta
                } else {
                    System.out.println("playerXOutput jest null, nie można wysłać ruchu.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Sprawdzanie, czy mamy zwycięzcę po ruchu gracza X
            String winner = checkWinner();
            if (winner != null) {
                JOptionPane.showMessageDialog(Main.this, "Zwycięstwo: " + winner);
                resetBoard();
            }
        }
    }

    private void processMove(String move, String mark) {
        System.out.println("Ruch od gracza " + mark + ": " + move);

        String[] parts = move.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        buttons[row][col].setText(mark); // Wstawienie ruchu gracza X
        isMyTurn = true;  // Teraz nasza kolej

        String winner = checkWinner(); // Sprawdzenie, czy jest zwycięzca
        if (winner != null) {
            System.out.println("Zwycięzca: " + winner);
            JOptionPane.showMessageDialog(Main.this, "Zwycięstwo: " + winner);
            resetBoard();
        }
    }

    private String checkWinner() {
        for (int row = 0; row < 3; row++) {
            if (buttons[row][0].getText().equals(buttons[row][1].getText()) &&
                    buttons[row][1].getText().equals(buttons[row][2].getText()) &&
                    !buttons[row][0].getText().equals("")) {
                return buttons[row][0].getText();
            }
        }

        for (int col = 0; col < 3; col++) {
            if (buttons[0][col].getText().equals(buttons[1][col].getText()) &&
                    buttons[1][col].getText().equals(buttons[2][col].getText()) &&
                    !buttons[0][col].getText().equals("")) {
                return buttons[0][col].getText();
            }
        }

        if (buttons[0][0].getText().equals(buttons[1][1].getText()) &&
                buttons[1][1].getText().equals(buttons[2][2].getText()) &&
                !buttons[0][0].getText().equals("")) {
            return buttons[0][0].getText();
        }

        if (buttons[0][2].getText().equals(buttons[1][1].getText()) &&
                buttons[1][1].getText().equals(buttons[2][0].getText()) &&
                !buttons[0][2].getText().equals("")) {
            return buttons[0][2].getText();
        }

        return null;
    }

    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col].setText("");
            }
        }
        isMyTurn = true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}
