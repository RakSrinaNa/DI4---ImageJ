import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class MonPlugin_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		byte[] pixels = (byte[]) ip.getPixels(); // Notez le cast en byte ()
		int width = ip.getWidth();
		int height = ip.getHeight();
		int ndg;
		double total = 0;
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
			{ // pas completement optimal mais pedagogique…
				ndg = pixels[y * width + x] & 0xff;
				total += ndg;
				if(ndg < 120)
					pixels[y * width + x] = (byte) 0;
				else
					pixels[y * width + x] = (byte) 255;
			}
		IJ.showMessage(String.format("Niveau de gris moyen: %.2f", total / pixels.length));
	}
	
	public int setup(String arg, ImagePlus imp)
	{
		if(arg.equals("about"))
		{
			IJ.showMessage("Traitement de l'image");
			return DONE;
		}
		return DOES_8G;
	}
}