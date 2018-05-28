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

public class PluginKind_ implements PlugInFilter
{
	private static HashMap<Color, String> baseColors;
	private ImagePlus imp;
	
	//For the colors see the PluginColor_, this is the same
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
		
		printOut(getKind(ip.duplicate()));
	}
	
	private String getKind(ImageProcessor imageProcessor)
	{
		// int cellsX = 10.D;
		// int cellsY = 10.D;
		// int sizeX = (int) Math.ceil(imageProcessor.getWidth() / (double)cellsX);
		// int sizeY = (int) Math.ceil(imageProcessor.getHeight() / (double)cellsY);
		
		//Create a region of 50x50px that will scan the image
		int sizeX = 50;
		int sizeY = 50;
		int cellsX = (int) Math.ceil(imageProcessor.getWidth() / (double) sizeX);
		int cellsY = (int) Math.ceil(imageProcessor.getHeight() / (double) sizeY);
		
		//For the colors
		ImageProcessor ip2 = imageProcessor.duplicate();
		ip2.medianFilter();
		ip2.setColorModel(ColorModel.getRGBdefault());
		
		//To find edges, pass in gray scale and find edges
		ImagePlus imagePlus = new ImagePlus("TESTT", imageProcessor);
		ImageConverter imageConverter = new ImageConverter(imagePlus);
		imageConverter.convertToGray8();
		ImageProcessor ip3 = imagePlus.getProcessor().convertToFloatProcessor();
		ip3.findEdges();
		ip3.setBinaryThreshold();
		
		//Output image
		ImageProcessor ip4 = ip2.duplicate();
		for(int i = 0; i < cellsX; i++)
			ip4.drawRect(i * sizeX, 0, 2, imageProcessor.getHeight());
		for(int i = 0; i < cellsY; i++)
			ip4.drawRect(0, i * sizeY, imageProcessor.getWidth(), 2);
		
		//Cout region of each kind, -1 is sky, 1 is sea
		HashMap<Integer, Double> counts = new HashMap<Integer, Double>();
		counts.put(-1, 0D);
		counts.put(1, 0D);
		
		//For each region
		for(int i = 0; i < cellsX; i++)
		{
			for(int j = 0; j < cellsY; j++)
			{
				//Get what is inside, -1 sky, 1 sea, 0 it wasn't blue
				float val = processPart(ip2, ip3, i * sizeX, Math.min((i + 1) * sizeX, imageProcessor.getWidth()), j * sizeY, Math.min((j + 1) * sizeY, imageProcessor.getHeight()));
				
				//If the edges are more than "400", then there's a lot of them and we assume it's the sea
				int index = val == 0f ? 0 : (val >= 400 ? 1 : -1);
				ip4.drawString(String.format("%s\n%.0f", index == 0 ? "Rien" : (index == -1 ? "Ciel" : "Mer"), val), (int) ((i + 0.1) * sizeX), (int) ((j + 0.5) * sizeY));
				if(index != 0)
					counts.put(index, counts.get(index) + 1);
			}
		}
		double tot = counts.get(-1) + counts.get(1);
		counts.put(-1, 100 * counts.get(-1) / tot);
		counts.put(1, 100 * counts.get(1) / tot);
		displayImage(String.format("Parts: Ciel=%.2f%% %s/Mer=%.2f%% %s", counts.get(-1), counts.get(-1) >= 30 ? "OK" : "NON", counts.get(1), counts.get(1) >= 30 ? "OK" : "NON"), ip4);
		
		return (counts.get(-1) >= 30 ? "Ciel " : "") + (counts.get(1) >= 30 ? "Mer" : "");
	}
	
	private float processPart(ImageProcessor imageProcessor, ImageProcessor imageEdges, int startX, int endX, int startY, int endY)
	{
		//For each pixels in the region, get the closest color
		HashMap<String, Integer> colors = new HashMap<String, Integer>();
		for(int i = startX; i < endX; i++)
		{
			for(int j = startY; j < endY; j++)
			{
				Color c = getClosestColor(imageProcessor.getColorModel().getRGB(imageProcessor.getPixel(i, j)));
				String colorName = baseColors.get(c);
				if(!colors.containsKey(colorName))
					colors.put(colorName, 0);
				colors.put(colorName, colors.get(colorName) + 1);
			}
		}
		
		//If the most present color is blue, we continue the process
		int maxCol = -1;
		String col = "";
		for(String color : colors.keySet())
			if(colors.get(color) > maxCol)
			{
				maxCol = colors.get(color);
				col = color;
			}
		
		if(!col.equals("Bleu"))
			return 0f;
		
		//Count the "quantity" of edges
		float total = 0;
		for(int i = startX; i < endX; i++)
		{
			for(int j = startY; j < endY; j++)
				total += imageEdges.getPixelValue(i, j) >= 100 ? 1 : 0;
		}
		
		return total;
	}
	
	//For the colors see the PluginColor_, this is the same
	private Color getClosestColor(int i)
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
	
	//For the colors see the PluginColor_, this is the same
	private double getDistanceHSB(float[] hsb1, float[] hsb2)
	{
		return 0.24 * Math.sqrt(Math.pow(hsb1[0] - hsb2[0], 2)) + 0.38 * Math.sqrt(Math.pow(hsb1[1] - hsb2[1], 2)) + 0.38 * Math.sqrt(Math.pow(hsb1[2] - hsb2[2], 2));
	}
	
	private void printOut(String kind)
	{
		File outFile = new File(IJ.getDirectory("current"), imp.getTitle() + "_tag" + ".txt");
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(new FileOutputStream(outFile));
			try
			{
				pw.println("Type: " + kind);
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
		this.imp = imp;
		if(arg.equals("about"))
		{
			IJ.showMessage("Traitement de l'image v2");
			return DONE;
		}
		return DOES_ALL;
	}
}
