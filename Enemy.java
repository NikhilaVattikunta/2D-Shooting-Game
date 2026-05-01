package main.entities;

import java.awt.*;
import java.util.Random;

public class Enemy {
    public int x, y;
    public int speed;
    int type; // 0 = normal, 1 = zigzag, 2 = fast

    Random rand = new Random();
    int direction = 1;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;

        type = rand.nextInt(3);

        if (type == 2) speed = 4; // fast
        else speed = 2;
    }

    public void update() {
        x -= speed;

        // Zig-zag movement
        if (type == 1) {
            y += direction * 2;

            if (y <= 0 || y >= 550) {
                direction *= -1;
            }
        }

        // Random movement
        if (type == 0) {
            if (rand.nextInt(20) == 0) {
                y += rand.nextInt(21) - 10;
            }
        }
    }

    public void draw(Graphics g) {
        if (type == 0) g.setColor(Color.RED);
        else if (type == 1) g.setColor(Color.ORANGE);
        else g.setColor(Color.MAGENTA);

        g.fillRect(x, y, 30, 30);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 30, 30);
    }
}