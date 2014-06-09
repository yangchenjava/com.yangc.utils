package com.yangc.utils.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CaptchaUtils {

	public static final String CAPTCHA = "CAPTCHA";

	private static final Random random = new Random();

	private CaptchaUtils() {
	}

	/**
	 * @功能: 英文, 数字, 中文, 英文数字, 英文数字中文
	 * @作者: yangc
	 * @创建日期: 2014年5月27日 下午5:06:51
	 */
	public enum CAPTCHA_TYPE {
		ENGLISH, NUMBER, CHINESE, ENGLISH_NUMBER, ALL
	}

	/**
	 * @功能: 生成验证码输出到页面
	 * @作者: yangc
	 * @创建日期: 2014年5月27日 下午5:06:51
	 */
	public static boolean captcha(int width, int height, int size, CAPTCHA_TYPE captchaType, HttpServletRequest request, HttpServletResponse response) {
		String code = getCode(size, captchaType);
		BufferedImage bi = getBufferedImage(width, height, size, captchaType, code);

		request.getSession().setAttribute(CAPTCHA, code);
		ServletOutputStream sos = null;
		try {
			response.setContentType("image/jpeg");
			sos = response.getOutputStream();
			boolean result = ImageIO.write(bi, "jpg", sos);
			sos.flush();
			sos.close();
			sos = null;
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (sos != null) sos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 生成验证码输出到文件
	 * @作者: yangc
	 * @创建日期: 2014年5月27日 下午5:07:25
	 */
	public static boolean captcha(int width, int height, int size, CAPTCHA_TYPE captchaType, String fileName) {
		String code = getCode(size, captchaType);
		BufferedImage bi = getBufferedImage(width, height, size, captchaType, code);

		try {
			return ImageIO.write(bi, "jpg", new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static BufferedImage getBufferedImage(int width, int height, int size, CAPTCHA_TYPE captchaType, String code) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();

		// 白色背景
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);

		// 黑色框
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, width - 1, height - 1);

		// 随机颜色的干扰点
		for (int i = 0; i < 100; i++) {
			g2d.setColor(getColor(180, 230));
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int w = random.nextInt(4);
			int d = random.nextInt(4);

			g2d.fillOval(x, y, w, d);
		}

		// 字体随机颜色的文字
		int fontSize = Math.min(width / size, height);
		g2d.setFont(new Font("宋体", Font.BOLD, fontSize));
		char[] chars = code.toCharArray();
		for (int i = 0; i < size; i++) {
			g2d.setColor(getColor(60, 150));
			// 旋转
			AffineTransform affine = new AffineTransform();
			affine.setToRotation(Math.PI / 4 * random.nextDouble() * (random.nextBoolean() ? 1 : -1), (width / size) * i + fontSize / 2, height / 2);
			g2d.setTransform(affine);
			g2d.drawChars(chars, i, 1, (width / size) * i, height / 2 + fontSize / 2);
		}
		g2d.dispose();
		return bi;
	}

	private static Color getColor(int fc, int bc) {
		int r = 0, g = 0, b = 0;
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;
		if (fc == 255 && bc == fc) {
			r = random.nextInt(255);
			g = random.nextInt(255);
			b = random.nextInt(255);
		} else {
			r = fc + random.nextInt(bc - fc);
			g = fc + random.nextInt(bc - fc);
			b = fc + random.nextInt(bc - fc);
		}
		return new Color(r, g, b);
	}

	public static String getCode(int size, CAPTCHA_TYPE captchaType) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			switch (captchaType) {
			case ENGLISH:
				sb.append(getEnglish());
				break;
			case NUMBER:
				sb.append(getNumber());
				break;
			case CHINESE:
				sb.append(getChinese());
				break;
			case ENGLISH_NUMBER:
				if (random.nextInt(i % 2 + 1) == 1) {
					sb.append(getNumber());
				} else {
					sb.append(getEnglish());
				}
				break;
			case ALL:
				int j = random.nextInt(i * (i + 3) / 2 + 1);
				if (j == 1) {
					sb.append(getNumber());
				} else if (j == 2) {
					sb.append(getChinese());
				} else {
					sb.append(getEnglish());
				}
				break;
			}
		}
		return sb.toString();
	}

	private static String getEnglish() {
		int temp = random.nextInt(26) + 65;
		return String.valueOf((char) temp);
	}

	private static String getNumber() {
		int temp = random.nextInt(10) + 48;
		return String.valueOf((char) temp);
	}

	private static String getChinese() {
		// 存储中文字符
		String temp = "";
		String[] rBase = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
		// 生成第1位的区码
		int r1 = random.nextInt(3) + 11; // 生成11到14之间的随机数
		String str_r1 = rBase[r1];
		// 生成第2位的区码
		int r2;
		if (r1 == 13) {
			r2 = random.nextInt(7); // 生成0到7之间的随机数
		} else {
			r2 = random.nextInt(16); // 生成0到16之间的随机数
		}
		String str_r2 = rBase[r2];

		// 生成第1位的位码
		int r3 = random.nextInt(6) + 10; // 生成10到16之间的随机数
		String str_r3 = rBase[r3];
		// 生成第2位的位码
		int r4;
		if (r3 == 10) {
			r4 = random.nextInt(15) + 1; // 生成1到16之间的随机数
		} else if (r3 == 15) {
			r4 = random.nextInt(15); // 生成0到15之间的随机数
		} else {
			r4 = random.nextInt(16); // 生成0到16之间的随机数
		}
		String str_r4 = rBase[r4];

		// 将生成机内码转换为汉字
		byte[] bytes = new byte[2];
		// 将生成的区码保存到字节数组的第1个元素中
		String str_r12 = str_r1 + str_r2;
		int tempLow = Integer.parseInt(str_r12, 16);
		bytes[0] = (byte) tempLow;
		// 将生成的位码保存到字节数组的第2个元素中
		String str_r34 = str_r3 + str_r4;
		int tempHigh = Integer.parseInt(str_r34, 16);
		bytes[1] = (byte) tempHigh;
		try {
			// 根据字节数组生成汉字
			temp = new String(bytes, "GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return temp;
	}

	public static void main(String[] args) {
		captcha(100, 40, 4, CAPTCHA_TYPE.ALL, "E:/test.jpg");
	}

}
