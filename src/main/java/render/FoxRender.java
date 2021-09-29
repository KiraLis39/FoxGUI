package render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;


public class FoxRender {
//	image.getScaledInstance(1024, 768, 2);
//	g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.0f));
	
	private FoxRender() {}
	
	public static Graphics2D setLowRender(Graphics2D g2D) {
		g2D.setRenderingHints(null);
		
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		
		return g2D;
	}
	
	public static Graphics2D setMedRender(Graphics2D g2D) {
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);

		return g2D;
	}
	
	public static Graphics2D setHQRender(Graphics2D g2D) {
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		
		g2D.setRenderingHint(RenderingHints.KEY_RESOLUTION_VARIANT, RenderingHints.VALUE_RESOLUTION_VARIANT_DPI_FIT); 
// 	(VALUE_RESOLUTION_VARIANT_DPI_FIT, VALUE_RESOLUTION_VARIANT_SIZE_FIT, VALUE_RESOLUTION_VARIANT_BASE)

		return g2D;
	}
}
