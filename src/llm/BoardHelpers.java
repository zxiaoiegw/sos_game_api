package llm;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * The BoardHelpers class provides utility methods to assist the Board class
 */
public class BoardHelpers {
    private final Board board;
    private final GUI gui;

    public BoardHelpers(Board board, GUI gui) {
        this.board = board;
        this.gui = gui;
    }

    // Switches the current player to the next player
    public void switchPlayer() {
        board.setCurrentPlayer(board.getCurrentPlayer().equals("Blue") ? "Red" : "Blue");
        gui.updateTurnLabel(board.getCurrentPlayer());
    }

    // Get the current selected letter based on the current player's selection
    public char getCurrentSelectedLetter() {
        char selectedLetter;
        if (board.getCurrentPlayer().equals("Blue")) {
            selectedLetter = gui.getBlueS().isSelected() ? 'S' : 'O';
        } else {
            selectedLetter = gui.getRedS().isSelected() ? 'S' : 'O';
        }
        System.out.println("Current Player: " + board.getCurrentPlayer() + ", Selected Letter: " + selectedLetter);
        return selectedLetter;
    }

    // Determines the current player's color
    public Color currentPlayerColor() {
        return board.getCurrentPlayer().equals("Blue") ? Color.BLUE : Color.RED;
    }

    // Draw lines over the SOS sequences
    public void drawSOSLines(List<GameModeBase.SOSSequence> sequences) {
        for (GameModeBase.SOSSequence sequence : sequences) {
            GameButton firstButton = getButtonAt(sequence.row1, sequence.col1);
            GameButton secondButton = getButtonAt(sequence.row2, sequence.col2);
            GameButton thirdButton = getButtonAt(sequence.row3, sequence.col3);

            if (firstButton != null && secondButton != null && thirdButton != null) {
                Color color = currentPlayerColor();
                GameButton.Line line = new GameButton.Line(color, sequence.direction);
                firstButton.addLine(line);
                secondButton.addLine(line);
                thirdButton.addLine(line);

                firstButton.repaint();
                secondButton.repaint();
                thirdButton.repaint();
            }
        }
    }
    
    // Retrieve the button at a specific row and column
    public GameButton getButtonAt(int row, int col) {
        if (row >= 0 && row < board.getBoardSize() && col >= 0 && col < board.getBoardSize()) {
            int index = row * board.getBoardSize() + col;
            Component component = gui.getBoardPanel().getComponent(index);
            if (component instanceof GameButton) {
                return (GameButton) component;
            }
        }
        return null;
    }

    // Disable the game board
    public void disableBoard() {
        Component[] components = gui.getBoardPanel().getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                component.setEnabled(false);
            }
        }
    }

    // Update scores on the GUI
    public void updateScores() {
        int blueScore = board.getGameMode().getBlueScore();
        int redScore = board.getGameMode().getRedScore();
        gui.updateScores(blueScore, redScore);
    }
}