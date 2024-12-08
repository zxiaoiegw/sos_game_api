package llm;

public class SimpleGameMode extends GameModeBase {
    private boolean gameOver;
    //private char[][] board;

    public SimpleGameMode() {
        super(3); // Simple game mode always uses a 3x3 board
        this.gameOver = false;
    }

    @Override
    protected void handleSOSFound() {
        gameOver = true; // Game ends immediately when an SOS is formed
    }

    @Override
    public int getBlueScore() {
        return 0; // No scoring in simple mode
    }

    @Override
    public int getRedScore() {
        return 0; // No scoring in simple mode
    }

    @Override
    public boolean isGameOver() {
    	// Game is over if an SOS is formed or the board is full
        return gameOver || super.isGameOver();
    }
}
