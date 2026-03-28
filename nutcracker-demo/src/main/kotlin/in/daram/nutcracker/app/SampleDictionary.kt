package `in`.daram.nutcracker.app

import `in`.daram.nutcracker.prediction.InputLanguage
import `in`.daram.nutcracker.prediction.WordEntry

object SampleDictionary {
    fun getKoreanWords(): List<WordEntry> = listOf(
        // 인사
        WordEntry("안녕하세요", 100),
        WordEntry("안녕", 90),
        WordEntry("안녕하십니까", 80),
        WordEntry("반갑습니다", 70),
        WordEntry("처음 뵙겠습니다", 60),

        // 일상 표현
        WordEntry("감사합니다", 100),
        WordEntry("고마워", 95),
        WordEntry("괜찮습니다", 85),
        WordEntry("네", 100),
        WordEntry("아니요", 100),
        WordEntry("모르겠습니다", 75),
        WordEntry("알겠습니다", 85),

        // 식사
        WordEntry("밥", 90),
        WordEntry("밥을 먹다", 80),
        WordEntry("맛있다", 85),
        WordEntry("식사", 80),
        WordEntry("밥 먹었어", 75),
        WordEntry("맛있게 드세요", 70),

        // 날씨
        WordEntry("날씨", 85),
        WordEntry("오늘", 95),
        WordEntry("내일", 95),
        WordEntry("비", 90),
        WordEntry("날씨가 좋다", 80),
        WordEntry("흐리다", 75),
        WordEntry("맑다", 75),

        // 시간
        WordEntry("시간", 90),
        WordEntry("지금", 95),
        WordEntry("나중에", 90),
        WordEntry("언제", 85),
        WordEntry("어제", 85),
        WordEntry("모레", 80),

        // 감정
        WordEntry("좋다", 95),
        WordEntry("싫다", 90),
        WordEntry("행복하다", 85),
        WordEntry("슬프다", 80),
        WordEntry("화나다", 75),
        WordEntry("신나다", 75),
        WordEntry("재미있다", 85),
        WordEntry("재미없다", 75),

        // 상태
        WordEntry("피곤하다", 80),
        WordEntry("피곤해", 75),
        WordEntry("배고프다", 75),
        WordEntry("배고파", 70),
        WordEntry("목마르다", 70),
        WordEntry("춥다", 75),
        WordEntry("덥다", 75),

        // 동작
        WordEntry("가다", 95),
        WordEntry("오다", 95),
        WordEntry("있다", 95),
        WordEntry("없다", 90),
        WordEntry("하다", 95),
        WordEntry("먹다", 90),
        WordEntry("자다", 85),
        WordEntry("일어나다", 80),
        WordEntry("걷다", 80),

        // 인물/관계
        WordEntry("나", 95),
        WordEntry("너", 95),
        WordEntry("우리", 90),
        WordEntry("엄마", 85),
        WordEntry("아빠", 85),
        WordEntry("형", 80),
        WordEntry("누나", 80),
        WordEntry("친구", 90),
        WordEntry("학생", 85),

        // 물건
        WordEntry("책", 85),
        WordEntry("펜", 80),
        WordEntry("휴대폰", 90),
        WordEntry("컴퓨터", 85),
        WordEntry("차", 85),
        WordEntry("집", 90),
        WordEntry("학교", 90),
        WordEntry("회사", 90),

        // 색상
        WordEntry("빨강", 80),
        WordEntry("파랑", 80),
        WordEntry("노랑", 75),
        WordEntry("하양", 75),
        WordEntry("검정", 75),
        WordEntry("초록", 75),

        // 숫자/수량
        WordEntry("하나", 85),
        WordEntry("둘", 80),
        WordEntry("셋", 75),
        WordEntry("넷", 70),
        WordEntry("많다", 85),
        WordEntry("적다", 80),
        WordEntry("모두", 85),

        // 질문
        WordEntry("뭐", 90),
        WordEntry("뭐예요", 85),
        WordEntry("뭐 하세요", 80),
        WordEntry("뭐 해", 75),
        WordEntry("어디", 85),
        WordEntry("어디 가", 80),
        WordEntry("왜", 90),
        WordEntry("왜 그래", 75),
        WordEntry("어떻게", 85),

        // 부정
        WordEntry("아니다", 90),
        WordEntry("싫어", 85),
        WordEntry("싫어요", 85),
        WordEntry("싫습니다", 80),
        WordEntry("안 된다", 80),
        WordEntry("못해", 75),

        // 긍정
        WordEntry("좋아", 90),
        WordEntry("좋아요", 90),
        WordEntry("좋습니다", 85),
        WordEntry("오케이", 80),
        WordEntry("괜찮아", 85),
        WordEntry("될 거야", 75),

        // 도움/요청
        WordEntry("도와줘", 75),
        WordEntry("도와주세요", 80),
        WordEntry("부탁해", 80),
        WordEntry("부탁합니다", 85),
        WordEntry("도움이 되었으면", 70),

        // 약속/계획
        WordEntry("만나자", 80),
        WordEntry("약속", 85),
        WordEntry("계획", 85),
        WordEntry("예정", 80),
        WordEntry("내일 뵙겠습니다", 70),

        // 일반 명사
        WordEntry("사람", 90),
        WordEntry("일", 90),
        WordEntry("말", 90),
        WordEntry("소리", 80),
        WordEntry("생각", 85),
        WordEntry("문제", 85),
        WordEntry("답", 85),
        WordEntry("길", 85),
        WordEntry("돈", 85),
        WordEntry("시간", 90),
    )
}
