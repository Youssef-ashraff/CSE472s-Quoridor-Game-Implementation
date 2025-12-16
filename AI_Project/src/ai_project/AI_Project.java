package ai_project;

import ai_project.agent.AIBot;
import ai_project.board.Board;
import ai_project.board.QuoridorBoard;
import ai_project.board.model.Move;
import ai_project.board.model.MoveKind;
import ai_project.board.model.WallOrientation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos; 
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;

public class AI_Project extends Application {

    // --- Configuration ---
    private static final int CELL_SIZE = 50;
    private static final int GAP_SIZE = 12;
    private static final int BOARD_PIXEL_SIZE = (9 * CELL_SIZE) + (8 * GAP_SIZE);

    // --- Colors ---
    private static final Color COLOR_BG = Color.web("#3E2723");        // Dark Wood Background
    private static final Color COLOR_CELL = Color.web("#EFEBE9");      // Light Cream Cells
    private static final Color COLOR_WALL_PREVIEW = Color.web("#BCAAA4", 0.5); // Ghost wall
    private static final Color COLOR_WALL_PLACED = Color.web("#D84315"); // Burnt Orange Walls
    private static final Color COLOR_P1 = Color.web("#C62828");        // Red Pawn
    private static final Color COLOR_P2 = Color.web("#1565C0");        // Blue Pawn
    private static final Color COLOR_HIGHLIGHT = Color.web("#A5D6A7"); // Valid Move Green

    // --- Game State ---
    private Board board;
    private AIBot aiBot;
    
    // Undo/Redo History
    private Stack<Board> undoStack = new Stack<>();
    private Stack<Board> redoStack = new Stack<>();

    private boolean isVsComputer = true;
    private String aiDifficulty = "medium";
    private boolean isProcessingTurn = false;
    
    private String p1Name = "Player 1";
    private String p2Name = "Player 2";

    // --- UI Components ---
    private StackPane rootLayout;
    private VBox menuPane;
    private BorderPane gamePane;
    private GridPane boardGrid;
    private Label statusLabel;
    private Label p1WallLabel;
    private Label p2WallLabel;
    private RadioButton rbHorizontal;
    
    private Button btnUndo;
    private Button btnRedo;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Root Layout handles layering (Menu over Game)
        rootLayout = new StackPane();
        initGameView();
        initMenuView();

        // Show Menu first
        gamePane.setVisible(false);
        menuPane.setVisible(true);

        rootLayout.getChildren().addAll(gamePane, menuPane);

