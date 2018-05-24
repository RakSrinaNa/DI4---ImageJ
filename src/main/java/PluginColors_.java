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

public class PluginColors_ implements PlugInFilter
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
		
		HashMap<Color, Double> colors = getColors(ip);
		
		IJ.showMessage(getBeautifulColors(colors, ip.getWidth() * ip.getHeight(), 0.1));
	}
	
	private String getBeautifulColors(Map<Color, Double> colors, int count, double threshold)
	{
		StringBuilder builder = new StringBuilder();
		
		for(Color color : colors.keySet())
			if(colors.get(color) > count * threshold)
				builder.append(baseColors.get(color)).append(":").append(colors.get(color) / (double) count).append(", ");
		
		if(builder.length() > 1)
			builder.delete(builder.length() - 2, builder.length());
		
		return builder.toString();
	}
	
	private HashMap<Color, Double> getColors(ImageProcessor ip)
	{
		ImageProcessor ip2 = ip.createProcessor(ip.getWidth(), ip.getHeight());
		ip.setColorModel(ColorModel.getRGBdefault());
		
		HashMap<Color, Double> colors = new HashMap<Color, Double>();
		
		for(int i = 0; i < ip.getWidth(); i++)
		{
			for(int j = 0; j < ip.getHeight(); j++)
			{
				HashMap<Color, Double> closest = getClosestColors(i, j, ip.getColorModel().getRGB(ip.getPixel(i, j)));
				for(Color c : closest.keySet())
				{
					if(!colors.containsKey(c))
						colors.put(c, 0.0);
					colors.put(c, colors.get(c) + closest.get(c));
				}
			}
		}
		ip.insert(ip2, 0, 0);
		return colors;
	}
	
	private HashMap<Color, Double> getClosestColors(int x, int y, int i)
	{
		//Set color
		Color c = new Color(i);
		float hsb1[] = new float[3];
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb1);
		
		//Tab comp
		HashMap<Color, Double> ref = new HashMap<Color, Double>();
		
		for(Color c2 : baseColors.keySet())
		{
			//Set color ref
			float hsb2[] = new float[3];
			Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsb2);
			ref.put(c2, getDistanceHSB(hsb1, hsb2));
		}
		
		//Normalize
		double sum = 0.0;
		for(double value : ref.values())
			sum += value;
		
		for(Color c3 : ref.keySet())
			ref.put(c3, ref.get(c3) / sum);
		
		if(x == 0)
			console.addtext(String.format("x: %d\t y: %d\t color: %s\t distance: %s", x, y, Arrays.toString(hsb1), ref.toString()));
		
		return ref;
	}
	
	private double getDistanceHSB(float[] hsb1, float[] hsb2)
	{
		return 0.22 * Math.sqrt(Math.pow(hsb1[0] - hsb2[0], 2)) + 0.39 * Math.sqrt(Math.pow(hsb1[1] - hsb2[1], 2)) + 0.39 * Math.sqrt(Math.pow(hsb1[2] - hsb2[2], 2));
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		if(arg.equals("about"))
		{
			IJ.showMessage("Traitement de l'image v2");
			return DONE;
		}
		return DOES_ALL;
	}
}
