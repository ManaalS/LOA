# LOA - Lines of Action - CS61B Project 2

Lines of Action is a board game invented by Claude Soucie. It is played on a checkerboard with ordinary checkers pieces. The two players take turns, each moving a piece, and possibly capturing an opposing piece. The goal of the game is to get all of one’s pieces into one group of pieces that are connected. Two pieces are connected if they are adjacent horizontally, vertically, or diagonally. Initially, the pieces are arranged as shown in Figure 1. Play alternates between Black and White, with Black moving first. Each move consists of moving a piece of your color horizontally, vertically, or diagonally onto an empty square or onto a square occupied by an opposing piece, which is then removed from the board. A piece may jump over friendly pieces (without disturbing them), but may not cross enemy pieces, except one that it captures. A piece must move a number of squares that is exactly equal to the total number of pieces (black and white) on the line along which it chooses to move (the line of action). This line contains both the squares behind and in front of the piece that moves, as well as the square the piece is on. A piece may not move off the board, onto another piece of its color, or over an opposing piece.

The game ends when one side's pieces are contiguous: that is, there is a path connecting any two pieces of that side's color by a sequence of steps to adjacent squares (horizontally, vertically, or diagonally), each of which contains a piece of same color. Hence, when a side is reduced to a single piece, all of its pieces are contiguous. If a move causes both sides' pieces to be contiguous, the winner is the side that made that move. One can have infinite games, where players just repeat positions indefinitely. We will prevent this with a move-limit rule: if the current move limit is L moves (the default is 60), then after the two sides both make L moves combined, the game ends in a tie. Our testing will always include time limits; somebody will eventually lose if two players repeat positions many times. Figure 2a shows a final position. Figure 2b shows a board just before a move that will give both sides contiguous pieces. Since the move is White's, White wins this game.

#### for more information and to see examples, please visit [here] [https://inst.eecs.berkeley.edu/~cs61b/sp20/materials/proj/proj2/index.html]


CONTENTS:


loa/			Directory containing the Lines of Action package.

    Makefile		A convenience Makefile so that you can issue 
			compilation commands from the game directory.

    Piece.java	 	An enumeration type describing the kinds of pieces.

    Board.java	        Represents a game board.  Contains much of the
			machinery for checking or generating possible moves.

    Square.java         Represents a position on a Board.

    Move.java		Represents a single move.

    Game.java           Controls play of the game.  Calls on Players to
                        provide moves, executes other commands,
                        and maintains a current Board.

    Player.java         Supertype representing common characteristics of
                        players.

    HumanPlayer.java	A kind of Player that reads moves from the standard
                        input (i.e., presumably from a human player).

    MachinePlayer.java  A kind of Player that chooses its moves automatically.

    Reporter.java       The supertype of "reporters", which announce errors,
                        moves, and other notes to the user.

    TextReporter.java   A type of Reporter that uses the standard output
                        (generally the terminal) for output.

    View.java           An interface for things that display the Board on
                        each move.

    NullView.java       A View that does nothing.

    Utils.java          Assorted utility functions for debugging messages and
                        error reporting.

    UnitTests.java      Class that coordinates unit testing of the loa package.

    BoardTest.java      Class containing unit tests of the Board class.

    HelpText.txt        Contains a brief description of the commands (intended
                        for printing when help requested).

    GUI.java            A class that represents a graphical user interface
                        (GUI) for the Loa game.

    BoardWidget.java    Used by the GUI class to display the board.

    GUIPlayer.java      A type of manual Player that takes move from the GUI.

    About.html           
    Help.html           Files displayable by the GUI containing various
                        documentation.

testing/

    Makefile            Directions for testing.

    *-1.in
    *-2.in	        Test cases.  Each one is input to a testing script
                        for test-loa.  Where there is just XXX-1.in, test-loa
                        tests a single program.  Where there are both
                        XXX-1.in and XXX-2.in, the ...-1 file gives the input
                        script for one of the programs and ...-2 for the
                        other.

    *-1.std
    *-2.std		Correct output from the corresponding .in files,
                        containing dumps of the board and win messages.

    test-loa            A program that feeds a tesitng script into one or two
                        running Loa games and checks the output.

    tester.py           Runs test-loa on a given set of *.in files.

    testing.py          General testing support.
