package com.fifino.flappydroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

//import java.util.Vector;
import android.graphics.Color;
import android.graphics.Paint;

import com.fifino.flappydroid.entities.Coin;
import com.fifino.flappydroid.entities.Floor;
import com.fifino.flappydroid.entities.GameCharacter;
//import com.fifino.flappydroid.entities.MenuItem;
import com.fifino.flappydroid.entities.Pipe;
import com.fifino.framework.BitmapTransform;
import com.fifino.framework.Entity;
import com.fifino.framework.entities.Rectangle;
import com.fifino.framework.entities.Rectangle.CollisionSpot;
import com.fifino.framework.implementation.AndroidEntity;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Graphics.ImageFormat;
import com.kilobolt.framework.Input.TouchEvent;
import com.kilobolt.framework.implementation.AndroidImage;
//import java.io.FileInputStream;

public class GameScreen extends FlappyDroidScreen implements Observer {
    enum GameState {
        Ready, Running, Paused, GameOver
    };

    public static int HEIGHT = 1280;
    public static int WIDTH = 800;

    // GameState state = GameState.Ready;
    GameState state = GameState.Running;
    boolean gameOverPause = false;
    int initialY = 90;
    // Variable Setup
    // You would create game objects here.
    Random rnd;
    int livesLeft = 1;
    int score = 0;
    Paint paint;
    ArrayList<Entity> entities;
    GameCharacter character;
    private Floor floor;
    AndroidImage skyImage;
    int skySpeed = 1;
    int skyX = 0;
    int skyY = 0;
    AndroidImage mountainsImage;
    int mountainsSpeed = 3;
    int mountainsX = 0;
    int mountainsY;
    int mountainsHeight = 400;
    // private MenuItem debugButton;

