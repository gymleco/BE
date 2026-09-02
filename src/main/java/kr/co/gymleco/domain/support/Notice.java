package kr.co.gymleco.domain.support;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, columnDefinition = "text")
    private String body = "";
    @Column(nullable = false)
    private boolean pinned = false;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(nullable = false)
    private boolean visible = false;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected Notice() {
    }
    public static Notice of(String title) {
        Notice n = new Notice();
        n.title = title;
        return n;
    }

    public void edit(String title, String body, boolean pinned) {
        this.title = title;
        this.body = body == null ? "" : body;
        this.pinned = pinned;
        touch();
    }

    public void changeVisibility(boolean visible) {
        if (visible && publishedAt == null) {
            publishedAt = Instant.now();
        }
        this.visible = visible;
        touch();
    }
    public void changePublishedAt(Instant at) {
        if (at == null && visible) {
            throw new IllegalArgumentException("공개 중인 공지는 발행일을 비울 수 없습니다.");
        }
        this.publishedAt = at;
        touch();
    }
    private void touch() { this.updatedAt = Instant.now(); }
    public Long getId()           { return id; }
    public String getTitle()      { return title; }
    public String getBody()       { return body; }
    public boolean isPinned()     { return pinned; }
    public Instant getPublishedAt() { return publishedAt; }
    public boolean isVisible()    { return visible; }
    public Instant getUpdatedAt() { return updatedAt; }
}
