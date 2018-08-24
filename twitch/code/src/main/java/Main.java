import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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

	private static long readUnsignedIntLE(InputStream in) throws IOException {
		long ch1 = in.read();
		long ch2 = in.read();
		long ch3 = in.read();
		long ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return (ch1 << 0) | (ch2 << 8) | (ch3 << 16) | (ch4 << 24);
	}

	private static void writeUnsignedIntLE(long v, OutputStream out) throws IOException {
		out.write((int) ((v >>> 0) & 0xFF));
		out.write((int) ((v >>> 8) & 0xFF));
		out.write((int) ((v >>> 16) & 0xFF));
		out.write((int) ((v >>> 24) & 0xFF));
	}

	private static long computeConstant(String s) {
		if (s.length() != 4) {
			throw new AssertionError();
		}
		byte[] bytes;
		try {
			bytes = s.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
		return ((long) bytes[3] << 24) | ((long) bytes[2] << 16) | ((long) bytes[1] << 8) | (long) bytes[0];
	}

	private final static long RIFF = computeConstant("RIFF");
	private final static long LIST = computeConstant("LIST");

	private static void checkValue(long value, long expected) {
		if (value != expected) {
			throw new AssertionError(String.format("Expected 0x%08X but was 0x%08X", expected, value));
		}
	}

	private static void checkValue(long value, String expected) {
		checkValue(value, computeConstant(expected));
	}

	@SuppressWarnings("unused")
	private static void dump(long value) {
		if (value < 0 | value > 0xFFFFFFFFL) {
			throw new AssertionError(Long.toHexString(value));
		}
		byte[] bytes = { (byte) ((value >> 0) & 0xFF), (byte) ((value >> 8) & 0xFF), (byte) ((value >> 16) & 0xFF),
				(byte) ((value >> 24) & 0xFF), };
		try {
			System.out.println(new String(bytes, "ASCII"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	private static void copyAviHeader(InputStream in, OutputStream out) throws Exception {
		long riff = readUnsignedIntLE(in);
		checkValue(riff, RIFF);
		writeUnsignedIntLE(riff, out);
		long size = readUnsignedIntLE(in);
		writeUnsignedIntLE(size, out);
		long fourCC = readUnsignedIntLE(in);
		checkValue(fourCC, "AVI ");
		writeUnsignedIntLE(fourCC, out);
	}

	private static final int COPY_BUFFER_SIZE = 4 * 4096;

	private static void copy(InputStream in, OutputStream out, long size, byte[] buffer) throws IOException {
		while (size > 0) {
			int toRead = size >= COPY_BUFFER_SIZE ? COPY_BUFFER_SIZE : (int) size;
			int read = in.read(buffer, 0, toRead);
			if (read < 0) {
				break; // EOF
			}
			out.write(buffer, 0, read);
			size -= read;
		}
	}

	private static byte[] readAll(InputStream in, int size) throws IOException {
		byte[] result = new byte[size];
		readAll(in, result);
		return result;
	}

	private static void readAll(InputStream in, byte[] buffer) throws IOException {
		int position = 0;
		int size = buffer.length;
		while (size > 0) {
			int read = in.read(buffer, position, size);
			if (read < 0) {
				throw new EOFException();
			}
			position += read;
			size -= read;
		}
	}

	private static void swapBytes(byte[] bytes) {
		// 0 ^= 3 3 ^= 0 0 ^= 3
		// 1 ^= 2 2 ^= 1 1 ^= 2
		for (int i = 0; i < bytes.length; i += 4 * 4) {
			// 1
			bytes[i + 0] ^= bytes[i + 3];
			bytes[3 + 0] ^= bytes[i + 0];
			bytes[i + 0] ^= bytes[i + 3];
			bytes[i + 1] ^= bytes[i + 2];
			bytes[2 + 0] ^= bytes[i + 1];
			bytes[i + 1] ^= bytes[i + 2];

			// 2
			bytes[i + 4 + 0] ^= bytes[i + 4 + 3];
			bytes[3 + 4 + 0] ^= bytes[i + 4 + 0];
			bytes[i + 4 + 0] ^= bytes[i + 4 + 3];
			bytes[i + 4 + 1] ^= bytes[i + 4 + 2];
			bytes[2 + 4 + 0] ^= bytes[i + 4 + 1];
			bytes[i + 4 + 1] ^= bytes[i + 4 + 2];

			// 3
			bytes[i + 8 + 0] ^= bytes[i + 8 + 3];
			bytes[3 + 8 + 0] ^= bytes[i + 8 + 0];
			bytes[i + 8 + 0] ^= bytes[i + 8 + 3];
			bytes[i + 8 + 1] ^= bytes[i + 8 + 2];
			bytes[2 + 8 + 0] ^= bytes[i + 8 + 1];
			bytes[i + 8 + 1] ^= bytes[i + 8 + 2];

			// 4
			bytes[i + 12 + 0] ^= bytes[i + 12 + 3];
			bytes[3 + 12 + 0] ^= bytes[i + 12 + 0];
			bytes[i + 12 + 0] ^= bytes[i + 12 + 3];
			bytes[i + 12 + 1] ^= bytes[i + 12 + 2];
			bytes[2 + 12 + 0] ^= bytes[i + 12 + 1];
			bytes[i + 12 + 1] ^= bytes[i + 12 + 2];
		}
	}

	private static void run(InputStream in, OutputStream out) throws Exception {
		copyAviHeader(in, out);

		final long AVIH = computeConstant("avih");
		final long videoFourCC = computeConstant("00dc");

		final byte[] copyBuffer = new byte[COPY_BUFFER_SIZE];
		int width = -1, height = -1;
		BufferedImage img = null;
		byte[] imgBytes = null;
		Graphics graphics = null;

		while (true) {
			long fourCC = readUnsignedIntLE(in);
			writeUnsignedIntLE(fourCC, out);
			// dump(fourCC);

			if (fourCC == videoFourCC) {
				long size = readUnsignedIntLE(in);
				if ((size & 0x1L) == 1) {
					size += 1;
				}
				writeUnsignedIntLE(size, out);
				if (size == imgBytes.length) {
					readAll(in, imgBytes);
					swapBytes(imgBytes); // RGBA -> ABGR
					drawSmileyFace(graphics);
					swapBytes(imgBytes); // ABGR -> RGBA
					out.write(imgBytes);
				} else {
					copy(in, out, size, copyBuffer);
				}
			} else if (fourCC == RIFF | fourCC == LIST) {
				writeUnsignedIntLE(readUnsignedIntLE(in), out);
				writeUnsignedIntLE(readUnsignedIntLE(in), out);
			} else if (fourCC == AVIH) {
				long size = readUnsignedIntLE(in);
				if (size < 0 | size > Integer.MAX_VALUE) {
					throw new AssertionError(Long.toString(size));
				}
				writeUnsignedIntLE(size, out);
				byte[] mainAviH = readAll(in, (int) size);
				out.write(mainAviH);
				ByteArrayInputStream bis = new ByteArrayInputStream(mainAviH);
				bis.skip(8 * 4);
				width = (int) readUnsignedIntLE(bis);
				height = (int) readUnsignedIntLE(bis);
				System.out.println("Size: " + width + "x" + height);
				img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				byte[][] bytes = ((DataBufferByte) img.getRaster().getDataBuffer()).getBankData();
				if (bytes.length != 1) {
					throw new AssertionError("" + bytes.length);
				}
				imgBytes = bytes[0];
				graphics = img.getGraphics();
			} else {
				long size = readUnsignedIntLE(in);
				if ((size & 0x1L) == 1) {
					size += 1;
				}
				// System.out.println("Copying " + size);
				writeUnsignedIntLE(size, out);
				copy(in, out, size, copyBuffer);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String input = args[0];

		Process inputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-loglevel", "error", "-i", input, "-c:v",
				"rawvideo", "-c:a", "copy", "-pix_fmt", "rgba", "-f", "avi", "-").start();
		new StreamLogger(inputProcess.getErrorStream(), "input");

		Process outputProcess = new ProcessBuilder("/usr/bin/ffmpeg", "-y", "-loglevel", "error", "-i", "-", "-vcodec",
				"libx264", "-threads", "0", "-pix_fmt", "yuv420p", "-acodec", "libmp3lame", "-ab", "256k", "-f", "flv",
				"rtmp://live.justin.tv/app/live_250715617_sWqGeTh1Hydnk8tIyQKu0407tL1IBx").start();
		new StreamLogger(outputProcess.getErrorStream(), "output");

		InputStream in = inputProcess.getInputStream();
		OutputStream out = outputProcess.getOutputStream();

		try {
			run(in, out);
		} catch (Exception e) {
			e.printStackTrace();
		}

		out.close();
		inputProcess.waitFor();
		outputProcess.waitFor();
	}
}
