package chess;

import java.util.ArrayList;
import java.util.Map;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * any signatures of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    private KingMetaMotion kingThink;
    private PawnMetaMotion pawnThink;
    public ChessGame() {
        turn = WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /////////////////
    // LEGAL MOVES //
    /////////////////

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return ArrayList of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public ArrayList<ChessMove> validMoves(ChessPosition startPosition) {
        // quick escape condition in case this has been called on nothing
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        if (board.getPiece(startPosition) == null) {
            return validMoves;
        }

        // copy everything down
        ChessBoard boardcopy = new ChessBoard(this.board);
        ChessPiece piece = board.getPiece(startPosition);
        TeamColor team = piece.getTeamColor();

        // find all possible moves the piece could physically make
        ArrayList<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        // include enPassant and castling, if relevant
        if (piece.getPieceType() == PAWN) {
            pawnThink = new PawnMetaMotion(this);
            possibleMoves.addAll(pawnThink.enPassant(startPosition));
        }
        if (piece.getPieceType() == KING) {
            kingThink = new KingMetaMotion(this);
            possibleMoves.addAll(kingThink.castling(startPosition));
        }

        // save info about hypothetical motion
        ChessPosition lastPosition = startPosition;
        ChessPiece lastTarget = null;

        // for all moves, move the piece there
        for (ChessMove move : possibleMoves){
            ChessPosition targetPosition = move.getEndPosition();
            ChessPiece targetPiece = board.getPiece(targetPosition);

            // move the piece to its new location
            board.removePiece(lastPosition);
            board.addPiece(targetPosition, piece);

            // replace the taken material, if there was any
            if (lastTarget != null) {
                board.addPiece(lastPosition, lastTarget);
            }

            // if we aren't in check now, the move is valid
            if (!isInCheck(team)) {
                validMoves.add(move);
            }

            // update the last place the piece was
            lastPosition = targetPosition;
            lastTarget = targetPiece;
        }

        // restore the board to the way it was and return
        this.board = boardcopy;
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // find relevant location and piece information
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);

        // verify that it's your turn
        if (piece.getTeamColor() != turn) {
            String correctionString = "Not your turn";
            throw new InvalidMoveException(correctionString);
        }

        // verify that the move being requested is in validMoves()
        for (ChessMove validMove : validMoves(start)) {
            // skip any invalid move
            if (!move.equals(validMove)) {
                continue;
            }

            // we're valid, so move the pieces, paying special attention to meta-gamerules
            movePieces(move);

            // update whose turn it is, and increment steps (including the double-jump pawns)
            turn = turn == WHITE ? BLACK : WHITE;
            piece.stepIncrement();

            pawnThink = new PawnMetaMotion(this);
            pawnThink.pawnStepIncrement(turn);

            //printing test
            System.out.println(this);

            return;
        }
        // if all failed, throw an exception
        String correctionString = "Invalid move requested";
        throw new InvalidMoveException(correctionString);
    }

    /**
     * A lil helper function to handle the physical motions of individual pieces
     * @param move The requested move
     */
    private void movePieces(ChessMove move) {
        // find relevant location and piece information
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        ChessPiece.PieceType type = board.getPiece(start).getPieceType();
        ChessPiece.PieceType promo = move.getPromotionPiece();

        board.removePiece(start);

        if (promo == null) {
            // handle the pawn deletion for en passant
            if (type == PAWN) {
                pawnThink = new PawnMetaMotion(this);
                pawnThink.removePawn(move);
            }

            // handle the rook teleportation for castling
            if (type == KING) {
                kingThink = new KingMetaMotion(this);
                kingThink.teleportCastle(move);
            }

            // add the piece where it lands
            board.addPiece(end, piece);
        } else {
            // add the promoted piece where it lands
            board.addPiece(end, new ChessPiece(turn, promo));
        }
    }


    /**
     * goes through and finds all of the LOCATIONS of pieces of a type (or all if null) on a team
     *
     * @param teamColor which team we're looking at
     * @param pieceType what kind of piece we're looking at (null means we don't care)
     * @return ArrayList of *ChessPositions*
     */
    public ArrayList<ChessPosition> teamPieces(TeamColor teamColor, ChessPiece.PieceType pieceType) {
        ArrayList<ChessPosition> places = new ArrayList<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                // skip if empty
                if (piece == null) {
                    continue;
                }
                // if we find a piece, check its team and type (accept it if we're looking for all)
                if (piece.getTeamColor() == teamColor && (pieceType == null || piece.getPieceType() == pieceType)) {
                    places.add(new ChessPosition(row, col));
                }
            }
        }
        return places;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // get the positions of all enemy pieces
        TeamColor enemyColor = teamColor == WHITE ? BLACK : WHITE;
        ArrayList<ChessPosition> enemyPositions = teamPieces(enemyColor, null);

        // get all of the positions the enemy pieces are attacking
        for (ChessPosition enemyPosition : enemyPositions) {
            ChessPiece piece = board.getPiece(enemyPosition);
            ArrayList<ChessMove> pieceMoves = piece.pieceMoves(board, enemyPosition);

            // check and see if the king is being targeted
            for (ChessMove pieceMove : pieceMoves) {
                ChessPiece target = board.getPiece(pieceMove.getEndPosition());
                // check if the square is occupied, then if it's the right type, then if it's the right team
                if (target != null &&
                        target.getPieceType() == ChessPiece.PieceType.KING &&
                        target.getTeamColor() == teamColor) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        kingThink = new KingMetaMotion(this);
        return kingThink.kingChecker(teamColor, true, false);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        kingThink = new KingMetaMotion(this);
        return kingThink.kingChecker(teamColor, false, false);
    }

    // add the cool chess pieces in here
    // remember the colors are inverted in dark mode, not sure what to do about that
    // but yeah go look up the unicode stuff and instead of going to caps, add the amount
    // necessary to turn the one color into the other

    final static Map<ChessPiece.PieceType, Character> TypetoGlyph = Map.of(
            PAWN, '♙',
            KNIGHT, '♘',
            ROOK, '♖',
            QUEEN, '♕',
            KING, '♔',
            BISHOP, '♗'
    );

    public String printValids(ChessPosition position) {
        ChessBoard board = this.board;
        String spacer = "\u2001\u2005\u200A";
        ArrayList<ChessMove> validmoves = validMoves(position);
        // build array list of positions, not moves

        StringBuilder output = new StringBuilder();
        output.append(spacer);
        output.append(spacer);
        for (int col = 1; col <= 8; col++) {
            output.append((char)(col + '`'));
            output.append(spacer);
        }
        output.append("\n");

        // iterate across the board and add all pieces while delimiting with "|"
        for (int row = 8; row >= 1; row--){
            output.append((char)(row + '0'));
            output.append(" |");
            for (int col = 1; col <= 8; col++) {
                ChessPosition select = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(select);
                if (piece == null){
                    if (true) { //valids.contains(new ChessPosition(row, col))){
                        output.append("+");
                    } else {
                        output.append(spacer);
                    }
                    output.append("|");
                    continue;
                }
                // white pieces are white
                Character type = TypetoGlyph.get(piece.getPieceType());
                type = piece.getTeamColor() == WHITE ? (char)(type + 6) : type;
                output.append(type);
                output.append("|");
            }
            output.append("\u2001\u2005");
            output.append((char)(row + '0'));
            output.append("\n");
        }
        output.append(spacer);
        output.append(spacer);
        for (int col = 1; col <= 8; col++) {
            output.append((char)(col + '`'));
            output.append(spacer);
        }
        output.append("\n");

        return output.toString();
    }

    @Override
    public String toString() {
        ChessBoard board = this.board;
        String spacer = "\u2001\u2005\u200A";

        StringBuilder output = new StringBuilder();
        output.append(spacer);
        output.append(spacer);
        for (int col = 1; col <= 8; col++) {
            output.append((char)(col + '`'));
            output.append(spacer);
        }
        output.append("\n");

        // iterate across the board and add all pieces while delimiting with "|"
        for (int row = 8; row >= 1; row--){
            output.append((char)(row + '0'));
            output.append(" |");
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null){
                    output.append(spacer);
                    output.append("|");
                    continue;
                }
                // white pieces are white
                Character type = TypetoGlyph.get(piece.getPieceType());
                type = piece.getTeamColor() == WHITE ? (char)(type + 6) : type;
                output.append(type);
                output.append("|");
            }
            output.append("\u2001\u2005");
            output.append((char)(row + '0'));
            output.append("\n");
        }
        output.append(spacer);
        output.append(spacer);
        for (int col = 1; col <= 8; col++) {
            output.append((char)(col + '`'));
            output.append(spacer);
        }
        output.append("\n");

        return output.toString();
    }
}
