package Map;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.JPanel;

import MainAgents.Coordinates;

public class MapGUIPanel extends JPanel implements ActionListener{

    private Map currentMapInfo;
    Timer timer;
    private BufferedImage busIcon;
    private BufferedImage stopIcon;


    public MapGUIPanel(Map currentMapInfo) {
        this.currentMapInfo = currentMapInfo;
        this.timer =new Timer(Map.REFRESH_RATE, this);
        timer.start();
        
        try {
            //URL url;
            File url = new File("src/img/bus.png");
            /*try{
                url = getClass().getResource("img/bus.png");
            }catch(Error e){
                url = getClass().getResource("src/img/bus.png");
            }*/
            busIcon = ImageIO.read(new File(url.getPath()));
            url = new File("src/img/stop.png");
            /*try{
                url = getClass().getResource("img/stop.png");
            }catch(Error e){
                url = getClass().getResource("src/img/stop.png");
            }*/
            stopIcon = ImageIO.read(new File(url.getPath()));
        } catch (IOException ex) {
            Logger.getLogger(MapGUIPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
       
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color busColor = Color.BLUE;
        Color stopColor = Color.RED;

        Font font = new Font("SansSerif", Font.BOLD, 10);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        
        colorize(busIcon,busColor);
        colorize(stopIcon,stopColor);
        
        Coordinates coord;
        
        g.setColor(busColor);
        int CELL_SIZE = 20;
        for(String id: currentMapInfo.getBusList().keySet()){
            coord = currentMapInfo.getBusList().get(id);
            g.drawImage(busIcon, coord.getX()* CELL_SIZE, coord.getY()* CELL_SIZE, CELL_SIZE, CELL_SIZE, this);
            g.drawString(id,coord.getX()* CELL_SIZE -metrics.stringWidth(id)/2+ CELL_SIZE /2, coord.getY()* CELL_SIZE + CELL_SIZE +font.getSize());
        }
     
        g.setColor(stopColor);
        for(String id : currentMapInfo.getStopList().keySet()){
            coord = currentMapInfo.getStopList().get(id);
            g.drawImage(stopIcon, coord.getX()* CELL_SIZE, coord.getY()* CELL_SIZE, CELL_SIZE, CELL_SIZE, this);
            g.drawString(id,coord.getX()* CELL_SIZE -metrics.stringWidth(id)/2+ CELL_SIZE /2, coord.getY()* CELL_SIZE + CELL_SIZE +font.getSize());
        }
        
    }
    
    private static void colorize(BufferedImage bImage, Color newColor) {
        for (int x = 0; x < bImage.getWidth(); x++) {
             for (int y = 0; y < bImage.getHeight(); y++) {
                 if(!isPixelTransparent(bImage,x,y)){
                    bImage.setRGB(x, y, newColor.getRGB());
                 }
             }
         }
    }
    
    private static boolean isPixelTransparent(BufferedImage img,int x, int y ) {
        int pixel = img.getRGB(x,y);
        return (pixel >> 24) == 0x00;
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               
            }
        });
    }
    
    public void actionPerformed(ActionEvent ev){
        if(ev.getSource()==timer){
          repaint();// this will call at the map refresh rate
        }
    }

}
