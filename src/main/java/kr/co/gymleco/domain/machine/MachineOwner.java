package kr.co.gymleco.domain.machine;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "machine_owner")
public class MachineOwner {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(nullable = false, length = 200)
    private String address = "";
    @Column(nullable = false, length = 40)
    private String region = "";
    @Column(name = "contact_name", nullable = false, length = 60)
    private String contactName= "";
    @Column(name = "phone_encrypted", nullable = false, columnDefinition = "text")
    private String phoneEncrypted = "";
    @Column(name = "phone_blind_index")
    private byte[] phoneBlindIndex;
    @Column(name = "center_id")
    private Long centerId;
    @Column(nullable =false, length = 300)
    private String note = "";
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected MachineOwner(){}
    public static MachineOwner of(String name, String region){
        MachineOwner o = new MachineOwner();
        o.name = name.trim();
        o.region = region == null ? "" : region.trim();
        return o;
    }
    public void applyPhone(String encrypted, byte[] blindIndex){
        this.phoneEncrypted = encrypted == null ? "" : encrypted;
        this.phoneBlindIndex = blindIndex;
        touch();
    }
    public void edit(String name, String region, String address, String contactName, String note){
        this.name = name.trim();
        this.region = nz(region);
        this.address = nz(address);
        this.contactName = nz(contactName);
        this.note = nz(note);
        touch();
    }
    public void linkCenter(Long centerId){this.centerId = centerId; touch();}
    private static String nz(String v){return v == null ? "" : v.trim();}
    private void touch(){this.updatedAt = Instant.now();}
    public Long getId(){return id;}
    public String getName(){return name;}
    public String getRegion(){return region;}
    public String getAddress(){return address;}
    public String getContactName(){return contactName;}
    public String getPhoneEncrypted(){return phoneEncrypted;}
    public byte[] getPhoneBlindIndex(){return phoneBlindIndex;}
    public Long getCenterId(){return centerId;}
    public String getNote(){return note;}
}
