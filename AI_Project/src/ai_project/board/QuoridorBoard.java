package ai_project.board;

import ai_project.board.model.Move;
import ai_project.board.model.MoveKind;
import ai_project.board.model.Pos;
import ai_project.board.model.WallOrientation;
import java.util.*;

public final class QuoridorBoard implements Board {

    public static final int SIZE = 9;
    public static final int MAX_WALLS = 10; // 10 walls per player

    private final Pos p1Pos, p2Pos;
    private final int p1Walls, p2Walls; // Represents "Walls Used" (0 -> 10)
    private final Set<Pos> wallsH, wallsV;
    private final int toMove;

    public QuoridorBoard() {
        // P1 starts at (8,4) [Bottom], P2 at (0,4) [Top]
        // Walls Used initialized to 0
        this(new Pos(8, 4), new Pos(0, 4), 0, 0, new HashSet<>(), new HashSet<>(), 1);
    }

    private QuoridorBoard(Pos p1, Pos p2, int w1, int w2, Set<Pos> wh, Set<Pos> wv, int tm) {
        this.p1Pos = p1; this.p2Pos = p2; this.p1Walls = w1; this.p2Walls = w2;
        this.wallsH = Collections.unmodifiableSet(wh);
        this.wallsV = Collections.unmodifiableSet(wv);
        this.toMove = tm;
    }

    private QuoridorBoard copyWith(Pos p1, Pos p2, int w1, int w2, Set<Pos> wh, Set<Pos> wv, int tm) {
        return new QuoridorBoard(p1, p2, w1, w2, wh, wv, tm);
    }

    private int otherPlayer(int pid) { return pid == 1 ? 2 : 1; }
    private Pos posOf(int pid) { return pid == 1 ? p1Pos : p2Pos; }
    private int goalRow(int pid) { return pid == 1 ? 0 : 8; }
    
    // Helper to get walls used by specific player ID
    private int getWallsUsed(int pid) { return pid == 1 ? p1Walls : p2Walls; }

    @Override
    public boolean isTerminal() { return p1Pos.row() == 0 || p2Pos.row() == 8; }

    @Override
    public Integer getWinner() {
        if (p1Pos.row() == 0) return 1;
        if (p2Pos.row() == 8) return 2;
        return null;
    }

