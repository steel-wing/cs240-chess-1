package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator {
    public static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessGame.TeamColor team = board.getPiece(myPosition).getTeamColor();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int targetrow;
        int targetcol;

        ArrayList<ChessMove> moves = new ArrayList<>();

        // I hate repeating code, so we're iterating through all four possible directions
        for (int dir = 1; dir > -2; dir -= 2) {                 // dir is forwards/backwards
            for (int bias = 1; bias > -2; bias -= 2) {          // bias is right/left
                targetrow = row + dir;
                targetcol = col + bias;

                // iterate until you hit a wall
                while (targetrow >= 1 && targetrow <= 8
                        && targetcol >= 1 && targetcol <= 8) {

                    // identify the target square
                    ChessPiece target = board.getPiece(new ChessPosition(targetrow, targetcol));

                    // if we hit an empty space, add it it to the available moves
                    if (target == null) {
                        moves.add(new chess.ChessMove(myPosition, new ChessPosition(targetrow, targetcol), null));
                    } else {
                        // if we hit a piece, check to see if its an enemy. If so, we can hit it, but no further
                        if (target.getTeamColor() != team) {
                            moves.add(new chess.ChessMove(myPosition, new ChessPosition(targetrow, targetcol), null));
                        }
                        break;
                    }
                    targetrow += dir;
                    targetcol += bias;
                }
            }
        }
        return moves;
    }
}