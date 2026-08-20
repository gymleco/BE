package kr.co.gymleco.domain.used;

import jakarta.persistence.*;

@Entity
@Table(name = "used_item_image")
public class UsedItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional  = false)
    @JoinColumn(name = "used_item_id", nullable = false)
    private UsedItem usedItem;
    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;
    @Column(name = "alt_text", nullable =false , length = 200)
    private String altText = "";
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    protected UsedItemImage(){
    }
    UsedItemImage(UsedItem usedItem, String imageKey, String altText, int sortOrder){
        this.usedItem = usedItem;
        this.imageKey = imageKey;
        this.altText = altText == null ? "" : altText;
        this.sortOrder = sortOrder;
    }
    public Long getId(){return id;}
    public String getImageKey(){return imageKey;}
    public String getAltText(){return altText;}
    public int getSortOrder(){return sortOrder;}
}
