package top.jplayer.audio.utils;

import android.graphics.Color;

import java.util.Random;

public class ColorUtil {
	/**
	 * 随机生成漂亮的颜色
	 * @return
	 */
	public static int randomColor() {
		Random random = new Random();

		int red = random.nextInt(150) + 50;
		
		int green = random.nextInt(150) + 50;
		
		int blue = random.nextInt(150) + 50;

		return Color.rgb(red, green, blue);		// 根据rgb混合生成一种新的颜色
	}

	/**
	 * 随机生成漂亮的颜色,带透明度的
	 * @return
	 */
	public static int randomColorArgb() {
		Random random = new Random();

		int alpha = random.nextInt(70) + 30;

		int red = random.nextInt(150) + 50;

		int green = random.nextInt(150) + 50;

		int blue = random.nextInt(150) + 50;

		return Color.argb(alpha, red, green, blue);		// 根据argb混合生成一种新的颜色
	}


	/**
	 * 颜色与上一个十六进制数ARGB，得到一个颜色加深的效果，效果从 0-F 深
	 * @param color
	 * @return
	 */
	public static int getColorDeeply(int color) {
//		| 0xF0000000 & 0xFFF5F5F5
		return color & 0xFFDDDDDD;
	}


	/**
	 * 颜色值取反
	 * @param color
	 * @return
	 */
	public static int getColorReverse(int color) {
		// String string = String.format("#%x", color); // string reverse
        int red = 255 - Color.red(color);
        int green = 255 - Color.green(color);
        int blue = 255 - Color.blue(color);
		return Color.argb(255, red, green, blue);
	}

	/**
	 * 获取两个颜色值之间渐变的某个点的颜色值
	 * @param resSColor
	 * @param resEColor
	 * @param rangeColorRate
	 * @return
	 */
	public static int getCompositeColor(int resSColor, int resEColor, float rangeColorRate) {
		int sc = resSColor;
		int ec = resEColor;
		int rS = Color.red(sc);
		int gS = Color.green(sc);
		int bS = Color.blue(sc);
		int rE = Color.red(ec);
		int gE = Color.green(ec);
		int bE = Color.blue(ec);
		int r = (int) (rS + (rE - rS) * 1f * rangeColorRate);
		int g = (int) (gS + (gE - gS) * 1f * rangeColorRate);
		int b = (int) (bS + (bE - bS) * 1f * rangeColorRate);
		return Color.argb(255, r, g, b);
	}

}
