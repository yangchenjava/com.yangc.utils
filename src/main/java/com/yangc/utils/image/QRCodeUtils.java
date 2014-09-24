package com.yangc.utils.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtils {

	private static final Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>() {
		private static final long serialVersionUID = 1L;
		{
			// 编码格式
			put(EncodeHintType.CHARACTER_SET, "UTF-8");
			// 容错级别
			put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		}
	};

	private QRCodeUtils() {
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
	public static boolean encode(String contents, int width, int height, String imagePath) {
		String imageType = imagePath.substring(imagePath.lastIndexOf(".") + 1);
		if (!StringUtils.equals(imageType, "png")) {
			throw new IllegalArgumentException("image must be png");
		}

		try {
			BitMatrix matrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);
			MatrixToImageWriter.writeToFile(matrix, imageType, new File(imagePath));
			return true;
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String decode(String imagePath) {
		try {
			BufferedImage bi = ImageIO.read(new File(imagePath));
			LuminanceSource source = new BufferedImageLuminanceSource(bi);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result result = new MultiFormatReader().decode(bitmap, hints);
			return result.getText();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String contents = "我不去想,是否能够成功,既然选择了远方,便只顾风雨兼程.我不去想,能否赢得爱情,既然钟情于玫瑰,就勇敢地吐露真诚.我不去想,身后会不会袭来寒风冷雨,既然目标是地平线,留给世界的只能是背影.我不去想,未来是平坦还是泥泞,只要热爱生命,一切,都在意料之中";
		// String contents = "杨晨";
		encode(contents, 300, 300, "E:/test.png");
		System.out.println(decode("E:/test.png"));
	}

}
