package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return gameOver == chessGame.gameOver
                && teamTurn == chessGame.teamTurn
                && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board, gameOver);
    }

    private TeamColor teamTurn;
    private ChessBoard board;
    private boolean gameOver;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
        this.gameOver = false;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard testBoard = copyBoard(board);
            applyMove(testBoard, move);
            ChessBoard originalBoard = board;
            board = testBoard;
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            board = originalBoard;
        }
        return validMoves;
    }

    private ChessBoard copyBoard(ChessBoard originalBoard) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                copy.addPiece(new ChessPosition(row, col), originalBoard.getPiece(new ChessPosition(row, col)));
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard board, ChessMove move) {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        board.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), movingPiece);
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        applyMove(board, move);

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false;
        }

        TeamColor enemyColor = oppositeColor(teamColor);

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = board.getPiece(currentPosition);

                if (pieceAttacksPosition(currentPiece, enemyColor, currentPosition, kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pieceAttacksPosition(ChessPiece piece, TeamColor pieceColor,
                                         ChessPosition piecePosition, ChessPosition targetPosition) {
        if (piece == null || piece.getTeamColor() != pieceColor) {
            return false;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, piecePosition);
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(targetPosition)) {
                return true;
            }
        }
        return false;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (isKingForTeam(piece, teamColor)) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean isKingForTeam(ChessPiece piece, TeamColor teamColor) {
        return piece != null
                && piece.getTeamColor() == teamColor
                && piece.getPieceType() == ChessPiece.PieceType.KING;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return !hasAnyValidMove(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return !hasAnyValidMove(teamColor);
    }

    private boolean hasAnyValidMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (hasValidMove(piece, teamColor, position)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasValidMove(ChessPiece piece, TeamColor teamColor, ChessPosition position) {
        if (piece == null || piece.getTeamColor() != teamColor) {
            return false;
        }

        Collection<ChessMove> moves = validMoves(position);
        return moves != null && !moves.isEmpty();
    }

    private TeamColor oppositeColor(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {
            return TeamColor.BLACK;
        }
        return TeamColor.WHITE;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
