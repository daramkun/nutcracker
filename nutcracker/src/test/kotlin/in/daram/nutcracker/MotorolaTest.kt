package `in`.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §7.7 모토로라 검증 케이스 */
class MotorolaTest {

    // '#' 은 MODE_SWITCH (한/영 변환키)
    private fun seq(vararg keys: Char) = processSequenceWithSpecial(
        motorola,
        keys.map { key ->
            when (key) {
                '#' -> KeyInput.Special(SpecialKey.MODE_SWITCH)
                '\u0008' -> KeyInput.Special(SpecialKey.BACKSPACE)
                '\u0020' -> KeyInput.Special(SpecialKey.SPACE)
                else -> KeyInput.Char(key)
            }
        }
    )

    @Test fun `ㄱ_ㅏ`() {
        val (c, p) = seq('1', '3')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `ㅋ_ㅏ`() {
        // ㄱ + [한] = ㅋ
        val (c, p) = seq('1', '#', '3')
        assertEquals("", c); assertEquals("카", p)
    }

    @Test fun `ㄴ_ㅏ`() {
        val (c, p) = seq('2', '3')
        assertEquals("", c); assertEquals("나", p)
    }

    @Test fun `ㅁ_ㅏ`() {
        val (c, p) = seq('2', '#', '3')
        assertEquals("", c); assertEquals("마", p)
    }

    @Test fun `ㄷ_ㅏ`() {
        val (c, p) = seq('4', '3')
        assertEquals("", c); assertEquals("다", p)
    }

    @Test fun `ㅌ_ㅏ`() {
        val (c, p) = seq('4', '#', '3')
        assertEquals("", c); assertEquals("타", p)
    }

    @Test fun `ㅇ_ㅏ`() {
        val (c, p) = seq('0', '3')
        assertEquals("", c); assertEquals("아", p)
    }

    @Test fun `ㅎ_ㅏ`() {
        val (c, p) = seq('0', '#', '3')
        assertEquals("", c); assertEquals("하", p)
    }

    @Test fun `ㅈ_ㅓ`() {
        // * = ㅈ, 3×2회 = ㅓ
        val (c, p) = seq('*', '3', '#')
        assertEquals("", c); assertEquals("저", p)
    }

    @Test fun `ㅊ_ㅏ`() {
        val (c, p) = seq('*', '#', '3')
        assertEquals("", c); assertEquals("차", p)
    }

    @Test fun `모음_한키_없이`() {
        // 3 두 번 → 별개 글자 (모토로라는 한 키 없이 3 두 번 = ㅏ 확정 + ㅏ 새 글자)
        val (c, p) = seq('3', '3')
        assertEquals("아", c); assertEquals("아", p)
    }

    @Test fun `모음_한키로변환`() {
        // 3 + # = ㅓ
        val (c, p) = seq('3', '#')
        assertEquals("", c); assertEquals("어", p)
    }

    @Test fun `복합중성_ㅘ`() {
        val (c, p) = seq('1', '6', '3')
        assertEquals("", c); assertEquals("과", p)
    }

    @Test fun `복합중성_ㅝ`() {
        // ㄱ + ㅜ(6+#) + ㅓ(3+#) = 궈
        val (c, p) = seq('1', '6', '#', '3', '#')
        assertEquals("", c); assertEquals("궈", p)
    }

    @Test fun `BS_종성제거`() {
        val (c, p) = seq('1', '3', '2', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `BS_중성제거`() {
        val (c, p) = seq('1', '3', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `ㅋ_BS_ㄱ복원`() {
        // ㅋ 입력 후 BS → ㄱ으로 복원
        val (c, p) = seq('1', '#', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }
}
