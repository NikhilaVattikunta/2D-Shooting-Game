package main;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

import main.entities.Bullet;
import main.entities.Enemy;
import main.entities.PowerUp;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    Thread gameThread;
    final int FPS = 60;

    int playerX = 100, playerY = 100;
    int playerSpeed = 5;

    boolean up, down, left, right;

    public static Image playerImg, enemyImg, bulletImg, bgImg;

    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<PowerUp> powerUps = new ArrayList<>();

    Random rand = new Random();

    int score = 0;
    int highScore = 0;
    int health = 3;
    boolean gameOver = false;

    int fireRate = 10;
    int fireCooldown = 0;

    // File to store high score
    File file = new File("highscore.txt");

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setFocusable(true);
        this.addKeyListener(this);

        // Load images
        playerImg = Toolkit.getDefaultToolkit().getImage("src/assets/player.png");
        enemyImg = Toolkit.getDefaultToolkit().getImage("src/assets/enemy.png");
        bulletImg = Toolkit.getDefaultToolkit().getImage("src/assets/bullet.png");
        bgImg = Toolkit.getDefaultToolkit().getImage("src/assets/background.png");

        loadHighScore(); // ⭐ Load saved score

        startGameThread();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {

        if (gameOver) return;

        // Movement
        if (up) playerY -= playerSpeed;
        if (down) playerY += playerSpeed;
        if (left) playerX -= playerSpeed;
        if (right) playerX += playerSpeed;

        playerX = Math.max(0, Math.min(playerX, 760));
        playerY = Math.max(0, Math.min(playerY, 560));

        // Spawn enemies
        if (rand.nextInt(100) < 3) {
            enemies.add(new Enemy(800, rand.nextInt(550)));
        }

        // Spawn power-ups
        if (rand.nextInt(500) == 0) {
            powerUps.add(new PowerUp(800, rand.nextInt(550), rand.nextInt(2)));
        }

        // Update bullets
        for (Bullet b : bullets) b.update();

        // Update enemies
        for (Enemy e : enemies) e.update();

        // Update power-ups
        for (PowerUp p : powerUps) p.x -= 2;

        // Bullet vs Enemy
        for (int i = 0; i < bullets.size(); i++) {
            Rectangle bRect = new Rectangle(bullets.get(i).x, bullets.get(i).y, 10, 10);

            for (int j = 0; j < enemies.size(); j++) {
                if (bRect.intersects(enemies.get(j).getBounds())) {
                    bullets.remove(i);
                    enemies.remove(j);
                    score++;
                    break;
                }
            }
        }

        // Enemy vs Player
        Rectangle playerRect = new Rectangle(playerX, playerY, 40, 40);

        for (int i = 0; i < enemies.size(); i++) {
            if (playerRect.intersects(enemies.get(i).getBounds())) {
                enemies.remove(i);
                health--;

                if (health <= 0) {
                    gameOver = true;

                    // Update high score
                    if (score > highScore) {
                        highScore = score;
                        saveHighScore(); // ⭐ Save to file
                    }
                }
            }
        }

        // Power-up collection
        for (int i = 0; i < powerUps.size(); i++) {
            if (playerRect.intersects(powerUps.get(i).getBounds())) {
                if (powerUps.get(i).type == 0) health++;
                else fireRate = 3;

                powerUps.remove(i);
            }
        }

        // Cleanup
        bullets.removeIf(b -> b.x < 0 || b.x > 800 || b.y < 0 || b.y > 600);
        enemies.removeIf(e -> e.x < 0);
        powerUps.removeIf(p -> p.x < 0);

        if (fireCooldown > 0) fireCooldown--;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.drawImage(bgImg, 0, 0, 800, 600, this);

        // Player
        g.drawImage(playerImg, playerX, playerY, 40, 40, this);

        // Bullets
        for (Bullet b : bullets) b.draw(g);

        // Enemies
        for (Enemy e : enemies) e.draw(g);

        // PowerUps
        for (PowerUp p : powerUps) p.draw(g);

        // UI
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Health: " + health, 10, 40);
        g.drawString("High Score: " + highScore, 10, 60);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 250, 300);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", 280, 350);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_W) up = true;
        if (e.getKeyCode() == KeyEvent.VK_S) down = true;
        if (e.getKeyCode() == KeyEvent.VK_A) left = true;
        if (e.getKeyCode() == KeyEvent.VK_D) right = true;

        if (!gameOver && fireCooldown == 0) {

            if (e.getKeyCode() == KeyEvent.VK_UP)
                bullets.add(new Bullet(playerX + 20, playerY, 0, -1));

            if (e.getKeyCode() == KeyEvent.VK_DOWN)
                bullets.add(new Bullet(playerX + 20, playerY + 40, 0, 1));

            if (e.getKeyCode() == KeyEvent.VK_LEFT)
                bullets.add(new Bullet(playerX, playerY + 20, -1, 0));

            if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                bullets.add(new Bullet(playerX + 40, playerY + 20, 1, 0));

            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                bullets.add(new Bullet(playerX + 20, playerY + 20, 1, 0));

            fireCooldown = fireRate;
        }

        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) up = false;
        if (e.getKeyCode() == KeyEvent.VK_S) down = false;
        if (e.getKeyCode() == KeyEvent.VK_A) left = false;
        if (e.getKeyCode() == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void restartGame() {
        playerX = 100;
        playerY = 100;
        bullets.clear();
        enemies.clear();
        powerUps.clear();
        score = 0;
        health = 3;
        gameOver = false;
        fireRate = 10;
    }

    // ⭐ Save high score
    public void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⭐ Load high score
    public void loadHighScore() {
        try {
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}