package kr.co.gymleco.support.image;

public enum ImageRendition {
    THUMB(400),
    CARD(800),
    FULL(1600);
    private final int maxWidth;
    ImageRendition(int maxWidth){
        this.maxWidth = maxWidth;
    }
    public int maxWidth() {return maxWidth;}
    public String fileName(String extension){return maxWidth + "." + extension;
    }
}
