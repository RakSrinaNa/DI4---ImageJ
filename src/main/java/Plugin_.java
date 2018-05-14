import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Plugin_ implements PlugInFilter
{
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
	
	public void run(ImageProcessor ip)
	{
		Set<Color> colors = getColors(ip, ip.getWidth() * ip.getHeight() * 0.2);
		IJ.showMessage(getBeautifulColors(colors));
	}
	
	private String getBeautifulColors(Collection<Color> colors)
	{
		StringBuilder builder = new StringBuilder();
		for(Color color : colors)
			builder.append(color.toString()).append(", ");
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
	
	private Set<Color> getColors(ImageProcessor ip, double threshold)
	{
		HashMap<Color, Integer> colors = new HashMap<Color, Integer>();
		for(int i = 0; i < ip.getWidth(); i++)
			for(int j = 0; j < ip.getHeight(); j++)
			{
				Color c = getClosestColor(ip.get(i, j));
				if(!colors.containsKey(c))
					colors.put(c, 0);
				colors.put(c, colors.get(c) + 1);
			}
		for(Color c : colors.keySet())
		{
			if(colors.get(c) < threshold)
				colors.remove(c);
		}
		return colors.keySet();
	}
	
	private Color getClosestColor(int i)
	{
		double minDist = Double.MAX_VALUE;
		Color bestColor = null;
		
		Color c = new Color(i);
		float hsb1[] = new float[3];
		Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb1);
		
		for(Color c2 : baseColors)
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
		return  Math.sqrt(
				0.475 * Math.pow(hsb1[0] - hsb2[0], 2) +
				0.2875 * Math.pow(hsb1[1] - hsb2[1], 2) +
				0.2375 * Math.pow(hsb1[2] - hsb2[2], 2)
		);
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
