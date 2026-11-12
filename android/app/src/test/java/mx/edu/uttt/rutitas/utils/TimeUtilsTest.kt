package mx.edu.uttt.rutitas.utils

import org.junit.Assert.*
import org.junit.Test

class TimeUtilsTest {
    @Test fun convertAmPmCorrectly() {
        assertEquals(0, TimeUtils.toMinutes("12:00 AM"))
        assertEquals(720, TimeUtils.toMinutes("12:00 PM"))
        assertEquals(1145, TimeUtils.toMinutes("7:05 pm"))
    }

    @Test fun rejectInvalidTimes() {
        assertNull(TimeUtils.toMinutes("13:00 PM"))
        assertNull(TimeUtils.toMinutes("7:60 AM"))
        assertNull(TimeUtils.toMinutes("sin hora"))
    }

    @Test fun formatWrapsAtMidnight() {
        assertEquals("12:00 AM", TimeUtils.format(1440))
        assertEquals("11:59 PM", TimeUtils.format(-1))
        assertEquals("1:15 PM", TimeUtils.format(795))
    }
}
