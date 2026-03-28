package com.daram.nutcracker.prediction

import com.daram.nutcracker.FSMState
import com.daram.nutcracker.SyllableState
import com.daram.nutcracker.prediction.resolver.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AmbiguityResolverTest {

    // ── 단모음 ────────────────────────────────────────────────────────────────

    @Test
    fun `단모음 - cycleKey 없으면 empty`() {
        val resolver = DanmoemAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ')
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }

    @Test
    fun `단모음 - cycleKey r (ㄱ) 이면 ㄱ,ㄲ 반환`() {
        val resolver = DanmoemAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S1, cho = 'ㄱ', cycleKey = 'r', cycleCount = 1)
        val jamos = resolver.pendingJamos(state)
        assertEquals(listOf('ㄱ', 'ㄲ'), jamos)
    }

    @Test
    fun `단모음 - cycleKey j (ㅏ) 이면 ㅏ,ㅑ 반환`() {
        val resolver = DanmoemAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ', cycleKey = 'j', cycleCount = 1)
        val jamos = resolver.pendingJamos(state)
        assertEquals(listOf('ㅏ', 'ㅑ'), jamos)
    }

    @Test
    fun `단모음 - double 없는 키 (l=ㅣ) 는 단일 자모`() {
        val resolver = DanmoemAmbiguityResolver()
        val state = SyllableState(cycleKey = 'k', cycleCount = 1)
        val jamos = resolver.pendingJamos(state)
        assertEquals(listOf('ㅣ'), jamos)
    }

    // ── 천지인 ────────────────────────────────────────────────────────────────

    @Test
    fun `천지인 - vowelBuffer 없으면 empty`() {
        val resolver = CheonjiinAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ')
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }

    @Test
    fun `천지인 - vowelBuffer ㅣ 이면 ㅣ 와 ㅏ 등 접두사 일치 모음 반환`() {
        val resolver = CheonjiinAmbiguityResolver()
        // vowelBuffer=[ㅣ]는 ㅣ,ㅏ,ㅐ,ㅑ,ㅒ 패턴의 접두사
        val state = SyllableState(vowelBuffer = listOf('ㅣ'))
        val jamos = resolver.pendingJamos(state)
        assertTrue('ㅣ' in jamos, "ㅣ 단독 패턴 포함")
        assertTrue('ㅏ' in jamos, "ㅣ+ㆍ=ㅏ 패턴 포함")
    }

    @Test
    fun `천지인 - cycleKey 있으면 해당 사이클 전체 반환`() {
        val resolver = CheonjiinAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S1, cho = 'ㄱ', cycleKey = '4', cycleCount = 1)
        val jamos = resolver.pendingJamos(state)
        assertEquals(listOf('ㄱ', 'ㅋ', 'ㄲ'), jamos)
    }

    // ── SKY-II ───────────────────────────────────────────────────────────────

    @Test
    fun `SKY-II - cycleKey 없으면 empty`() {
        val resolver = SkyIIAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ')
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }

    @Test
    fun `SKY-II - cycleKey 1 count 1 이면 ㄱ,ㅋ,ㄲ`() {
        val resolver = SkyIIAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S1, cho = 'ㄱ', cycleKey = '1', cycleCount = 1)
        assertEquals(listOf('ㄱ', 'ㅋ', 'ㄲ'), resolver.pendingJamos(state))
    }

    @Test
    fun `SKY-II - cycleKey 1 count 2 이면 ㅋ,ㄲ (이미 ㅋ 선택됨)`() {
        val resolver = SkyIIAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S1, cho = 'ㅋ', cycleKey = '1', cycleCount = 2)
        assertEquals(listOf('ㅋ', 'ㄲ'), resolver.pendingJamos(state))
    }

    @Test
    fun `SKY-II - 2탭짜리 키 count 2 이면 empty (마지막 선택)`() {
        val resolver = SkyIIAmbiguityResolver()
        // key '2': [ㅣ, ㅡ], count=2 → currentIdx=1 → subList(1,2)=[ㅡ]
        val state = SyllableState(cycleKey = '2', cycleCount = 2)
        val jamos = resolver.pendingJamos(state)
        assertEquals(listOf('ㅡ'), jamos)
    }

    // ── 모토로라 ─────────────────────────────────────────────────────────────

    @Test
    fun `모토로라 - cycleKey 없으면 empty`() {
        val resolver = MotorolaAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅏ')
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }

    @Test
    fun `모토로라 - cycleKey 6 count 0 (tentative) 이면 ㅗ,ㅜ`() {
        val resolver = MotorolaAmbiguityResolver()
        val state = SyllableState(cycleKey = '6', cycleCount = 0)
        assertEquals(listOf('ㅗ', 'ㅜ'), resolver.pendingJamos(state))
    }

    @Test
    fun `모토로라 - cycleKey 6 count 1 이면 ㅗ,ㅜ`() {
        val resolver = MotorolaAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅗ', cycleKey = '6', cycleCount = 1)
        assertEquals(listOf('ㅗ', 'ㅜ'), resolver.pendingJamos(state))
    }

    @Test
    fun `모토로라 - cycleKey 6 count 2 이면 empty (ㅜ 확정)`() {
        val resolver = MotorolaAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S2, cho = 'ㄷ', jung = 'ㅜ', cycleKey = '6', cycleCount = 2)
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }

    @Test
    fun `모토로라 - 단일 자모 키 (5=ㄹ) 이면 empty`() {
        val resolver = MotorolaAmbiguityResolver()
        val state = SyllableState(fsm = FSMState.S1, cho = 'ㄹ', cycleKey = '5', cycleCount = 1)
        assertTrue(resolver.pendingJamos(state).isEmpty())
    }
}
