package com.badlogic.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
//игровой экран реалезует экран 
class GameScreen implements Screen {

    //screen
    private Camera camera;
//    видовой экран
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
//    текстура 
    private TextureAtlas textureAtlas;
//    фон
//    часть тестуры фоновые области текстур
//    выделяем память для фона чтобы можно было пробежатся по классам 
    private TextureRegion[] backgrounds;
//    высота фона в мировых единицах измерения
    private float backgroundHeight;
    
    
//    Регион текстур Регион текстур игроков, регион текстур игроков, регион текстур игроков, регион текстур врагов,
    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion,
    enemyShipTextureRegion, enemyShieldTextureRegion,
    playerLaserTextureRegion, enemyLaserTextureRegion;

    //timing
//    смещение фона 
    private float[] backgroundOffsets = {0, 0, 0, 0};
//    максимальная Скорость Прокрутки фона
    private float backgroundMaxScrollingSpeed;

    //world parameters
    private final int WORLD_WIDTH = 72;
    private final int WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 5f;
    
    
    private Ship playerShip;
    private Ship enemyShip;
//    создаем лист лазер 
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;

    GameScreen() {
//камера работа с проекциями 
    	camera = new OrthographicCamera();
//    	видовой экран
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        
        
      //настройка атласа текстур
//        выделяем память для текстур 
        textureAtlas = new TextureAtlas("images.atlas");
        
       //фон имеет 4 изображение 
      //настройка фона
        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

//скорость прокрутки по высое 
        backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = (float) (WORLD_HEIGHT) / 4;
        
        
        //инициализация областей текстуры
        playerShipTextureRegion = textureAtlas.findRegion("playerShip2_blue");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed3");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false, true);

        playerLaserTextureRegion= textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion= textureAtlas.findRegion("laserRed03");
        
        //настройка игровых объектов
        //set up game objects
//        передаем в констркутор 
        playerShip = new PlayerShip(WORLD_WIDTH / 2, WORLD_HEIGHT / 4,
                10, 10,
                48, 3,
                0.4f, 4, 45, 0.5f,
                playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);

        enemyShip = new EnemyShip(WORLD_WIDTH / 2, WORLD_HEIGHT * 3 / 4,
                10, 10,
                2, 1,
                0.3f, 5, 50, 0.8f,
                enemyShipTextureRegion, enemyShieldTextureRegion ,enemyLaserTextureRegion);
//создали лазеры 
        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();
        
//    ресует
        batch = new SpriteBatch();
    }

