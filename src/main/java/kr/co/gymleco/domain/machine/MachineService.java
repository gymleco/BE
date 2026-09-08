package kr.co.gymleco.domain.machine;


import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "machine_service")
public class MachineService {
    /*
     * ★ 이름이 DB 의 ck_machine_service_kind 와 «글자 단위로» 같아야 한다.
     *   EnumType.STRING 이라 이 이름이 그대로 저장된다.
     */
    public enum Kind{
        INSPECTION, //정기점검
        REPAIR,//수리
        PART,//부품 교체
        MOVE,//이전 설치
        CLAIM //보증 청구
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_unit_id", nullable = false)
    private Long machineUnitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Kind kind;

    @Column(name = "happened_on", nullable = false)
    private LocalDate happenedOn;

    @Column(nullable = false, length = 300)
    private String summary = "";

    @Column(name = "cost_krw")
    private Integer costKrw;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected MachineService() {
    }

    public static MachineService of(Long machineUnitId, Kind kind,
                                    LocalDate happenedOn, String summary, Integer costKrw) {
        if (costKrw != null && costKrw < 0) {
            throw new IllegalArgumentException("비용은 0 이상이어야 합니다.");
        }
        MachineService s = new MachineService();
        s.machineUnitId = machineUnitId;
        s.kind = kind;
        s.happenedOn = happenedOn;
        s.summary = summary == null ? "" : summary.trim();
        s.costKrw = costKrw;
        return s;
    }

    public Long getId() { return id; }
    public Long getMachineUnitId() { return machineUnitId; }
    public Kind getKind() { return kind; }
    public LocalDate getHappenedOn() { return happenedOn; }
    public String getSummary() { return summary; }
    public Integer getCostKrw() { return costKrw; }
    public Instant getCreatedAt() { return createdAt; }
}
