package com.yusufatakanozmen.tankapp;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.Animation;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


import java.util.Random;

public class TankGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TANK_SIZE = 50;
    private static final int BULLET_SIZE = 5;

    private Pane root;
    private Rectangle playerTank;
    private Rectangle[] enemyTanks;
    private Pane bullets;
    private boolean isPlayerTankHit = false;
    private boolean[] isEnemyTankHit = new boolean[3];
    private boolean isTankFacingRight = true;
    private Text gameOverText;
    private Timeline gameLoop;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Tank Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        initializeTanks();
        initializeKeyEvents(scene);
        startGameLoop();
        initializeGameOverText();


    }
    private void initializeGameOverText() {
        gameOverText = new Text("Game Over");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gameOverText.setFill(Color.RED);
        gameOverText.setX(WIDTH / 2 - gameOverText.getLayoutBounds().getWidth() / 2);
        gameOverText.setY(HEIGHT / 2);
        gameOverText.setVisible(false);
        root.getChildren().add(gameOverText);
    }


    private void gameOver() {
        gameOverText.setVisible(true);
        gameLoop.stop();
        // Remove all enemy tanks

        for (Rectangle enemyTank : enemyTanks) {
            root.getChildren().remove(enemyTank);
        }
    }
    private void initializeTanks() {
        playerTank = createTank(Color.BLUE, WIDTH / 2, HEIGHT - TANK_SIZE - 10);
        enemyTanks = new Rectangle[3];
        for (int i = 0; i < enemyTanks.length; i++) {
            enemyTanks[i] = createTank(Color.RED, 50 + i * 250, 50);
        }
        bullets = new Pane();

        root.getChildren().add(playerTank);
        root.getChildren().addAll(enemyTanks);
        root.getChildren().add(bullets);
    }

    private Rectangle createTank(Color color, double x, double y) {
        Rectangle tank = new Rectangle(TANK_SIZE, TANK_SIZE);
        tank.setFill(color);
        tank.setTranslateX(x);
        tank.setTranslateY(y);
        return tank;
    }

    private void initializeKeyEvents(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            switch (code) {
                case LEFT:
                    moveTank(playerTank, -10, 0);
                    isTankFacingRight = false;
                    break;
                case RIGHT:
                    moveTank(playerTank, 10, 0);
                    isTankFacingRight = true;
                    break;
                case UP:
                    moveTank(playerTank, 0, -10);
                    break;
                case DOWN:
                    moveTank(playerTank, 0, 10);
                    break;
                case SPACE:
                    fireBullet(playerTank, true);
                    break;
            }
        });
    }

    private void moveTank(Rectangle tank, double deltaX, double deltaY) {
        double newX = tank.getTranslateX() + deltaX;
        double newY = tank.getTranslateY() + deltaY;
        tank.setTranslateX(interpolate(tank.getTranslateX(), newX, 0.5));
        tank.setTranslateY(interpolate(tank.getTranslateY(), newY, 0.5));
    }

    private double interpolate(double oldValue, double targetValue, double alpha) {
        return oldValue + alpha * (targetValue - oldValue);
    }

    private void fireBullet(Rectangle tank, boolean isPlayerTank) {
        Rectangle bullet = createBullet(isPlayerTank);
        bullet.setTranslateX(tank.getTranslateX() + TANK_SIZE / 2 - BULLET_SIZE / 2);
        bullet.setTranslateY(tank.getTranslateY() + TANK_SIZE / 2 - BULLET_SIZE / 2);
        bullets.getChildren().add(bullet);
        double angle;

        if (isPlayerTank) {
            angle = -Math.PI / 2.0; // Yukarı doğru ateş et
        } else {
            // Düşman tankı ateş ettiğinde oyuncuya doğru yönlendirilsin
            double playerX = playerTank.getTranslateX();
            double playerY = playerTank.getTranslateY();

            double deltaX = playerX - bullet.getTranslateX();
            double deltaY = playerY - bullet.getTranslateY();

            angle = Math.atan2(deltaY, deltaX);
        }

        double speed = 2.0;

        double deltaX = speed * Math.cos(angle);
        double deltaY = speed * Math.sin(angle);

        Timeline bulletMovement = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            bullet.setTranslateX(bullet.getTranslateX() + deltaX);
            bullet.setTranslateY(bullet.getTranslateY() + deltaY);

            // Çarpışma kontrolü burada yapılabilir...

            if (bullet.getTranslateX() < 0 || bullet.getTranslateX() > WIDTH ||
                    bullet.getTranslateY() < 0 || bullet.getTranslateY() > HEIGHT) {
                bullets.getChildren().remove(bullet);
            }
        }));

        bulletMovement.setCycleCount(Timeline.INDEFINITE);
        bulletMovement.play();
    }
    private Rectangle createBullet(boolean isPlayerBullet) {
        Rectangle bullet = new Rectangle(BULLET_SIZE, BULLET_SIZE);
        bullet.setFill(Color.BLACK);
        bullet.getProperties().put("isPlayerBullet", isPlayerBullet);
        return bullet;
    }

    private boolean intersects(Rectangle bullet, Rectangle tank) {
        Bounds bulletBounds = bullet.getBoundsInParent();
        Bounds tankBounds = tank.getBoundsInParent();

        boolean isIntersecting = bulletBounds.intersects(tankBounds) &&
                bulletBounds.getWidth() > 0 && bulletBounds.getHeight() > 0;

        return isIntersecting;
    }

    private void stopEnemyTankFire(int tankIndex) {
        if (enemyBulletTimelines[tankIndex] != null) {
            enemyBulletTimelines[tankIndex].stop();
        }
    }

    private Timeline[] enemyBulletTimelines = new Timeline[3];



    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            moveEnemyTanks();
            checkCollisions();
            fireEnemyBullets();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void fireEnemyBullets() {
    for (int i = 0; i < enemyTanks.length; i++) {
        Rectangle enemyTank = enemyTanks[i];
        if (!isEnemyTankHit[i] && Math.random() < 0.0001) { // Decrease this value for less frequent firing
            fireBullet(enemyTank, false);
        }
    }
}

    private void moveEnemyTanks() {
        for (int i = 0; i < enemyTanks.length; i++) {
            Rectangle enemyTank = enemyTanks[i];
            double playerX = playerTank.getTranslateX();
            double playerY = playerTank.getTranslateY();

            double angle = Math.atan2(playerY - enemyTank.getTranslateY(), playerX - enemyTank.getTranslateX());
            double speed = 1.0; // Adjust the speed as needed (decreased for slower movement)

            double deltaX = speed * Math.cos(angle);
            double deltaY = speed * Math.sin(angle);

            moveTank(enemyTank, deltaX, deltaY);

            if (!isEnemyTankHit[i] && Math.random() < 0.01) {
                fireBullet(enemyTank, false);
            }
        }
    }
    public void checkGameOver() {
        gameOver();
        if (gameOverText.isVisible() && gameLoop.getStatus() == Animation.Status.STOPPED) {
            System.out.println("Game Over method is working correctly.");
        } else {
            System.out.println("Game Over method is not working correctly.");
        }
    }
