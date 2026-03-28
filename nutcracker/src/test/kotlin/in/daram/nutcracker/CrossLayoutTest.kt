package `in`.daram.nutcracker

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * §9 크로스 레이아웃 검증
 * 동일한 단어를 각 레이아웃으로 입력했을 때 동일한 출력이 나와야 한다.
 */
class CrossLayoutTest {

    // ── §9.2 공통 Backspace 동작 ──────────────────────────────────────────

    private fun testCommonBackspace(automata: HangulAutomata, cho: Char, jung: Char, jong: Char) {
        val name = automata.layoutName

        // 1. 초성 입력: committed="" (composing은 레이아웃별로 다를 수 있으므로 비체크)
        run {
            val (c, _) = processSequence(automata, cho)
            assertEquals("", c, "$name: S1 committed")
        }
        // 2. 중성 입력
        run {
            val (c, _) = processSequence(automata, cho, jung)
            assertEquals("", c, "$name: S2 committed")
        }
        // 3. 종성 입력
        run {
            val (c, _) = processSequence(automata, cho, jung, jong)
            assertEquals("", c, "$name: S3 committed")
        }
        // 4. BS → S2
        run {
            val (c, _) = processSequence(automata, cho, jung, jong, '\u0008')
            assertEquals("", c, "$name: BS→S2 committed")
        }
        // 5. BS → S1: committed=""
        run {
            val (c, _) = processSequence(automata, cho, jung, jong, '\u0008', '\u0008')
            assertEquals("", c, "$name: BS→S1 committed")
        }
        // 6. BS → S0
        run {
            val (c, p) = processSequence(automata, cho, jung, jong, '\u0008', '\u0008', '\u0008')
            assertEquals("", c, "$name: BS→S0 committed"); assertEquals("", p, "$name: BS→S0 composing")
        }
        // 7. BS (무시)
        run {
            val (c, p) = processSequence(automata, cho, jung, jong, '\u0008', '\u0008', '\u0008', '\u0008')
            assertEquals("", c, "$name: BS ignore committed"); assertEquals("", p, "$name: BS ignore composing")
        }
    }

    @Test fun `두벌식_공통BS`() {
        testCommonBackspace(dubeolsik, 'r', 'k', 's')
    }

    @Test fun `단모음_공통BS`() {
        testCommonBackspace(danmoem, 'r', 'j', 's')
    }

    // ── §9.1 "한글" 입력 ─────────────────────────────────────────────────

    @Test fun `두벌식_한글`() {
        // g(ㅎ) k(ㅏ) s(ㄴ) r(ㄱ) l(ㅣ)
        val (c, p) = processSequence(dubeolsik, 'g', 'k', 's', 'r', 'm', 'f')
        assertEquals("한", c); assertEquals("글", p)
    }

    @Test fun `단모음_한글`() {
        // G(ㅎ) J(ㅏ) S(ㄴ) R(ㄱ) K(ㅣ)
        val (c, p) = processSequence(danmoem, 'g', 'j', 's', 'r', 'n', 'f')
        assertEquals("한", c); assertEquals("글", p)
    }

    // ── §9.3 겹받침 분리 검증 ────────────────────────────────────────────

    private fun testDoubleJongseong(automata: HangulAutomata, cho: Char, jung: Char, jong1: Char, jong2: Char, nextJung: Char) {
        val name = automata.layoutName
        // 5단계: 모음 입력 → 겹받침 분리
        val (c, p) = processSequence(automata, cho, jung, jong1, jong2, nextJung)
        // 기대: "(초+중+겹왼)" 확정 + "(겹오+nextJung)" composing
        // 예) 달가: 달 confirmed, 가 composing
    }

    @Test fun `두벌식_겹받침분리`() {
        // ㄷ+ㅏ+ㄹ+ㄱ+ㅏ = 달가
        val (c, p) = processSequence(dubeolsik, 'e', 'k', 'f', 'r', 'k')
        assertEquals("달", c); assertEquals("가", p)
    }

    @Test fun `두벌식_겹받침BS`() {
        // 닭 → BS → 달
        val (c, p) = processSequence(dubeolsik, 'e', 'k', 'f', 'r', '\u0008')
        assertEquals("", c); assertEquals("달", p)
    }

    @Test fun `단모음_겹받침분리`() {
        val (c, p) = processSequence(danmoem, 'e', 'j', 'f', 'r', 'j')
        assertEquals("달", c); assertEquals("가", p)
    }

    // ── §9.4 단모음 이중모음 BS 검증 ────────────────────────────────────

    @Test fun `단모음_이중모음단독BS`() {
        // JJ(ㅑ) → BS → 빈
        val (c, p) = processSequence(danmoem, 'j', 'j', '\u0008')
        assertEquals("", c); assertEquals("", p)
    }

    @Test fun `단모음_초성이중모음BS`() {
        // D(ㅇ) + JJ(ㅑ) → BS → ㅇ(S1)
        val (c, p) = processSequence(danmoem, 'd', 'j', 'j', '\u0008')
        assertEquals("", c); assertEquals("ㅇ", p)
    }
}
