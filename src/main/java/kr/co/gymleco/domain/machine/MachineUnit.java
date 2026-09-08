package kr.co.gymleco.domain.machine;

import jakarta.persistence.*;
import kr.co.gymleco.domain.product.Product;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "machine_unit")
public class MachineUnit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false, length = 60)
    private String serial;
    @Column(name = "serial_norm", nullable = false, length = 60)
    private String serialNorm;
    @Column(name = "made_year")
    private Short madeYear;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MachineUnitStatus status = MachineUnitStatus.REGISTERED;
    // V9 에서 center_id 는 machine_owner 로 자리를 옮겼다.
    // 소유는 «업체» 가 갖고, 그 업체가 공식 헬스장이기도 하면 거기서 잇는다.
    @Column(name = "owner_id")
    private Long ownerId;
    @Column(name = "sold_at")        private LocalDate soldAt;
    @Column(name = "installed_at")   private LocalDate installedAt;
    @Column(name = "warranty_until") private LocalDate warrantyUntil;
    @Column(nullable = false, length = 300)
    private String note = "";
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected MachineUnit(){}
    public static MachineUnit register(Product product, String serial, Short madeYear){
        String norm = SerialNormalizer.normalize(serial);
        if(norm.isEmpty()){
            throw new IllegalArgumentException("일렬번호가 비어 있습니다.");
        }
        MachineUnit u = new MachineUnit();
        u.product = product;
        u.serial = serial.trim();
        u.serialNorm = norm;
        u.madeYear = madeYear;
        return u;
    }
    public void changeStatus(MachineUnitStatus next, String note){
        this.status = next;
        this.note = note == null ? "" : note;
        touch();
    }
    /**
     * 판매 기록.
     *
     * DB 가 「상태가 SOLD 이상이면 업체가 반드시 있어야 한다」 를 막고 있다.
     * 여기서도 같은 것을 막아 둔다 — 제약 위반 예외는 어디서 잘못됐는지
     * 말해 주지 않는다.
     */
    public void markSold(Long ownerId, LocalDate soldAt, LocalDate warrantyUntil){
        if(ownerId == null){
            throw new IllegalArgumentException("판매하려면 업체를 지정해야 합니다.");
        }
        this.ownerId = ownerId;
        this.soldAt = soldAt;
        this.warrantyUntil = warrantyUntil;
        this.status = MachineUnitStatus.SOLD;
        touch();
    }

    public void markInstalled(LocalDate installedAt){
        if(soldAt != null && installedAt != null && installedAt.isBefore(soldAt)){
            throw new IllegalArgumentException("설치일이 판매일보다 앞설 수 없습니다.");
        }
        this.installedAt = installedAt;
        this.status = MachineUnitStatus.INSTALLED;
        touch();
    }
    private void touch(){this.updatedAt = Instant.now();}
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSerial() { return serial; }
    public String getSerialNorm() { return serialNorm; }
    public Short getMadeYear() { return madeYear; }
    public MachineUnitStatus getStatus() { return status; }
    public Long getOwnerId() { return ownerId; }
    public LocalDate getSoldAt() { return soldAt; }
    public LocalDate getInstalledAt() { return installedAt; }
    public LocalDate getWarrantyUntil() { return warrantyUntil; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
