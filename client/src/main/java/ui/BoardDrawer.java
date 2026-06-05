package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardDrawer {

    public static String drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder result = new StringBuilder();

        int[] rows;
        int[] cols;

        if (perspective == ChessGame.TeamColor.BLACK) {
            rows = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            cols = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
        } else {
            rows = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
            cols = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        }

        appendColumnLabels(result, perspective);

        for (int row : rows) {
            appendRow(result, board, row, cols);
        }

        appendColumnLabels(result, perspective);

        return  result.toString();
    }

    public static void appendColumnLabels(StringBuilder result, ChessGame.TeamColor perspective) {
        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        if (perspective == ChessGame.TeamColor.BLACK) {
            result.append("    h  g  f  e  d  c  b  a\n");
        } else {
            result.append("    a  b  c  d  e  f  g  h\n");
        }
        result.append(EscapeSequences.RESET_TEXT_COLOR);
    }

    public static void appendRow(StringBuilder result, ChessBoard board, int row, int[] cols) {
        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        result.append(" ").append(row).append(" ");
        result.append(EscapeSequences.RESET_TEXT_COLOR);

        for (int col : cols) {
            ChessPiece piece = board.getPiece(new ChessPosition(row, col));

            if ((row + col) % 2 == 1) {
                result.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            } else {
                result.append(EscapeSequences.SET_BG_COLOR_WHITE);
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
