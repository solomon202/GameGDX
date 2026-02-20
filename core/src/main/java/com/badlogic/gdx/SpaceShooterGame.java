package com.badlogic.gdx;

import com.badlogic.gdx.Game;
//игра
public class SpaceShooterGame extends Game {

	GameScreen gameScreen;
//создаем игровое поле 
	@Override
	public void create() {
		gameScreen = new GameScreen();
//		установить экран лаунчер 
		//переходим на экран игры
		setScreen(gameScreen);
	}


	@Override
	public void dispose() {
		gameScreen.dispose();
	}


	@Override
	public void render() {
		super.render();
	}


	@Override
	public void resize(int width, int height) {
		gameScreen.resize(width, height);
	}
}
