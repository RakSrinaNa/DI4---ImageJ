import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FindSimilar_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		String path = "/home/TP/images/";
		File[] files = listFiles(path);
		if(files.length != 0)
		{
			Map<Double, List<File>> similarities = new HashMap<>();
			//initialization variables locales
			for(File file : files)
			{
				// creation d’une image temporaire
				ImagePlus tempImg = new ImagePlus(file.getAbsolutePath());
				new ImageConverter(tempImg).convertToGray8();
				ImageProcessor ipTemp = tempImg.getProcessor();
				double avg = AverageNdg(ipTemp);
				List<File> files1 = similarities.getOrDefault(avg, new ArrayList<>());
				files1.add(file);
				similarities.put(avg, files1);
			}
			double avg = AverageNdg(ip);
			Map<Double, List<File>> distances = similarities.keySet().stream().collect(Collectors.toMap(k -> k - avg, similarities::get));
			double dist = distances.keySet().stream().min(Comparator.comparingDouble(Math::abs)).orElse(0D);
			List<File> closes = distances.getOrDefault(dist, new ArrayList<>());
			IJ.showMessage("L’image la plus proche est " + closes.stream().map(File::getAbsolutePath).collect(Collectors.joining(", ")) + " avec une distance de" + dist);
		}
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