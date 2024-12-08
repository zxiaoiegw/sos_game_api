package llm;

public class GeneralGameMode extends GameModeBase {
    private int blueScore; // Score for the blue player
    private int redScore; // Score for the red player

    public GeneralGameMode(int boardSize) {
        super(boardSize);
        this.blueScore = 0;
        this.redScore = 0;
    }

    @Override
    protected void handleSOSFound() {
    	// Update the score for the current player based on the number of sequences formed
        int sequencesFormed = lastMoveSequences.size();
        if (currentPlayer == 'B') {
            blueScore += sequencesFormed;  // Add points equal to number of sequences formed
        } else {
            redScore += sequencesFormed;   // Add points equal to number of sequences formed
        }
    }

    @Override
    public int getBlueScore() {
        return blueScore;
    }

    @Override
    public int getRedScore() {
        return redScore;
    }
}
