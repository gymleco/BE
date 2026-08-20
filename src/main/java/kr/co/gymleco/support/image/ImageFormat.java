package kr.co.gymleco.support.image;

public enum ImageFormat {

    JPEG("image/jpeg", "jpg", new int[]{0xFF, 0xD8, 0xFF}),
    PNG("image/png", "png", new int[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),

    WEBP("image/webp", "webp", new int[]{0x52, 0x49, 0x46, 0x46});

    private final String mimeType;
    private final String extension;
    private final int[] magic;

    ImageFormat(String mimeType, String extension, int[] magic) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.magic = magic;
    }

    public String mimeType()  { return mimeType; }
    public String extension() { return extension; }

    public boolean matches(byte[] head) {
        if (head.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if ((head[i] & 0xFF) != magic[i]) {
                return false;
            }
        }
        // WebP 는 8~11 바이트가 "WEBP" 인지까지 봐야 한다
        if (this == WEBP) {
            return head.length >= 12
                && (head[8] & 0xFF) == 0x57   // W
                && (head[9] & 0xFF) == 0x45   // E
                && (head[10] & 0xFF) == 0x42  // B
                && (head[11] & 0xFF) == 0x50; // P
        }
        return true;
    }

    public static ImageFormat detect(byte[] head) {
        for (ImageFormat format : values()) {
            if (format.matches(head)) {
                return format;
            }
        }
        return null;
    }
}
