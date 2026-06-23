package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChessPiece {
    private static final int[][] KNIGHT_OFFSETS = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    private static final int[][] BISHOP_DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] ROOK_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    private static final int[][] QUEEN_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] KING_OFFSETS = {
            {1, 0}, {1, 1}, {0, 1}, {-1, 1},
            {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
    };

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (getPieceType()) {
            case KNIGHT -> fixedDistanceMoves(board, myPosition, KNIGHT_OFFSETS);
            case BISHOP -> slidingMoves(board, myPosition, BISHOP_DIRECTIONS);
            case ROOK -> slidingMoves(board, myPosition, ROOK_DIRECTIONS);
            case QUEEN -> slidingMoves(board, myPosition, QUEEN_DIRECTIONS);
            case KING -> fixedDistanceMoves(board, myPosition, KING_OFFSETS);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> fixedDistanceMoves(ChessBoard board, ChessPosition start, int[][] offsets) {
        List<ChessMove> moves = new ArrayList<>();
        int row = start.getRow();
        int col = start.getColumn();

        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            addFixedMove(board, start, moves, newRow, newCol);
        }

        return moves;
    }

    private void addFixedMove(ChessBoard board, ChessPosition start, List<ChessMove> moves,
                              int newRow, int newCol) {
        if (!isOnBoard(newRow, newCol)) {
            return;
        }

        ChessPosition end = new ChessPosition(newRow, newCol);
        ChessPiece targetPiece = board.getPiece(end);
        if (canMoveTo(targetPiece)) {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition start, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        int row = start.getRow();
        int col = start.getColumn();

        for (int[] direction : directions) {
            addSlidingMovesInDirection(board, start, moves, row, col, direction);
        }

        return moves;
    }

    private void addSlidingMovesInDirection(ChessBoard board, ChessPosition start, List<ChessMove> moves,
                                            int row, int col, int[] direction) {
        int newRow = row + direction[0];
        int newCol = col + direction[1];

        while (isOnBoard(newRow, newCol)) {
            ChessPosition end = new ChessPosition(newRow, newCol);
            ChessPiece targetPiece = board.getPiece(end);

            if (targetPiece == null) {
                moves.add(new ChessMove(start, end, null));
            } else {
                addCaptureMove(start, moves, end, targetPiece);
                return;
            }

            newRow += direction[0];
            newCol += direction[1];
        }
    }

    private void addCaptureMove(ChessPosition start, List<ChessMove> moves,
                                ChessPosition end, ChessPiece targetPiece) {
        if (targetPiece.getTeamColor() != getTeamColor()) {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();
        int row = start.getRow();
        int col = start.getColumn();
        int nextRow = row + pawnDirection();

        if (!isOnBoard(nextRow, col)) {
            return moves;
        }

        addPawnForwardMoves(board, start, moves, row, col, nextRow);
        addPawnCaptureMoves(board, start, moves, col, nextRow);
        return moves;
    }

    private void addPawnForwardMoves(ChessBoard board, ChessPosition start, List<ChessMove> moves,
                                     int row, int col, int nextRow) {
        ChessPosition oneStepPosition = new ChessPosition(nextRow, col);
        if (board.getPiece(oneStepPosition) != null) {
            return;
        }

        if (nextRow == pawnPromoteRow()) {
            addPromotionMoves(start, moves, oneStepPosition);
            return;
        }

        moves.add(new ChessMove(start, oneStepPosition, null));
        addPawnTwoStepMove(board, start, moves, row, col);
    }

    private void addPawnTwoStepMove(ChessBoard board, ChessPosition start,
                                    List<ChessMove> moves, int row, int col) {
        if (row != pawnStartRow()) {
            return;
        }

        int twoStepRow = row + 2 * pawnDirection();
        if (!isOnBoard(twoStepRow, col)) {
            return;
        }

        ChessPosition twoStepPosition = new ChessPosition(twoStepRow, col);
        if (board.getPiece(twoStepPosition) == null) {
            moves.add(new ChessMove(start, twoStepPosition, null));
        }
    }

    private void addPawnCaptureMoves(ChessBoard board, ChessPosition start,
                                     List<ChessMove> moves, int col, int nextRow) {
        int[] captureColumns = {col + 1, col - 1};
        for (int captureCol : captureColumns) {
            addPawnCaptureMove(board, start, moves, captureCol, nextRow);
        }
    }

    private void addPawnCaptureMove(ChessBoard board, ChessPosition start, List<ChessMove> moves,
                                    int captureCol, int nextRow) {
        if (!isOnBoard(nextRow, captureCol)) {
            return;
        }

        ChessPosition capturePosition = new ChessPosition(nextRow, captureCol);
        ChessPiece targetPiece = board.getPiece(capturePosition);
        if (!canCapture(targetPiece)) {
            return;
        }

        if (nextRow == pawnPromoteRow()) {
            addPromotionMoves(start, moves, capturePosition);
        } else {
            moves.add(new ChessMove(start, capturePosition, null));
        }
    }

    private void addPromotionMoves(ChessPosition start, List<ChessMove> moves, ChessPosition end) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
    }

    private boolean canMoveTo(ChessPiece targetPiece) {
        return targetPiece == null || targetPiece.getTeamColor() != getTeamColor();
    }

    private boolean canCapture(ChessPiece targetPiece) {
        return targetPiece != null && targetPiece.getTeamColor() != getTeamColor();
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private int pawnDirection() {
        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            return 1;
        }
        return -1;
    }

    private int pawnStartRow() {
        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            return 2;
        }
        return 7;
    }

    private int pawnPromoteRow() {
        if (getTeamColor() == ChessGame.TeamColor.WHITE) {
            return 8;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
