/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private HashSet<String> wordSet = new HashSet<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placedTiles = new Stack<>();
    private LinearLayout word1LL;
    private LinearLayout word2LL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length() == WORD_LENGTH){
                    words.add(word);
                    wordSet.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());

        word1LL = findViewById(R.id.word1);
        word2LL = findViewById(R.id.word2);
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
//                if (stackedLayout.empty()) {
//                    TextView messageBox = (TextView) findViewById(R.id.message_box);
//                    messageBox.setText(word1 + " " + word2);
//                }
                gameEnd();
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
//                    if (stackedLayout.empty()) {
//                        TextView messageBox = (TextView) findViewById(R.id.message_box);
//                        messageBox.setText(word1 + " " + word2);
//
//                    }
                    gameEnd();
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public void gameEnd(){
        if(stackedLayout.empty()){

            String word1 = "";
            String word2 = "";

            ViewGroup viewGroup1 = (ViewGroup) word1LL;
            for (int i = 0; i < viewGroup1.getChildCount(); i++) {
                View child = viewGroup1.getChildAt(i);
                if(child instanceof LetterTile){
                    word1 += ((LetterTile) child).getText();
                }
            }

            ViewGroup viewGroup2 = (ViewGroup) word2LL;
            for (int i = 0; i < viewGroup2.getChildCount(); i++) {
                View child = viewGroup2.getChildAt(i);
                if(child instanceof LetterTile){
                    word2 += ((LetterTile) child).getText();
                }
            }
            TextView endMsg = findViewById(R.id.endMessage);
            if(wordSet.contains(word1) && wordSet.contains(word2)){
                endMsg.setText("You won!!");
                endMsg.setVisibility(View.VISIBLE);
                TextView messageBox = (TextView) findViewById(R.id.message_box);
                messageBox.setText(word1 + " " + word2);
                Button undoButton = findViewById(R.id.button);
                undoButton.setEnabled(false);
            }



        }

    }

    public boolean onStartGame(View view) {

        TextView endMsg = findViewById(R.id.endMessage);
        endMsg.setVisibility(View.INVISIBLE);

        Button undoButton = findViewById(R.id.button);
        undoButton.setEnabled(true);

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        word1LL.removeAllViews();
        word2LL.removeAllViews();

        stackedLayout.clear();

        int random1 = random.nextInt(words.size());
        int random2 = random.nextInt(words.size());
        while(random1 == random2)
            random2 = random.nextInt(words.size());
        word1 = words.get(random1);
        word2 = words.get(random2);

        //scrambling words
        char[] letterArray = scramble(word1, word2);

        TextView message = findViewById(R.id.message_box);
        message.setText(new String(letterArray));

        for(int count = letterArray.length - 1; count >= 0; count--){
            LetterTile tile = new LetterTile(this.getApplicationContext(), letterArray[count]);
            stackedLayout.push(tile);
        }

        return true;
    }

    public char[] scramble(String word1, String word2){

        int counter1 = 0;
        int counter2 = 0;
        char[] letterArray = new char[WORD_LENGTH*2];
        int arrayCount = 0;

        while(counter1 < WORD_LENGTH || counter2 < WORD_LENGTH){
            if((random.nextBoolean() && counter1 < WORD_LENGTH) || counter2 >= WORD_LENGTH){
                letterArray[arrayCount] = word1.charAt(counter1);
                counter1++;
            }
            else{
                letterArray[arrayCount] = word2.charAt(counter2);
                counter2++;
            }
            arrayCount++;
        }

        Log.i("test", "Word 1:" + word1 + " Word 2:" + word2 + " Result:" + new String(letterArray));

        return letterArray;

    }

    public boolean onUndo(View view) {
        if(placedTiles.empty())
            return false;
        placedTiles.pop().moveToViewGroup(stackedLayout);
        return true;
    }
}
