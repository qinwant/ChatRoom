package whisper.util;

import javax.swing.*;
import java.awt.*;

public class ImageUtil extends JPanel{
	
	private String imgPath;

	/**
	 * 有参构造
	 * @param imgPath
	 */
	public ImageUtil(String imgPath) {
		this.imgPath = imgPath;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		ImageIcon icon=new ImageIcon(imgPath);
		g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), null);
	}
	
	
}
