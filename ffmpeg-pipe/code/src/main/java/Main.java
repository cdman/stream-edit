import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.imageio.ImageIO;

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
		String output = args[1];

		Process inputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-loglevel", "error", "-i", input, "-c:v", "png",
				"-f", "image2pipe", "-").start();
		new StreamLogger(inputProcess.getErrorStream(), "input");

		Process outputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-loglevel", "error", "-y", "-f", "image2pipe",
				"-i", "-", "-r", "23.98", "-c:v", "libx264", "-crf", "18", output).start();
//		Process outputProcess = new ProcessBuilder("/bin/sed", "-n", "w " + output).start();
		new StreamLogger(outputProcess.getErrorStream(), "output");

		ImageExtractor imageExtractor = new ImageExtractor(inputProcess.getInputStream());
		OutputStream outputStream = outputProcess.getOutputStream();
		int frameCount = 0;
		while (true) {
			BufferedImage image = imageExtractor.next();
			if (image == null) {
				break;
			}
			drawSmileyFace(image.getGraphics());
			System.out.println("Frame " + frameCount);
			frameCount++;
//			ImageIO.write(image, "png", new java.io.File("/tmp/test.png"));
			ImageIO.write(image, "png", outputStream);
		}

		outputStream.close();
		inputProcess.waitFor();
		outputProcess.waitFor();
	}

}
