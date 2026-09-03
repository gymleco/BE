package kr.co.gymleco.service.admin;

import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.domain.setting.SettingKey;
import kr.co.gymleco.domain.setting.SiteSetting;
import kr.co.gymleco.domain.setting.SiteSettingRepository;
import kr.co.gymleco.infra.revalidate.ContentChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingAdminService {

    private final SiteSettingRepository repository;
    private final AuditLogger auditLogger;
    private final ApplicationEventPublisher events;

    public SettingAdminService(SiteSettingRepository repository,
                               AuditLogger auditLogger,
                               ApplicationEventPublisher events) {
        this.repository = repository;
        this.auditLogger = auditLogger;
        this.events = events;
    }
    @Transactional(readOnly = true)
    public Map<String, String> all() {
        Map<String, String> stored = new LinkedHashMap<>();
        for (SiteSetting s : repository.findAllByOrderByKeyAsc()) {
            stored.put(s.getKey(), s.getValue());
        }

        Map<String, String> out = new LinkedHashMap<>();
        for (SettingKey key : SettingKey.values()) {
            out.put(key.key(), stored.getOrDefault(key.key(), ""));
        }
        return out;
    }
    @Transactional(readOnly = true)
    public Map<String, String> publicOnly() {
        Map<String, String> all = all();
        Map<String, String> out = new LinkedHashMap<>();
        for (SettingKey key : SettingKey.values()) {
            if (key.isPublic()) {
                out.put(key.key(), all.get(key.key()));
            }
        }
        return out;
    }
    @Transactional
    public void updateAll(Map<String, String> incoming, AdminUser actor, String ip) {
        if (incoming == null || incoming.isEmpty()) {
            throw new IllegalArgumentException("바꿀 값이 없습니다.");
        }

        Map<SettingKey, String> clean = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : incoming.entrySet()) {
            SettingKey key = SettingKey.of(entry.getKey())
                .orElseThrow(() -> new UnknownSettingKeyException(entry.getKey()));
            String value = entry.getValue() == null ? "" : entry.getValue().trim();

            /*
             * 살균기를 쓰지 않고 «거부» 한다.
             *
             * HtmlSanitizer 는 결과가 HTML 안에 들어갈 것을 전제로 하므로
             * @ 를 &#64; 로, & 를 &amp; 로 바꾼다. 이 값들은 마크업이 아니라
             * 글로 나가기 때문에 그 변환이 그대로 손상이 된다 — 이메일이 검증을
             * 통과하지 못하고, 무엇보다 저장할 때마다 한 번 더 escape 돼서
             * "김대표 & 회사" 를 두 번 저장하면 "&amp;amp;" 가 된다.
             *
             * 이 칸들은 짧은 운영 문구다. < 나 > 가 들어올 이유가 없으므로
             * 조용히 바꾸지 말고 왜 안 되는지 말해 주는 편이 낫다.
             */
            if (value.indexOf('<') >= 0 || value.indexOf('>') >= 0) {
                throw new IllegalArgumentException(
                    key.label() + " 에는 < 나 > 를 넣을 수 없습니다.");
            }

            if (value.length() > key.maxLength()) {
                throw new IllegalArgumentException(
                    key.label() + " 은(는) " + key.maxLength() + "자까지 넣을 수 있습니다.");
            }
            if (!value.isEmpty()) {
                try {
                    key.type().check(value);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(key.label() + ": " + e.getMessage());
                }
            }
            clean.put(key, value);
        }
        List<String> changed = new ArrayList<>();
        for (Map.Entry<SettingKey, String> entry : clean.entrySet()) {
            SettingKey key = entry.getKey();
            String value = entry.getValue();

            SiteSetting row = repository.findById(key.key()).orElse(null);
            if (row == null) {
                repository.save(SiteSetting.of(key, value));
                changed.add(key.label());
            } else if (!row.getValue().equals(value)) {
                row.change(value);
                changed.add(key.label());
            }
        }
        if (changed.isEmpty()) {
            return;
        }

        auditLogger.contentChanged(actor, AuditAction.SETTING_UPDATED,
            "SiteSetting", null, String.join(" · ", changed) + " 변경", ip);

        events.publishEvent(ContentChangedEvent.settings());
    }
}
