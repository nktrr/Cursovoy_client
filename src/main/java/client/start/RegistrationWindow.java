package client.start;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class RegistrationWindow extends JFrame{
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public RegistrationWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 600);
		this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
				screenSize.height / 2 - this.getSize().height / 2);
	}

}