        Scene scene = new Scene(rootLayout, BOARD_PIXEL_SIZE + 100, BOARD_PIXEL_SIZE + 180);
        primaryStage.setTitle("Quoridor - CSE472s Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- Initialization Views ---

    private void initMenuView() {
        menuPane = new VBox(20);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-padding: 40;");

        Text title = new Text("QUORIDOR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setFill(COLOR_BG);

        // Game Mode
        Label modeLabel = new Label("Select Game Mode:");
        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("Human vs. Computer", "Human vs. Human");
        modeCombo.setValue("Human vs. Computer");

        // Difficulty
        Label diffLabel = new Label("AI Difficulty:");
        ComboBox<String> diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll("Easy", "Medium", "Hard");
        diffCombo.setValue("Medium");

        // Disable difficulty if PvP is selected
        modeCombo.setOnAction(e -> {
            boolean vsComp = modeCombo.getValue().equals("Human vs. Computer");
            diffLabel.setDisable(!vsComp);
            diffCombo.setDisable(!vsComp);
        });

        Button startButton = new Button("START GAME");
        startButton.setStyle("-fx-font-size: 18px; -fx-background-color: #3E2723; -fx-text-fill: white; -fx-padding: 10 30;");
        startButton.setOnAction(e -> {
            isVsComputer = modeCombo.getValue().equals("Human vs. Computer");
            aiDifficulty = diffCombo.getValue();
            startGame();
        });

        menuPane.getChildren().addAll(title, modeLabel, modeCombo, diffLabel, diffCombo, startButton);
    }

    private void initGameView() {
        gamePane = new BorderPane();
        gamePane.setPadding(new Insets(15));
        gamePane.setStyle("-fx-background-color: #D7CCC8;"); // Outer BG

        // --- HUD (Top) ---
        HBox hud = new HBox(20);
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(0, 0, 15, 0));
        
        statusLabel = new Label("Player 1's Turn");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statusLabel.setTextFill(COLOR_BG);
        
        p1WallLabel = createWallLabel("P1 Walls: 0 / 10", COLOR_P1);
        p2WallLabel = createWallLabel("P2 Walls: 0 / 10", COLOR_P2);
        
        hud.getChildren().addAll(p1WallLabel, statusLabel, p2WallLabel);
        gamePane.setTop(hud);

        // --- Board Grid (Center) ---
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setBackground(new Background(new BackgroundFill(COLOR_BG, new CornerRadii(5), Insets.EMPTY)));
        boardGrid.setPadding(new Insets(10));
        gamePane.setCenter(boardGrid);

        // --- Controls (Bottom) ---
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(15, 0, 0, 0));

        Label orientLabel = new Label("Walls:");
        ToggleGroup group = new ToggleGroup();
        rbHorizontal = new RadioButton("Horiz");
        rbHorizontal.setToggleGroup(group);
        rbHorizontal.setSelected(true);
        RadioButton rbVertical = new RadioButton("Vert");
        rbVertical.setToggleGroup(group);
        
        btnUndo = new Button("Undo");
        btnUndo.setOnAction(e -> handleUndo());
        btnUndo.setDisable(true); 
        
        btnRedo = new Button("Redo");
        btnRedo.setOnAction(e -> handleRedo());
        btnRedo.setDisable(true);
        
        Button btnReset = new Button("Reset");
        btnReset.setOnAction(e -> startGame()); // Restart match
        
        Button menuButton = new Button("Menu");
        menuButton.setOnAction(e -> showMenu());

        controls.getChildren().addAll(
            orientLabel, rbHorizontal, rbVertical, 
            new Separator(javafx.geometry.Orientation.VERTICAL),
            btnUndo, btnRedo,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            btnReset, menuButton
        );
        gamePane.setBottom(controls);
    }
    
    private Label createWallLabel(String text, Color color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(color);
        return l;
    }

    // --- State Management ---

    private void showMenu() {
        gamePane.setVisible(false);
        menuPane.setVisible(true);
    }

    private void startGame() {
        // Initialize logic
        board = new QuoridorBoard();
        undoStack.clear();
        redoStack.clear();
        updateUndoRedoButtons();
        
        // Configure Mode & Names
        if (isVsComputer) {
            aiBot = new AIBot(2, aiDifficulty);
            p1Name = "Human";
            p2Name = "Bot";
        } else {
            aiBot = null;
            p1Name = "Player 1";
            p2Name = "Player 2";
        }
        isProcessingTurn = false;
        
        // Show Game
        menuPane.setVisible(false);
        gamePane.setVisible(true);
        updateHUD(p1Name + "'s Turn");
        renderBoard();
    }
    
    // --- Undo / Redo Logic ---

    private void saveStateForUndo() {
        undoStack.push(board);
        redoStack.clear(); 
        updateUndoRedoButtons();
    }

    private void handleUndo() {
        if (undoStack.isEmpty() || isProcessingTurn) return;

        redoStack.push(board);
        board = undoStack.pop();

        // Smart Undo for PvAI: Undo twice to skip back to Human turn
        if (isVsComputer && board.getToMove() == 2) {
            if (!undoStack.isEmpty()) {
                redoStack.push(board);
                board = undoStack.pop();
            }
        }

        renderBoard();
        String name = (board.getToMove() == 1) ? p1Name : p2Name;
        updateHUD(name + "'s Turn");
        updateUndoRedoButtons();
    }

