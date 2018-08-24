import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class StreamLogger {
	public StreamLogger(InputStream stream, String prefix) {
		new Thread(prefix + " logger") {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
					while (true) {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						System.out.println("[" + prefix + "] " + line);
					}
				} catch (IOException e) {
					// do nothing
				}
			}
		}.start();
		;
	}
}
