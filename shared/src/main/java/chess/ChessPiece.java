package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;
    private int hasMoved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
        // pawns can get captured by other pawns the turn after they do the double-step, if they do
        this.hasMoved = 0;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public int getHistory() {
        return hasMoved;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos){

        //int history = this.hasMoved;

        if (this.type == ChessPiece.PieceType.PAWN){
            // this.hasMoved = history < 2 ? history++ : 2;
            return PawnMovesCalculator.pieceMoves(board, pos);
        } else if (this.type == ChessPiece.PieceType.KNIGHT){
            return KnightMovesCalculator.pieceMoves(board, pos);
        } else if (this.type == ChessPiece.PieceType.BISHOP){
            return BishopMovesCalculator.pieceMoves(board, pos);
        } else if (this.type == ChessPiece.PieceType.ROOK){
            return RookMovesCalculator.pieceMoves(board, pos);
        } else if (this.type == ChessPiece.PieceType.QUEEN){
            return QueenMovesCalculator.pieceMoves(board, pos);
        } else if (this.type == ChessPiece.PieceType.KING){
            return KingMovesCalculator.pieceMoves(board, pos);
        }
        // this is just here since Java yells at me if it isn't here
        return null;
    }


    public static ArrayList<ChessMove> linearMotion(ChessPosition pos, int dir, int bias, int steps, ChessBoard board) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor team = board.getPiece(pos).getTeamColor();

        int row = pos.getRow();
        int col = pos.getColumn();

        int targetrow = row + dir;
        int targetcol = col + bias;

        // while in the bounds of the board, and not overstepping
        while (targetrow >= 1 && targetrow <= 8 && targetcol >= 1 && targetcol <= 8 && steps > 0) {

            // identify the target square
            ChessPiece target = board.getPiece(new ChessPosition(targetrow, targetcol));

            // if we hit an empty space, add it to the available moves
            if (target == null) {
                moves.add(new chess.ChessMove(pos, new ChessPosition(targetrow, targetcol), null));

                // iterate onwards in the direction indicated by dir and bias
                targetrow += dir;
                targetcol += bias;
                steps--;
            } else {
                // if we hit a piece we go no further. Check to see if it's an enemy: if so, we can hit it
                if (target.getTeamColor() != team) {
                    moves.add(new chess.ChessMove(pos, new ChessPosition(targetrow, targetcol), null));
                }
                break;
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return color + " " + type;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }
    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}
