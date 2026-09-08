package kr.co.gymleco.service.machine;

/**
 * 정품 확인 결과 — 공개 화면에 나가는 값.
 *
 * ★ 여기에 «업체 · 연락처 · 비고 · 판매일» 을 절대 넣지 않는다.
 *   어느 헬스장에 무엇이 몇 대 있는지는 그 업체의 영업 정보이고,
 *   동시에 장물 표적 목록이 된다. 이 record 의 필드 목록 자체가
 *   «무엇까지 내보내도 되는가» 의 경계다.
 *
 * ★ 「번호가 있다」 만 답하지 않는다.
 *   위조범은 진짜 명판을 베껴 같은 번호를 여러 대에 붙인다. 존재 여부만
 *   답하면 우리가 가짜를 인증해 주는 꼴이다. 그래서 «이 번호의 기구는
 *   무엇인가» 를 함께 돌려주고, 눈앞의 기구와 대조하게 만든다.
 */
public record VerificationView(
    Outcome outcome,
    String serial,        // 저장된 표기 그대로. 못 찾았으면 null
    String modelCode,
    String productNameKo,
    Short madeYear
) {
    public enum Outcome {
        /** 번호와 모델이 맞다 */
        GENUINE,
        /** 번호는 있는데 모델이 다르다 — 가장 강한 위조 신호 */
        MODEL_MISMATCH,
        /** 등록되지 않은 번호 */
        UNKNOWN,
        /** 확인이 필요한 기구 (도난 신고 등). 이유를 말하지 않는다 */
        NEEDS_CHECK
    }

    static VerificationView unknown() {
        return new VerificationView(Outcome.UNKNOWN, null, null, null, null);
    }

    static VerificationView needsCheck() {
        return new VerificationView(Outcome.NEEDS_CHECK, null, null, null, null);
    }
}
