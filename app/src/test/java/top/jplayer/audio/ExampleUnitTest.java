package top.jplayer.audio;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        System.out.println(getTime());
    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改

        int hour = c.get(Calendar.HOUR_OF_DAY);

        int minute = c.get(Calendar.MINUTE);

        int second = c.get(Calendar.SECOND);

        return (hour + ":" + minute + ":" + second);

    }
}