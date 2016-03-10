package paint;


import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;



public class paint
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Paint");
        
        JPanel content = new JPanel();
        frame.add(content);

        content.setLayout(new BorderLayout());

        JPaintPane paint = new JPaintPane(800,600);
        content.add(paint.getTools(), BorderLayout.NORTH);
        content.add(paint, BorderLayout.CENTER);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
