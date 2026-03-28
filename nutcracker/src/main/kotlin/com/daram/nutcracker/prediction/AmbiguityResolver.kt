package com.daram.nutcracker.prediction

import com.daram.nutcracker.SyllableState

/**
 * 레이아웃별 미확정 자모 추출 인터페이스.
 *
 * 멀티탭/버퍼 방식 레이아웃에서 현재 SyllableState가 아직 결정되지 않은
 * (사용자가 더 입력할 수 있는) 자모 후보 목록을 반환한다.
 *
 * 두벌식·나랏글처럼 결정론적인 레이아웃은 항상 빈 리스트를 반환한다.
 */
interface AmbiguityResolver {
    /** HangulAutomata.layoutName과 일치하는 레이아웃 이름 */
    val layoutName: String

    /**
     * [state]에서 현재 미확정 상태인 자모의 후보 목록을 반환한다.
     * - 빈 리스트: 현재 상태가 결정론적 (미확정 없음)
     * - 비어 있지 않음: 사용자가 추가 입력으로 다른 자모를 선택할 수 있는 상태
     *
     * 예:
     *   SKY-II, cycleKey='1', cycleCount=1 → [ㄱ, ㅋ, ㄲ]  (추가 탭 가능)
     *   천지인, vowelBuffer=[ㆍ] → [ㅏ, ㅗ, ...]  (더 입력 시 다른 모음 가능)
     */
    fun pendingJamos(state: SyllableState): List<Char>
}
