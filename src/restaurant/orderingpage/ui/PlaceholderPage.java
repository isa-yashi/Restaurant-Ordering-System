package restaurant.orderingpage.ui;

import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Empty page for the sidebar entries that your teammates will build (Order Queue, History...). */
public class PlaceholderPage extends JPanel {
    public PlaceholderPage(String title) {
        super(new GridBagLayout());
        setBackground(ColorTheme.PAGE_BG);
        JLabel l = new JLabel("<html><center>" + title + "<br><span style='font-size:11px'>"
                + "(page not built yet)</span></center></html>");
        l.setFont(ColorTheme.f(Font.PLAIN, 22));
        l.setForeground(ColorTheme.GRAY_TXT);
        add(l);
    }
}
