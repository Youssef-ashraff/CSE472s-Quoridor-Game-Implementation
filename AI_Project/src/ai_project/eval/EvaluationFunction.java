package ai_project.eval;
import ai_project.board.Board;

public interface EvaluationFunction {
    double evaluate(Board state, int playerId, int opponentId);
}