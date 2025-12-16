package ai_project.eval;

import ai_project.board.Board;
import ai_project.board.QuoridorBoard;
import ai_project.board.model.Move;
import ai_project.board.model.MoveKind;
import ai_project.board.model.Pos;
import ai_project.board.model.WallOrientation;

import java.util.List;

public final class PathLengthEvaluation implements EvaluationFunction {

    @Override
    public double evaluate(Board state, int playerId, int opponentId) {
        if (state.isTerminal()) {
            Integer w = state.getWinner();
            if (w == null) return 0.0;
            return w == playerId ? 1_000_000.0 : -1_000_000.0;
        }

        int myDist = state.shortestPathLength(playerId);
        int oppDist = state.shortestPathLength(opponentId);

        if (myDist == Integer.MAX_VALUE) return -100_000.0;
        if (oppDist == Integer.MAX_VALUE) return 100_000.0;

        // --- 1. Base Score (Path Difference) ---
        // We still prioritize being closer than the opponent.
        double score = (oppDist - myDist) * 10.0;

        // --- 2. "Kill Move" / "Panic" Logic ---
        // If we are extremely close, prioritize winning above everything.
        if (myDist <= 1) score += 5000.0;
        else if (myDist <= 3) score += 500.0;

        // If opponent is extremely close, panic and prioritize blocking.
        if (oppDist <= 1) score -= 5000.0;
        else if (oppDist <= 3) score -= 500.0;

        // --- 3. ADVANCED: Path Redundancy (The "Trap" Detector) ---
        // A path is only "safe" if we can't be easily blocked.
        // We roughly estimate this by checking how many immediate pawn moves move us closer.
        long myGoodMoves = countGoodMoves(state, playerId, myDist);
        long oppGoodMoves = countGoodMoves(state, opponentId, oppDist);

        // Reward having multiple options; penalize opponent having options.
        score += (myGoodMoves * 2.0);
        score -= (oppGoodMoves * 2.0);

        // --- 4. Wall Conservation (Tie-Breaker) ---
        // Prefer saving walls if the result is otherwise equal.
        if (state instanceof QuoridorBoard) {
            QuoridorBoard qb = (QuoridorBoard) state;
            int wallsUsed = (playerId == 1) ? qb.getP1Walls() : qb.getP2Walls();
            score -= (wallsUsed * 0.1); 
        }

        return score;
    }

    /**
     * Counts how many immediate pawn moves result in a distance < current distance.
     * This helps the AI prefer "wide" paths over "narrow" paths that are easily blocked.
     */
    private long countGoodMoves(Board board, int pid, int currentDist) {
        return board.getLegalMoves(pid).stream()
            .filter(m -> m.kind() == MoveKind.PAWN)
            .map(board::applyMove)
            .filter(nextBoard -> nextBoard.shortestPathLength(pid) < currentDist)
            .count();
    }
}