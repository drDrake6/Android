package step.learning.basics;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.os.VibratorManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game2048Activity extends AppCompatActivity {
    public enum Direction {UP, DOWN, LEFT, RIGHT}
    private int[][] cells = new int[4][4];
    private int[][] prev_cells = new int[4][4];
    private TextView[][] tvCells = new TextView[4][4];
    private int score;
    private int bestScore;
    private TextView tvScore;
    private TextView tvBestScore;
    private final String bestScoreFilename = "best_score.txt";
    private final Random random = new Random();
    private Animation spawnAnimation;
    private Animation scaleAnimation;
    boolean wasUndo;
    List<Integer> newCells;
    private boolean isContinuePlaying;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        spawnAnimation = AnimationUtils.loadAnimation(this, R.anim.spawn_cell);
        spawnAnimation.reset();

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_cell);
        scaleAnimation.reset();

        tvScore = findViewById(R.id.tv_score);

        tvBestScore = findViewById(R.id.tv_best_score);
        tvBestScore.setText(getString(R.string.best_score_text, bestScore));

        newCells = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tvCells[i][j] = findViewById(
                        getResources().getIdentifier("cell" + i + j,
                                "id",
                                getPackageName())
                );
            }
        }

        startNewGame();

        findViewById(R.id.layout_2048)
                .setOnTouchListener(new OnSwipeListener(Game2048Activity.this){
                    @Override
                    public void OnSwipeRight() {
                        if (move(Direction.RIGHT)) spawnCell();
                        else wrongMove();

                    }

                    @Override
                    public void OnSwipeLeft() {
                        if(move(Direction.LEFT)) spawnCell();
                        else wrongMove();
                    }

                    @Override
                    public void OnSwipeTop() {
                        if(move(Direction.UP)) spawnCell();
                        else wrongMove();
                    }

                    @Override
                    public void OnSwipeBottom() {
                        if(move(Direction.DOWN)) spawnCell();
                        else wrongMove();
                    }
                });

        findViewById(R.id.undoButton)
                .setOnClickListener(this::undoClick);

        findViewById(R.id.newButton)
                .setOnClickListener(this::newGameClick);

    }

    private void wrongMove(){
        Toast.makeText(Game2048Activity.this, "Nothing can be moved!", Toast.LENGTH_SHORT).show();
        long[] vibrationPattern = {0, 200, 100, 200};
        Vibrator vibrator;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            VibratorManager vibratorManager = (VibratorManager)
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }
        else{
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(
                    VibrationEffect.createWaveform(vibrationPattern, -1)
            );
        }
        else {
            vibrator.vibrate(vibrationPattern, -1);
        }
    }
    private boolean saveBestScore(){
        try ( FileOutputStream fos = openFileOutput(bestScoreFilename, Context.MODE_PRIVATE)){
            DataOutputStream writer = new DataOutputStream(fos);
            writer.writeInt(bestScore);
            writer.flush();
            writer.close();
        }
        catch (IOException ex) {
            Log.d("saveBestScore", ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean loadBestScore(){
        try ( FileInputStream fis = openFileInput(bestScoreFilename)){
            DataInputStream reader = new DataInputStream(fis);
            bestScore = reader.readInt();
            reader.close();
        }
        catch (IOException ex) {
            Log.d("loadBestScore", ex.getMessage());
            return false;
        }
        return true;
    }

    private void showWinDialog(){
        new AlertDialog.Builder(this, R.style.Theme_FirstApp)
                .setTitle("Победа!")
                .setMessage("Вы собрали 2048")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        isContinuePlaying = true;
                    }
                })
                .setNegativeButton("Выйти", (dialog, whichButton) -> {
                    finish();
                })
                .setNeutralButton("Заново", (dialog, whichButton) -> {
                    startNewGame();
                })
                .setCancelable(false)
                .show();
    }

    private void showLoseDialog(){
        new AlertDialog.Builder(this, R.style.Theme_FirstApp)
                .setTitle("Лось!")
                .setMessage("Вы всрали)")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton("Отмена хода", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        undo();
                    }
                })
                .setNegativeButton("Выйти", (dialog, whichButton) -> {
                    finish();
                })
                .setNeutralButton("Заново", (dialog, whichButton) -> {
                    startNewGame();
                })
                .setCancelable(false)
                .show();
    }

    private boolean isWin(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(cells[i][j] == 8){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean spawnCell(){
        List<Integer> freeCells = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(cells[i][j] == 0){
                    freeCells.add(i * 10 + j);
                }
            }
        }

        int cnt = freeCells.size();
        if(cnt == 0) return false;

        int rnd = random.nextInt(cnt);
        int x = freeCells.get(rnd) / 10;
        int y = freeCells.get(rnd) % 10;
        cells[x][y] = random.nextInt(10) == 0 ? 4 : 2;
        tvCells[x][y].startAnimation(spawnAnimation);
        showField();
        return true;
    }

    private boolean spawnCellIn(int x, int y){
        List<Integer> freeCells = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(cells[i][j] == 0){
                    freeCells.add(i * 10 + j);
                }
            }
        }

        int cnt = freeCells.size();
        if(cnt == 0) return false;

        cells[x][y] = random.nextInt(10) == 0 ? 4 : 2;
        tvCells[x][y].startAnimation(spawnAnimation);
        showField();
        return true;
    }

    private void showField() {
        if(isLose()){
            showLoseDialog();
        }
        for (int i = 0; i < newCells.size(); i++) {
            int x = newCells.get(i) / 4;
            int y = newCells.get(i) % 4;
            tvCells[x][y].startAnimation(scaleAnimation);
        }
        newCells.clear();
        Resources resources = getResources();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tvCells[i][j].setText(String.valueOf(cells[i][j]));
                tvCells[i][j].setTextAppearance(getResources().getIdentifier("Cell_" + cells[i][j],
                        "style",
                        getPackageName()));
                tvCells[i][j].setBackgroundColor(
                        resources.getColor(resources.getIdentifier(
                                "_" + cells[i][j],
                                "color",
                                getPackageName()
                        ), getTheme())
                );

            }
        }

        tvScore.setText(getString(R.string.score_text, score));
        if(score > bestScore){
            bestScore = score;
            saveBestScore();
            tvBestScore.setText(getString(R.string.best_score_text, bestScore));
        }
        if(!isContinuePlaying){
            if(isWin()){
                showWinDialog();
            }
        }

    }

    private void savePrevField(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                prev_cells[i][j] = cells[i][j];
            }
        }
        wasUndo = false;
    }

    private boolean isFullField(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(cells[i][j] == 0){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasNearSameCell(int i, int j, int value){
        if(i - 1 >= 0 && cells[i - 1][j] == value) return true;
        else if (i + 1 <= 3 && cells[i + 1][j] == value) return true;
        else if (j - 1 >= 0 && cells[i][j - 1] == value) return true;
        else if (j + 1 <= 3 && cells[i][j + 1] == value) return true;
        else return false;
    }

    private boolean hasMove(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(hasNearSameCell(i, j, cells[i][j])){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLose(){
        if(isFullField() && !hasMove()) return true;
        else return false;
    }

    private boolean move(Direction direction){
        savePrevField();
        if(direction == Direction.LEFT) return moveLeft();
        else if(direction == Direction.RIGHT) return moveRight();
        else if(direction == Direction.UP) return moveUp();
        else return moveDown();
    }

    private boolean moveLeft() {
        int lastFree;
        boolean result = false;

        for (int i = 0; i < 4; i++) {
            lastFree = 0;
            for (int j = 1; j < 4; j++) {
                if(cells[i][j - 1] != 0 && cells[i][j] == 0)
                {
                    lastFree = j;
                }
                else if(cells[i][j - 1] == 0 && cells[i][j] != 0){
                    cells[i][lastFree] = cells[i][j];
                    cells[i][j] = 0;
                    lastFree++;
                    result = true;
                    score += cells[i][j];
                }
            }

            for (int j = 0; j < 3; j++) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i][j + 1]){
                        cells[i][j] *= 2;
                        newCells.add(i * cells[i].length + j);
                    for (int k = j + 1; k < 3; k++) {
                        cells[i][k] = cells[i][k + 1];
                    }
                    cells[i][3] = 0;
                    result = true;
                    score += cells[i][j];
                }
            }
        }

        return result;
    }

    private boolean moveRight() {
        int lastFree;
        boolean result = false;

        for (int i = 0; i < 4; i++) {
            lastFree = 3;
            for (int j = 2; j >= 0; j--) {
                if(cells[i][j + 1] != 0 && cells[i][j] == 0)
                {
                    lastFree = j;
                }
                else if(cells[i][j + 1] == 0 && cells[i][j] != 0){
                    cells[i][lastFree] = cells[i][j];
                    cells[i][j] = 0;
                    lastFree--;
                    result = true;
                }
            }

            for (int j = 3; j > 0; j--) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i][j - 1]){
                    cells[i][j] *= 2;
                    newCells.add(i * cells[i].length + j);
                    for (int k = j - 1; k > 0; k--) {
                        cells[i][k] = cells[i][k - 1];
                    }
                    cells[i][0] = 0;
                    result = true;
                    score += cells[i][j];
                }
            }
        }

        return result;
    }

    private boolean moveUp() {
        int lastFree;
        boolean result = false;

        for (int j = 0; j < 4; j++) {
            lastFree = 0;
            for (int i = 1; i < 4; i++) {
                if(cells[i - 1][j] != 0 && cells[i][j] == 0)
                {
                    lastFree = i;
                }
                else if(cells[i - 1][j] == 0 && cells[i][j] != 0){
                    cells[lastFree][j] = cells[i][j];
                    cells[i][j] = 0;
                    lastFree++;
                    result = true;
                }
            }

            for (int i = 0; i < 3; i++) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i + 1][j]){
                    cells[i][j] *= 2;
                    newCells.add(i * cells[i].length + j);
                    for (int k = i + 1; k < 3; k++) {
                        cells[k][j] = cells[k + 1][j];
                    }
                    cells[3][j] = 0;
                    result = true;
                    score += cells[i][j];
                }
            }
        }

        return result;
    }

    private boolean moveDown() {
        int lastFree;
        boolean result = false;

        for (int j = 0; j < 4; j++) {
            lastFree = 3;
            for (int i = 2; i >= 0; i--) {
                if(cells[i + 1][j] != 0 && cells[i][j] == 0)
                {
                    lastFree = i;
                }
                else if(cells[i + 1][j] == 0 && cells[i][j] != 0){
                    cells[lastFree][j] = cells[i][j];
                    cells[i][j] = 0;
                    lastFree--;
                    result = true;
                }
            }

            for (int i = 3; i > 0; i--) {
                if (cells[i][j] != 0 && cells[i][j] == cells[i - 1][j]){
                    cells[i][j] *= 2;
                    newCells.add(i * cells[i].length + j);
                    for (int k = i - 1; k > 0; k--) {
                        cells[k][j] = cells[k - 1][j];
                    }
                    cells[0][j] = 0;
                    result = true;
                    score += cells[i][j];
                }
            }
        }

        return result;
    }

    private void undo(){
        if (!wasUndo){
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    cells[i][j] = prev_cells[i][j];
                }
            }
            wasUndo = true;
            showField();
        }
    }
    private void undoClick(View v){
        undo();
    }

    private void clearField(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                cells[i][j] = prev_cells[i][j] = 0;
            }
        }
    }

    private void startNewGame(){
        wasUndo = true;

        score = 0;
        if(!loadBestScore()){
            bestScore = 0;
        }

        clearField();
        showField();

        spawnCell();
        spawnCell();
    }

    private void newGameClick(View v){
        startNewGame();
    }

}