    public GameScreen(FlappyDroidGame game) {
        super(game);
        GameScreen.HEIGHT = game.getGraphics().getHeight();
        GameScreen.WIDTH = game.getGraphics().getHeight();

        // Defining a paint object
        paint = new Paint();
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        rnd = new Random();

        // Setup entities
        entities = new ArrayList<Entity>();
        initializeAssets();
        setupEntities();
    }
    private String getPipePicture() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        String character;
        switch (month) {
        // case 0:
        // character = "january_pipe.png";
        // break;
            case 1 :
                character = "february_pipe.png";
                break;
            default :
                character = "default_pipe.png";
                break;
        }
        return character;
    }
    private String getCharacterPicture() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        String character;
        switch (month) {
            case 0 :
                character = "january_character.png";
                break;
            case 1 :
                character = "february_character.png";
                break;
            default :
                character = "default_character.png";
                break;
        }
        return character;
    }
    private String getCoinPicture() {
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        String character;
        switch (month) {
            case 0 :
                character = "january_coin.png";
                break;
            case 1 :
                character = "february_coin.png";
                break;
            default :
                character = "default_coin.png";
                break;
        }
        return character;
    }
    protected void initializeAssets() {
        if (Assets.background == null) {
            Graphics graphics = game.getGraphics();
            Assets.background = graphics.newImage("bg-vertical.png",
                    ImageFormat.RGB565);
            skyImage = (AndroidImage) Assets.background;
            skyImage.setBitmap(BitmapTransform.scale(skyImage.getBitmap(),
                    GameScreen.WIDTH, GameScreen.HEIGHT));
            Assets.mountains = graphics.newImage("mountains.png",
                    ImageFormat.RGB565);
            mountainsImage = (AndroidImage) Assets.mountains;
            mountainsImage.setBitmap(BitmapTransform.scale(
                    mountainsImage.getBitmap(), GameScreen.WIDTH,
                    mountainsHeight));

            Assets.bluePipe = graphics.newImage(this.getPipePicture(),
                    ImageFormat.RGB565);

            Assets.tileDirt = graphics.newImage("tile-dirt.png",
                    ImageFormat.RGB565);

            Assets.character = graphics.newImage(this.getCharacterPicture(),
                    ImageFormat.RGB565);

            Assets.gameOver = graphics.newImage("game-over.png",
                    ImageFormat.RGB565);

            Assets.coin = graphics.newImage(this.getCoinPicture(),
                    ImageFormat.RGB565);
            Assets.debugButton = graphics.newImage("debug.png",
                    ImageFormat.RGB565);

            Assets.jumpSound = game.getAudio().createSound("jump.wav");
            Assets.hitSound = game.getAudio().createSound("hit.wav");
            Assets.coinSound = game.getAudio().createSound("coin.wav");
            Assets.tenCoinsSound = game.getAudio().createSound("10-coins.wav");
        }

        // if (FlappyDroidGame.debugMode != FlappyDroidGame.DebugMode.OFF) {
        // AndroidImage debugButtonImage = (AndroidImage) Assets.debugButton;
        // debugButton = new MenuItem(debugButtonImage, 10, 10);
        // entities.add(debugButton);
        // }
        mountainsY = GameScreen.HEIGHT - mountainsHeight - Floor.HEIGHT;
    }

    @Override
    protected void setupEntities() {
        setupPipes();
        setupFloor();
        setupCharacter();
    }

    Pipe pipe1, pipe2;

    private void setupPipes() {
        pipe1 = new Pipe();
        pipe2 = new Pipe();
        entities.add(pipe1);
        entities.add(pipe2);
        pipe1.setX(1200).setPipe(pipe2).addObserver(this);
        pipe2.setX(pipe1.getX() + Pipe.SEPARATION).setPipe(pipe1)
                .addObserver(this);
        Coin c1 = new Coin();
        Coin c2 = new Coin();
        entities.add(c1);
        entities.add(c2);
        pipe1.setCoin(c1);
        pipe2.setCoin(c2);
    }

    private void setupFloor() {
        floor = new Floor();
        entities.add(floor);
    }

    private void setupCharacter() {
        character = new GameCharacter();
        entities.add(character);
        character.addObserver(this);
    }

    @Override
    public void update(float deltaTime) {
        this.skyX -= skySpeed * deltaTime;
        if (skyX <= -GameScreen.WIDTH) {
            this.skyX = 0;
        }
        this.mountainsX -= mountainsSpeed * deltaTime;
        if (mountainsX <= -GameScreen.WIDTH) {
            this.mountainsX = 0;
        }

        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

        // We have four separate update methods in this example.
        // Depending on the state of the game, we call different update methods.
        // Refer to Unit 3's code. We did a similar thing without separating the
        // update methods.

        if (state == GameState.Ready) {
            updateReady(touchEvents);
        } else if (state == GameState.Running) {
            updateRunning(touchEvents, deltaTime);
        } else if (state == GameState.Paused) {
            updatePaused(touchEvents);
        } else if (state == GameState.GameOver) {
            updateGameOver(touchEvents, deltaTime);
        }
    }

    private void updateReady(List<TouchEvent> touchEvents) {

        // This example starts with a "Ready" screen.
        // When the user touches the screen, the game begins.
        // state now becomes GameState.Running.
        // Now the updateRunning() method will be called!

        // if (touchEvents.size() > 0) {
        // Assets.click.play();
        // state = GameState.Running;
        // }
    }

    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
        // 1. All touch input is handled here:
        int len = touchEvents.size();
        try {
            for (int i = 0; i < len; i++) {
                TouchEvent event = touchEvents.get(i);

                if (event.type == TouchEvent.TOUCH_DOWN) {
                    // if (debugButton != null && debugButton.collides(new
                    // Point(event.x, event.y))) {
                    // FlappyDroidGame.debugMode = FlappyDroidGame.debugMode ==
                    // FlappyDroidGame.DebugMode.OFF ?
                    // FlappyDroidGame.DebugMode.FILL
                    // : FlappyDroidGame.DebugMode.OFF;
                    // }
                    character.jump();
                    JSONObject obj = new JSONObject();
                    obj.put("level", 1);
                    obj.put("area", "Game_Screen");
                    game.getAnalyticsProvider().track("Jump", obj, 1, event.x,
                            event.y, 0);
                    Assets.jumpSound.play();
                }
            }
            // 2. Check miscellaneous events like death:
            if (livesLeft == 0) {
                gameOverPause = true;
                state = GameState.GameOver;
            }

        } catch (JSONException e) {
            FlappyDroidGame.ANALYTICS_PROVIDER.exception(e, "updateRunning");
        }
        // 3. Call individual update() methods here.
        // This is where all the game updates happen.
        // For example, robot.update();
        updateEntities(deltaTime);
        checkCollisions();
    }

    private void updateEntities(float delta) {
        for (Entity entity : entities) {
            entity.update(delta);
        }
    }

    protected void collisionDetected(AndroidEntity entity,
            Rectangle[] collisionRectangles) {
        livesLeft--;
        if (collisionRectangles == null || collisionRectangles.length <= 1
                || collisionRectangles.length > 2) {
            return;
        }
        CollisionSpot collisionSpot = Rectangle.getCollisionSpot(
                collisionRectangles[0], collisionRectangles[1]);
        switch (collisionSpot) {
            case LEFT :
                character.setX(entity.getX() - character.getWidth());
                break;
            case RIGHT :
                character.setX(entity.getX() + entity.getWidth());
                break;
            case TOP :
                character.setY(entity.getY() - character.getHeight());
                break;
            case BOTTOM :
                character.setY(entity.getY() + entity.getHeight());
                break;
            case UPPER_LEFT :
                character.setX(entity.getX() - character.getWidth());
                character.setY(entity.getY() - character.getHeight());
                break;
            case BOTTOM_LEFT :
                character.setY(entity.getY() + entity.getHeight());
                character.setX(entity.getX() - character.getWidth());
                break;
            case UPPER_RIGHT :
                character.setY(entity.getY() - character.getHeight());
                character.setX(entity.getX() + entity.getWidth());
                break;
            case BOTTOM_RIGHT :
                character.setY(entity.getY() + entity.getHeight());
                character.setX(entity.getX() + entity.getWidth());
                break;
            default :
                break;
        }
    }

    private void collisionDetected(AndroidEntity entity) {
        Assets.hitSound.play();
        livesLeft--;
    }

    private void checkCollisions() {
        try {
            if (character.collides(floor)) {
                collisionDetected(floor);
                JSONObject obj = new JSONObject();
                obj.put("level", 1);
                obj.put("area", "floor");
                game.getAnalyticsProvider().track("Collides", obj, 1,
                        character.getX(), character.getY(), 0);
                return;
            }
            if (character.collides(pipe1)) {
                collisionDetected(pipe1);
                JSONObject obj = new JSONObject();
                obj.put("level", 1);
                obj.put("area", "pipe1");
                game.getAnalyticsProvider().track("Collides", obj, 1,
                        character.getX(), character.getY(), 0);
                return;
            }
            if (character.collides(pipe2)) {
                collisionDetected(pipe2);
                JSONObject obj = new JSONObject();
                obj.put("level", 1);
                obj.put("area", "pipe2");
                game.getAnalyticsProvider().track("Collides", obj, 1,
                        character.getX(), character.getY(), 0);
                return;
            }
            Coin c1 = pipe1.getCoin(), c2 = pipe2.getCoin();
            if (c1.isVisible() && character.collides(c1)) {
                pipe1.getCoin().setVisible(false);
                scored();
                return;
            }
            if (c2.isVisible() && character.collides(c2)) {
                c2.setVisible(false);
                scored();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {

            }
        }
    }

    private void updateGameOver(List<TouchEvent> touchEvents, float deltaTime) {
        if (gameOverPause) {
            final GameScreen that = this;
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1500);
                        gameOverPause = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        int len = touchEvents.size();
        try {
            for (int i = 0; i < len && !gameOverPause; i++) {
                TouchEvent event = touchEvents.get(i);
                if (event.type == TouchEvent.TOUCH_DOWN) {
                    nullify();
                    FlappyDroidGame.saveHighScore(game);
                    game.setScreen(new MainMenuScreen(game));
                }
            }
        } catch (Exception e) {
            System.err.println("input read error");
        }
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        // First draw the game elements.
        // Example:
        // g.drawImage(Assets.background, 0, 0);
        g.drawImage(Assets.background, skyX, skyY);
        g.drawImage(Assets.background, skyX + GameScreen.WIDTH, skyY);

        g.drawImage(Assets.mountains, mountainsX, mountainsY);
        g.drawImage(Assets.mountains, mountainsX + GameScreen.WIDTH, mountainsY);

        g.drawImage(Assets.mountains, mountainsX, mountainsY);
        g.drawImage(Assets.mountains, mountainsX + GameScreen.WIDTH, mountainsY);

        // Secondly, draw the UI above the game elements.
        if (state == GameState.Ready) {
            drawReadyUI();
        }
        if (state == GameState.Running) {
            drawRunningUI();
        }
        if (state == GameState.Paused) {
            drawPausedUI();
        }
        if (state == GameState.GameOver) {
            drawGameOverUI();
            // drawRunningUI();
        }

    }

    private void nullify() {

        // Set all variables to null. You will be recreating them in the
        // constructor.
        paint = null;
        entities = null;
        pipe1 = null;
        pipe2 = null;
        character = null;
        floor = null;

        // Call garbage collector to clean up memory.
        System.gc();
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        g.drawARGB(155, 0, 0, 0);
        g.drawString("Tap again to start.", 0, 0, paint);

    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();
        // Vector<Entity> entities = (Vector<Entity>) this.entities.clone();
        for (Entity entity : entities) {
            entity.draw(g);
        }
        drawScore(g);
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        // Darken the entire screen so you can display the Paused screen.
        g.drawARGB(155, 0, 0, 0);

    }

    private void drawGameOverUI() {
        int textX = 10, textY = 50, step = 50;
        Graphics g = game.getGraphics();
        // Vector<Entity> entities = (Vector<Entity>) this.entities.clone();
        for (Entity entity : entities) {
            entity.draw(g);
        }
        // // Defining a paint object
        // Paint paint = new Paint();
        // paint.setTextSize(40);
        // paint.setTextAlign(Paint.Align.LEFT);
        // paint.setAntiAlias(true);
        // paint.setColor(Color.BLACK);

        this.drawScore(g);
        this.drawHighScores(g);

        // g.drawString("Score: " + score, textX, textY, paint);
        // if (FlappyDroidGame.HIGH_SCORE > 0) {
        // textY += step;
        // g.drawString("High Score: " + FlappyDroidGame.HIGH_SCORE, textX,
        // textY,
        // paint);
        // }
        // if (FlappyDroidGame.HIGHEST_SCORE > 0) {
        // textY += step;
        // g.drawString("WWW Score: " + FlappyDroidGame.HIGHEST_SCORE, textX,
        // textY, paint);
        // }
    }
    protected Paint[] paints = null;
    public void initPaints() {
        // Defining a paint object
        Paint paint = new Paint();
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        Paint paint2 = new Paint();
        paint2.setTextSize(80);
        paint2.setTextAlign(Paint.Align.CENTER);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.WHITE);
        paint2.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        this.paints = new Paint[]{paint, paint2};
    }
    public Paint[] getPaints() {
        if (this.paints != null) {
            return this.paints;
        }
        initPaints();
        return this.paints;

    }
    public void drawHighScores(Graphics g) {
        int width = g.getWidth();
        int textX = (int) (width / 2), step = 90;
        int textY = initialY + step;

        Paint paints[] = getPaints();
        Paint paint = paints[0];
        Paint paint2 = paints[1];

        g.drawString("High Score: " + FlappyDroidGame.HIGH_SCORE, textX, textY,
                paint);
        g.drawString("High Score: " + FlappyDroidGame.HIGH_SCORE, textX - 2,
                textY - 2, paint2);

         if (FlappyDroidGame.HIGHEST_SCORE > 0) {
            textY += step;
            g.drawString("WWW: " + FlappyDroidGame.HIGHEST_SCORE, textX, textY,
                    paint);
            g.drawString("WWW: " + FlappyDroidGame.HIGHEST_SCORE, textX - 2,
                    textY - 2, paint2);
         }
        
        if(!this.gameOverPause){
            textY += step*2;
            g.drawString("< tap >", textX, textY,
                    paint);
            g.drawString("< tap >", textX - 2,
                    textY - 2, paint2);
            textY += step;
        }
        // if (FlappyDroidGame.HIGH_SCORE > 0) {
        // textY += step;
        // g.drawString("High Score: " + FlappyDroidGame.HIGH_SCORE, textX,
        // textY,
        // paint);
        // }
        // if (FlappyDroidGame.HIGHEST_SCORE > 0) {
        // textY += step;
        // g.drawString("WWW Score: " + FlappyDroidGame.HIGHEST_SCORE, textX,
        // textY, paint);
        // }

    }
    public void drawScore(Graphics g) {
        int width = g.getWidth();
        int textX = (int) (width / 2 - 20), textY = initialY;
        Paint paints[] = getPaints();
        Paint paint = paints[0];
        Paint paint2 = paints[1];
        g.drawString("" + score, textX, textY, paint);
        g.drawString("" + score, textX - 2, textY - 2, paint2);
    }

    @Override
    public void pause() {
        if (state == GameState.Running) {
            state = GameState.Paused;
        }
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {
        pause();
    }

    @Override
    public void update(Observable observable, Object arg) {
        throw new UnsupportedOperationException(
                "Observable might going to be removed.");
        // if (observable instanceof Pipe) {
        // // Pipe pipe = (Pipe) observable;
        // // pipe.deleteObservers();
        // // this.entities.remove(pipe);
        // scored();
        // } else if (observable instanceof GameCharacter) {
        // // user hit something
        // livesLeft--;
        // }
    }

    private void scored() {
        score++;
        JSONObject obj = new JSONObject();
        try {
            obj.put("level", 1);
            obj.put("area", "coin");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (score % 10 == 0) {
            Assets.tenCoinsSound.play();
            game.getAnalyticsProvider().track("Scored", obj, 10,
                    character.getX(), character.getY(), 0);
        } else {
            Assets.coinSound.play();
            game.getAnalyticsProvider().track("Scored", obj, 1,
                    character.getX(), character.getY(), 0);
        }
        if (score > FlappyDroidGame.HIGH_SCORE) {
            FlappyDroidGame.HIGH_SCORE = score;
        }
    }
}