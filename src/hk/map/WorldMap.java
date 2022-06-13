package hk.map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WorldMap {

    private final int size = 750;
    private Area area;

    private final ArrayList<Shape> shapes;

    private final JLabel output;
    private final JComponent ui;

    private final BufferedImage image;

    public WorldMap(File image) throws IOException {
        this.image = ImageIO.read(image);
        this.area = getOutline(this.image, Color.WHITE, 12);
        this.shapes = shapeRegions(area);
        this.output = new JLabel();
        this.ui = new JPanel(new BorderLayout(4, 4));
        this.ui.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.ui.add(output);
    }

    public void refresh() {
        output.setIcon(new ImageIcon(getImage()));
    }

    private ArrayList<Shape> shapeRegions(Shape shape) {

        var regions = new ArrayList<Shape>();
        var pathIterator = shape.getPathIterator(null);
        var path = new GeneralPath();

        while (!pathIterator.isDone()) {

            var coords = new double[6];
            var segmentType = pathIterator.currentSegment(coords);
            var windingRule = pathIterator.getWindingRule();

            path.setWindingRule(windingRule);

            switch (segmentType) {

                case PathIterator.SEG_MOVETO -> {
                    path = new GeneralPath();
                    path.setWindingRule(windingRule);
                    path.moveTo(coords[0], coords[1]);
                }

                case PathIterator.SEG_CLOSE -> {
                    path.closePath();
                    regions.add(new Area(path));
                }

                case PathIterator.SEG_LINETO -> path.lineTo(coords[0], coords[1]);
                case PathIterator.SEG_QUADTO -> path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                case PathIterator.SEG_CUBICTO -> path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);

                default -> System.err.println("ERROR! Segment type not supported: " + segmentType);

            }

            pathIterator.next();

        }

        return regions;

    }

    public Area getOutline(BufferedImage image, Color target, int tolerance) {
        GeneralPath gp = new GeneralPath();

        boolean cont = false;
        for (int xx = 0; xx < image.getWidth(); xx++) {
            for (int yy = 0; yy < image.getHeight(); yy++) {
                if (isEligible(new Color(image.getRGB(xx, yy)), target, tolerance)) {
                    //if (bi.getRGB(xx,yy)==targetRGB) {
                    if (cont) {
                        gp.lineTo(xx, yy);
                        gp.lineTo(xx, yy + 1);
                        gp.lineTo(xx + 1, yy + 1);
                        gp.lineTo(xx + 1, yy);
                        gp.lineTo(xx, yy);
                    } else {
                        gp.moveTo(xx, yy);
                    }
                    cont = true;
                } else {
                    cont = false;
                }
            }
            cont = false;
        }
        gp.closePath();

        // construct the Area from the GP & return it
        return new Area(gp);
    }

    private BufferedImage getImage() {

        BufferedImage image = new BufferedImage(2 * size, size, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        g.drawImage(image, 0, 0, output);
        g.setColor(Color.WHITE);
        g.fill(area);
        g.setColor(Color.BLACK);
        g.draw(area);

        try {

            Point p = MouseInfo.getPointerInfo().getLocation();
            Point p1 = output.getLocationOnScreen();

            int delta_x = p.x - p1.x;
            int delta_y = p.y - p1.y;

            Point point_on_image = new Point(delta_x, delta_y);

            for (Shape shape : shapes) {
                if (shape.contains(point_on_image)) {
                    g.setColor(Color.BLUE.brighter());
                    g.fill(shape);
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR! " + e);
            e.printStackTrace();
        }

        g.dispose();

        return image;

    }

    private boolean isEligible(Color target, Color pixel, int tolerance) {
        var tR = target.getRed();
        var tG = target.getGreen();
        var tB = target.getBlue();
        var pR = pixel.getRed();
        var pG = pixel.getGreen();
        var pB = pixel.getBlue();
        return ((pR - tolerance <= tR) && (tR <= pR + tolerance) && (pG - tolerance <= tG) && (tG <= pG + tolerance) && (pB - tolerance <= tB) && (tB <= pB + tolerance));
    }

    public Container getUI() {
        return ui;
    }

    public JLabel getOutput() {
        return output;
    }

}
