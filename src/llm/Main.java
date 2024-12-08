package llm;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GUI gui = new GUI();
                new Board(gui); // Initialize the game board with the GUI
                gui.setVisible(true); // Make the GUI visible
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}