package com.badlogic.gdx;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
//набор методов, которые класс должен реализоват
public class Main implements ApplicationListener {
	//картинки звуки квадрат картинки обьекты 
	
	//	//сначало все создали и потом все нарисовали 
	
	
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Texture bulletTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;
    float dropTimer;
    float ballTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    //create() — вызывается один раз при создании приложения.
    @Override
    public void create() {
    	//Это позволяет загрузить ресурсы в память после запуска
    	// уже имеющийся классы просто прописываешь свою ссылку 
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        bulletTexture = new Texture("bullet.png");
        
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        
        
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(9, 7);
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        touchPos = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }
  //Вызывается один раз сразу после метода create() размер фоновой  картинки 
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
//render() выполняется 60 раз в секунду 
    @Override
    public void render() {
    	//сначало все создали и потом все нарисовали 
        input();
        logic();
        draw();
    }

    private void input() {
    	//скорость передвижения корзины 
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();
  //если нажата кнопка то это 
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
        	//товызывается токойто спрайт с такойто скоростью по горизонтале 
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }
 //мышка
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }
    }

    private void logic() {
    	//получить ширену высоту фона 
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        ////получить ширену высоту ведра
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();
        //чтобы ведро не смещалось за края 
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
     //получить текущую дельту
        float delta = Gdx.graphics.getDeltaTime();
        //размер ведра и его позицыя переводим в квадрат 
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }
//создать капли дождя вывереном тайменги 
        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
           
        }
        
        
        
        ballTimer += delta;
        if (ballTimer > 3f) {
        	ballTimer= 0;
           createBall();
        
    }
        
    }
//рисуем 
    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
//группа спрайтов 
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }
//создать капли 
    private void createDroplet() {
        float dropWidth = 1;
        float dropHeight = 1;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);
        
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        dropSprites.add(dropSprite);
    }
  
    
    
    //создать пулю 
    private void createBall() {
    	//размер пули 
        float dropWidth = -1;
        float dropHeight = +1;
        // получить размер фоновой картинки
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
   
    	//создаем спрайты пули
        Sprite dropBall = new Sprite(bulletTexture);
        //установить размер картинки 
        dropBall.setSize(dropWidth, dropHeight);
       //вылет пули  с какого места 

        dropBall.setX(worldHeight);
        
        dropSprites.add(dropBall);
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
}