package ai_project.board.model;
public record Move(MoveKind kind, int row, int col, WallOrientation orientation) {
    public static Move pawn(int r, int c) { return new Move(MoveKind.PAWN, r, c, null); }
    public static Move wall(int r, int c, WallOrientation o) { return new Move(MoveKind.WALL, r, c, o); }
}