package edu.unc.sjyan.eightqueens;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    static int[][] board = new int[8][8];
    static int[] queens = new int[8];
    static int[][] solutions = new int[92][8];
    static int solutionCount = 0;
    static int queensAlive = 0;
    static String warning = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){
        TextView solutionCountTV = (TextView) findViewById(R.id.textView18);
        TextView warningTV = (TextView) findViewById(R.id.textView20);
        final TextView solutionTV = (TextView) findViewById(R.id.textView19);
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);

        if(v.getTag() != null && v.getTag().equals("1") && queensAlive < 8) {
            ImageButton b = (ImageButton) findViewById(v.getId());
            String cellId = getResources().getResourceEntryName(v.getId());
            int row = (Integer.parseInt(cellId.replaceAll("[^0-9]", "")) - 1) / 8;
            int col = (Integer.parseInt(cellId.replaceAll("[^0-9]", "")) - 1) % 8;

            if(isSafe(row, col)) {
                board[row][col] = 1;
                queens[col] = row;
                queensAlive++;
                b.setImageResource(R.drawable.crown10);
                v.setTag(getString(R.string.int2));
                warningTV.setText("");
                if(queensAlive == 8) showPop("You won!");
            } else {
                warningTV.setTextColor(Color.RED);
                warningTV.setText(warning);
            }
        } else if(v.getTag() != null && v.getTag().equals("2")) {
            ImageButton b = (ImageButton) findViewById(v.getId());
            String cellId = getResources().getResourceEntryName(v.getId());
            int row = (Integer.parseInt(cellId.replaceAll("[^0-9]", "")) - 1) / 8;
            int col = (Integer.parseInt(cellId.replaceAll("[^0-9]", "")) - 1) % 8;

            np.setVisibility(View.INVISIBLE);
            solutionCountTV.setText("");
            solutionTV.setText("");

            board[row][col] = 0;
            queens[col] = -1;
            queensAlive--;
            b.setImageResource(0);
            v.setTag(getString(R.string.int1));
            warningTV.setText("");

        } else if(v.getId() == R.id.button) {
            solutionCount = 0;
            solve(queens, 0);
            if(solutionCount != 0) {
                warningTV.setText("");
                queensAlive = 0; // reset since queens will all be placed on board
                solutionCountTV.setText("Choose which solution to display");
                np.setVisibility(View.VISIBLE);
                np.setMaxValue(solutionCount);
                np.setMinValue(1);
                np.setValue(1);
                displaySolution(0);
                solutionTV.setText("Now displaying solution #1");


                np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        for (int i = 0; i < board.length; i++) {
                            for (int j = 0; j < board.length; j++) {
                                int index = ((i + 1) * board.length + (j + 1)) - board.length;
                                int iid = getResources().getIdentifier("imageButton" +
                                        Integer.toString(index), "id", getPackageName());
                                View v = findViewById(iid);

                                if(v.getTag().equals("2")) {
                                    removeQueen(i, j);
                                }
                            }
                        }

                        displaySolution(newVal - 1);
                        solutionTV.setText("Now displaying solution #" + newVal);
                    }
                });
            } else {
                showPop("No solution!");
            }

        } else if(v.getId() == R.id.button2) {
            // clear text view
            warningTV.setText("");
            solutionCountTV.setText("");
            solutionTV.setText("");
            np.setVisibility(View.INVISIBLE);
            for(int i = 0; i < board.length; i++) {
                for(int j = 0; j < board.length; j++) {
                    removeQueen(i, j);
                }
            }
            queensAlive = 0;
        } else if(v.getId() == R.id.button3) {
            new AlertDialog.Builder(this)
                    .setTitle("How to Play")
                    .setMessage("The 8-queens puzzle challenges you to put 8 queens on an " +
                            "8x8 board where none of the queens are challenging each other. " +
                            "You may click anywhere on the board to place a queen and likewise " +
                            "to remove a queen, but an invalid move will not register on the " +
                            "board. If you succeed in placing all 8 queens on the board legally, " +
                            "you will receive a popup message stating that you won. If you choose " +
                            "to give up, the puzzle will give you the possible solutions " +
                            "from your current configuration if there exists solutions. " +
                            "Good luck!")
                    .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .show();
        }

    }

    public void displaySolution(int s) {
        for(int row = 0; row < solutions[0].length; row++) {
            for (int col = 0; col < solutions[0].length; col++) {
                if (solutions[s][col] == row) {
                    placeQueen(row, col);
                }
            }
        }
    }


    public void solve(int[] queens, int qi) {
        for(int i = 0; i < board.length; i++) {
            if(qi == board.length) {
                for(int k = 0; k < queens.length; k++) {
                    solutions[solutionCount][k] = queens[k];
                }
                solutionCount++;
                return;
            }

            if(isSafe(i, qi) && !existsPlayedQueen(qi)) {
                queens[qi] = i;
                board[i][qi] = 1;

                solve(queens, qi + 1);

                queens[qi] = -1;
                board[i][qi] = 0;
            } else if(existsPlayedQueen(qi)) {
                queens[qi] = getQueenRow(qi);
                qi++;
                i = 0;
            }
        }
    }

    public boolean existsPlayedQueen(int col) {
        for (int i = 0; i < board.length; i++) {
            int index = ((i + 1) * board.length + (col + 1)) - board.length;
            int iid = getResources().getIdentifier("imageButton" + Integer.toString(index),
                    "id", getPackageName());
            View v = findViewById(iid);

            if (board[i][col] == 1 && v.getTag() != null && v.getTag().equals("2")) {
                return true;
            }
        }

        return false;
    }

    public int getQueenRow(int col) {
        for (int i = 0; i < board.length; i++) {
            int index = ((i + 1) * board.length + (col + 1)) - board.length;
            int iid = getResources().getIdentifier("imageButton" + Integer.toString(index),
                    "id", getPackageName());
            View v = findViewById(iid);

            if (board[i][col] == 1 && v.getTag() != null && v.getTag().equals("2")) {
                return i;
            }
        }

        return -1;
    }


    public boolean isSafe(int x, int y) {
        for(int i = 0; i < board.length; i++) {
            if(board[x][i] == 1) {
                warning = "Queen " + (char)(65 + i) + "" + (x + 1) + " is threatening";
                return false;
            }
        }

        for (int i = 0; i < board.length; i++) {
            if (board[i][y] == 1) {
                warning = "Queen " + (char)(65 + y) + "" + (i + 1) + " is threatening";
                return false;
            }
        }

        for(int i = 0, j = 0; x + i < board.length && y + j < board.length; i++, j++) {
            if(board[x + i][y + j] == 1) {
                warning = "Queen " + (char)(65 + (y + j)) + "" + (x + i + 1) + " is threatening";
                return false;
            }
        }

        for(int i = 0, j = 0; x - i >= 0 && y - j >= 0; i++, j++) {
            if(board[x - i][y - j] == 1) {
                warning = "Queen " + (char) (65 + (y - j)) + "" + (x - i + 1) + " is threatening";
                return false;
            }
        }

        for(int i = 0, j = 0; x + i < board.length && y - j >= 0; i++, j++) {
            if(board[x + i][y - j] == 1) {
                warning = "Queen " + (char) (65 + (y - j)) + "" + (x + i + 1) + " is threatening";
                return false;
            }
        }

        for(int i = 0, j = 0; x - i >= 0 && y + j < board.length; i++, j++) {
            if(board[x - i][y + j] == 1) {
                warning = "Queen " + (char) (65 + (y + j)) + "" + (x - i + 1) + " is threatening";
                return false;
            }
        }

        return true;
    }

    public void placeQueen(int x, int y) {
        int index = ((x + 1) * board.length + (y + 1)) - board.length;
        int iid = getResources().getIdentifier("imageButton" + Integer.toString(index),
                "id", getPackageName());
        View v = findViewById(iid);
        v.setTag("2");
        ImageButton tempB = (ImageButton) v;
        tempB.setImageResource(R.drawable.crown10);

        queens[y] = x;
        board[x][y] = 1;
        queensAlive++;
    }

    public void removeQueen(int x, int y) {
        int index = ((x + 1) * board.length + (y + 1)) - board.length;
        int iid = getResources().getIdentifier("imageButton" + Integer.toString(index),
                "id", getPackageName());
        View v = findViewById(iid);
        v.setTag("1");
        ImageButton tempB = (ImageButton) v;
        tempB.setImageResource(0);

        queens[y] = -1;
        board[x][y] = 0;
        queensAlive--;
    }


    public void showPop(String s){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, s, duration);
        toast.show();

    }

}
