package com.ayora.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * Genere les favicons Ayora a partir d'un rendu Java AWT.
 *
 * Monogramme :
 *   - fond bordeaux degrade (#8B1A2B -> #5C0E1B), coins arrondis ~16%
 *   - 2 filets dores fins en haut et en bas (style maison de luxe)
 *   - lettre A serif elegante (Georgia / Serif), doree
 *
 * Sorties (toutes ecrites dans WebContent/) :
 *   - favicon-16x16.png
 *   - favicon-32x32.png
 *   - apple-touch-icon.png (180x180)
 *   - favicon.ico (contient un PNG 32x32 - format moderne, supporte Windows Vista+)
 *
 * Lance via :
 *   java -cp build/classes com.ayora.util.GenerateFavicons
 */
public class GenerateFavicons {

	private static final Color BG_TOP    = new Color(0x8B, 0x1A, 0x2B);
	private static final Color BG_BOTTOM = new Color(0x5C, 0x0E, 0x1B);
	private static final Color GOLD_TOP    = new Color(0xE8, 0xC8, 0x79);
	private static final Color GOLD_BOTTOM = new Color(0xB8, 0x92, 0x2F);

	public static void main(String[] args) throws Exception {
		String outDir = args.length > 0 ? args[0] : "WebContent";

		// Pour 16x16 on cache les filets dores qui deviennent invisibles
		// et illisibles (trop fins). La lettre A doit prendre toute la place.
		BufferedImage img16  = renderMonogram(16,  /*showLines*/ false);
		BufferedImage img32  = renderMonogram(32,  /*showLines*/ false);
		BufferedImage img180 = renderMonogram(180, /*showLines*/ true);
		BufferedImage img48  = renderMonogram(48,  /*showLines*/ true); // pour ICO secondaire

		writePng(img16,  outDir + "/favicon-16x16.png");
		writePng(img32,  outDir + "/favicon-32x32.png");
		writePng(img180, outDir + "/apple-touch-icon.png");
		writeIco(new BufferedImage[]{img16, img32, img48}, outDir + "/favicon.ico");

		System.out.println("OK : favicons generes dans " + outDir + "/");
		System.out.println("  - favicon-16x16.png      (" + img16.getWidth()  + "x" + img16.getHeight()  + ")");
		System.out.println("  - favicon-32x32.png      (" + img32.getWidth()  + "x" + img32.getHeight()  + ")");
		System.out.println("  - apple-touch-icon.png   (" + img180.getWidth() + "x" + img180.getHeight() + ")");
		System.out.println("  - favicon.ico            (16+32+48 multi-res)");
	}

	/** Rendu monogramme a une taille donnee. */
	private static BufferedImage renderMonogram(int size, boolean showLines) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			// Fond bordeaux degrade
			double radius = size * 0.16;
			g.setPaint(new GradientPaint(0, 0, BG_TOP, 0, size, BG_BOTTOM));
			g.fill(new RoundRectangle2D.Double(0, 0, size, size, radius * 2, radius * 2));

			// Filets dores horizontaux (visibles uniquement >= 32px)
			if (showLines) {
				g.setPaint(new GradientPaint(0, 0, GOLD_TOP, size, 0, GOLD_BOTTOM));
				float lineH = Math.max(1f, size * 0.015f);
				double inset = size * 0.22;
				g.fill(new Rectangle2D.Double(inset, size * 0.21, size - 2 * inset, lineH));
				g.fill(new Rectangle2D.Double(inset, size * 0.78 - lineH, size - 2 * inset, lineH));
			}

			// Lettre A serif doree, centree
			float targetCapHeight = showLines ? size * 0.46f : size * 0.66f;
			Font font = findBestSerif(g, "A", targetCapHeight);
			g.setFont(font);

			FontRenderContext frc = g.getFontRenderContext();
			TextLayout layout = new TextLayout("A", font, frc);
			Rectangle2D bounds = layout.getBounds();
			float x = (float) ((size - bounds.getWidth()) / 2.0 - bounds.getX());
			float y = (float) ((size + bounds.getHeight()) / 2.0 - bounds.getY() - bounds.getHeight());

			g.setPaint(new GradientPaint(0, size * 0.15f, GOLD_TOP, 0, size * 0.85f, GOLD_BOTTOM));
			layout.draw(g, x, y);
		} finally {
			g.dispose();
		}
		return img;
	}

	/** Trouve la taille de font Serif qui donne la hauteur de capital souhaitee. */
	private static Font findBestSerif(Graphics2D g, String s, float targetCapHeight) {
		// On essaie Georgia d'abord (plus elegant), puis Serif en fallback
		String[] candidates = {"Georgia", "Times New Roman", "Serif"};
		String family = "Serif";
		for (String c : candidates) {
			Font f = new Font(c, Font.PLAIN, 10);
			if (!f.getFamily().equalsIgnoreCase("Dialog")) {
				family = c;
				break;
			}
		}

		// Recherche binaire approchee
		float lo = 4f, hi = 200f;
		FontRenderContext frc = g.getFontRenderContext();
		for (int i = 0; i < 20; i++) {
			float mid = (lo + hi) / 2f;
			Font test = new Font(family, Font.PLAIN, 1).deriveFont(mid);
			TextLayout tl = new TextLayout(s, test, frc);
			double h = tl.getBounds().getHeight();
			if (h < targetCapHeight) lo = mid; else hi = mid;
		}
		return new Font(family, Font.PLAIN, 1).deriveFont((lo + hi) / 2f);
	}

	private static void writePng(BufferedImage img, String path) throws IOException {
		Files.createDirectories(Paths.get(path).getParent());
		ImageIO.write(img, "png", Paths.get(path).toFile());
	}

	/**
	 * Ecrit un fichier .ico contenant N images PNG.
	 * Format ICO (https://en.wikipedia.org/wiki/ICO_(file_format)) :
	 *   Header (6 bytes) : reserved=0, type=1, count=N
	 *   Pour chaque image : ICONDIRENTRY (16 bytes)
	 *   Puis les donnees PNG concatenees.
	 */
	private static void writeIco(BufferedImage[] images, String path) throws IOException {
		// Encode chaque image en PNG en memoire
		byte[][] pngs = new byte[images.length][];
		for (int i = 0; i < images.length; i++) {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			ImageIO.write(images[i], "png", buf);
			pngs[i] = buf.toByteArray();
		}

		try (OutputStream out = new FileOutputStream(path)) {
			// ICONDIR header
			writeLE2(out, 0);  // reserved
			writeLE2(out, 1);  // type=ICO
			writeLE2(out, images.length);

			// Offset apres header (6) + N * 16 bytes d'ICONDIRENTRY
			int offset = 6 + 16 * images.length;
			for (int i = 0; i < images.length; i++) {
				int w = images[i].getWidth();
				int h = images[i].getHeight();
				out.write(w >= 256 ? 0 : w);    // width (0 = 256)
				out.write(h >= 256 ? 0 : h);    // height
				out.write(0);                    // palette
				out.write(0);                    // reserved
				writeLE2(out, 1);                // planes
				writeLE2(out, 32);               // bits per pixel
				writeLE4(out, pngs[i].length);   // image size
				writeLE4(out, offset);           // offset
				offset += pngs[i].length;
			}

			// Donnees PNG
			for (byte[] png : pngs) out.write(png);
		}
	}

	private static void writeLE2(OutputStream out, int v) throws IOException {
		out.write(v & 0xFF);
		out.write((v >> 8) & 0xFF);
	}

	private static void writeLE4(OutputStream out, int v) throws IOException {
		out.write(v & 0xFF);
		out.write((v >> 8) & 0xFF);
		out.write((v >> 16) & 0xFF);
		out.write((v >> 24) & 0xFF);
	}
}
