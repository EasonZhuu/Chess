package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardDrawer {

    public static String drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder result = new StringBuilder();

        appendColumnLabels(result);

        for (int row = 8; row > 0; row--) {
            appendRow(result, board, row);
        }

        appendColumnLabels(result);

        return  result.toString();
    }

    public static void appendColumnLabels(StringBuilder result) {
        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        result.append("    a  b  c  d  e  f  g  h\n");
        result.append(EscapeSequences.RESET_TEXT_COLOR);
    }

    public static void appendRow(StringBuilder result, ChessBoard board, int row) {
        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        result.append(" ").append(row).append(" ");
        result.append(EscapeSequences.RESET_TEXT_COLOR);

        for (int col = 1; col <= 8; col++) {
            ChessPiece piece = board.getPiece(new ChessPosition(row, col));

            if ((row + col) % 2 == 1) {
                result.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            } else {
                result.append(EscapeSequences.SET_BG_COLOR_GREEN);
            }

            if (piece == null) {
                result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
            } else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                result.append(EscapeSequences.SET_TEXT_COLOR_RED);
            } else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                result.append(EscapeSequences.SET_TEXT_COLOR_BLUE);
            }

            result.append(pieceSymbol(piece));
            result.append(EscapeSequences.RESET_TEXT_COLOR);
            result.append(EscapeSequences.RESET_BG_COLOR);
        }

        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        result.append(" ").append(row).append(" \n");
        result.append(EscapeSequences.RESET_TEXT_COLOR);
    }

    public static String pieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        return switch (piece.getPieceType()) {
            case KING -> EscapeSequences.WHITE_KING;
            case QUEEN -> EscapeSequences.WHITE_QUEEN;
            case BISHOP -> EscapeSequences.WHITE_BISHOP;
            case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
            case ROOK -> EscapeSequences.WHITE_ROOK;
            case PAWN -> EscapeSequences.WHITE_PAWN;
        };
    }
}
