package com.yangc.utils.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Position;

import org.apache.commons.lang3.StringUtils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageUtils {

	private ImageUtils() {
	}

	/**
	 * @功能: 等比压缩图片到指定路径(根据指定的宽度计算出等比的高度)
	 * @作者: yangc
	 * @创建日期: 2014年5月27日 上午10:59:53
	 */
	public static boolean zipImage(String srcImagePath, String zipImagePath, int zipImageWidth) {
		FileOutputStream fos = null;
		try {
			Image image = ImageIO.read(new File(srcImagePath));
			int width = image.getWidth(null);
			int height = image.getHeight(null);
			int zipImageHeight = zipImageWidth * height / width;
			BufferedImage bi = new BufferedImage(zipImageWidth, zipImageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			boolean result = g.drawImage(image, 0, 0, zipImageWidth, zipImageHeight, null);
			g.dispose();

			fos = new FileOutputStream(zipImagePath);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
			encoder.encode(bi);
			fos.flush();
			fos.close();
			fos = null;
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 截图
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午7:02:06
	 * @param srcImagePath
	 * @param destImagePath
	 * @param x
	 * @param y
	 * @param cutImageWidth
	 * @param cutImageHeight
	 */
	public static boolean screenshot(String srcImagePath, String destImagePath, int x, int y, int cutImageWidth, int cutImageHeight) {
		String imageType = srcImagePath.substring(srcImagePath.lastIndexOf(".") + 1);

		FileInputStream fis = null;
		ImageInputStream iis = null;
		try {
			fis = new FileInputStream(srcImagePath);
			ImageReader reader = ImageIO.getImageReadersByFormatName(imageType).next();
			iis = ImageIO.createImageInputStream(fis);
			// 读取源true: 只向前搜索
			reader.setInput(iis, true);
			int width = reader.getWidth(0);
			int height = reader.getHeight(0);
			// 获取图片的宽高, 判断截取的图片是否超过了该图片的宽高
			if (x + cutImageWidth > width || y + cutImageHeight > height) {
				throw new IllegalArgumentException("截图部分超过了该图片的宽高");
			}
			ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceRegion(new Rectangle(x, y, cutImageWidth, cutImageHeight));
			return ImageIO.write(reader.read(0, param), imageType, new File(destImagePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != iis) iis.close();
				if (null != fis) fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 处理图片
	 * @作者: yangc
	 * @创建日期: 2014年10月13日 下午3:16:40
	 * @param srcImagePath 原图路径
	 * @param width 缩放的宽
	 * @param height 缩放的高
	 * @param keep 是否保持原图对比度
	 * @param angle 旋转角度(0不旋转,正数顺时针旋转,负数逆时针旋转)
	 * @param watermarkPath 水印图路径(null不打水印)
	 * @param position 水印位置
	 * @param opacity 水印透明度,0.0f全透,1.0f不透
	 * @param destImagePath 处理后保存路径
	 */
	public static boolean process(String srcImagePath, int width, int height, boolean keep, double angle, String watermarkPath, Position position, float opacity, String destImagePath) {
		try {
			Builder<File> builder = Thumbnails.of(srcImagePath).size(width, height).keepAspectRatio(keep);
			if (angle != 0) {
				builder.rotate(angle);
			}
			if (StringUtils.isNotBlank(watermarkPath)) {
				builder.watermark(position, ImageIO.read(new File(watermarkPath)), opacity);
			}
			builder.toFile(destImagePath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @功能: 截图
	 * @作者: yangc
	 * @创建日期: 2014年10月13日 下午3:29:53
	 * @param srcImagePath 原图路径
	 * @param x x坐标
	 * @param y y坐标
	 * @param width 截取的宽
	 * @param height 截取的高
	 * @param destImagePath 截图路径
	 * @return
	 */
	public static boolean screenshot(String srcImagePath, int x, int y, int width, int height, String destImagePath) {
		try {
			Thumbnails.of(srcImagePath).sourceRegion(x, y, width, height).toFile(destImagePath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