    @Override
    public void render(float deltaTime) {
//    	происходит рисование 
        batch.begin();
        
        detectInput(deltaTime);
        
        playerShip.update(deltaTime);
        enemyShip.update(deltaTime);

        //scrolling background
        renderBackground(deltaTime);
        //вражеские корабли
        enemyShip.draw(batch);
        //корабль игрока
        playerShip.draw(batch);
        
        //lasers
        renderLasers(deltaTime);

        //обнаружение столкновений между лазерами и кораблями
        detectCollisions();

        //explosions
        renderExplosions(deltaTime);
        
        batch.end();
        }
//    обнаружение ввода
    private void detectInput(float deltaTime) {
    	//ввод с клавиатуры

    	//стратегия: определите максимальное расстояние, на которое может двигаться корабль.
    	//проверьте каждый ключ, который имеет значение, и двигайтесь соответственно
//предел движения 
        float leftLimit, rightLimit, upLimit, downLimit;
//        boundingBox.ограничивающая рамка.
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
//        лимит по верху на половину
        upLimit = WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;
//при нажатии 
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerShip.translate(Math.min(playerShip.movementSpeed*deltaTime, rightLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerShip.translate( 0f, Math.min(playerShip.movementSpeed*deltaTime, upLimit));
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerShip.translate(Math.max(-playerShip.movementSpeed*deltaTime, leftLimit), 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed*deltaTime, downLimit));
        }

        //touch input (also mouse)
      //сенсорный ввод (также мышь)
        if (Gdx.input.isTouched()) {
            //get the screen position of the touch
        	//получить положение экрана касания
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            //convert to world position
//            выделяем память 
            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
//            точка касания видовой экран непроецированная точка касания
            touchPoint = viewport.unproject(touchPoint);
          //вычислите разности x и y
            //calculate the x and y differences
//            ограничительная коробка корабля игрока y высота ограничительной коробки корабля игрока
            Vector2 playerShipCentre = new Vector2(
                    playerShip.boundingBox.x + playerShip.boundingBox.width/2,
                    playerShip.boundingBox.y + playerShip.boundingBox.height/2);
//расстояние касания поплавка Точка касания dst игрок Центр корабля
           float touchDistance = touchPoint.dst(playerShipCentre);
//скорость переещения 
            if (touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference = touchPoint.x - playerShipCentre.x;
                float yTouchDifference = touchPoint.y - playerShipCentre.y;
              //масштабирование до максимальной скорости судна
                //scale to the maximum speed of the ship
                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * deltaTime;
//для нахождения максимума и минимума чисел что оно не выходит за пределы
                if (xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove,leftLimit);

                if (yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove,downLimit);
//       переместить по х у          translate x Move y Move
                playerShip.translate(xMove,yMove);
            }
        }

    }

    
    
    
//    обнаружение столкновений
    private void detectCollisions() {
        //for each player laser, check whether it intersects an enemy ship
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            if (enemyShip.intersects(laser.boundingBox)) {
                //contact with enemy ship
                enemyShip.hit(laser);
                iterator.remove();
            }
        }
        //for each enemy laser, check whether it intersects the player ship
        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            if (playerShip.intersects(laser.boundingBox)) {
                //contact with player ship
                playerShip.hit(laser);
                iterator.remove();
            }
        }
    }

    private void renderExplosions(float deltaTime) {

    }

    private void renderLasers(float deltaTime) {
    
    
    
      //лазеры
      //создание новых лазеров
      //лазеры игроков
        if (playerShip.canFireLaser()) {
            Laser[] lasers = playerShip.fireLasers();
            playerLaserList.addAll(Arrays.asList(lasers));
        }
        
      //вражеские лазеры
        if (enemyShip.canFireLaser()) {
            Laser[] lasers = enemyShip.fireLasers();
            enemyLaserList.addAll(Arrays.asList(lasers));
        }

      //нарисуйте лазеры
      //удаление старых лазеров
//        вставляем в переменную сылку на метод  метод 
//        список лазеров
        ListIterator<Laser> iterator = playerLaserList.listIterator();
//        Этот метод добавляет элемент в коллекцию на следующую позицию после элемента, полученного с помощью метода next(). boolean hasNext() 
//        Этот метод проверяет, есть ли в коллекции следующий элемент.
//        hasNext(): возвращает true, если в коллекции имеется следующий элемент, иначе возвращает false
        while(iterator.hasNext()) {
//        	next(): возвращает текущий элемент и переходит к следующему, если такого нет, то генерируется исключение NoSuchElementException
            Laser laser = iterator.next();
//            рисует лазер 
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed * deltaTime;
//                  laser.boundingBox.y += laser.movementSpeed * deltaTime;
            if (laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }
        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed * deltaTime;
            if (laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

//        создать фон 
    private void renderBackground(float deltaTime) {
//смещение фона  время умноженое на максимально время прокрутки делим на время 
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed /8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed /4 ;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed /2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed;

        //draw each background layer нарисуйте каждый фоновый слой
//        пробежать 
        for (int layer = 0; layer < backgroundOffsets.length; layer++) {
//        	если высота меньше 
            if (backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
//            метод где происходит отрисовка кртинки по номеру расположение 
//       минус  смещения фона по высоте 
            batch.draw(backgrounds[layer], 0, -backgroundOffsets[layer],
                    WORLD_WIDTH, backgroundHeight);
        }
    }


    //маштабировать 
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
