package kr.co.gymleco.support.image;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ImageProcessor {
    private static final int HEADER_BYTES = 12;
    private static final long MAX_PIXELS = 50_000_000L;
    private static final double JPEG_QUALITY = 0.85;
    public record Processed(
        ImageFormat sourceFormat,
        String outputExtension,
        Map<ImageRendition, byte[]> renditions
    ){}
    public Processed process(byte[] original){
        if(original.length < HEADER_BYTES){
            throw new InvalidImageException("이미지 파일이 아닙니다.");
        }
        byte[] head = new byte[HEADER_BYTES];
        System.arraycopy(original, 0, head, 0, HEADER_BYTES);
        ImageFormat format = ImageFormat.detect(head);
        if(format == null){
            throw new InvalidImageException("JPG, PNG, WebP 파일만 올릴 수 있습니다.");
        }
        BufferedImage source;
        try{
            source = ImageIO.read(new ByteArrayInputStream(original));
        }catch (IOException e){
            throw new InvalidImageException("이미지를 읽을 수 없습니다.");
        }
        if (source == null){
            throw new InvalidImageException("이미지가 손상되었거나 지원하지 않는 형식입니다.");
        }
        long pixels = (long) source.getWidth() * source.getHeight();
        if(pixels > MAX_PIXELS){
            throw new InvalidImageException("이미지 해상도가 너무 큽니다. 가로·세로 합쳐 5천만 화소 이하로 올려주세요.");
        }
        boolean hasAlpha = source.getColorModel().hasAlpha();
        String ext = hasAlpha ? "png" : "jpg";
        Map<ImageRendition, byte[] > out = new EnumMap<>(ImageRendition.class);
        for (ImageRendition rendition : ImageRendition.values()) {
            out.put(rendition, encode(source, rendition, hasAlpha));
        }
        return new Processed(format, ext, out);
    }
    private byte[] encode(BufferedImage source, ImageRendition rendition, boolean hasAlpha){
        try{
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            /*
             * ★ 원본보다 키우지 않는다.
             *
             * Thumbnailator 의 size() 는 기본적으로 확대한다.
             * 1200px 사진을 1600px 로 늘리면 흐려지기만 하고 용량만 커진다.
             * 목표 크기를 원본의 긴 변으로 잘라 확대를 막는다.
             *
             * 작은 원본에서는 여러 렌디션이 같은 크기가 될 수 있는데
             * 그건 정상이다 — 없는 화질을 만들어낼 수는 없다.
             */
            int target = Math.min(rendition.maxWidth(),
                    Math.max(source.getWidth(), source.getHeight()));

            Thumbnails.Builder<BufferedImage> builder =
                    Thumbnails.of(source).size(target, target).keepAspectRatio(true);
            if(hasAlpha){
                builder.outputFormat("png");
            }else{
                builder.outputFormat("jpg").outputQuality(JPEG_QUALITY);
            }
            builder.toOutputStream(buffer);
            return buffer.toByteArray();
        }catch(IOException e) {
            throw new InvalidImageException("이미지 변환에 실패했습니다.");
        }
    }
}
