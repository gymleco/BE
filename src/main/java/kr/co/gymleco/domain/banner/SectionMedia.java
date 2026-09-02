package kr.co.gymleco.domain.banner;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "section_media")
public class SectionMedia {

    @Id
    @Column(name = "section_key", length = 60)
    private String sectionKey;

    @Column(name = "image_pc_key", nullable = false, length = 255)
    private String imagePcKey;

    @Column(name = "image_mobile_key", nullable = false, length = 255)
    private String imageMobileKey;

    @Column(name = "alt_text", nullable = false, length = 200)
    private String altText = "";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected SectionMedia() {
    }

    public static SectionMedia of(String sectionKey) {
        SectionMedia m = new SectionMedia();
        m.sectionKey = sectionKey;
        return m;
    }

    public void change(String pcKey, String mobileKey, String altText) {
        if (pcKey == null || pcKey.isBlank()) {
            throw new IllegalArgumentException("PC 이미지를 올려주세요.");
        }
        if (mobileKey == null || mobileKey.isBlank()) {
            throw new IllegalArgumentException("모바일 이미지를 올려주세요.");
        }
        this.imagePcKey = pcKey;
        this.imageMobileKey = mobileKey;
        this.altText = altText == null ? "" : altText;
        this.updatedAt = Instant.now();
    }

    public String getSectionKey()     { return sectionKey; }
    public String getImagePcKey()     { return imagePcKey; }
    public String getImageMobileKey() { return imageMobileKey; }
    public String getAltText()        { return altText; }
    public Instant getUpdatedAt()     { return updatedAt; }
}
