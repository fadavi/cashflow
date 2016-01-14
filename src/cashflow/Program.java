package cashflow;

import com.alee.laf.WebLookAndFeel;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import static cashflow.DB.initDB;
import static java.lang.Integer.parseInt;

public final class Program {
    private static MainFrame mainFrame;
    
    private Program() {}
    
    public static void main(String[] args) {
        initUI();
        initDB();

        mainFrame = new MainFrame();
        
        EventQueue.invokeLater(() -> mainFrame.setVisible(true));
    }
    
    public static MainFrame getMainFrame() {
        return mainFrame;
    }
    
    public static JButton button(String text, ActionListener actionListener) {
        JButton btn = new JButton(text);
        btn.addActionListener(actionListener);

        return btn;
    }

    private static void initUI() {
        WebLookAndFeel.setOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        WebLookAndFeel.setDecorateFrames(true);
        WebLookAndFeel.setDecorateDialogs(true);
        WebLookAndFeel.install();
    }

    public static void fatal(Exception ex) {
        String message = ex.getMessage();
        
        System.out.println("ERROR: " + message);
        JOptionPane.showMessageDialog(null, message, "ERROR!", JOptionPane.ERROR_MESSAGE);
        
        System.exit(1);
    }

    public static boolean isValidDate(String date) {
        if(date.isEmpty()) {
            return false;
        }

        String[] factors = date.split("/");

        if(factors.length != 3) {
            return false;
        }

        int year, month, day;

        try {
            year = parseInt(factors[0]);
            month = parseInt(factors[1]);
            day = parseInt(factors[2]);
        } catch(NumberFormatException nfex) {
            return false;
        }

        if(year > 3000 || year < 1000) {
            return false;
        } else if(month > 12 || month < 1) {
            return false;
        } else if(day > 31 || day < 1) {
            return false;
        }

        return true;
    }

    public static boolean isValidAmount(String amountString) {
        try {
            int amount = Integer.parseInt(amountString);
            
            if(amount < 0) {
                return false;
            }
        } catch(NumberFormatException nfex) {
            return false;
        }
        
        return true;
    }
    
    public static void moveToCenter(Window win) {
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        
        int x = (int) (scr.getWidth() - win.getWidth()) / 2;
        int y = (int) (scr.getHeight() - win.getHeight()) / 2;
        
        win.setLocation(x, y);
    }
    
    public static void moveToCenter(Window win, Window par) {
        Point loc = par.getLocation();
        
        int x = loc.x + (int) (par.getWidth() - win.getWidth()) / 2;
        int y = loc.y + (int) (par.getHeight() - win.getHeight()) / 2;
        
        win.setLocation(x, y);
    }
}
