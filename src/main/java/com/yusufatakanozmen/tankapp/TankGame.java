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
        System.out.println("Firing bullet from " + (isPlayerTank ? "player tank" : "enemy tank"));

        Rectangle bullet = new Rectangle(BULLET_SIZE, BULLET_SIZE);
        bullet.setFill(Color.BLACK);
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
    private boolean intersects(Rectangle bullet, Rectangle tank) {
        Bounds bulletBounds = bullet.getBoundsInParent();
        Bounds tankBounds = tank.getBoundsInParent();

        double padding = 5.0; // Belirli bir boşluk bırakabilirsiniz

        boolean isIntersecting = bulletBounds.intersects(tankBounds.getMinX() + padding, tankBounds.getMinY() + padding,
                tankBounds.getWidth() - 2 * padding, tankBounds.getHeight() - 2 * padding) &&
                bulletBounds.getWidth() > 0 && bulletBounds.getHeight() > 0;

        if (isIntersecting) {
            System.out.println("Bullet intersects with tank!");
        }

        return isIntersecting;
    }


    private void stopEnemyTankFire(int tankIndex) {
        if (enemyBulletTimelines[tankIndex] != null) {
            enemyBulletTimelines[tankIndex].stop();
        }
    }

    private Timeline[] enemyBulletTimelines = new Timeline[3];


    private void startGameLoop() {
        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            moveEnemyTanks();
            checkCollisions();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
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

            if (!isEnemyTankHit[i] && Math.random() < 0.02) {
                fireBullet(enemyTank, false);
            }
        }
    }
    private void checkCollisions() {
        // Oyuncu tankının düşman tanklarına çarpıp çarpmadığını kontrol et
        for (Rectangle enemyTank : enemyTanks) {
            if (intersects(playerTank, enemyTank)) {
                // Oyuncu tankı düşman tankına çarptı
                // Burada gerekirse oyun durumuyla ilgili işlemler yapılabilir
                isPlayerTankHit = true;
                System.out.println("Player tank hit by enemy tank!");
                // Örneğin, oyunu durdurabilir veya oyunu yeniden başlatabilirsiniz.
            }
        }

        // Düşman tanklarının ateşlediği mermilerin oyuncu tankına çarpıp çarpmadığını kontrol et
        for (int i = 0; i < enemyBulletTimelines.length; i++) {
            if (enemyBulletTimelines[i] != null) {
                Rectangle enemyBullet = (Rectangle) bullets.getChildren().get(i);
                if (intersects(enemyBullet, playerTank)) {
                    // Düşman tankının mermisi oyuncu tankına çarptı
                    // Burada gerekirse oyun durumuyla ilgili işlemler yapılabilir
                    stopEnemyTankFire(i); // Düşmanın ateşini durdur
                    isPlayerTankHit = true;
                    System.out.println("Player tank hit by enemy bullet!");
                    // Örneğin, oyunu durdurabilir veya oyunu yeniden başlatabilirsiniz.
                }
            }
        }
    }

}