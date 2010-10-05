/**
 *  QueensTask.java
 *
 *  Author:       Alistair Atkinson {alatkins@utas.edu.au}
 *  Created:      1-9-2003
 *  Modified:     13-8-2007
 *
 *  Description:
 */

package apps.nQueens;

public class QueensTask implements java.io.Serializable {
    public Integer inRow1, inRow2;
    public Integer N;

    public QueensTask(int N, int inRow1, int inRow2) {
        this.N = new Integer(N);
        this.inRow1 = new Integer(inRow1);
        this.inRow2 = new Integer(inRow2);
    }

    public QueensResult execute() {
        int[] board = new int[N.intValue()+1];  // need 1 through N (0 unused)

        for (int i = 0; i < board.length; i++)
            board[i] = 0;

        board[1] = inRow1.intValue();
        board[2] = inRow2.intValue();

        int numFound = place(3, board);

        return new QueensResult(numFound);
    }

    private boolean safe(int row, int column, int[] board) {
        for (int j=1; j<column; j++) {
            if (board[column-j] == row   ||
                board[column-j] == row-j ||
                board[column-j] == row+j) {
                    return false;
            }
        }
        return true;
    }

    private int place(int column, int[] board) {
        int numFound = 0;
        for (int row = 1; row <= N.intValue(); row++) {
            board[column] = row;
            if (safe(row, column, board)) {
                if (column==N.intValue()) numFound++;
                else numFound += place(column+1, board);
            }
            board[column] = 0;
        }
        return numFound;
    }
}
