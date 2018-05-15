import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.*;
import java.util.*;

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
		StringBuilder builder = new StringBuilder(", ");
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

		Set<Color> set = new HashSet<Color>();
		for (Color c : colors.keySet())
		{
			if(colors.get(c) > threshold)
				set.add(c);
		}
		return set;
	}
	
	private Color getClosestColor(int i)
	{
		double minDist = Double.MAX_VALUE;
		Color bestColor = null;
		
		Color c = new Color(i);
		
		for(Color c2 : baseColors)
		{
			double dist = getDistance(c, c2);
			if(dist < minDist)
			{
				minDist = dist;
				bestColor = c2;
			}
		}
		
		return bestColor;
	}

	private double getDistance(Color c1, Color c2){
		return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getGreen() - c2.getGreen(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2));
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
