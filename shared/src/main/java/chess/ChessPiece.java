package chess;

import java.util.*;

public class ChessPiece {
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
        if (getPieceType() == PieceType.KNIGHT) {

            List<ChessMove> moves = new ArrayList<>();

            int[][] offsets = {
                    {2, 1}, {2, -1},
                    {-2, 1}, {-2, -1},
                    {1, 2}, {1, -2},
                    {-1, 2}, {-1, -2}
            };

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            for (int[] offset : offsets) {

                int newRow = row + offset[0];
                int newCol = col + offset[1];

                // out of boundary check
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    continue;
                }

                ChessPosition end = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(end);

                if (targetPiece == null || targetPiece.getTeamColor() != getTeamColor()) {
                    moves.add(new ChessMove(myPosition, end, null));
                }
            }
            return moves;
        }
        if (getPieceType() == PieceType.BISHOP) {

            List<ChessMove> moves = new ArrayList<>();

            int[][] directions = {
                    { 1,  1}, { 1, -1},
                    {-1,  1}, {-1, -1}
            };

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            for (int[] direction : directions) {
                int dr = direction[0];
                int dc = direction[1];

                int newRow = row + dr;
                int newCol = col + dc;

                while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition end = new ChessPosition(newRow, newCol);
                    ChessPiece targetPiece = board.getPiece(end);

                    if (targetPiece == null) {
                        moves.add(new ChessMove(myPosition, end, null));
                    } else {
                        if (targetPiece.getTeamColor() != getTeamColor()) {
                            moves.add(new ChessMove(myPosition, end, null));
                        }
                        break;
                    }

                    newRow += dr;
                    newCol += dc;
                }
            }
            return moves;
        }
        if (getPieceType() == PieceType.ROOK) {
            List<ChessMove> moves = new ArrayList<>();

            int[][] directions = {
                    { 1,  0}, {-1,  0},
                    { 0,  1}, { 0, -1}
            };

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            for (int[] direction : directions) {
                int dr = direction[0];
                int dc = direction[1];

                int newRow = row + dr;
                int newCol = col + dc;

                while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition end = new ChessPosition(newRow, newCol);
                    ChessPiece targetPiece = board.getPiece(end);

                    if (targetPiece == null) {
                        moves.add(new ChessMove(myPosition, end, null));
                    } else {
                        if (targetPiece.getTeamColor() != getTeamColor()) {
                            moves.add(new ChessMove(myPosition, end, null));
                        }
                        break;
                    }

                    newRow += dr;
                    newCol += dc;
                }
            }
            return moves;
        }
        if (getPieceType() == PieceType.QUEEN) {
            List<ChessMove> moves = new ArrayList<>();

            int[][] directions = {
                    { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1},
                    { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
            };

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            for (int[] direction : directions) {
                int dr = direction[0];
                int dc = direction[1];

                int newRow = row + dr;
                int newCol = col + dc;

                while (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition end = new ChessPosition(newRow, newCol);
                    ChessPiece targetPiece = board.getPiece(end);

                    if (targetPiece == null) {
                        moves.add(new ChessMove(myPosition, end, null));
                    } else {
                        if (targetPiece.getTeamColor() != getTeamColor()) {
                            moves.add(new ChessMove(myPosition, end, null));
                        }
                        break;
                    }

                    newRow += dr;
                    newCol += dc;
                }
            }
            return moves;
        }
        if (getPieceType() == PieceType.KING) {
            List<ChessMove> moves = new ArrayList<>();

            int[][] offsets = {
                    { 1,  0}, { 1,  1}, { 0,  1}, {-1,  1},
                    {-1,  0}, {-1, -1}, { 0, -1}, { 1, -1}
            };

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            for (int[] offset : offsets) {

                int newRow = row + offset[0];
                int newCol = col + offset[1];

                // out of boundary check
                if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                    continue;
                }

                ChessPosition end = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(end);

                if (targetPiece == null || targetPiece.getTeamColor() != getTeamColor()) {
                    moves.add(new ChessMove(myPosition, end, null));
                }
            }
            return moves;
        }
        if (getPieceType() == PieceType.PAWN) {
            List<ChessMove> moves = new ArrayList<>();

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            int direction;
            int startRow;
            int promoteRow;

            if (getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1;
                startRow = 2;
                promoteRow = 8;
            } else {
                direction = -1;
                startRow = 7;
                promoteRow = 1;
            }

            int nextRow = row + direction;
            if (nextRow >= 1 && nextRow <= 8) {
                ChessPosition oneStepPosition = new ChessPosition(nextRow, col);
                if (board.getPiece(oneStepPosition) == null) {
                    if (nextRow == promoteRow) {
                        moves.add(new ChessMove(myPosition, oneStepPosition, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, oneStepPosition, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, oneStepPosition, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, oneStepPosition, PieceType.ROOK));
                    } else {
                        moves.add(new ChessMove(myPosition, oneStepPosition, null));

                        // two steps at first time
                        if (row == startRow) {
                            int twoStepRow = row + 2 * direction;
                            if (twoStepRow >= 1 && twoStepRow <= 8) {
                                ChessPosition twoStepPosition = new ChessPosition(twoStepRow, col);
                                if (board.getPiece(twoStepPosition) == null) {
                                    moves.add(new ChessMove(myPosition, twoStepPosition, null));
                                }
                            }
                        }
                    }
                }

                // eat
                int[] eatCols = {col + 1, col - 1};
                for (int eatCol : eatCols) {
                    if (eatCol > 8 || eatCol < 1) {
                        continue;
                    }

                    ChessPosition eatPosition = new ChessPosition(nextRow, eatCol);
                    ChessPiece target = board.getPiece(eatPosition);

                    if (target != null && target.getTeamColor() != getTeamColor()) {
                        if (nextRow == promoteRow) {
                            moves.add(new ChessMove(myPosition, eatPosition, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, eatPosition, PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, eatPosition, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, eatPosition, PieceType.ROOK));
                        } else {
                            moves.add(new ChessMove(myPosition, eatPosition, null));
                        }
                    }
                }
            }
            return moves;
        }
        return Collections.emptyList();
    }
}