    private void handleRedo() {
        if (redoStack.isEmpty() || isProcessingTurn) return;

        undoStack.push(board);
        board = redoStack.pop();

        // Smart Redo for PvAI: Redo twice to include AI turn
        if (isVsComputer && board.getToMove() == 2) {
            if (!redoStack.isEmpty()) {
                undoStack.push(board);
                board = redoStack.pop();
            }
        }

        renderBoard();
        String name = (board.getToMove() == 1) ? p1Name : p2Name;
        updateHUD(name + "'s Turn");
        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        btnUndo.setDisable(undoStack.isEmpty());
        btnRedo.setDisable(redoStack.isEmpty());
    }

    // --- Rendering Logic ---

    private void renderBoard() {
        boardGrid.getChildren().clear();
        QuoridorBoard qb = (QuoridorBoard) board;
        int activePlayer = board.getToMove();
        
        // Only allow input if human turn and not currently thinking
        boolean isHumanMoving = !isProcessingTurn && (!isVsComputer || activePlayer == 1);
        List<Move> legalMoves = isHumanMoving ? board.getLegalMoves(activePlayer) : List.of();

        // 1. Draw Cells
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(COLOR_CELL);
                cell.setArcWidth(10); cell.setArcHeight(10); 

                // Highlight valid pawn moves
                for (Move m : legalMoves) {
                    if (m.kind() == MoveKind.PAWN && m.row() == r && m.col() == c) {
                        cell.setFill(COLOR_HIGHLIGHT);
                    }
                }

                // Click Handler
                final int row = r;
                final int col = c;
                cell.setOnMouseClicked(e -> {
                    if (isHumanMoving) handlePawnClick(row, col);
                });

                boardGrid.add(cell, c * 2, r * 2);

                // Draw Pawns
                if (qb.getP1Pos().row() == r && qb.getP1Pos().col() == c) {
                    drawPawn(COLOR_P1, c, r);
                } else if (qb.getP2Pos().row() == r && qb.getP2Pos().col() == c) {
                    drawPawn(COLOR_P2, c, r);
                }
            }
        }

        // 2. Draw Gaps (Interactive)
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 8; c++) {
                Rectangle vGap = new Rectangle(GAP_SIZE, CELL_SIZE);
                vGap.setFill(Color.TRANSPARENT);
                final int row = r;
                final int col = c;
                boolean finalIsHumanMoving = isHumanMoving;
                
                vGap.setOnMouseEntered(e -> { if(finalIsHumanMoving) vGap.setFill(COLOR_WALL_PREVIEW); });
                vGap.setOnMouseExited(e -> vGap.setFill(Color.TRANSPARENT));
                vGap.setOnMouseClicked(e -> { 
                    if (finalIsHumanMoving) handleWallClick(row, col, WallOrientation.VERTICAL); 
                });
                boardGrid.add(vGap, c * 2 + 1, r * 2);
            }
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 9; c++) {
                Rectangle hGap = new Rectangle(CELL_SIZE, GAP_SIZE);
                hGap.setFill(Color.TRANSPARENT);
                final int row = r;
                final int col = c;
                boolean finalIsHumanMoving = isHumanMoving;
                
                hGap.setOnMouseEntered(e -> { if(finalIsHumanMoving) hGap.setFill(COLOR_WALL_PREVIEW); });
                hGap.setOnMouseExited(e -> hGap.setFill(Color.TRANSPARENT));
                hGap.setOnMouseClicked(e -> { 
                    if (finalIsHumanMoving) handleWallClick(row, col, WallOrientation.HORIZONTAL); 
                });
                boardGrid.add(hGap, c * 2, r * 2 + 1);
            }
        }

        // 3. Draw Placed Walls
        for (ai_project.board.model.Pos p : qb.getWallsH()) {
            Rectangle w = new Rectangle((CELL_SIZE * 2) + GAP_SIZE, GAP_SIZE, COLOR_WALL_PLACED);
            w.setArcWidth(5); w.setArcHeight(5);
            w.setEffect(new DropShadow(5, Color.BLACK));
            boardGrid.add(w, p.col() * 2, p.row() * 2 + 1);
            GridPane.setColumnSpan(w, 3);
        }
        for (ai_project.board.model.Pos p : qb.getWallsV()) {
            Rectangle w = new Rectangle(GAP_SIZE, (CELL_SIZE * 2) + GAP_SIZE, COLOR_WALL_PLACED);
            w.setArcWidth(5); w.setArcHeight(5);
            w.setEffect(new DropShadow(5, Color.BLACK));
            boardGrid.add(w, p.col() * 2 + 1, p.row() * 2);
            GridPane.setRowSpan(w, 3);
        }

        // Update Wall Counts
        p1WallLabel.setText(p1Name + " Walls: " + qb.getP1Walls() + " / 10");
        p2WallLabel.setText(p2Name + " Walls: " + qb.getP2Walls() + " / 10");
    }
    
    private void drawPawn(Color color, int c, int r) {
        Rectangle pawn = new Rectangle(30, 30, color);
        pawn.setArcWidth(30); pawn.setArcHeight(30);
        pawn.setStroke(Color.WHITE);
        pawn.setStrokeWidth(2);
        pawn.setEffect(new DropShadow(3, Color.BLACK));
        boardGrid.add(pawn, c * 2, r * 2);
        GridPane.setHalignment(pawn, javafx.geometry.HPos.CENTER);
    }

    // --- Move Execution ---

    private void handlePawnClick(int r, int c) {
        if (isProcessingTurn || board.isTerminal()) return;
        Move move = Move.pawn(r, c);
        
        int currentPlayer = board.getToMove();
        if (isMoveLegal(move, currentPlayer)) {
            executeMove(move);
        }
    }

    private void handleWallClick(int r, int c, WallOrientation clickedOrientation) {
        if (isProcessingTurn || board.isTerminal()) return;
        
        // Prioritize Radio Button selection, otherwise use clicked gap orientation
        WallOrientation orientation = rbHorizontal.isSelected() ? 
                                      WallOrientation.HORIZONTAL : WallOrientation.VERTICAL;
        
        Move move = Move.wall(r, c, orientation);
        int currentPlayer = board.getToMove();
        
        if (isMoveLegal(move, currentPlayer)) {
            executeMove(move);
        } else {
            statusLabel.setText("Invalid Wall Placement!");
        }
    }

    private boolean isMoveLegal(Move m, int pid) {
        return board.getLegalMoves(pid).contains(m);
    }

    private void executeMove(Move move) {
        saveStateForUndo();
        
        board = board.applyMove(move);
        renderBoard();
        
        if (checkGameStatus()) return;

        int nextPlayer = board.getToMove();

        // If it's now Bot's turn, trigger it
        if (isVsComputer && nextPlayer == 2) {
            triggerAITurn();
        } else {
            // Update turn text for PvP
            String nextName = (nextPlayer == 1) ? p1Name : p2Name;
            updateHUD(nextName + "'s Turn");
        }
    }

    private void triggerAITurn() {
        isProcessingTurn = true;
        updateHUD(p2Name + " is thinking...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                // Short delay for visual pacing
                Thread.sleep(700); 
                Move aiMove = aiBot.chooseMove(board);
                return aiMove;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(aiMove -> {
            Platform.runLater(() -> {
                if (aiMove != null) {
                    try {
                        // Save state BEFORE AI moves so Undo works
                        undoStack.push(board);
                        redoStack.clear();
                        updateUndoRedoButtons();
                        
                        board = board.applyMove(aiMove);
                        if (!checkGameStatus()) {
                            updateHUD(p1Name + "'s Turn");
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                        updateHUD("AI Error");
                    }
                }
                isProcessingTurn = false; 
                renderBoard(); 
            });
        });
    }

    private boolean checkGameStatus() {
        if (board.isTerminal()) {
            Integer winner = board.getWinner();
            String winnerName = (winner == 1) ? p1Name : p2Name;
            String msg = winnerName.toUpperCase() + " WON!";
            
            updateHUD(msg);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
            alert.setHeaderText("Game Over");
            alert.show();
            return true;
        }
        return false;
    }

    private void updateHUD(String msg) {
        statusLabel.setText(msg);
    }
}