    @Override
    public List<Move> getLegalMoves(int playerId) {
        if (isTerminal()) return List.of();
        List<Move> moves = new ArrayList<>();
        
        // 1. Pawn Moves
        for (Pos t : legalPawnTargets(playerId)) moves.add(Move.pawn(t.row(), t.col()));
        
        // 2. Wall Moves (Only if used < 10)
        if (getWallsUsed(playerId) < MAX_WALLS) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Pos cell = new Pos(r, c);
                    if (isWallPlacementValid(playerId, cell, WallOrientation.HORIZONTAL))
                        moves.add(Move.wall(r, c, WallOrientation.HORIZONTAL));
                    if (isWallPlacementValid(playerId, cell, WallOrientation.VERTICAL))
                        moves.add(Move.wall(r, c, WallOrientation.VERTICAL));
                }
            }
        }
        return moves;
    }

    @Override
    public Board applyMove(Move move) {
        Pos np1 = p1Pos, np2 = p2Pos;
        int nw1 = p1Walls, nw2 = p2Walls;
        Set<Pos> nwh = new HashSet<>(wallsH), nwv = new HashSet<>(wallsV);
        
        if (move.kind() == MoveKind.PAWN) {
            if (toMove == 1) np1 = new Pos(move.row(), move.col());
            else np2 = new Pos(move.row(), move.col());
        } else {
            // Validate
            if (!isWallPlacementValid(toMove, new Pos(move.row(), move.col()), move.orientation()))
                throw new IllegalArgumentException("Illegal wall move detected: " + move);
                
            // Increment Wall Count (Used)
            if (toMove == 1) nw1++; else nw2++; 
            
            if (move.orientation() == WallOrientation.HORIZONTAL) nwh.add(new Pos(move.row(), move.col()));
            else nwv.add(new Pos(move.row(), move.col()));
        }
        return copyWith(np1, np2, nw1, nw2, nwh, nwv, otherPlayer(toMove));
    }

    @Override
    public int shortestPathLength(int playerId) {
        Pos start = posOf(playerId);
        int goal = goalRow(playerId);
        
        // BFS
        Queue<Pos> q = new ArrayDeque<>();
        q.add(start);
        int[][] dist = new int[SIZE][SIZE];
        for(int[] row : dist) Arrays.fill(row, -1);
        dist[start.row()][start.col()] = 0;

        while (!q.isEmpty()) {
            Pos cur = q.remove();
            if (cur.row() == goal) return dist[cur.row()][cur.col()];
            
            for (Pos nb : neighbors4(cur)) {
                if (dist[nb.row()][nb.col()] == -1 && !isEdgeBlocked(cur, nb)) {
                    dist[nb.row()][nb.col()] = dist[cur.row()][cur.col()] + 1;
                    q.add(nb);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private List<Pos> neighbors4(Pos p) {
        List<Pos> res = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}, dc = {0, 0, -1, 1};
        for (int i=0; i<4; i++) {
            int nr = p.row()+dr[i], nc = p.col()+dc[i];
            if (nr>=0 && nr<SIZE && nc>=0 && nc<SIZE) res.add(new Pos(nr, nc));
        }
        return res;
    }

    private boolean isEdgeBlocked(Pos a, Pos b) {
        if (a.col() == b.col()) { // Vertical move
            int r = Math.min(a.row(), b.row());
            if (wallsH.contains(new Pos(r, a.col()))) return true;
            if (a.col() > 0 && wallsH.contains(new Pos(r, a.col()-1))) return true;
        } else { // Horizontal move
            int c = Math.min(a.col(), b.col());
            if (wallsV.contains(new Pos(a.row(), c))) return true;
            if (a.row() > 0 && wallsV.contains(new Pos(a.row()-1, c))) return true;
        }
        return false;
    }
    
    private List<Pos> legalPawnTargets(int pid) {
        List<Pos> res = new ArrayList<>();
        Pos my = posOf(pid), opp = posOf(otherPlayer(pid));
        
        for (Pos n : neighbors4(my)) {
            if (isEdgeBlocked(my, n)) continue;
            
            if (!n.equals(opp)) {
                res.add(n);
            } else { 
                // Jump Logic
                int dr = n.row() - my.row(), dc = n.col() - my.col();
                Pos jump = new Pos(n.row()+dr, n.col()+dc);
                
                if (jump.row()>=0 && jump.row()<SIZE && jump.col()>=0 && jump.col()<SIZE && !isEdgeBlocked(n, jump)) {
                    res.add(jump);
                } else { 
                    if (dr!=0) {
                        if(n.col()>0 && !isEdgeBlocked(n, new Pos(n.row(), n.col()-1))) res.add(new Pos(n.row(), n.col()-1));
                        if(n.col()<SIZE-1 && !isEdgeBlocked(n, new Pos(n.row(), n.col()+1))) res.add(new Pos(n.row(), n.col()+1));
                    } else {
                        if(n.row()>0 && !isEdgeBlocked(n, new Pos(n.row()-1, n.col()))) res.add(new Pos(n.row()-1, n.col()));
                        if(n.row()<SIZE-1 && !isEdgeBlocked(n, new Pos(n.row()+1, n.col()))) res.add(new Pos(n.row()+1, n.col()));
                    }
                }
            }
        }
        return res;
    }

    private boolean isWallPlacementValid(int pid, Pos cell, WallOrientation o) {
        // --- CHANGE: Check constraint (Must be less than 10 used) ---
        if (getWallsUsed(pid) >= MAX_WALLS) return false;

        // 1. Overlap & Crossing Checks
        if (o == WallOrientation.HORIZONTAL) {
            if (wallsH.contains(cell) || wallsV.contains(cell)) return false;
            if (wallsH.contains(new Pos(cell.row(), cell.col()-1))) return false;
            if (wallsH.contains(new Pos(cell.row(), cell.col()+1))) return false;
        } else {
            if (wallsV.contains(cell) || wallsH.contains(cell)) return false;
            if (wallsV.contains(new Pos(cell.row()-1, cell.col()))) return false;
            if (wallsV.contains(new Pos(cell.row()+1, cell.col()))) return false;
        }
        
        // 2. Path Existence Check
        Set<Pos> nh = new HashSet<>(wallsH), nv = new HashSet<>(wallsV);
        if(o == WallOrientation.HORIZONTAL) nh.add(cell); else nv.add(cell);
        
        QuoridorBoard test = copyWith(p1Pos, p2Pos, p1Walls, p2Walls, nh, nv, toMove);
        return test.shortestPathLength(1) != Integer.MAX_VALUE && test.shortestPathLength(2) != Integer.MAX_VALUE;
    }

    @Override public int getToMove() { return toMove; }
    
    // Getters
    public Pos getP1Pos() { return p1Pos; }
    public Pos getP2Pos() { return p2Pos; }
    public int getP1Walls() { return p1Walls; }
    public int getP2Walls() { return p2Walls; }
    public Set<Pos> getWallsH() { return wallsH; }
    public Set<Pos> getWallsV() { return wallsV; }
}