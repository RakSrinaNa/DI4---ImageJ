import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
	
	private static HashMap<Color, String> baseColors;
	
	private static Console console;
	
	public void run(ImageProcessor ip)
	{
		baseColors = new HashMap<Color, String>();
		baseColors.put(Color.YELLOW, "Jaune");
		baseColors.put(Color.GREEN, "vert");
		baseColors.put(Color.RED, "Rouge");
		baseColors.put(Color.BLUE, "Bleu");
		baseColors.put(new Color(102, 51, 0), "Marron");
		baseColors.put(Color.GRAY, "Gris");
		baseColors.put(Color.WHITE, "Blanc");
		baseColors.put(Color.BLACK, "Noir");
		baseColors.put(Color.ORANGE, "Orange");
		console = new Console();
		console.setVisible(true);
		HashMap<Color, Integer> colors = getColors(ip.duplicate().convertToRGB());
		IJ.showMessage(getBeautifulColors(colors, ip.getWidth() * ip.getHeight(), 0.1));
	}
	
	private String getBeautifulColors(Map<Color, Integer> colors, int count, double threshold)
	{
		StringBuilder builder = new StringBuilder();
		for(Color color : colors.keySet())
			if(colors.get(color) > threshold * count)
				builder.append(baseColors.get(color)).append(":").append(colors.get(color) / (double) count).append(", ");

		if(builder.length() > 1)
			builder.delete(builder.length() - 2, builder.length());

		return builder.toString();
	}
	
	private HashMap<Color, Integer> getColors(ImageProcessor ip)
	{
		ip.medianFilter();
		ImageProcessor ip2 = ip.createProcessor(ip.getWidth(), ip.getHeight());
		ip.setColorModel(ColorModel.getRGBdefault());
		HashMap<Color, Integer> colors = new HashMap<Color, Integer>();
		for(int i = 0; i < ip.getWidth(); i++)
		{
			for(int j = 0; j < ip.getHeight(); j++)
			{
				Color c = getClosestColor(i, j, ip.getColorModel().getRGB(ip.getPixel(i, j)));
				if(!colors.containsKey(c))
					colors.put(c, 0);
				colors.put(c, colors.get(c) + 1);
				ip2.putPixel(i, j, c.getRGB());
			}
		}
		ip.insert(ip2, 0, 0);
		return colors;
	}
	
	private Color getClosestColor(int x, int y, int i)
	{
		double minDist = Double.MAX_VALUE;
		Color bestColor = null;
		
		Color c = new Color(i);
		float hsb1[] = new float[3];
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb1);
		
		for(Color c2 : baseColors.keySet())
		{
			float hsb2[] = new float[3];
			Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsb2);
			double dist = getDistanceHSB(hsb1, hsb2);
			//double dist = getDistance(c, c2);
			if(x == 0)
			{
				//console.addtext(String.format("x: %d, y: %d, color: %s, testColor: %s, distance: %f", x, y, c.toString(), c2.toString(), dist));
				console.addtext(String.format("x: %d, y: %d, color: %s, testColor: %s, distance: %f", x, y, Arrays.toString(hsb1), Arrays.toString(hsb2), dist));
			}
			if(dist < minDist)
			{
				minDist = dist;
				bestColor = c2;
			}
		}
		
		return bestColor;
	}
	
	private double getDistanceHSB(float[] hsb1, float[] hsb2)
	{
		return 0.24 * Math.sqrt(Math.pow(hsb1[0] - hsb2[0], 2)) + 0.38 * Math.sqrt(Math.pow(hsb1[1] - hsb2[1], 2)) + 0.38 * Math.sqrt(Math.pow(hsb1[2] - hsb2[2], 2));
	}
	
	private double getDistance(Color c1, Color c2)
	{
		return Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2);
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		if(arg.equals("about"))
		{
			IJ.showMessage("Traitement de l'image v2");
			return DONE;
		}
		return DOES_RGB + DOES_8G + DOES_16 + DOES_32;
	}
}