private void checkCollisions() {
    List<Node> bulletsToRemove = new ArrayList<>();
    List<Node> tanksToRemove = new ArrayList<>();
    List<Rectangle> tanksToAdd = new ArrayList<>();

    // Check if any bullet collides with any enemy tank
    for (Node node : bullets.getChildren()) {
        Rectangle bullet = (Rectangle) node;
        boolean isPlayerBullet = (boolean) bullet.getProperties().get("isPlayerBullet");
        for (int i = 0; i < enemyTanks.length; i++) {
            Rectangle enemyTank = enemyTanks[i];
            if (intersects(bullet, enemyTank)) {
                // Bullet hit enemy tank
                if (isPlayerBullet) {
                    // Add the enemy tank and the bullet to the removal lists
                    tanksToRemove.add(enemyTank);
                    bulletsToRemove.add(bullet);
                    isEnemyTankHit[i] = true;

                    // Create a new enemy tank at a random position
                    Random rand = new Random();
                    double x = rand.nextInt(WIDTH - TANK_SIZE);
                    double y = rand.nextInt(HEIGHT / 2);
                    Rectangle newEnemyTank = createTank(Color.RED, x, y);
                    tanksToAdd.add(newEnemyTank);
                    enemyTanks[i] = newEnemyTank;

                    // Reset the hit status for the new tank
                    isEnemyTankHit[i] = false;
                }
            }
        }
    }

    // Check if any enemy bullet collides with player tank
    for (Node node : bullets.getChildren()) {
        Rectangle bullet = (Rectangle) node;
        boolean isPlayerBullet = (boolean) bullet.getProperties().get("isPlayerBullet");
        if (intersects(bullet, playerTank) && !isPlayerBullet) {
            // Enemy bullet hit player tank
            // Add the player tank and the bullet to the removal lists
            tanksToRemove.add(playerTank);
            bulletsToRemove.add(bullet);
            isPlayerTankHit = true;
            gameOver();
        }
    }

    // Remove the tanks and bullets outside of the iteration
    root.getChildren().removeAll(tanksToRemove);
    bullets.getChildren().removeAll(bulletsToRemove);

    // Add the new tanks to the root
    root.getChildren().addAll(tanksToAdd);
}}