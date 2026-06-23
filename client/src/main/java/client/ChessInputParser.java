package client;

import chess.ChessPiece;
import chess.ChessPosition;

public final class ChessInputParser {
    private ChessInputParser() {
    }

    public static ChessPosition parsePosition(String text) {
        if (text == null || text.length() != 2) {
            return null;
        }

        char file = Character.toLowerCase(text.charAt(0));
        char rank = text.charAt(1);

        if (file < 'a' || file > 'h') {
            return null;
        }

        if (rank < '1' || rank > '8') {
            return null;
        }

        int col = file - 'a' + 1;
        int row = rank - '0';

        return new ChessPosition(row, col);
    }

    public static ChessPiece.PieceType parsePromotionPiece(String text) {
        if (text.equalsIgnoreCase("queen")) {
            return ChessPiece.PieceType.QUEEN;
        }

        if (text.equalsIgnoreCase("rook")) {
            return ChessPiece.PieceType.ROOK;
        }

        if (text.equalsIgnoreCase("bishop")) {
            return ChessPiece.PieceType.BISHOP;
        }

        if (text.equalsIgnoreCase("knight")) {
            return ChessPiece.PieceType.KNIGHT;
        }

        return null;
    }
}
