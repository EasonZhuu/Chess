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
        if (getPieceType() != PieceType.KNIGHT) {
            return List.of();
        }

        List<ChessMove> moves = new ArrayList<>();

        int[][] offsets = {
                { 2,  1}, { 2, -1},
                {-2,  1}, {-2, -1},
                { 1,  2}, { 1, -2},
                {-1,  2}, {-1, -2}
        };

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        for (int[] offset : offsets){
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            //out of boundary check
            if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8){
                continue;
            }

            ChessPosition end = new ChessPosition(newRow, newCol);
            ChessPiece targetPiece = board.getPiece(end);

            if (targetPiece == null || targetPiece.getTeamColor() != getTeamColor()){
                moves.add(new ChessMove(myPosition, end, null));
            }
        }
    return moves;
    }
}
