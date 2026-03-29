package com.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/** §4.7 천지인 검증 케이스 */
class CheonjiinTest {

    // 천지인 키: '1'=ㅣ, '2'=ㆍ, '3'=ㅡ, '4'~'0'=자음
    private fun seq(vararg keys: Char) = processSequence(cheonjiin, *keys)

    @Test fun `ㄱ_ㅏ`() {
        // 4(ㄱ) + 1(ㅣ) + 2(ㆍ) = 가
        val (c, p) = seq('4', '1', '2')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `ㅇ_ㅗ`() {
        // 0(ㅇ) + 2(ㆍ) + 3(ㅡ) = 오
        val (c, p) = seq('0', '2', '3')
        assertEquals("", c); assertEquals("오", p)
    }

    @Test fun `ㄱ_ㅛ`() {
        // 4(ㄱ) + 2+2+3(ㅛ) = 교
        val (c, p) = seq('4', '2', '2', '3')
        assertEquals("", c); assertEquals("교", p)
    }

    @Test fun `ㅅ_ㅣ`() {
        // 8(ㅅ) + 1(ㅣ) = 시
        val (c, p) = seq('8', '1')
        assertEquals("", c); assertEquals("시", p)
    }

    @Test fun `ㅌ_ㅏ`() {
        // 6(ㄷ) + 6(ㅌ) + 1+2(ㅏ) = 타
        val (c, p) = seq('6', '6', '1', '2')
        assertEquals("", c); assertEquals("타", p)
    }

    @Test fun `복합중성_ㅘ`() {
        // 0(ㅇ) + [2+3=ㅗ 완성] + [1+2=ㅏ 완성] → ㅘ
        val (c, p) = seq('0', '2', '3', '1', '2')
        assertEquals("", c); assertEquals("와", p)
    }

    @Test fun `종성_ㄴ`() {
        // 0+ㅏ+5(ㄴ) = 안
        val (c, p) = seq('0', '1', '2', '5')
        assertEquals("", c); assertEquals("안", p)
    }

    @Test fun `종성이_다음초성으로`() {
        // 안 + 2+3(ㅗ 완성 앞에 가운뎃점) = ㄴ이 다음 초성
        val (c, p) = seq('0', '1', '2', '5', '1', '2')
        assertEquals("아", c); assertEquals("나", p)
    }

    @Test fun `ㅋ`() {
        // 4(ㄱ)+4(ㅋ)+1+2(ㅏ) = 카
        val (c, p) = seq('4', '4', '1', '2')
        assertEquals("", c); assertEquals("카", p)
    }

    @Test fun `ㅐ`() {
        // 0+ㅐ(1+2+1) = 애
        val (c, p) = seq('0', '1', '2', '1')
        assertEquals("", c); assertEquals("애", p)
    }

    @Test fun `ㅔ`() {
        // 0+ㅔ(2+1+1) = 에
        val (c, p) = seq('0', '2', '1', '1')
        assertEquals("", c); assertEquals("에", p)
    }

    @Test fun `ㅒ`() {
        // 0+ㅒ(1+2+2+1) = 얘
        val (c, p) = seq('0', '1', '2', '2', '1')
        assertEquals("", c); assertEquals("얘", p)
    }

    @Test fun `ㅖ`() {
        // 0+ㅖ(2+2+1+1) = 예
        val (c, p) = seq('0', '2', '2', '1', '1')
        assertEquals("", c); assertEquals("예", p)
    }

    @Test fun `BS_중성제거`() {
        // 4+ㅏ(1+2) → BS → ㄱ(S1)
        val (c, p) = seq('4', '1', '2', '\u0008')
        assertEquals("", c); assertEquals("ㄱ", p)
    }

    @Test fun `BS_종성제거`() {
        // 4+ㅏ+5(ㄴ) → BS → 가(S2)
        val (c, p) = seq('4', '1', '2', '5', '\u0008')
        assertEquals("", c); assertEquals("가", p)
    }

    @Test fun `미완성패턴_버려지고_자음처리`() {
        // ㆍ+ㆍ 후 4(ㄱ) → ㆍㆍ 미완성, 버려지고 ㄱ 처리
        val (c, p) = seq('2', '2', '4')
        assertEquals("", c); assertEquals("ㄱ", p)
    }
}
