import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PluginColor_ implements PlugInFilter
{
	private static HashMap<Color, String> baseColors;
	
	public void run(ImageProcessor ip)
	{
		baseColors = new HashMap<Color, String>();
		baseColors.put(new Color(255, 255, 0), "Jaune");
		
		baseColors.put(new Color(0, 255, 0), "Vert");
		baseColors.put(new Color(0, 189, 9), "Vert");
		baseColors.put(new Color(105, 240, 112), "Vert");
		baseColors.put(new Color(0, 118, 36), "Vert");
		baseColors.put(new Color(72, 86, 33), "Vert");
		baseColors.put(new Color(44, 58, 58), "Vert");
		
		baseColors.put(new Color(255, 0, 0), "Rouge");
		baseColors.put(new Color(255, 0, 66), "Rouge");
		baseColors.put(new Color(199, 0, 39), "Rouge");
		baseColors.put(new Color(129, 25, 0), "Rouge");
		
		baseColors.put(new Color(0, 0, 255), "Bleu");
		baseColors.put(new Color(17, 31, 58), "Bleu");
		baseColors.put(new Color(119, 24, 255), "Bleu");
		baseColors.put(new Color(91, 33, 74), "Bleu");
		baseColors.put(new Color(117, 63, 121), "Bleu");
		baseColors.put(new Color(0, 255, 255), "Bleu");
		baseColors.put(new Color(0, 189, 195), "Bleu");
		baseColors.put(new Color(0, 174, 255), "Bleu");
		baseColors.put(new Color(134, 217, 255), "Bleu");
		baseColors.put(new Color(98, 140, 255), "Bleu");
		
		baseColors.put(new Color(102, 51, 0), "Marron");
		baseColors.put(new Color(113, 76, 43), "Marron");
		
		baseColors.put(new Color(128, 128, 128), "Gris");
		baseColors.put(new Color(192, 192, 192), "Gris");
		baseColors.put(new Color(64, 64, 64), "Gris");
		baseColors.put(new Color(64, 64, 64), "Gris");
		baseColors.put(new Color(30, 19, 17), "Gris");
		
		baseColors.put(new Color(255, 255, 255), "Blanc");
		baseColors.put(new Color(202, 212, 221), "Blanc");
		
		baseColors.put(new Color(0, 0, 0), "Noir");
		baseColors.put(new Color(2, 11, 12), "Noir");
		baseColors.put(new Color(7, 18, 19), "Noir");
		
		baseColors.put(new Color(255, 200, 0), "Orange");
		baseColors.put(new Color(220, 74, 1), "Orange");
		baseColors.put(new Color(234, 142, 119), "Orange");
		getColors(ip.duplicate().convertToRGB());
	}
	
	private String getBeautifulColors(String title, Map<String, Integer> colors, int count, double threshold)
	{
		StringBuilder builder = new StringBuilder();
		for(String color : colors.keySet())
			if(colors.get(color) > threshold * count)
				builder.append(color).append(":").append(String.format("%.2f%%", 100 * colors.get(color) / (double) count)).append(", ");
		
		if(builder.length() > 1)
			builder.delete(builder.length() - 2, builder.length());
		
		File outFile = new File(title + "_tag" + ".txt");
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(new FileOutputStream(outFile));
			try
			{
				pw.print("Quality: ");
				for(String color : colors.keySet())
					if(colors.get(color) > threshold)
						builder.append(color).append(" ");
				pw.println();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(pw != null)
				pw.close();
		}
		
		return builder.toString();
	}
	
	private HashMap<String, Integer> getColors(ImageProcessor ip)
	{
		ImageProcessor ip2 = ip.createProcessor(ip.getWidth(), ip.getHeight());
		ip.medianFilter();
		ip.setColorModel(ColorModel.getRGBdefault());
		HashMap<String, Integer> colors = new HashMap<String, Integer>();
		for(int i = 0; i < ip.getWidth(); i++)
		{
			for(int j = 0; j < ip.getHeight(); j++)
			{
				Color c = getClosestColor(i, j, ip.getColorModel().getRGB(ip.getPixel(i, j)));
				String colorName = baseColors.get(c);
				if(!colors.containsKey(colorName))
					colors.put(colorName, 0);
				colors.put(colorName, colors.get(colorName) + 1);
				ip2.putPixel(i, j, c.getRGB());
			}
		}
		
		displayImage("Coleurs: " + getBeautifulColors(new File(IJ.getDirectory("image")).getName(), colors, ip.getWidth() * ip.getHeight(), 0.1), ip2);
		
		return colors;
	}
	
	private void displayImage(String title, ImageProcessor imageProcessor)
	{
		final ImageWindow iw = new ImageWindow(new ImagePlus(WindowManager.makeUniqueName(title), imageProcessor));
		iw.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e)
			{
				super.windowClosed(e);
				WindowManager.removeWindow(iw);
			}
		});
		WindowManager.addWindow(iw);
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
