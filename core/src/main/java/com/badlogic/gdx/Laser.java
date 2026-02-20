package com.badlogic.gdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

class Laser {
	
	//position and dimensions
    Rectangle boundingBox;
    //laser physical characteristics
//    скорость движения
    float movementSpeed; //world units per second

    //graphics
//    изображение 
    TextureRegion textureRegion;
//получаем параметры в конструкторе 
    public Laser(float xCentre, float yBottom, float width, float height, float movementSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(xCentre - width / 2, yBottom, width, height);

        this.movementSpeed = movementSpeed;
        this.textureRegion = textureRegion;
    }
// ресуем изображение фото позиция размер 
    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }
    
}
    
    
    
    
    
    
    
    
    
    
    
    
