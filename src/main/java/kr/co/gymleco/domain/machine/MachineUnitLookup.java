package kr.co.gymleco.domain.machine;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "machine_unit_lookup")
public class MachineUnitLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "machine_unit_id")
    private Long machineUnitId;
    @Column(name = "serial_tried", nullable = false, length = 60)
    private String serialTried;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LookupResult result;
    @Column(name = "source_ip", length = 45)
    private String sourceIp;
    @Column(name = "user_agent", nullable = false, length = 300)
    private String userAgent = "";
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected MachineUnitLookup(){}
    private static String trim(String v, int max){
        if(v == null) return "";
        return v.length() <= max ? v : v.substring(0, max);
    }
    public static MachineUnitLookup record(Long machineUnitId, String serialTried, LookupResult result, String sourceIp, String userAgent){
        MachineUnitLookup l = new MachineUnitLookup();
        l.machineUnitId = machineUnitId;
        l.serialTried = trim(serialTried, 60);
        l.result = result;
        l.sourceIp = sourceIp;
        l.userAgent = trim(userAgent == null ? "" : userAgent, 300);
        return l;
    }
    public Long getId() {return id;}
    public Long getMachineUnitId(){return machineUnitId;}
    public String getSerialTried(){return serialTried;}
    public LookupResult getResult(){return result;}
    public String getSourceIp(){return sourceIp;}
    public String getUserAgent(){return userAgent;}
    public Instant getCreatedAt(){return createdAt;}
}
