package kr.co.gymleco.domain.audit;

/**
 * 감사 로그 행위 유형.
 *
 * 문자열을 그때그때 적으면 "INQUIRY_VIEW" 와 "INQUIRY_VIEWED" 가 섞이고,
 * 나중에 집계할 때 둘 다 세야 한다는 걸 아무도 기억하지 못한다.
 */
public enum AuditAction {

    // ── 인증 ──
    LOGIN_SUCCEEDED,
    LOGIN_FAILED,
    LOGIN_BLOCKED,
    LOGIN_NEW_IP,
    LOGOUT,
    REFRESH_TOKEN_REUSE,

    // ── 개인정보 ──
    INQUIRY_VIEWED,
    INQUIRY_EXPORTED,
    INQUIRY_STATUS_CHANGED,
    INQUIRY_DELETED,
    INQUIRY_PURGED,

    // ── 콘텐츠 ──
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    PRODUCT_VISIBILITY_CHANGED,
    NEWS_CREATED,
    NEWS_UPDATED,
    NEWS_DELETED,
    SETTING_UPDATED,

    // ── 계정 ──
    ADMIN_CREATED,
    ADMIN_PASSWORD_CHANGED,
    ADMIN_DISABLED
}
