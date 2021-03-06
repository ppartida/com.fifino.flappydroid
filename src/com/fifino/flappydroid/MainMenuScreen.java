package com.fifino.flappydroid;

import java.util.List;

import android.graphics.Color;
import android.graphics.Point;

import com.fifino.flappydroid.entities.MenuItem;
//import com.kilobolt.framework.Game;
import com.kilobolt.framework.Graphics;
import com.kilobolt.framework.Graphics.ImageFormat;
//import com.kilobolt.framework.Screen;
import com.kilobolt.framework.Input.TouchEvent;
import com.kilobolt.framework.implementation.AndroidImage;

public class MainMenuScreen extends FlappyDroidScreen {
    private MenuItem startMenuItem;
    public MainMenuScreen(FlappyDroidGame game) {
        super(game);
        initializeAssets();
        setupEntities();
    }

    @Override
    protected void setupEntities() {
    	AndroidImage image = (AndroidImage)Assets.menu_start;
        int x = 800/2 - image.getWidth()/2;
        int y = 1200/2 - image.getHeight()/2;
        startMenuItem = new MenuItem(image, x, y);
    }

    @Override
    public void update(float deltaTime) {

        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
        	if(touchEvents.size() < len){
        		//Handles out of bounds exception for the list getting empty after getting the size.
        		return;
        	}
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (startMenuItem.collides(new Point(event.x, event.y))) {
                    //Refresh high scores.
                    FlappyDroidGame.loadHighScore(game);
                    // START GAME
                    game.setScreen(new GameScreen(game));
                }
            }
        }
    }
    
    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();
        g.fillRect(0, 0, g.getWidth(), g.getHeight(), Color.WHITE);
        startMenuItem.draw(g);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void backButton() {
        // Display "Exit Game?" Box
    }

    @Override
    protected void initializeAssets() {
        Graphics g = game.getGraphics();
        if(Assets.menu_start == null){
        	Assets.menu_start = g.newImage("start.png", ImageFormat.RGB565);
        }
    }
}