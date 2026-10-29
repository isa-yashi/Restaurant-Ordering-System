import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Order Queue");

        //bg color
        frame.setSize(1280, 832);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.decode("#d5d5d5"));

        //panel
        frame.setLayout(new BorderLayout());
        JPanel left = new JPanel();
        left.setBackground(Color.decode("#f8f8f8"));
        left.setPreferredSize(new Dimension(237, 100));

        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);

        frame.add(left, BorderLayout.WEST);
        frame.add(center, BorderLayout.CENTER);

    }
}
