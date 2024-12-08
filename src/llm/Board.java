package llm;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.util.List;
import javax.swing.Timer;

public class Board {
    private String currentPlayer; // "Blue" or "Red"
    private int boardSize;
    private GUI gui;
    private boolean isSimpleMode;
    private GameMode gameMode;
    private ComputerPlayer blueComputer;
    private ComputerPlayer redComputer;
    private Timer computerMoveTimer;
    private boolean gameInProgress;
    private BoardHelpers helpers; // Helper methods for board operations
    
    private GameRecordService recorder;
    private boolean isRecording;
    private boolean isReplaying;
    private GameRecord replayGame;
    private int replayMoveIndex;
    private Timer replayTimer;

    private static final String CLAUDE_API_KEY = "api_key_here";
   
    // Initializes computer players based on user selection
    private void initializeComputerPlayers() {
        if (gui.getBlueComputer().isSelected()) {
            blueComputer = new ComputerPlayerAPI('B', gameMode, isSimpleMode, CLAUDE_API_KEY);
        }
        if (gui.getRedComputer().isSelected()) {
            redComputer = new ComputerPlayerAPI('R', gameMode, isSimpleMode, CLAUDE_API_KEY);
        }
    }
    
    public Board(GUI gui) {
        this.gui = gui;
        this.gameInProgress =false;
        this.helpers = new BoardHelpers(this, gui);
        
        this.recorder = new GameRecordService();
        this.replayTimer = new Timer(1000, e -> playNextReplayMove());
        replayTimer.setRepeats(true);

        // Add window listener to handle game saving when closing window
        gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        gui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isRecording && gameInProgress) {
                    int choice = JOptionPane.showConfirmDialog(
                        gui,
                        "Do you want to save the current game before exiting?",
                        "Save Game",
                        JOptionPane.YES_NO_CANCEL_OPTION
                    );
                    
                    if (choice == JOptionPane.CANCEL_OPTION) {
                        return; // Don't close if user cancels
                    }
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        recorder.interruptRecording(
                            gameMode.getBlueScore(),
                            gameMode.getRedScore()
                        );
                    }
                }
                
                // Add cleanup for Claude API
                if (blueComputer instanceof ComputerPlayerAPI) {
                    ((ComputerPlayerAPI) blueComputer).cleanup();
                }
                if (redComputer instanceof ComputerPlayerAPI) {
                    ((ComputerPlayerAPI) redComputer).cleanup();
                }
                
                gui.dispose(); // Close the window
                System.exit(0); // Exit the application
            }
        });
        
        // Action listener for New Game button to generate the game board
        gui.getBtnNewGame().addActionListener(e -> {
            if (isRecording && gameInProgress) {
                // Ask user if they want to save the current game
                int choice = JOptionPane.showConfirmDialog(
                    gui,
                    "Do you want to save the current game before starting a new one?",
                    "Save Game",
                    JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    recorder.interruptRecording(
                        gameMode.getBlueScore(),
                        gameMode.getRedScore()
                    );
                }
            }
            isSimpleMode = gui.getSimpleGame().isSelected();
            generateGameBoard();
        });
        
        // Initialize computer move timer with 600ms delay
        computerMoveTimer = new Timer(600, e -> makeComputerMove());
        computerMoveTimer.setRepeats(false);
        
        // Add listener for record checkbox
        gui.getRecordGameCheckBox().addActionListener(e -> {
            isRecording = gui.getRecordGameCheckBox().isSelected();
        });
        
        // Add listener for replay button
        gui.getBtnReplay().addActionListener(e -> startReplay());
    }

    // Method to generate the game board based on the input size
    private void generateGameBoard() {
        try {
            // First, set the board size and game mode
            if (!isReplaying) {
                // Get board size from input for new games
                boardSize = Integer.parseInt(gui.getBoardSizeField().getText());
                
                // Validate board size based on game mode
                if (isSimpleMode) {
                    if (boardSize != 3) {    
                        boardSize = 3; 
                        JOptionPane.showMessageDialog(gui, "Invalid board size. Defaulting to 3x3.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    gameMode = new SimpleGameMode();    
                } else {
                    if (boardSize < 4 || boardSize > 8) {
                        boardSize = 8;
                        JOptionPane.showMessageDialog(gui, "Invalid board size. Board size must be between 4-8. Defaulting to 8x8.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    gameMode = new GeneralGameMode(boardSize);
                }
            } else {
                // Use recorded settings for replay
                boardSize = replayGame.getBoardSize();
                if (isSimpleMode) {
                    gameMode = new SimpleGameMode();
                } else {
                    gameMode = new GeneralGameMode(boardSize);
                }
            }

            // Start recording after board size is properly set
            if (isRecording && !isReplaying) {
                String blueType = gui.getBlueComputer().isSelected() ? "Computer" : "Human";
                String redType = gui.getRedComputer().isSelected() ? "Computer" : "Human";              
                recorder.startRecording(isSimpleMode, boardSize, blueType, redType);
            }
            
            gameInProgress = true;
            currentPlayer = "Blue";
            initializeComputerPlayers();

            // Generate the game board on the GUI
            gui.getBoardPanel().removeAll();
            gui.getBoardPanel().setLayout(new GridLayout(boardSize, boardSize));

            // Create buttons for each cell
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    GameButton button = new GameButton(" ");
                    button.setFont(new Font("Arial", Font.BOLD, 35));
                    int finalRow = row;
                    int finalCol = col;
                    button.addActionListener(e -> handleButtonClick(button, finalRow, finalCol));
                    gui.getBoardPanel().add(button);
                }
            }

            gui.getBoardPanel().revalidate();
            gui.getBoardPanel().repaint();

            gui.updateTurnLabel(currentPlayer);
            gui.updateScores(0, 0);
            
            if (!isReplaying && gui.getBlueComputer().isSelected()) {
                computerMoveTimer.start();
            }
            
        } catch (NumberFormatException ex) {
            if (!isReplaying) {
                if (isSimpleMode) {
                    boardSize = 3;  
                    gameMode = new SimpleGameMode();
                } else {
                    boardSize = 8;  
                    gameMode = new GeneralGameMode(boardSize);
                }
                JOptionPane.showMessageDialog(gui, "Error with board size. Using default size.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }    
    }
    
    
    // Makes a move for the computer player when it's their turn
    private void makeComputerMove() {
        if (!gameInProgress) return;

        boolean isBlueComputer = currentPlayer.equals("Blue") && gui.getBlueComputer().isSelected();
        boolean isRedComputer = currentPlayer.equals("Red") && gui.getRedComputer().isSelected();

        if (isBlueComputer || isRedComputer) {
            ComputerPlayer currentComputerPlayer = isBlueComputer ? blueComputer : redComputer;
            char[][] boardState = getCurrentBoardState();
            
            ComputerPlayer.Move move = currentComputerPlayer.makeMove(boardState);
            if (move != null) {
                GameButton button = helpers.getButtonAt(move.row, move.col);
                if (button != null) {
                    button.setText(String.valueOf(move.letter));
                    button.setForeground(helpers.currentPlayerColor());
                    
                    // Record computer's move before processing
                    if (isRecording) {
                        recorder.recordMove(move.row, move.col, move.letter, currentPlayer);
                    }
                    
                    processMove(button, move.row, move.col, move.letter, true);
                }
            }
        }
    }
    
    // Retrieves the current state of the game board
    private char[][] getCurrentBoardState() {
    	char[][] board = new char[boardSize][boardSize];
    	for(int i =0; i < boardSize; i++) {
    		for (int j = 0; j < boardSize; j++) {
    			GameButton button = helpers.getButtonAt(i, j);
    			board[i][j] = button.getText().charAt(0);
    		}
    	}
    	return board;
    }
    
    // Handles moves when a cell is clicked by human player
    private void handleButtonClick(GameButton button, int row, int col) {
        if (!gameInProgress) return;
        
        // Check if it's a human player's turn
        boolean isHumanTurn = (currentPlayer.equals("Blue") && gui.getBlueHuman().isSelected()) ||
                             (currentPlayer.equals("Red") && gui.getRedHuman().isSelected());
        
        if (isHumanTurn && button.getText().equals(" ")) {
            char selectedLetter = helpers.getCurrentSelectedLetter();
            button.setText(String.valueOf(selectedLetter));
            button.setForeground(helpers.currentPlayerColor());
            processMove(button, row, col, selectedLetter, false);
        }
    }
    
    
    /**
     * Checks if a game is currently in progress
     * Displays a dialog for selecting a recorded game
     * Loads the selected game 
     */
    private void startReplay() {
        if (gameInProgress) return; // Prevent replay while a game is being played
        
        String[] games = recorder.listRecordedGames(); // Gets list of saved games
        if (games.length == 0) {
            JOptionPane.showMessageDialog(gui, "No recorded games found.");
            return;
        }
        
        // Display dialog for game selection
        String selected = (String) JOptionPane.showInputDialog(
            gui,
            "Choose a game to replay:",
            "Replay Game",
            JOptionPane.QUESTION_MESSAGE,
            null,
            games,   // The list from listRecordedGames()
            games[0]
        );
        
        // Load and setup the selected game if one was chosen
        if (selected != null) {
            replayGame = recorder.loadGame(selected);
            if (replayGame != null) {
                setupReplay();
            } 
        }
    }

    private void setupReplay() {   	
        // Set game mode and board size according to recorded game
        isSimpleMode = replayGame.isSimpleMode();
        boardSize = replayGame.getBoardSize();
        
        // update GUI to reflect the recored game mode
        gui.getSimpleGame().setSelected(isSimpleMode);
        gui.getGeneralGame().setSelected(!isSimpleMode);
        
        // Set player types in GUI to match recorded game
        gui.getBlueHuman().setSelected(replayGame.getBluePlayerType().equals("Human"));
        gui.getBlueComputer().setSelected(replayGame.getBluePlayerType().equals("Computer"));
        gui.getRedHuman().setSelected(replayGame.getRedPlayerType().equals("Human"));
        gui.getRedComputer().setSelected(replayGame.getRedPlayerType().equals("Computer"));
        
        // Create new board with recorded settings
        isReplaying = true;  
        generateGameBoard();
        
        // Start replay 
        replayMoveIndex = 0;
        replayTimer.start();
        
        // Disable controls during loading
        gui.getBtnNewGame().setEnabled(false);
        gui.getRecordGameCheckBox().setEnabled(false);
    }

    private void playNextReplayMove() {
    	// check if replay is complete
        if (replayMoveIndex >= replayGame.getMoves().size()) {
            replayTimer.stop();
            isReplaying = false;
            gui.getBtnNewGame().setEnabled(true);
            gui.getRecordGameCheckBox().setEnabled(true);
            
            // Update final scores for general game mode
            if (!isSimpleMode) {
                int recordedBlueScore = replayGame.getFinalBlueScore();
                int recordedRedScore = replayGame.getFinalRedScore();
                gui.updateScores(recordedBlueScore, recordedRedScore);
            }
            return;
        }
        
        //  Apply the next move
        GameRecord.Move move = replayGame.getMoves().get(replayMoveIndex++);
        GameButton button = helpers.getButtonAt(move.getRow(), move.getCol());
        if (button != null) {
            button.setText(String.valueOf(move.getLetter()));
            button.setForeground(move.getPlayer().equals("Blue") ? Color.BLUE : Color.RED);
            processMove(button, move.getRow(), move.getCol(), move.getLetter(), false);
        }
    } 
    

    // Processes a move made by a player, updating the game state, handles SOS formation and turn switching
    private void processMove(GameButton button, int row, int col, char letter, boolean isComputerMove) {
        boolean sosFormed = gameMode.makeMove(row, col, letter);
        List<GameModeBase.SOSSequence> sosSequences = gameMode.getLastMoveSequences();

        if (isRecording && !isComputerMove) {
            recorder.recordMove(row, col, letter, currentPlayer);
        }   
        
        if (sosFormed) {
            helpers.drawSOSLines(sosSequences);
            
            if (!isSimpleMode) {
                helpers.updateScores();
                gui.updateTurnLabel(currentPlayer + " (Another turn)");
            } else {
            	if (!isReplaying) {
            		JOptionPane.showMessageDialog(gui, currentPlayer + " Player" + " wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);		
            	}
                endGame();
                return;
            }
        } else {
            helpers.switchPlayer();
        }

        gui.updateTurnLabel(currentPlayer);

        if (gameMode.isGameOver()) {
            handleGameOver();
        } else if (!isReplaying && isComputerTurn()) {
            computerMoveTimer.start();
        }    
    }

    // Handles game over conditions and display the winner or draw
    private void handleGameOver() {
        if (isSimpleMode) {
            JOptionPane.showMessageDialog(gui, "Game Over! It's a draw.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            int blueScore = gameMode.getBlueScore();
            int redScore = gameMode.getRedScore();
            String winner = determineWinner(blueScore, redScore);
            JOptionPane.showMessageDialog(gui, winner, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        endGame();
    }

    // Determines the winner based on the scores in general game mode
    private String determineWinner(int blueScore, int redScore) {
        if (blueScore > redScore) {
            return "Blue player wins with " + blueScore + " points!";
        } else if (redScore > blueScore) {
            return "Red player wins with " + redScore + " points!";
        } else {
            return "It's a draw! Both players scored " + blueScore + " points.";
        }
    }

    // Checks if current turn belongs to a computer player
    private boolean isComputerTurn() {
        return (currentPlayer.equals("Blue") && gui.getBlueComputer().isSelected()) ||
               (currentPlayer.equals("Red") && gui.getRedComputer().isSelected());
    }

    // Ends the game and disabling the game board
    private void endGame() {
    	if (isRecording) {
            recorder.endRecording(
                gameMode.getBlueScore(),
                gameMode.getRedScore()
            );
        }
    	
        gameInProgress = false;
        computerMoveTimer.stop();
        helpers.disableBoard();
    }
    
    // Getters and setters needed by BoardHelpers
    public ComputerPlayer getBlueComputer() {
        return blueComputer;
    }

    public ComputerPlayer getRedComputer() {
        return redComputer;
    }
    
    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String player) {
        this.currentPlayer = player;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int size) {
        this.boardSize = size;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }
}