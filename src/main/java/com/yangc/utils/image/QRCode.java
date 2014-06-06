package com.yangc.utils.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCode {

	public static final int BLACK = 0xFF000000;
	public static final int DKGRAY = 0xFF444444;
	public static final int GRAY = 0xFF888888;
	public static final int LTGRAY = 0xFFCCCCCC;
	public static final int WHITE = 0xFFFFFFFF;
	public static final int RED = 0xFFFF0000;
	public static final int GREEN = 0xFF00FF00;
	public static final int BLUE = 0xFF0000FF;
	public static final int YELLOW = 0xFFFFFF00;
	public static final int CYAN = 0xFF00FFFF;
	public static final int MAGENTA = 0xFFFF00FF;
	public static final int TRANSPARENT = 0;

	private QRCode() {
	}

	/**
	 * @功能: 生成二维码
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午8:39:13
	 * @param contents
	 * @param width
	 * @param height
	 * @param imagePath
	 * @return
	 */
	public static boolean create(String contents, int width, int height, String imagePath) {
		String imageType = imagePath.substring(imagePath.lastIndexOf(".") + 1);

		try {
			Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			BitMatrix matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					bi.setRGB(x, y, matrix.get(x, y) ? QRCode.BLACK : QRCode.WHITE);
				}
			}

			return ImageIO.write(bi, imageType, new File(imagePath));
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {
		create("杨晨", 300, 300, "E:/test.jpg");
	}

}
