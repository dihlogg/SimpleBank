package Client;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel {
	private JTextArea logArea;

	public LogPanel() {
		setLayout(new BorderLayout());
		logArea = new JTextArea();
		logArea.setEditable(false);

		JScrollPane logScrollPane = new JScrollPane(logArea);
		add(logScrollPane, BorderLayout.CENTER);
	}

	public void logMessage(String message) {
		logArea.append(message + "\n");
	}
}