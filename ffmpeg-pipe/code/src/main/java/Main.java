import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPEncoder;

public final class Main {
	private static void drawSmileyFace(Graphics g) {
		g.drawArc(100, 45, 80, 80, 0, 360);

		g.setColor(Color.blue);
		g.drawArc(120, 70, 10, 10, 0, 360);
		g.drawArc(150, 70, 10, 10, 0, 360);

		g.setColor(Color.magenta);
		g.drawLine(140, 85, 140, 100);

		g.setColor(Color.red);
		g.drawArc(110, 55, 60, 60, 0, -180);
	}

	public static void main(String[] args) throws Exception {
		String input = args[0];

		Process inputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-loglevel", "error", "-i", input, "-c:v", "bmp",
				"-pix_fmt", "rgb24", "-f", "image2pipe", "-r", "10", "-").start();
		new StreamLogger(inputProcess.getErrorStream(), "input");

		Process outputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-loglevel", "error", "-i", "-", "-r", "10",
				"http://localhost:8090/feed1.ffm").start();
		new StreamLogger(outputProcess.getErrorStream(), "output");

		InputStream in = inputProcess.getInputStream();
		OutputStream out = outputProcess.getOutputStream();
		while (true) {
			BufferedImage img;
			try {
				img = new BMPDecoder(in).getBufferedImage();
			} catch (Exception e) {
				break;
			}
			drawSmileyFace(img.getGraphics());
			BMPEncoder.write(img, out);
		}

		out.close();
		inputProcess.waitFor();
		outputProcess.waitFor();
	}

}
