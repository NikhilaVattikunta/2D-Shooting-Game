package main.entities;

import java.awt.*;

public class PowerUp {
    public int x, y;
    public int type; // 0 = health, 1 = rapid fire

    public PowerUp(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void draw(Graphics g) {
        if (type == 0) g.setColor(Color.GREEN);
        else g.setColor(Color.CYAN);

        g.fillOval(x, y, 20, 20);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }
}