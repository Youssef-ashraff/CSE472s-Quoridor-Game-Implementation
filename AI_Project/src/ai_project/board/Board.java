package ai_project.board;
import ai_project.board.model.Move;
import java.util.List;

public interface Board {
    boolean isTerminal();
    Integer getWinner();
    List<Move> getLegalMoves(int playerId);
    Board applyMove(Move move);
    int shortestPathLength(int playerId);
    int getToMove();
}