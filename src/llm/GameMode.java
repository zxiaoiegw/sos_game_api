package llm;

import java.util.List;

public interface GameMode {
    boolean makeMove(int row, int col, char letter); // Make a move on the board
    char getCurrentPlayer(); // Get the current player, 'B' or 'R'
    int getBlueScore(); // Get the current score of the blue player
    int getRedScore(); // Get the current score of the red player
    boolean isGameOver(); // Check if the game is over
    void switchPlayer(); // Switch to the next player
    
    // Get the SOS sequences formed in the last move
    List<GameModeBase.SOSSequence> getLastMoveSequences();
}
