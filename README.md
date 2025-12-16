# Quoridor AI Project

A complete implementation of the strategy board game **Quoridor** in Java, developed for **CSE472s: Artificial Intelligence**. This project features a graphical user interface, a smart AI opponent using Minimax, and a full implementation of the game rules.

---

## 📖 Game Description
**Quoridor** is a 2-player strategy board game played on a 9x9 grid.
* **Objective:** The goal is to be the first player to move their pawn to any square on the opposite side of the board.
* **Pieces:** Each player controls one pawn and starts with **10 walls**.
* **Gameplay:** On your turn, you may either:
    1.  **Move your pawn** one square orthogonally (horizontally or vertically).
    2.  **Place a wall** to block your opponent's path.
* **Rules:**
    * Walls are 2 squares long and cannot overlap or cross other walls.
    * You cannot place a wall that completely locks a player in; a valid path to the goal must always exist.
    * Pawns can jump over adjacent opponents.

## 📸 Screenshots

### 1. Main Menu
![Main Menu](https://github.com/Youssef-ashraff/CSE472s-Quoridor-Game-Implementation/blob/main/Screenshot%202025-12-16%20123237.png)

*Select Game Mode and AI Difficulty.*

### 2. Gameplay (Human vs. Bot)
![Gameplay Action](https://github.com/Youssef-ashraff/CSE472s-Quoridor-Game-Implementation/blob/main/Screenshot%202025-12-16%20123340.png)

*Visualizing valid moves (green) and placed walls (orange).*

### 3. Winning State
![Game Over](https://github.com/Youssef-ashraff/CSE472s-Quoridor-Game-Implementation/blob/main/Screenshot%202025-12-16%20123506.png)

*Winner announcement.*

## 🎥 Demo Video
**[Click Here to Watch the Demo Video](INSERT_YOUR_YOUTUBE_OR_DRIVE_LINK_HERE)**


## ⚙️ Installation and Running Instructions

### Prerequisites
* **Java Development Kit (JDK) 17** or higher.
* **JavaFX SDK** (Required if not bundled with your JDK).
* Any Java IDE (IntelliJ IDEA, Eclipse, or NetBeans).

### How to Run
1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/Youssef-ashraff/CSE472s-Quoridor-Game-Implementation.git](https://github.com/Youssef-ashraff/CSE472s-Quoridor-Game-Implementation.git)
    ```
2.  **Open in IDE:**
    * Open the project folder in your IDE.
    * Ensure the `src` folder is marked as the "Sources Root".
3.  **Run the Application:**
    * Navigate to `src/ai_project/AI_Project.java`.
    * Right-click the file and select **Run 'AI_Project'**.

## 🎮 Controls Explanation
The game is played entirely with the mouse.

* **Move Pawn:**
    * **Left-Click** on any highlighted **Green Cell** to move your pawn there.
    * Valid moves are automatically calculated based on walls and opponent position.

* **Place Wall:**
    * **Select Orientation:** Use the radio buttons at the bottom ("Horizontal" or "Vertical") to choose wall direction.
    * **Left-Click** on the **Gaps** between squares to place a wall.
    * Ghost walls appear on hover to show valid placements.
    * Invalid placements (overlapping or blocking the path) are prevented automatically.

* **Game Controls:**
    * **Undo:** Reverts the last move (in Human vs. Bot, it undoes both your move and the bot's move).
    * **Redo:** Re-applies the undone move.
    * **Reset:** Restarts the game instantly with the current settings.
    * **Menu:** Returns to the main screen to change difficulty or game mode.

---

## 🧠 AI Implementation Details
* **Algorithm:** Minimax Search with Alpha-Beta Pruning.
* **Heuristics:**
    * **Shortest Path:** Calculates distance to goal using BFS.
    * **Evaluation:** Prioritizes winning when close to the goal and blocks the opponent if they are about to win.
    * **Difficulty Levels:**
        * *Easy:* Random mistakes allowed.
        * *Medium:* Looks 2 moves ahead.
        * *Hard:* Looks 3 moves ahead with advanced trap detection.

## 👥 Team Members
* **Youssef Ashraf Mohammed** - 2201056
* **Student 2 Name** - ID
* **Student 3 Name** - ID
* **Student 4 Name** - ID
* **Student 5 Name** - ID
