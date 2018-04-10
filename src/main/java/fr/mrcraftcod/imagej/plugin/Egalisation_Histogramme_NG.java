package fr.mrcraftcod.imagej.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Egalisation_Histogramme_NG implements PlugInFilter {

	public void run(ImageProcessor ip){
		int [] hist = new int[256];
		int w = ip.getWidth();
		int h = ip.getHeight();
		int N = h*w;
		for (int x = 0; x < w; ++x)
			for (int y=0; y < h; ++y)
				hist[ip.getPixel(x,  y)]++;

		int lut[]=new int[256];
		int sum = 0;
		for (int ng = 0; ng < 256; ng++) {
		  sum += hist[ng]; // calcul case ng de l'histo cumulé
		  lut[ng]=255*sum/N;
		}
		
		ip.applyTable(lut);
	}

	public int setup(String arg, ImagePlus imp){
		if (arg.equals("about")){
			IJ.showMessage("Egalisation d'histogramme");
			return DONE;
		}
		return DOES_8G;
	}
}
