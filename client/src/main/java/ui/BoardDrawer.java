package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;

public class BoardDrawer {

    public static String drawBoard(ChessBoard board, ChessGame.TeamColor perspective) {
        return drawBoard(board, perspective, null, null);
    }

    public static String drawBoard(ChessBoard board, ChessGame.TeamColor perspective,
                                   ChessPosition selectedPosition,
                                   Collection<ChessPosition> highlightedPositions) {
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
            appendRow(result, board, row, cols, selectedPosition, highlightedPositions);
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
        appendRow(result, board, row, cols, null, null);
    }

    public static void appendRow(StringBuilder result, ChessBoard board, int row, int[] cols,
                                 ChessPosition selectedPosition,
                                 Collection<ChessPosition> highlightedPositions) {
        result.append(EscapeSequences.SET_TEXT_COLOR_WHITE);
        result.append(" ").append(row).append(" ");
        result.append(EscapeSequences.RESET_TEXT_COLOR);

        for (int col : cols) {
            ChessPosition position = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(position);

            if (selectedPosition != null && selectedPosition.equals(position)) {
                result.append(EscapeSequences.SET_BG_COLOR_YELLOW);
            } else if (containsPosition(highlightedPositions, position)) {
                result.append(EscapeSequences.SET_BG_COLOR_GREEN);
            } else if ((row + col) % 2 == 1) {
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

    private static boolean containsPosition(Collection<ChessPosition> positions, ChessPosition position) {
        if (positions == null) {
            return false;
        }

        for (ChessPosition currentPosition : positions) {
            if (currentPosition.equals(position)) {
                return true;
            }
        }

        return false;
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
