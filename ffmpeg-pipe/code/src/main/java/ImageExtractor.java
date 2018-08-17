import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

final class ImageExtractor {
	private final DataInputStream source;

	public ImageExtractor(InputStream source) {
		this.source = new DataInputStream(source);
	}

	private static final byte[] HEADER = { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A,
			(byte) 0x1A, (byte) 0x0A };
	private static final byte[] END = { (byte) 0x49, (byte) 0x45, (byte) 0x4e, (byte) 0x44 };

	private BufferedImage readNext() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024 * 1024);
		DataOutputStream bytesWrapper = new DataOutputStream(bytes);

		byte[] header = new byte[8];
		source.readFully(header);
		if (!Arrays.equals(header, HEADER)) {
			throw new AssertionError("Not header! " + Arrays.toString(header));
		}
		bytesWrapper.write(header);

		boolean finished = false;
		byte[] type = new byte[4];
		byte[] copyBuffer = new byte[4 * 4096];
		while (!finished) {
			int length = source.readInt();
			bytesWrapper.writeInt(length);
			source.readFully(type);
			bytesWrapper.write(type);
			IOUtils.copyLarge(source, bytes, 0, length + 4, copyBuffer); // includes the CRC
			finished = Arrays.equals(type, END);
		}

		byte[] imageBytes = bytes.toByteArray();
		return ImageIO.read(new ByteArrayInputStream(imageBytes));
	}

	public BufferedImage next() {
		try {
			return readNext();
		} catch (EOFException e) {
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
