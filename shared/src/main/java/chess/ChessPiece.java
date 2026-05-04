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

    public ChessGame.TeamColor getTeamColor(){
        return pieceColor;
    }

    public PieceType getPieceType(){
        return  type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition){
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

                //out of boundary check
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
        if (getPieceType() == PieceType.BISHOP){

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
        if (getPieceType() == PieceType.ROOK){
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
        if (getPieceType() == PieceType.QUEEN){
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
        if (getPieceType() == PieceType.KING){}
        if (getPieceType() == PieceType.PAWN){}
        return List.of(); //temp
    }
}
