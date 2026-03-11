package com.tapan.stegano.algorithms;

import java.awt.image.BufferedImage;

/**
 * LSB (Least Significant Bit) Steganography Algorithm.
 *
 * Each pixel has R, G, B channels — each is 8 bits (0-255).
 * We replace the last bit of each channel with 1 bit of our message.
 * 3 bits per pixel. Change is invisible to the human eye.
 *
 * Example:
 *   Original R = 11001010
 *   Message bit = 1
 *   New R      = 11001011   ← only last bit changed, colour barely changes
 */
public class LSBSteganography {

    // Marks where the hidden message ends inside the image
    public static final String DELIMITER = "$$END$$";

    /**
     * Embeds message bits into the image pixels.
     * Modifies the image in-place.
     *
     * @param image   the carrier image (will be modified)
     * @param message the secret text to hide
     * @throws IllegalArgumentException if message is too large for the image
     */
    public void embed(BufferedImage image, String message) throws IllegalArgumentException {
        String fullMessage = message + DELIMITER;
        byte[] bytes;
        try {
            bytes = fullMessage.getBytes("UTF-8");
        } catch (Exception e) {
            bytes = fullMessage.getBytes();
        }

        int maxBytes = getMaxBytes(image);
        if (bytes.length > maxBytes) {
            throw new IllegalArgumentException(
                    "Message is too large for this image!\n" +
                            "Max capacity : " + maxBytes + " bytes\n" +
                            "Message size : " + bytes.length + " bytes\n\n" +
                            "Use a larger image or shorten the message."
            );
        }

        int[] bits     = toBits(bytes);
        int   bitIndex = 0;
        int   total    = bits.length;
        int   width    = image.getWidth();
        int   height   = image.getHeight();

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= total) break outer;

                int pixel = image.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8)  & 0xFF;
                int b =  pixel        & 0xFF;

                if (bitIndex < total) r = (r & 0xFE) | bits[bitIndex++];
                if (bitIndex < total) g = (g & 0xFE) | bits[bitIndex++];
                if (bitIndex < total) b = (b & 0xFE) | bits[bitIndex++];

                image.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    /**
     * Extracts a hidden message from the image pixels.
     *
     * @param image the stego image to read from
     * @return the extracted secret message
     * @throws IllegalArgumentException if no hidden message is found
     */
    public String extract(BufferedImage image) throws IllegalArgumentException {
        int     width  = image.getWidth();
        int     height = image.getHeight();
        StringBuilder bits = new StringBuilder(width * height * 3);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                bits.append((pixel >> 16) & 1); // R LSB
                bits.append((pixel >> 8)  & 1); // G LSB
                bits.append( pixel        & 1); // B LSB
            }
        }

        String extracted = bitsToString(bits.toString());
        int delimIdx = extracted.indexOf(DELIMITER);

        if (delimIdx == -1) {
            throw new IllegalArgumentException(
                    "No hidden message found in this image.\n" +
                            "Make sure this image was encoded using Stegano."
            );
        }

        return extracted.substring(0, delimIdx);
    }

    /**
     * Returns max number of bytes that can be hidden in this image.
     */
    public int getMaxBytes(BufferedImage image) {
        return (image.getWidth() * image.getHeight() * 3) / 8
                - DELIMITER.getBytes().length;
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private int[] toBits(byte[] bytes) {
        int[] bits = new int[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = (bytes[i] >> (7 - j)) & 1;
            }
        }
        return bits;
    }

    private String bitsToString(String bits) {
        // Collect raw bytes first, then decode as UTF-8
        // This correctly handles multi-byte characters like emojis
        java.util.List<Byte> byteList = new java.util.ArrayList<>();
        for (int i = 0; i + 8 <= bits.length(); i += 8) {
            int code = Integer.parseInt(bits.substring(i, i + 8), 2);
            if (code == 0) break;
            byteList.add((byte) code);
        }
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; i++) bytes[i] = byteList.get(i);
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return new String(bytes);
        }
    }
}