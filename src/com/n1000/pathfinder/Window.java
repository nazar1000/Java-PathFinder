package com.n1000.pathfinder;

import javax.swing.*;
import java.awt.*;

public class Window extends Canvas {

    public Window(String title, PathFinder pathFinder) {
        JFrame frame = new JFrame(title);

        frame.setMinimumSize(new Dimension(pathFinder.WIN_WIDTH+16, pathFinder.WIN_HEIGHT+39));

        //frame.setPreferredSize(new Dimension(pathFinder.WIN_WIDTH, pathFinder.WIN_HEIGHT));
        //frame.setMaximumSize(new Dimension(pathFinder.WIN_WIDTH, pathFinder.WIN_HEIGHT));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if( 1 > -1 && 1 < gd.length ) {
            frame.setLocation(gd[1].getDefaultConfiguration().getBounds().x+200, frame.getY()+200);


        } else if( gd.length > 0 ) {
            frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
        } else {
            throw new RuntimeException( "No Screens Found" );
        }

        frame.setExtendedState(JFrame.NORMAL);
        frame.setUndecorated(false);
        frame.setVisible(true);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
       //frame.setLocationRelativeTo(null);
        frame.add(pathFinder);
        frame.setVisible(true);
        pathFinder.start();









    }
}
