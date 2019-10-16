
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tiaguinho
 */
public class MapGUIPanel extends JPanel implements ActionListener{
    private final int CELL_SIZE = 10;
    
    private Map currentMapInfo;
    Timer timer;


    public MapGUIPanel(Map currentMapInfo) {
        this.currentMapInfo = currentMapInfo;
        this.timer =new Timer(Map.REFRESH_RATE, this);
        timer.start();
    }
       
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();

        int rX = (size.width - 5)/2;
        int rY = (size.height - 5)/2;

        g.setColor(Color.RED);
        
        for(Coordinates coord : currentMapInfo.getBusList().values()){
            g2.fillRect(coord.getX()*CELL_SIZE, coord.getY()*CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
        
         g.setColor(Color.BLUE);
        
        for(Coordinates coord : currentMapInfo.getStopList().values()){
            g2.fillRect(coord.getX()*CELL_SIZE, coord.getY()*CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    public static void main(String args[]) {
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
