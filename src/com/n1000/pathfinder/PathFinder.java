package com.n1000.pathfinder;

import java.awt.*;
import java.awt.image.BufferStrategy;

// Coded by nazar1000 (GitHub)
// Idea and info gotten from https://www.youtube.com/watch?v=-L-WgKMFuhE&t=136s

public class PathFinder extends Canvas implements Runnable {

    public static int WIN_WIDTH = 800, WIN_HEIGHT = 600;

    private Thread thread;
    private boolean running = false;
    private int tick = 0;
    private Map map;


    public PathFinder () {
        map = new Map();
        new Window("PathFinder", this);
        this.addMouseListener(map);
        this.addMouseWheelListener(map);
        this.addMouseMotionListener(map);
    }


    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;

    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double ns = 5000000; // 1/4 of a second   (10000000 = 1sec)
        double delta = 0;
        int frames = 0;
        long timer = System.currentTimeMillis();
        while(running) {
            long now = System.nanoTime();
            delta +=  (now - lastTime) / ns;
            lastTime = now;

            while(delta >= 1) {
                    update();
                    delta--;
                    render();

            }
            if(running)
                frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                //System.out.println("FPS: " + frames);

                frames = 0;
            }
        }
        stop();
    }

    private void update() {
    map.update();

    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, WIN_WIDTH, WIN_HEIGHT);

        map.render(g);

        g.dispose();
        bs.show();
    }



    public static void main(String[] args) {
        new PathFinder();
    }
}

