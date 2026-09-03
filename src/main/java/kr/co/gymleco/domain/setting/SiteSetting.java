package kr.co.gymleco.domain.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "site_setting")
public class SiteSetting {
    @Id @Column(name = "key", length = 80)
    private String key;
    @Column(name = "value", nullable = false, columnDefinition = "text")
    private String value = "";
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected SiteSetting(){}
    public static SiteSetting of(SettingKey key, String value){
        SiteSetting s = new SiteSetting();
        s.key = key.key();
        s.value = value == null ? "" : value;
        return s;
    }
    public void change(String value){
        this.value = value  == null ? "" : value;
        this.updatedAt = Instant.now();
    }
    public String getKey(){return key;}
    public String getValue(){return value;}
    public Instant getUpdatedAt(){return updatedAt;}
}
