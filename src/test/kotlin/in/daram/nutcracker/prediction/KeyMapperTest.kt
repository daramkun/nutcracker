package `in`.daram.nutcracker.prediction

import `in`.daram.nutcracker.prediction.mapper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KeyMapperTest {

    // ── 두벌식 ────────────────────────────────────────────────────────────────

    @Test
    fun `두벌식 - 기본 자음 역매핑`() {
        val mapper = DubeolsikKeyMapper()
        assertEquals(listOf(listOf('r')), mapper.charToKeySequences('ㄱ'))
        assertEquals(listOf(listOf('e')), mapper.charToKeySequences('ㄷ'))
        assertEquals(listOf(listOf('n')), mapper.charToKeySequences('ㅜ'))
    }

    @Test
    fun `두벌식 - Shift 자음 역매핑`() {
        val mapper = DubeolsikKeyMapper()
        assertEquals(listOf(listOf('R')), mapper.charToKeySequences('ㄲ'))
        assertEquals(listOf(listOf('E')), mapper.charToKeySequences('ㄸ'))
    }

    @Test
    fun `두벌식 - 지원하지 않는 자모는 빈 리스트`() {
        val mapper = DubeolsikKeyMapper()
        assertTrue(mapper.charToKeySequences('X').isEmpty())
    }

    @Test
    fun `두벌식 - firstKeysFor`() {
        val mapper = DubeolsikKeyMapper()
        assertEquals(setOf('k'), mapper.firstKeysFor('ㅏ'))
    }

    // ── 단모음 ────────────────────────────────────────────────────────────────

    @Test
    fun `단모음 - 단일 모음`() {
        val mapper = DanmoemKeyMapper()
        assertEquals(listOf(listOf('j')), mapper.charToKeySequences('ㅏ'))
    }

    @Test
    fun `단모음 - 이중모음은 동일 키 2회`() {
        val mapper = DanmoemKeyMapper()
        assertEquals(listOf(listOf('j', 'j')), mapper.charToKeySequences('ㅑ'))
        assertEquals(listOf(listOf('y', 'y')), mapper.charToKeySequences('ㅛ'))
    }

    @Test
    fun `단모음 - 쌍자음은 동일 키 2회`() {
        val mapper = DanmoemKeyMapper()
        assertEquals(listOf(listOf('r', 'r')), mapper.charToKeySequences('ㄲ'))
    }

    // ── 나랏글 ────────────────────────────────────────────────────────────────

    @Test
    fun `나랏글 - 기본 자음`() {
        val mapper = NaratgeulKeyMapper()
        assertEquals(listOf(listOf('1')), mapper.charToKeySequences('ㄱ'))
        assertEquals(listOf(listOf('2')), mapper.charToKeySequences('ㄴ'))
    }

    @Test
    fun `나랏글 - 획추가 자음`() {
        val mapper = NaratgeulKeyMapper()
        assertEquals(listOf(listOf('1', '*')), mapper.charToKeySequences('ㅋ'))
        assertEquals(listOf(listOf('2', '*')), mapper.charToKeySequences('ㄷ'))
        assertEquals(listOf(listOf('2', '*', '*')), mapper.charToKeySequences('ㅌ'))
    }

    @Test
    fun `나랏글 - 모음 순환`() {
        val mapper = NaratgeulKeyMapper()
        assertEquals(listOf(listOf('3')), mapper.charToKeySequences('ㅏ'))
        assertEquals(listOf(listOf('3', '3')), mapper.charToKeySequences('ㅓ'))
        assertEquals(listOf(listOf('6')), mapper.charToKeySequences('ㅗ'))
        assertEquals(listOf(listOf('6', '6')), mapper.charToKeySequences('ㅜ'))
    }

    @Test
    fun `나랏글 - 이중모음 획추가`() {
        val mapper = NaratgeulKeyMapper()
        assertEquals(listOf(listOf('3', '*')), mapper.charToKeySequences('ㅑ'))
        assertEquals(listOf(listOf('6', '*')), mapper.charToKeySequences('ㅛ'))
    }

    @Test
    fun `나랏글 - firstKeysFor ㅜ는 6`() {
        val mapper = NaratgeulKeyMapper()
        assertEquals(setOf('6'), mapper.firstKeysFor('ㅜ'))
    }

    // ── 천지인 ────────────────────────────────────────────────────────────────

    @Test
    fun `천지인 - 기본 자음 (탭 1회)`() {
        val mapper = CheonjiinKeyMapper()
        assertEquals(listOf(listOf('4')), mapper.charToKeySequences('ㄱ'))
        assertEquals(listOf(listOf('0')), mapper.charToKeySequences('ㅇ'))
    }

    @Test
    fun `천지인 - 멀티탭 자음`() {
        val mapper = CheonjiinKeyMapper()
        assertEquals(listOf(listOf('4', '4')), mapper.charToKeySequences('ㅋ'))
        assertEquals(listOf(listOf('4', '4', '4')), mapper.charToKeySequences('ㄲ'))
    }

    @Test
    fun `천지인 - 모음 시퀀스`() {
        val mapper = CheonjiinKeyMapper()
        assertEquals(listOf(listOf('1')), mapper.charToKeySequences('ㅣ'))
        assertEquals(listOf(listOf('1', '2')), mapper.charToKeySequences('ㅏ'))
        assertEquals(listOf(listOf('2', '1')), mapper.charToKeySequences('ㅓ'))
        assertEquals(listOf(listOf('2', '3')), mapper.charToKeySequences('ㅗ'))
        assertEquals(listOf(listOf('3', '2')), mapper.charToKeySequences('ㅜ'))
    }

    // ── SKY-II ───────────────────────────────────────────────────────────────

    @Test
    fun `SKY-II - 1탭 자모`() {
        val mapper = SkyIIKeyMapper()
        assertEquals(listOf(listOf('1')), mapper.charToKeySequences('ㄱ'))
        assertEquals(listOf(listOf('3')), mapper.charToKeySequences('ㅏ'))
    }

    @Test
    fun `SKY-II - 멀티탭 자모`() {
        val mapper = SkyIIKeyMapper()
        assertEquals(listOf(listOf('1', '1')), mapper.charToKeySequences('ㅋ'))
        assertEquals(listOf(listOf('1', '1', '1')), mapper.charToKeySequences('ㄲ'))
        assertEquals(listOf(listOf('#', '#')), mapper.charToKeySequences('ㅠ'))
    }

    @Test
    fun `SKY-II - firstKeysFor ㄲ는 1`() {
        val mapper = SkyIIKeyMapper()
        assertEquals(setOf('1'), mapper.firstKeysFor('ㄲ'))
    }

    // ── 모토로라 ─────────────────────────────────────────────────────────────

    @Test
    fun `모토로라 - 1회 자모`() {
        val mapper = MotorolaKeyMapper()
        assertEquals(listOf(listOf('1')), mapper.charToKeySequences('ㄱ'))
        assertEquals(listOf(listOf('3')), mapper.charToKeySequences('ㅏ'))
        assertEquals(listOf(listOf('6')), mapper.charToKeySequences('ㅗ'))
    }

    @Test
    fun `모토로라 - 변환키 자모`() {
        val mapper = MotorolaKeyMapper()
        assertEquals(listOf(listOf('1', '#')), mapper.charToKeySequences('ㅋ'))
        assertEquals(listOf(listOf('3', '#')), mapper.charToKeySequences('ㅓ'))
        assertEquals(listOf(listOf('6', '#')), mapper.charToKeySequences('ㅜ'))
    }

    @Test
    fun `모토로라 - firstKeysFor ㅜ는 6`() {
        val mapper = MotorolaKeyMapper()
        assertEquals(setOf('6'), mapper.firstKeysFor('ㅜ'))
    }

    // ── QWERTY ───────────────────────────────────────────────────────────────

    @Test
    fun `QWERTY - 영문자 역매핑`() {
        val mapper = QwertyKeyMapper()
        assertEquals(listOf(listOf('h')), mapper.charToKeySequences('h'))
        assertEquals(listOf(listOf('h')), mapper.charToKeySequences('H'))  // 대문자→소문자
    }
}
