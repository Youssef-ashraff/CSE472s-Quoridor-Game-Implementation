package ai_project.search;
import ai_project.board.Board;
import ai_project.board.model.Move;
import ai_project.eval.EvaluationFunction;

public interface SearchStrategy {
    Move chooseMove(Board board, int playerId, int depth, EvaluationFunction eval);
}