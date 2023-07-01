import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;

public class World extends JFrame {

  private JPanel map;
  private JLabel[][] maplink;
  public int[][] mapInt;
  public ArrayList<int[]> finishPoints = new ArrayList<int[]>();
  private int width;
  private int height;
  private String res = "resources/";
  private String road = "road";
  private String green = "green";
  private String build = "build";
  private String agent = "agent";
  private String goal = "goal";
  private String sizepng = "7.png";

  public World(Operator a) throws IOException {

    File file = new File(res+ "map3.png");
    Color[][] colors = loadPixelsFromFile(file);
    width = colors.length;
    height = colors[0].length;

    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());

    map = new JPanel();
    map.setLayout(new GridLayout(height, width));
    mapInt = new int[height][width];
    maplink = new JLabel[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        JLabel pix = new JLabel();
        switch (colors[j][i].getRGB()) {
          case (-16777216): {           //road
            pix.setIcon(new ImageIcon(res + road + sizepng));
            mapInt[i][j] = 1;
            break;
          }
          case (-14503604): {          //green
            pix.setIcon(new ImageIcon(res + green + sizepng));
            mapInt[i][j] = 0;
            break;
          }
          case (-8421505): {            //build
            mapInt[i][j] = 0;
            pix.setIcon(new ImageIcon(res + build + sizepng));
          }
        }
        map.add(pix);
        maplink[i][j] = pix;
      }
    }
    p.add(map, BorderLayout.CENTER);
    getContentPane().add(p, BorderLayout.CENTER);
  }

  public void setAgent(int height, int width) {
    if (maplink[height][width].getIcon().equals(new ImageIcon(res + agent + sizepng)))
      System.out.println("Ошибка, врезались!!!");
    else {
      maplink[height][width].setIcon(new ImageIcon(res + agent + sizepng));
      mapInt[height][width] = 0;
    }
  }

  public void clearAgentLastPlace(int height, int width) {
    int[] a = {height,width};
    boolean check = false;
    for(var f : finishPoints)
      if (f[0]== a[0]&&f[1]== a[1]) {
        check = true;
        break;
      }
    if (check)
      maplink[height][width].setIcon(new ImageIcon(res + goal + sizepng));
    else
      maplink[height][width].setIcon(new ImageIcon(res + road + sizepng));

    mapInt[height][width] = 1;
  }


  public void showGui() {
    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerX = (int) screenSize.getWidth() / 2;
    int centerY = (int) screenSize.getHeight() / 2;
    setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
    setResizable(false);
    super.setVisible(true);
  }

  public Color[][] loadPixelsFromFile(File file) throws IOException {

    BufferedImage image = ImageIO.read(file);
    Color[][] colors = new Color[image.getWidth()][image.getHeight()];

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        colors[x][y] = new Color(image.getRGB(x, y));
      }
    }

    return colors;
  }

  public void setStart(int height, int width) {
    finishPoints.add(new int[]{height,width});
    maplink[height][width].setIcon(new ImageIcon(res + goal + sizepng));
  }

  public boolean isBarrier(int y, int x) {
    if (mapInt[y][x] == 1) {
      mapInt[y][x] = 0;
      return false;
    }
    else
      return true;
  }
}
