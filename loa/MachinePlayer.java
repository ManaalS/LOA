/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.List;
import java.util.ArrayList;
import static loa.Piece.*;

/** An automated Player.
 *  @author Manaal
 */
class MachinePlayer extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new MachinePlayer with no piece or controller (intended to produce
     * a template).
     */
    MachinePlayer() {
        this(null, null);
    }

    /**
     * A MachinePlayer that plays the SIDE pieces in GAME.
     */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move after searching the game tree to DEPTH>0 moves
     * from the current position. Assumes the game is not over.
     */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */

    public int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if ((depth == 0) || (board.gameOver())) {
            return staticScore(board);
        }
        ArrayList<Board> boardlist = boardList(board);
        int res;
        Board bestBoard = null;
        if (sense == 1) {
            int maxSofar = -INFTY;
            for (Board b : boardlist) {
                int val = findMove(b, depth - 1, false,
                        -sense, alpha, beta);
                if (val > maxSofar) {
                    maxSofar = val;
                    bestBoard = b;
                }
                alpha = Math.max(alpha, val);
                if (beta <= alpha) {
                    break;
                }
            }
            res = maxSofar;
        } else {
            int minSofar = INFTY;
            for (Board b : boardlist) {
                int val = findMove(b, depth - 1, false,
                        -sense, alpha, beta);
                if (val < minSofar) {
                    minSofar = val;
                    bestBoard = b;
                }
                beta = Math.min(beta, val);
                if (beta <= alpha) {
                    break;
                }
            }
            res = minSofar;
        }
        if (saveMove) {
            assert bestBoard != null;
            _foundMove = bestBoard.lastMove();
        }
        return res;
    }

    /** Get a list of possible boards m.
     * @return ArrayList
     * @param b */
    private ArrayList<Board> boardList(Board b) {
        ArrayList<Board> nextBoard = new ArrayList<>();
        List<Move> legalMoveSet = b.legalMoves();
        if (legalMoveSet != null) {
            for (Move mv : legalMoveSet) {
                Board help = new Board(b);
                help.makeMove(mv);
                nextBoard.add(help);
            }
        }
        return nextBoard;
    }
    /**
     * Return a search depth for the current position.
     */
    private int chooseDepth() {
        return 3;
    }

    /** return an evaluation of the board atm.
     * @param board */
    public int staticScore(Board board) {
        if (board.gameOver()) {
            if (board.winner() == WP) {
                return WINNING_VALUE;
            } else if (board.winner() == BP) {
                return -WINNING_VALUE;
            } else {
                return 0;
            }
        }
        List whiteRegions = board.getRegionSizes(WP);
        List blackRegions = board.getRegionSizes(BP);
        int maxW = board.getRegionSizes(WP).get(0);
        int maxB = board.getRegionSizes(BP).get(0);
        int numW = board.getNum(WP);
        int numB = board.getNum(BP);
        int contigRegions = (numB - maxB) - (numW - maxW);

        int regionsNumW = whiteRegions.size();
        int regionsNumB = blackRegions.size();
        int regionsNumDifference = regionsNumB - regionsNumW;

        return 5 * contigRegions + 2 * regionsNumDifference;
    }

    /**
     * Used to convey moves discovered by findMove.
     */
    private Move _foundMove;
}
