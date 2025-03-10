/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static java.lang.System.arraycopy;
import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Manaal
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;
    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                Piece curr = contents[r][c];
                set(sq(c, r), curr);
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _blackRegionSizes.clear();
        _whiteRegionSizes.clear();
        _moves.clear();
        _winner = null;
        _winnerKnown = false;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }


    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _subsetsInitialized = board._subsetsInitialized;
        _blackRegionSizes.clear();
        _whiteRegionSizes.clear();
        _whiteRegionSizes.addAll(board.getRegionSizes(WP));
        _blackRegionSizes.addAll(board.getRegionSizes(BP));
        arraycopy(board._board, 0, _board, 0, _board.length);
        _moves.clear();
        _moves.addAll(board._moves);
        _turn = board._turn;
        _winner = board.winner();
        _moveLimit = board._moveLimit;
        computeRegions();
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        int index = sq.index();
        _board[index] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        Square from = move.getFrom(),
                to = move.getTo();
        if ((get(from) != get(to)) && (get(to) != EMP)) {
            move = Move.mv(from, to, true);
        }
        set(to, get(from));
        set(from, EMP);
        _turn = get(to).opposite();
        _moves.add(move);
        _subsetsInitialized = false;
        _winnerKnown = false;
        computeRegions();
    }

    /** Retract (unmake) one move, returning to the state immediately before
       that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move last = _moves.get(_moves.size() - 1);
        _moves.remove(_moves.size() - 1);
        Square from = last.getFrom();
        Square to = last.getTo();
        set(from, get(to));
        if (!last.isCapture()) {
            set(to, EMP);
        } else {
            set(to, get(to).opposite());
        }
        _turn = _turn.opposite();
        _subsetsInitialized = false;
        _winnerKnown = false;
        computeRegions();
    }

    /** Return the Piece representing who is
       next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
       move. */
    boolean isLegal(Square from, Square to) {
        Move move = Move.mv(from, to);
        if (from != null && to != null && !get(from).equals(EMP)) {
            Piece start = get(from);
            if (from.equals(to)) {
                return false;
            }
            if (move.length() != countAlong(move)) {
                return false;
            }
            if (start != _turn) {
                return false;
            }
            if (turn() != get(from)) {
                return false;
            }
            if (get(from) == get(to)) {
                return false;
            }
            if (blocked(from, to)) {
                return false;
            }
        } else if (move == null) {
            return false;
        }
        return true;
    }

    /** Return pieces along the direction of the move.
     * @return int
     * @param move */
    private int countAlong(Move move) {
        Square from = move.getFrom();
        Square to = move.getTo();
        Square dummy = from;
        int direction = from.direction(to);
        int steps = 1;
        int numPieces = 1;
        while (dummy.moveDest(direction, steps) != null) {
            Square curr = dummy.moveDest(direction, steps);
            Piece curr1 = get(curr);
            int index = curr.index();
            if (get(curr) != EMP) {
                numPieces++;
            }
            steps++;
        }
        int direction2 = to.direction(from);
        steps = 1;
        Square hello = dummy.moveDest(direction2, steps);
        while (dummy.moveDest(direction2, steps) != null) {
            Square curr = dummy.moveDest(direction2, steps);
            if (get(curr) != EMP) {
                numPieces++;
            }
            steps++;
        }
        return numPieces;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        if (move == null) {
            return false;
        }
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    /** This is based on the current player.
     * @return List legalmoves. */
    List<Move> legalMoves() {
        List<Move> legal = new ArrayList<Move>();
        for (int c = 0; c < BOARD_SIZE; c += 1) {
            for (int r = 0; r < BOARD_SIZE; r += 1) {
                Square from = sq(c, r);
                for (int a = 0; a < BOARD_SIZE; a += 1) {
                    for (int b = 0; b < BOARD_SIZE; b += 1) {
                        Square to = sq(a, b);
                        Piece curr = get(from);
                        if (isLegal(Move.mv(from, to)) && curr == _turn) {
                            legal.add(Move.mv(from, to));
                        }
                    }
                }
            }
        }
        return legal;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        if (winner() != null) {
            System.out.println("winner is " + winner());
        }
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. m */
    Piece winner() {
        if (!_winnerKnown) {
            boolean wpCon = piecesContiguous(WP);
            boolean bpCon = piecesContiguous(BP);
            if ((bpCon) && (wpCon)) {
                _winnerKnown = true;
                if (turn() == WP) {
                    _winner =  BP;
                    _winnerKnown = true;
                } else {
                    _winner = WP;
                    _winnerKnown = true;
                }
            } else if (wpCon) {
                _winnerKnown = true;
                _winner = WP;
            } else if (bpCon) {
                _winnerKnown = true;
                _winner = BP;
            } else if (_moves.size() >= _moveLimit) {
                _winnerKnown = true;
                _winner = EMP;
            }
            return _winner;
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();

    }

    /** Return the very last move. delete?*/
    Move lastMove() {
        return _moves.get(_moves.size() - 1);
    }
    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        Square dummy = from;
        Move m = Move.mv(from, to);
        int dir = from.direction(to);
        int steps = 1;
        Square curr = dummy.moveDest(dir, steps);
        Piece tur = _turn;
        while (dummy.moveDest(dir, steps) != null && steps <= countAlong(m)) {
            curr = dummy.moveDest(dir, steps);
            if (curr.equals(to)) {
                break;
            }
            if (get(curr).equals(_turn.opposite())) {
                return true;
            }
            steps++;
        }
        if (get(to).equals(_turn)) {
            return true;
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. m */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        int number = 1;
        Square[] nearby = sq.adjacent();
        Piece p1 = get(sq);
        if (p1.equals(p)) {
            visited[sq.row()][sq.col()] = true;
            for (int i = 0; i < nearby.length; i++) {
                Square currAdj = nearby[i];
                int row = currAdj.row();
                int col = currAdj.col();
                if (get(currAdj).equals(p) && !visited[row][col]) {
                    number += numContig(currAdj, visited, p);
                }
            }
        }
        return number;
    }
    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();

        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Square sq = sq(c, r);
                if ((visited[r][c]) || (get(sq) == EMP)) {
                    continue;
                }
                int size = numContig(sq, visited, get(sq));
                if (size > 0) {
                    if (get(sq) == WP) {
                        _whiteRegionSizes.add(size);
                    } else {
                        _blackRegionSizes.add(size);
                    }
                }
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }
    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** Return the number of pieces a player has.
     * @return int @param side */
    public int getNum(Piece side) {
        int num = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (get(sq(i, j)).equals(side)) {
                    num++;
                }
            }
        }
        return num;
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
            _whiteRegionSizes = new ArrayList<>(),
            _blackRegionSizes = new ArrayList<>();
}
