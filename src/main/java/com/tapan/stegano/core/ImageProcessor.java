package com.tapan.stegano.core;

import com.tapan.stegano.algorithms.LSBSteganography;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Handles all image I/O and bridges the GUI with the LSB algorithm.
 * The GUI talks to this class only — it never touches LSBSteganography directly.
 */
public class ImageProcessor {

    private final LSBSteganography lsb = new LSBSteganography();

    /**
     * Encodes a secret message into an image and saves the result.
     *
     * @param inputPath  path to the original image (PNG or BMP)
     * @param outputPath path to save the stego image (PNG or BMP)
     * @param message    the secret text to hide
     * @throws IOException              on read/write failure
     * @throws IllegalArgumentException if message is too large
     */
    public void encode(String inputPath, String outputPath, String message)
            throws IOException, IllegalArgumentException {

        BufferedImage image = readImage(inputPath);
        lsb.embed(image, message);
        writeImage(image, outputPath);
    }

    /**
     * Decodes a secret message from a stego image.
     *
     * @param imagePath path to the stego image
     * @return the hidden message
     * @throws IOException              on read failure
     * @throws IllegalArgumentException if no message found
     */
    public String decode(String imagePath)
            throws IOException, IllegalArgumentException {

        BufferedImage image = readImage(imagePath);
        return lsb.extract(image);
    }

    /**
     * Returns the max number of characters that can be hidden in the image.
     *
     * @param imagePath path to the image
     * @return character capacity (approx — assumes ASCII)
     * @throws IOException if image cannot be read
     */
    public int getCapacity(String imagePath) throws IOException {
        BufferedImage image = readImage(imagePath);
        return lsb.getMaxBytes(image);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private BufferedImage readImage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File not found: " + path);
        }
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IOException(
                    "Cannot read image: " + path + "\n" +
                            "Supported formats: PNG, BMP"
            );
        }
        return img;
    }

    private void writeImage(BufferedImage image, String path) throws IOException {
        String fmt = path.toLowerCase().endsWith(".bmp") ? "BMP" : "PNG";
        boolean success = ImageIO.write(image, fmt, new File(path));
        if (!success) {
            throw new IOException("Failed to write image: " + path);
        }
    }
}