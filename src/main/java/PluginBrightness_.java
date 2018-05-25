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
import java.util.HashMap;
import java.util.Map;

public class PluginBrightness_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		getIntensity(ip.duplicate().convertToRGB());
	}
	
	private String getBeautifulIntensity(Map<String, Double> colors, double threshold)
	{
		StringBuilder builder = new StringBuilder();
		for(String color : colors.keySet())
			if(colors.get(color) > threshold)
				builder.append(color).append(":").append(String.format("%.2f%%", colors.get(color) * 100)).append(", ");
		
		if(builder.length() > 1)
			builder.delete(builder.length() - 2, builder.length());
		
		return builder.toString();
	}
	
	private HashMap<String, Double> getIntensity(ImageProcessor ip)
	{
		ImageProcessor ip2 = ip.createProcessor(ip.getWidth(), ip.getHeight());
		ip.medianFilter();
		ip.setColorModel(ColorModel.getRGBdefault());
		HashMap<String, Double> colors = new HashMap<String, Double>();
		for(int i = 0; i < ip.getWidth(); i++)
		{
			for(int j = 0; j < ip.getHeight(); j++)
			{
				Color c = new Color(ip.getColorModel().getRGB(ip.getPixel(i, j)));
				float hsb1[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb1);
				
				String colorName = hsb1[2] < 0.33 ? "Sombre" : (hsb1[2] > 0.66 ? "Clair" : "Normal");
				if(!colors.containsKey(colorName))
					colors.put(colorName, 0D);
				colors.put(colorName, colors.get(colorName) + 1);
				ip2.putPixel(i, j, c.getRGB());
			}
		}
		
		double max = 0;
		for(double count : colors.values())
			max = Math.max(max, count);
		for(String key : colors.keySet())
			colors.put(key, colors.get(key) / max);
		
		IJ.showMessage("Intensité: " + getBeautifulIntensity(colors, 0.6));
		
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
