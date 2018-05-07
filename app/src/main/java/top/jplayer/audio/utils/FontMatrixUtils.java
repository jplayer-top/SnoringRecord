package top.jplayer.audio.utils;

import android.graphics.Paint;

/**
 * @author dyzs
 * Created on 2016/3/24.
 */
public class FontMatrixUtils {
    private FontMatrixUtils() {
        throw new UnsupportedOperationException("font matrix can't be initialized");
    }
    /**
     * @details 准确的计算画笔绘制文字的Y轴的正确坐标，这个坐标计算的基础为 paint X = 0，y = 0
     * @param paint
     * @return
     */
    public static float calcTextCenterVerticalBaselineY(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float ret = paint.getTextSize() / 2 - fm.descent
                + (fm.bottom - fm.top) / 2;
        return ret;
    }

    public static float calcTextHalfHeightPoint (Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float ret = (fm.bottom - fm.top) / 2;
        return ret;
    }
}
