package ai_project.search;

import ai_project.board.Board;
import ai_project.board.model.Move;
import ai_project.eval.EvaluationFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MinimaxSearch implements SearchStrategy {
    private final Random rng = new Random();

    @Override
    public Move chooseMove(Board board, int playerId, int depth, EvaluationFunction eval) {
        List<Move> legal = board.getLegalMoves(playerId);
        if (legal.isEmpty()) return null;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Move> bestMoves = new ArrayList<>();
        double alpha = Double.NEGATIVE_INFINITY, beta = Double.POSITIVE_INFINITY;
        int opponentId = (playerId == 1) ? 2 : 1;

        for (Move move : legal) {
            double score = minimax(board.applyMove(move), depth - 1, false, playerId, opponentId, eval, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (Math.abs(score - bestScore) < 1e-9) {
                bestMoves.add(move);
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMoves.get(rng.nextInt(bestMoves.size()));
    }

    private double minimax(Board state, int depth, boolean maximizing, int pid, int oppId, EvaluationFunction eval, double alpha, double beta) {
        if (depth == 0 || state.isTerminal()) return eval.evaluate(state, pid, oppId);
        
        int current = maximizing ? pid : oppId;
        List<Move> moves = state.getLegalMoves(current);
        if (moves.isEmpty()) return eval.evaluate(state, pid, oppId);

        double best = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (Move move : moves) {
            double val = minimax(state.applyMove(move), depth - 1, !maximizing, pid, oppId, eval, alpha, beta);
            if (maximizing) {
                best = Math.max(best, val);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, val);
                beta = Math.min(beta, best);
            }
            if (beta <= alpha) break;
        }
        return best;
    }
}