package llm;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    private JLabel boardSizeLabel;
    private JTextField boardSizeField;
    private JButton btnReplay,btnNewGame;
	private JRadioButton simpleGame, generalGame;

    private JRadioButton blueHuman, blueS, blueO, blueComputer;
    private JRadioButton redHuman, redS, redO, redComputer;
    private JCheckBox recordGameCheckBox;
    
	private JLabel currentTurnLabel;
    private JLabel blueScoreLabel;
    private JLabel redScoreLabel;
    private JPanel boardPanel;

    public GUI() {

        setTitle("SOS Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(new JLabel("SOS"));
        simpleGame = new JRadioButton("Simple game", true);
        generalGame = new JRadioButton("General game");
        
        // Group the game mode radio buttons
        ButtonGroup topPanelGroup = new ButtonGroup();
        topPanelGroup.add(simpleGame);
        topPanelGroup.add(generalGame);

        boardSizeLabel = new JLabel("Board size ");
        boardSizeField = new JTextField("8", 2);
        
        // Add components to the top panel
        topPanel.add(simpleGame);
        topPanel.add(generalGame);
        topPanel.add(boardSizeLabel);
        topPanel.add(boardSizeField);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components

        // Blue player panel on the left
        JPanel bluePlayerPanel = new JPanel();
        bluePlayerPanel.setLayout(new BoxLayout(bluePlayerPanel, BoxLayout.Y_AXIS));
        bluePlayerPanel.setBorder(BorderFactory.createTitledBorder("Blue player"));
        blueHuman = new JRadioButton("Human", true);
        blueS = new JRadioButton("S", true);
        blueO = new JRadioButton("O");
        blueComputer = new JRadioButton("Computer");

        // Grouping the radio buttons for blue player
        ButtonGroup bluePlayerGroup = new ButtonGroup();
        bluePlayerGroup.add(blueHuman);
        bluePlayerGroup.add(blueComputer);

        ButtonGroup blueSOGroup = new ButtonGroup();
        blueSOGroup.add(blueS);
        blueSOGroup.add(blueO);

        // Create a panel for S and O options and indent them
        JPanel blueSubPanel = new JPanel();
        blueSubPanel.setLayout(new BoxLayout(blueSubPanel, BoxLayout.Y_AXIS));
        blueSubPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0)); // Indentation

        blueSubPanel.add(blueS);
        blueSubPanel.add(blueO);

        // Add components to the blue player panel
        bluePlayerPanel.add(blueHuman);
        bluePlayerPanel.add(blueSubPanel);
        bluePlayerPanel.add(blueComputer);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.BOTH;
        centerPanel.add(bluePlayerPanel, gbc);

        // Middle Panel for the Game Board
        boardPanel = new JPanel();
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        boardPanel.setPreferredSize(new Dimension(400, 400));

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        centerPanel.add(boardPanel, gbc);

        // Red player panel on the right
        JPanel redPlayerPanel = new JPanel();
        redPlayerPanel.setLayout(new BoxLayout(redPlayerPanel, BoxLayout.Y_AXIS));
        redPlayerPanel.setBorder(BorderFactory.createTitledBorder("Red player"));
        
        // Red player options
        redHuman = new JRadioButton("Human", true);
        redS = new JRadioButton("S", true);
        redO = new JRadioButton("O");
        redComputer = new JRadioButton("Computer");

        // Group the radio buttons for red player
        ButtonGroup redPlayerGroup = new ButtonGroup();
        redPlayerGroup.add(redHuman);
        redPlayerGroup.add(redComputer);

        ButtonGroup redSOGroup = new ButtonGroup();
        redSOGroup.add(redS);
        redSOGroup.add(redO);

        // Sub-panel for red player's 'S' and 'O' selection
        JPanel redSubPanel = new JPanel();
        redSubPanel.setLayout(new BoxLayout(redSubPanel, BoxLayout.Y_AXIS));
        redSubPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        redSubPanel.add(redS);
        redSubPanel.add(redO);

        // Add components to the red player panel
        redPlayerPanel.add(redHuman);
        redPlayerPanel.add(redSubPanel);
        redPlayerPanel.add(redComputer);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.BOTH;
        centerPanel.add(redPlayerPanel, gbc);

        // Add center panel to the frame
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel for Game Controls and status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        recordGameCheckBox = new JCheckBox("Record game");
        currentTurnLabel = new JLabel("Current turn: Blue");
        blueScoreLabel = new JLabel("Blue Score: 0");
        redScoreLabel = new JLabel("Red Score: 0");
        btnReplay = new JButton("Replay");
        btnNewGame = new JButton("New Game");

        // Add components to the bottom panel
        bottomPanel.add(recordGameCheckBox);
        bottomPanel.add(currentTurnLabel);
        bottomPanel.add(blueScoreLabel);
        bottomPanel.add(redScoreLabel);
        bottomPanel.add(btnReplay);
        bottomPanel.add(btnNewGame);
        
        // Add the bottom panel to the frame
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Add listeners for computer/human radio buttons
        blueHuman.addActionListener(e -> updatePlayerControls("Blue"));
        blueComputer.addActionListener(e -> updatePlayerControls("Blue"));
        redHuman.addActionListener(e -> updatePlayerControls("Red"));
        redComputer.addActionListener(e -> updatePlayerControls("Red"));

        // Add listeners for game mode selection
        simpleGame.addActionListener(e -> updateGameModeSettings());
        generalGame.addActionListener(e -> updateGameModeSettings());
        
        // Set default close operation
        //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    // Update the score labels for blue and red players
    public void updateScores(int blueScore, int redScore) {
        blueScoreLabel.setText("Blue Score: " + blueScore);
        redScoreLabel.setText("Red Score: " + redScore);
    }
    
    public void updatePlayerControls(String player) {
        if (player.equals("Blue")) {
            // Enable S/O selection
            blueS.setEnabled(blueHuman.isSelected());
            blueO.setEnabled(blueHuman.isSelected());
        } else {
            // Enable S/O selection
            redS.setEnabled(redHuman.isSelected());
            redO.setEnabled(redHuman.isSelected());
        }
    }
   
    private void updateGameModeSettings() {
    	// For simple game mode: enforce 3x3 board size
        if (simpleGame.isSelected()) {
            boardSizeField.setText("3");
            boardSizeField.setEnabled(false);
            // For general game mode: if current size is 3, set default general game mode size to 8
        } else {
            boardSizeField.setEnabled(true);
            if (boardSizeField.getText().equals("3")) {
                boardSizeField.setText("8");
            }
        }
    }

    // Update the turn label to show computer moves
    public void updateTurnLabel(String player) {
        String playerType = "";
        if (player.equals("Blue")) {
            playerType = blueComputer.isSelected() ? " (Computer)" : " (Human)";
        } else {
            playerType = redComputer.isSelected() ? " (Computer)" : " (Human)";
        }
        currentTurnLabel.setText("Current turn: " + player + playerType);
    }

    // Add method to disable/enable player controls during game
    public void setControlsEnabled(boolean enabled) {
        blueHuman.setEnabled(enabled);
        blueComputer.setEnabled(enabled);
        redHuman.setEnabled(enabled);
        redComputer.setEnabled(enabled);
        simpleGame.setEnabled(enabled);
        generalGame.setEnabled(enabled);
        boardSizeField.setEnabled(enabled && generalGame.isSelected());
    }
    
    // getters
    public JTextField getBoardSizeField() {
        return boardSizeField;
    }

    public JButton getBtnNewGame() {
        return btnNewGame;
    }
    
    public JButton getBtnReplay() {
		return btnReplay;
	}
    
    public JCheckBox getRecordGameCheckBox() {
		return recordGameCheckBox;
	}

    public JPanel getBoardPanel() {
        return boardPanel;
    }

    // Getters for blue player choices
    public JRadioButton getBlueS() {
        return blueS;
    }

    public JRadioButton getBlueO() {
        return blueO;
    }

    public JRadioButton getBlueHuman() {
        return blueHuman;
    }

    public JRadioButton getBlueComputer() {
        return blueComputer;
    }

    // Getters for red player choices
    public JRadioButton getRedS() {
        return redS;
    }

    public JRadioButton getRedO() {
        return redO;
    }

    public JRadioButton getRedHuman() {
        return redHuman;
    }

    public JRadioButton getRedComputer() {
        return redComputer;
    }

    public JRadioButton getSimpleGame() {
        return simpleGame;
    }

    public JRadioButton getGeneralGame() {
        return generalGame;
    }
}
