import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Plugin_ implements PlugInFilter
{
	private class Console extends JFrame implements WindowListener
	{
		private static final long serialVersionUID = -1929422500948532428L;
		private final static int MAXCHARCOUNT = 100000;
		private final JTextArea textArea = new JTextArea();
		
		public Console()
		{
			super("Console");
			setAlwaysOnTop(false);
			setResizable(true);
			setVisible(true);
			setSize(400, 400);
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			textArea.setCaretPosition(textArea.getDocument().getLength());
			JButton button = new JButton("ERASE");
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setAutoscrolls(true);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
			{
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e)
				{
					if(!(e.getSource() instanceof JScrollBar))
						return;
					JScrollBar scBar = (JScrollBar) e.getSource();
					int maximum = scBar.getMaximum() - scBar.getHeight();
					if(maximum - e.getValue() < 0.02f * maximum)
						scBar.setValue(maximum);
				}
			});
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			getContentPane().add(button, BorderLayout.SOUTH);
			setVisible(true);
			addWindowListener(this);
			button.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					textArea.setText("");
				}
			});
		}
		
		public void addtext(String text)
		{
			if(textArea != null)
			{
				textArea.append("\n" + text);
				textArea.repaint();
				//if(textArea.getText().length() > MAXCHARCOUNT)
				//	textArea.setText(textArea.getText().substring(MAXCHARCOUNT / 10, textArea.getText().length()));
			}
		}
		
		@Override
		public void windowOpened(WindowEvent e)
		{
		}
		
		@Override
		public synchronized void windowClosing(WindowEvent evt)
		{
		}
		
		@Override
		public void windowClosed(WindowEvent e)
		{
			setVisible(false);
			dispose();
		}
		
		@Override
		public void windowIconified(WindowEvent e)
		{
		}
		
		@Override
		public void windowDeiconified(WindowEvent e)
		{
		}
		
		@Override
		public void windowActivated(WindowEvent e)
		{
		}
		
		@Override
		public void windowDeactivated(WindowEvent e)
		{
		}
	}
	
	private static final Color[] baseColors = {
			Color.YELLOW,
			Color.GREEN,
			Color.RED,
			Color.BLUE,
			new Color(102, 51, 0),
			Color.GRAY,
			Color.WHITE,
			Color.BLACK,
			Color.ORANGE
	};
	
	private static Console console;
	
	public void run(ImageProcessor ip)
	{
		console = new Console();
		console.setVisible(true);
		Set<Color> colors = getColors(ip, ip.getWidth() * ip.getHeight() * 0.2);
		IJ.showMessage(getBeautifulColors(colors));
	}
	
	private String getBeautifulColors(Collection<Color> colors)
	{
		StringBuilder builder = new StringBuilder();
		for(Color color : colors)
			builder.append(color.toString()).append(", ");
		if(builder.length() > 1)
			builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
	
	private Set<Color> getColors(ImageProcessor ip, double threshold)
	{
		HashMap<Color, Integer> colors = new HashMap<Color, Integer>();
		for(int i = 0; i < ip.getWidth(); i++)
			for(int j = 0; j < ip.getHeight(); j++)
			{
				Color c = getClosestColor(i, j, ip.getColorModel().getRGB(ip.getPixel(i, j)));
				if(!colors.containsKey(c))
					colors.put(c, 0);
				colors.put(c, colors.get(c) + 1);
			}
		
		Set<Color> set = new HashSet<Color>();
		for(Color c : colors.keySet())
		{
			if(colors.get(c) > threshold)
				set.add(c);
		}
		return set;
	}
	
	private Color getClosestColor(int x, int y, int i)
	{
		double minDist = Double.MAX_VALUE;
		Color bestColor = null;
		
		Color c = new Color(i);
		
		for(Color c2 : baseColors)
		{
			double dist = getDistance(c, c2);
			if(x == 0)
				console.addtext(String.format("x: %d, y: %d, color: %d, color2: %s, testColor: %s, distance: %f", x, y, i, c.toString(), c2.toString(), dist));
			if(dist < minDist)
			{
				minDist = dist;
				bestColor = c2;
			}
		}
		
		return bestColor;
	}
	
	private double getDistance(Color c1, Color c2)
	{
		return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2));
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		if(arg.equals("about"))
		{
			IJ.showMessage("Traitement de l'image v2");
			return DONE;
		}
		return DOES_8G;
	}
}
