package kr.co.gymleco.domain.machine;

/**
 * 기구 한 대의 상태.
 *
 * ★ 이름이 DB 의 CHECK 제약과 «글자 단위로» 같아야 한다.
 *   @Enumerated(EnumType.STRING) 이라 이 이름이 그대로 저장된다.
 *   V9 의 ck_machine_unit_status 와 어긋나면 저장이 거절되는데,
 *   그 상태로 넘어가는 순간에만 터지므로 한참 뒤에 발견된다.
 *
 * ★ FLAGGED 를 공개 화면에서 «도난» 이라고 말하지 않는다.
 *   훔친 사람이 조회해 보고 처분 경로를 바꾼다.
 *   공개 응답은 「확인이 필요합니다」 로만 받고, 관리자에게 알린다.
 */
public enum MachineUnitStatus {
    REGISTERED, // 입고 · 미판매
    SOLD,       // 판매됨 · 미출고
    SHIPPED,    // 출고
    INSTALLED,  // 설치 완료
    RECOVERED,  // 회수 → 중고로
    SCRAPPED,   // 폐기
    FLAGGED     // 확인 필요 (도난 신고 등)
}
