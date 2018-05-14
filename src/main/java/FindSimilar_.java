import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.io.File;
import java.util.*;

public class FindSimilar_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		String path = "/home/TP/images/";
		File[] files = listFiles(path);
		if(files.length != 0)
		{
			double avgReal = AverageNdg(ip);
			Map<Double, List<File>> similarities = new HashMap<Double, List<File>>();
			//initialization variables locales
			for(File file : files)
			{
				// creation d’une image temporaire
				ImagePlus tempImg = new ImagePlus(file.getAbsolutePath());
				new ImageConverter(tempImg).convertToGray8();
				ImageProcessor ipTemp = tempImg.getProcessor();
				double dst = Math.abs(AverageNdg(ipTemp) - avgReal);
				if(!similarities.containsKey(dst))
					similarities.put(dst, new ArrayList<File>());
				similarities.get(dst).add(file);
			}
			double minDist = Collections.min(similarities.keySet());
			IJ.showMessage("L’image la plus proche est " + getBeautifulFiles(similarities.get(minDist)) + " avec une distance de" + minDist);
		}
	}
	
	private String getBeautifulFiles(List<File> files)
	{
		StringBuilder builder = new StringBuilder();
		for(File file : files)
			builder.append(file.getAbsolutePath()).append(", ");
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
	
	public File[] listFiles(String directoryPath)
	{
		File[] files = null;
		File directoryToScan = new File(directoryPath);
		files = directoryToScan.listFiles();
		return files;
	}
	
	// Retourne la moyenne des NdG d’une image en NdG
	public double AverageNdg(ImageProcessor ip)
	{
		byte[] pixels = (byte[]) ip.getPixels();
		int width = ip.getWidth();
		int height = ip.getHeight();
		double total = 0;
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				total += pixels[y * width + x] & 0xff;
		return total / pixels.length;
	}
}