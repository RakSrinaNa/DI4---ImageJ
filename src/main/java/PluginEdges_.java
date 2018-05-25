import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

public class PluginEdges_ implements PlugInFilter
{
	public void run(ImageProcessor ip)
	{
		new ImageConverter(new ImagePlus("Title", ip.duplicate())).convertToGray8();
		convolve(ip.convertToFloatProcessor());
	}
	
	private void convolve(ImageProcessor ip)
	{
		ip.smooth();
		ImageProcessor ip2 = ip.duplicate();
		Convolver cv = new Convolver();
		cv.convolve(ip, new float[]{
				-1, 0, 1,
				-2, 0, 2,
				-1, 0, 1
		}, 3, 3);
		cv.convolve(ip2, new float[]{
				-1, -2, -1,
				0, 0, 0,
				1, 2, 1
		}, 3, 3);
		
		for(int i = 0; i < ip.getWidth(); i++)
			for(int j = 0; j < ip.getHeight(); j++)
				ip.putPixelValue(i, j, Math.sqrt(Math.pow(ip.getPixelValue(i, j), 2) + Math.pow(ip2.getPixelValue(i, j), 2)));
		displayImage("Contours", ip);
	}
	
	private void displayImage(String title, ImageProcessor imageProcessor)
	{
		final ImageWindow iw = new ImageWindow(new ImagePlus(WindowManager.makeUniqueName(title + new Random().nextInt()), imageProcessor));
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
