package paint;
/**
 * 
 */

/**
 * @author Karel Dhondt (biohax)
 * 
 * Description : JPaintPane is a basic paint wrapped in a JComponent
 * 
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class JPaintPane extends JComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private BufferedImage preview;

	private int tool;
	private double scale = 1;

	private boolean firstPolygon = true;

	private String text;
        private Font font;
        private Color color;
	private int size = 1;
        
	private Point startDrag, endDrag;
        private JDialog inputDialog;

	final static int TOOL_PEN = 0;
	final static int TOOL_LINE = 1;
	final static int TOOL_RECT = 2;
	final static int TOOL_OVAL = 3;
	final static int TOOL_POLYGON = 4;
	final static int TOOL_ERASER = 5;
	final static int TOOL_TEXT = 6;
	
        private static final Integer[] SIZEARRAY = {4,6,8,10,12,14,16,36,42};

        
	

	public JPaintPane(int x, int y)
	{
            BufferedImage temp = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = temp.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, temp.getWidth(), temp.getHeight());
            g.dispose();

            init(temp);
	}

	public JPaintPane(BufferedImage image)
	{
            init(image);
	}
	
	public void init(BufferedImage image)
	{
            super.setBackground(Color.green);
            setMinimumSize(new Dimension(image.getWidth(), image.getHeight()));
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

            setColor(Color.BLACK);
            this.image = image;
            this.preview = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = preview.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            addListeners();
	}
	
	public void setColor(Color c)
	{
		this.color = c;
	}

	private void addListeners()
	{

            this.addMouseListener(new MouseAdapter()
            {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        switch(tool){
                            case TOOL_POLYGON:
                                if (firstPolygon){
                                    startDrag = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));
                                    firstPolygon = false;
                                } else{
                                    startDrag = endDrag;
                                    endDrag = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));
                                }						
                            break;

                            default: startDrag = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        endDrag = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));

                        if (tool == TOOL_TEXT){
                            if(inputDialog==null){
                                inputDialog = new TextInputDialog();
                            }
                            if (!inputDialog.isActive()){
                                inputDialog.show();
                            }
                            
                        } else {
                            submitCurrentTool();
                        }
                    }
            });

            this.addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    endDrag = new Point((int) (e.getX()/scale), (int) (e.getY()/scale));
                    if (tool == TOOL_PEN || tool == TOOL_ERASER){
                        submitCurrentTool();
                    } else {
                        repaint();
                    }
                }
            });
	}

        @Override
	public void paintComponent(Graphics gg)
	{
		super.paintComponent(gg);
		
		Graphics2D g = preview.createGraphics();
		// uses last buffer to create preview
		
		scale = ((double) getWidth() / (double) image.getWidth(null));
		g.drawImage(image, 0, 0, null);
		g.setPaint(this.color);
                g.setFont(font);
		g.setStroke(new BasicStroke(this.size));
		
		if (startDrag != null && endDrag != null)
		{
			
			switch (tool)
			{
			
                            case TOOL_PEN:
                                    g.drawLine(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                                    startDrag=endDrag;
                            break;

                            case TOOL_ERASER:
                                    g.fillRect(startDrag.x, startDrag.y, 2*size, 2*size);
                                    startDrag=endDrag;
                            break;

                            case TOOL_LINE:
                                    g.drawLine(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                                    break;

                            case TOOL_RECT:
                                    g.drawRect(Math.min(startDrag.x, endDrag.x), Math.min(startDrag.y, endDrag.y), Math.abs(startDrag.x - endDrag.x), Math.abs(startDrag.y - endDrag.y));
                                    break;

                            case TOOL_OVAL:
                                    g.drawOval(Math.min(startDrag.x, endDrag.x), Math.min(startDrag.y, endDrag.y), Math.abs(startDrag.x - endDrag.x), Math.abs(startDrag.y - endDrag.y));
                                    break;

                            case TOOL_POLYGON:
                                    g.drawLine(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                            break;

                            case TOOL_TEXT:
                                    g.drawString(text, endDrag.x, endDrag.y);
                            break;
                            
                            
			
			}

		} 
		
		g.dispose();
		gg.drawImage(preview, 0, 0, (int) (preview.getWidth() * scale), (int) (preview.getHeight() * scale), null);
		gg.dispose();
	}

	public void clear()
	{
            // Reset drag points
            startDrag=null;
            endDrag=null;        
            
            // Reset buffer & image            
            Graphics2D imageGraph = image.createGraphics();
            Graphics2D previewGraph = preview.createGraphics();
            
            imageGraph.setPaint(Color.WHITE);
            previewGraph.setPaint(Color.WHITE);
            
            imageGraph.fillRect(0, 0, image.getWidth(),image.getHeight());
            previewGraph.fillRect(0, 0, image.getWidth(),image.getHeight());
            
            imageGraph.setPaint(Color.BLACK);
            previewGraph.setPaint(Color.BLACK);
            
            imageGraph.dispose();
            previewGraph.dispose();
            repaint();
	}
	
	private void submitCurrentTool()
        {
            Graphics2D g = image.createGraphics();
            g.drawImage(preview, 0, 0, null);
            g.dispose();
            repaint();
	}

	private void cursorchange()
	{
            if (tool == TOOL_ERASER)
            {
                URL imageurl = this.getClass().getResource("/resources/eraser.png");
                Image image = Toolkit.getDefaultToolkit().getImage(imageurl);
                Point hotSpot = new Point(0, 0);
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot, "Eraser");
                setCursor(cursor);
                setColor(Color.WHITE);
            } else
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                setColor(Color.BLACK);
            }
	}
        
	public void setTool(int tool)
	{
            // reset points
            startDrag = null;
            endDrag = null;

            this.tool = tool;

            if (tool == TOOL_POLYGON)
            {
                firstPolygon = true;
            }

            setDoubleBuffered(false);
            cursorchange();
        }

	public BufferedImage getImage()
	{
            return preview;
	}       

	public JPanel getTools()
	{
		JPanel tools = new JPanel();
		// tools.setLayout(new BoxLayout(tools, BoxLayout.PAGE_AXIS));

		JButton clearButton = new JButton("Clear");
		tools.add(clearButton);
		clearButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.clear();
			}
		});

		JButton eraseButton = new JButton("Eraser");
		tools.add(eraseButton);
		eraseButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_ERASER);
			}
		});

		JButton rectButton = new JButton("Rectangle");
		tools.add(rectButton);
		rectButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_RECT);
			}
		});

		JButton ovalButton = new JButton("Oval");
		tools.add(ovalButton);
		ovalButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
                            JPaintPane.this.setTool(JPaintPane.TOOL_OVAL);

			}
		});

		JButton penButton = new JButton("Pencil");
		tools.add(penButton);
		penButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_PEN);
			}
		});

		JButton lineButton = new JButton("Line");
		tools.add(lineButton);
		lineButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_LINE);
			}
		});

		JButton polygonButton = new JButton("Polygon");
		tools.add(polygonButton);
		polygonButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_POLYGON);
			}
		});

		JButton textButton = new JButton("Text");
		tools.add(textButton);
		textButton.addActionListener(new ActionListener()
		{
                        @Override
			public void actionPerformed(ActionEvent e)
			{
				JPaintPane.this.setTool(JPaintPane.TOOL_TEXT);
			}
		});

		final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 16, 1);
		tools.add(slider);
		slider.addChangeListener(new ChangeListener()
		{
                        @Override
			public void stateChanged(ChangeEvent event)
			{
				JPaintPane.this.size = slider.getValue();
			}
		});

		return tools;

	}
        
        //TODO colorPanel
        
        private class TextInputDialog extends JDialog {


            private final JPanel fieldsPanel;

            private final JComboBox<String> fonts;
            private final JTextField input;
            private final JComboBox<Integer> sizeInput;
            private final JTextComponent tc;
            
    
            private final String[] FONTNAMES = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            
            public TextInputDialog() {
                                
                this.setSize(300,70);
                this.setLocationRelativeTo(null); //center of main jframe
                this.setResizable(false);              
                
                fieldsPanel = new JPanel(new GridLayout(1,3));
                
                sizeInput = new JComboBox<>(SIZEARRAY);
                sizeInput.setEditable(true);
                tc = (JTextComponent) sizeInput.getEditor().getEditorComponent();
                
                input = new JTextField();
                fonts = new JComboBox<>(FONTNAMES);
                
		fieldsPanel.add(sizeInput);
                fieldsPanel.add(input);
                fieldsPanel.add(fonts);
                
                this.add(fieldsPanel);
                
                addListners();
                
            }
        
            private void addListners() {
                input.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent documentEvent) { warn(); }
                    public void insertUpdate(DocumentEvent documentEvent)  { warn(); }
                    public void removeUpdate(DocumentEvent documentEvent)  { warn(); } 
                });
                tc.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent documentEvent) { warn(); }
                    public void insertUpdate(DocumentEvent documentEvent)  { warn(); }
                    public void removeUpdate(DocumentEvent documentEvent)  { warn(); } 

                });
                fonts.addActionListener (new ActionListener () {
                    public void actionPerformed(ActionEvent e) { warn(); }
                });
                input.addKeyListener(new KeyAdapter() {
                    
                    @Override
                    public void keyPressed(KeyEvent evt){
                        if(evt.getKeyCode()==evt.VK_ENTER){
                            submitCurrentTool();
                            hide();
                        }
                    } 
                });
                
            }
            
            private void warn() {
                
                size = Integer.valueOf(tc.getText());
                font = new Font( (String)fonts.getSelectedItem(), Font.PLAIN, size);
                text = input.getText();
                
                JPaintPane.this.repaint();
                
	    }

            @Override
            public void show() {
                new Thread(){
                    @Override
                    public void run(){
                        text=""; //empty String
                        TextInputDialog.super.show();
                    }
                }.start();
            }  

            //TODO
            //Actionhandler for window 'X'
            
        }
}