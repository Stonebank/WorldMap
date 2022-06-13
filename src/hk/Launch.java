package hk;

import hk.map.WorldMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

public class Launch implements MouseMotionListener {

    private static WorldMap map;

    public static void main(String[] args) throws IOException {

        map = new WorldMap(new File("./resources/world_map.png"));

        Runnable runnable = () -> {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("ERROR!" + e);
                e.printStackTrace();
            }

            map.getOutput().addMouseMotionListener(new Launch());

            JFrame frame = new JFrame("World map created by Hassan K");
            frame.setPreferredSize(new Dimension(1920, 1080));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationByPlatform(true);
            frame.setContentPane(map.getUI());
            frame.pack();
            frame.setVisible(true);

        };

        SwingUtilities.invokeLater(runnable);

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        map.refresh();
    }

}
