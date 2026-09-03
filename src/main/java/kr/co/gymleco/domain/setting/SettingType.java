package kr.co.gymleco.domain.setting;

import java.util.regex.Pattern;

/**
 * 설정값의 종류. 어떻게 검증하고, 관리 화면이 어떤 칸으로 그릴지를 정한다.
 *
 * ★ URL 을 그냥 문자열로 두면 안 된다
 *   javascript:alert(1) 도 문자열로는 멀쩡하다. 화면이 <a href={값}> 으로
 *   그리는 순간 클릭 한 번에 실행된다. 그래서 http/https 로 시작하는지를
 *   저장 시점에 본다. 화면에서 거르면 화면 하나를 빠뜨리는 순간 구멍이 된다.
 *
 * ★ 빈 값은 언제나 통과시킨다
 *   대표님께 아직 못 받은 항목이 많다. 비워 두는 것이 «아직 없음» 이고,
 *   그걸 오류로 막으면 나머지 칸도 저장할 수 없게 된다.
 */
public enum SettingType {

    TEXT {
        @Override public void check(String value) {
            // 길이만 본다. 길이는 SettingKey 가 알고 있다.
        }
    },

    URL {
        @Override public void check(String value) {
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                throw new IllegalArgumentException(
                    "주소는 http:// 또는 https:// 로 시작해야 합니다.");
            }
        }
    },

    PHONE {
        private static final Pattern SHAPE = Pattern.compile("^[0-9+\\-() ]+$");
        @Override public void check(String value) {
            if (!SHAPE.matcher(value).matches()) {
                throw new IllegalArgumentException(
                    "전화번호에는 숫자와 - ( ) + 만 넣을 수 있습니다.");
            }
        }
    },

    EMAIL {
        @Override public void check(String value) {
            // 이메일의 완전한 문법 검사는 실무에서 늘 틀린다.
            // 사람이 오타를 알아챌 정도만 본다.
            int at = value.indexOf('@');
            if (at <= 0 || value.indexOf('.', at) < 0 || value.endsWith(".")) {
                throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
            }
        }
    },

    /** 쉼표로 이어 붙인 제품 주소 목록 — 예: power-rack,smith-machine */
    SLUG_LIST {
        private static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
        @Override public void check(String value) {
            for (String part : value.split(",")) {
                String slug = part.trim();
                if (slug.isEmpty() || !SLUG.matcher(slug).matches()) {
                    throw new IllegalArgumentException(
                        "제품 주소 목록이 올바르지 않습니다: " + slug);
                }
            }
        }
    },

    THEME {
        @Override public void check(String value) {
            if (!value.equals("dark") && !value.equals("light")) {
                throw new IllegalArgumentException("테마는 dark 또는 light 만 됩니다.");
            }
        }
    };

    /** 값이 비어 있지 않을 때만 불린다. 어긋나면 IllegalArgumentException. */
    public abstract void check(String value);
}
