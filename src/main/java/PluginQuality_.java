import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class PluginQuality_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		printOut(getIntensity(ip.duplicate()), getBlurrNess(ip.duplicate()));
	}
	
	private String getBlurrNess(ImageProcessor ip)
	{
		ImagePlus imagePlus = new ImagePlus("TESTT", ip);
		ImageConverter imageConverter = new ImageConverter(imagePlus);
		imageConverter.convertToGray8();
		ImageProcessor imageProcessor = imagePlus.getProcessor().convertToFloatProcessor();
		/*imageProcessor.convolve(new float[]{ //LoG, sigma=1.4
		                                     0,0,3,2,2,2,3,0,0,
		                                     0,2,3,5,5,5,3,2,0,
		                                     3,3,5,3,0,3,5,3,3,
		                                     2,5,3,-12,-23,-12,3,5,2,
		                                     2,5,0,-23,-40,-23,0,5,2,
		                                     2,5,3,-12,-23,-12,3,5,2,
		                                     3,3,5,3,0,3,5,3,3,
		                                     0,2,3,5,5,5,3,2,0,
		                                     0,0,3,2,2,2,3,0,0
		}, 9, 9);*/
		imageProcessor.convolve(new float[]{ //LoG, sigma=1.4
		                                     0,
		                                     1,
		                                     1,
		                                     2,
		                                     2,
		                                     2,
		                                     1,
		                                     1,
		                                     0,
		                                     1,
		                                     2,
		                                     4,
		                                     5,
		                                     5,
		                                     5,
		                                     4,
		                                     2,
		                                     1,
		                                     1,
		                                     4,
		                                     5,
		                                     3,
		                                     0,
		                                     3,
		                                     5,
		                                     4,
		                                     1,
		                                     2,
		                                     5,
		                                     3,
		                                     -12,
		                                     -24,
		                                     -12,
		                                     3,
		                                     5,
		                                     2,
		                                     2,
		                                     5,
		                                     0,
		                                     -24,
		                                     -40,
		                                     -24,
		                                     0,
		                                     5,
		                                     2,
		                                     2,
		                                     5,
		                                     3,
		                                     -12,
		                                     -24,
		                                     -12,
		                                     3,
		                                     5,
		                                     2,
		                                     1,
		                                     4,
		                                     5,
		                                     3,
		                                     0,
		                                     3,
		                                     5,
		                                     4,
		                                     1,
		                                     1,
		                                     2,
		                                     4,
		                                     5,
		                                     5,
		                                     5,
		                                     4,
		                                     2,
		                                     1,
		                                     0,
		                                     1,
		                                     1,
		                                     2,
		                                     2,
		                                     2,
		                                     1,
		                                     1,
		                                     0,
		                                     }, 9, 9);
		//imageProcessor.convolve3x3(new int[]{0, 1, 0, 1, -4, 1, 0, 1, 0});
		
		long average = 0;
		for(int i = 0; i < ip.getWidth(); i++)
			for(int j = 0; j < ip.getHeight(); j++)
				average += ip.getPixelValue(i, j);
		average /= ip.getWidth() * ip.getHeight();
		
		long variance = 0;
		for(int i = 0; i < ip.getWidth(); i++)
			for(int j = 0; j < ip.getHeight(); j++)
				variance += Math.pow(ip.getPixelValue(i, j) - average, 2);
		variance /= ip.getWidth() * ip.getHeight();
		
		String valTxt = variance < 3000 ? "Net" : "Flou";
		displayImage("Log: " + valTxt + "(" + variance + ")", imageProcessor);
		return valTxt;
	}
	
	private void printOut(String intensity, String blurrness)
	{
		File outFile = new File(WindowManager.getActiveWindow().getName() + "_tag" + ".txt");
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(new FileOutputStream(outFile));
			try
			{
				pw.println("Quality: " + intensity + " " + blurrness);
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
	}
	
	private String getIntensity(ImageProcessor ip)
	{
		ip = ip.convertToRGB();
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
		
		String maxKey = null;
		double max = 0;
		for(String key : colors.keySet())
		{
			double count = colors.get(key);
			max = Math.max(max, count);
			if(max == count)
				maxKey = key;
		}
		IJ.showMessage("Intensity", maxKey);
		return maxKey;
	}
	
	private void displayImage(String title, ImageProcessor imageProcessor)
	{
		final ImageWindow iw = new ImageWindow(new ImagePlus(WindowManager.makeUniqueName(title), imageProcessor));
		iw.addWindowListener(new WindowAdapter()
		{
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
		return DOES_ALL;
	}
}
