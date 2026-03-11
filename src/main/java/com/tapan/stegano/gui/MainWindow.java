package com.tapan.stegano.gui;

import com.tapan.stegano.core.ImageProcessor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Main application window — dark themed, two-tab Swing GUI.
 * Tab 1: Encode  — hide a message inside an image
 * Tab 2: Decode  — extract a hidden message from an image
 */
public class MainWindow extends JFrame {

    // ── Theme ─────────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18,  18,  24);
    private static final Color BG_PANEL     = new Color(28,  28,  38);
    private static final Color BG_INPUT     = new Color(38,  38,  52);
    private static final Color ACCENT       = new Color(99,  179, 237);
    private static final Color ACCENT_HOVER = new Color(144, 205, 244);
    private static final Color SUCCESS      = new Color(72,  199, 142);
    private static final Color ERROR        = new Color(252, 100, 100);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 240);
    private static final Color TEXT_MUTED   = new Color(140, 140, 160);
    private static final Color BORDER       = new Color(55,  55,  75);

    private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font F_LABEL  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font F_MONO   = new Font("Consolas", Font.PLAIN, 13);

    private final ImageProcessor processor = new ImageProcessor();

    public MainWindow() {
        setTitle("Stegano — Image Steganography");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 600);
        setMinimumSize(new Dimension(680, 540));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(18, 28, 18, 28)
        ));

        JLabel title = new JLabel("🔒  Stegano");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Hide secrets inside images — LSB Steganography");
        sub.setFont(F_SMALL);
        sub.setForeground(TEXT_MUTED);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.add(title);
        col.add(Box.createVerticalStrut(4));
        col.add(sub);
        p.add(col, BorderLayout.WEST);
        return p;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(F_LABEL);
        tabs.addTab("  📥  Encode  ", buildEncodeTab());
        tabs.addTab("  📤  Decode  ", buildDecodeTab());
        return tabs;
    }

    // ── Encode Tab ────────────────────────────────────────────────────────────

    private JPanel buildEncodeTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Source image row
        JTextField srcField     = placeholder("Select a PNG or BMP image...");
        JButton    srcBtn       = smallButton("Browse");
        JLabel     capacityLbl  = mutedLabel(" ");

        srcBtn.addActionListener(e -> {
            File f = pickImage(false);
            if (f == null) return;
            srcField.setText(f.getAbsolutePath());
            srcField.setForeground(TEXT_PRIMARY);
            try {
                int cap = processor.getCapacity(f.getAbsolutePath());
                capacityLbl.setText("  ✔  Capacity: ~" + cap + " characters");
                capacityLbl.setForeground(SUCCESS);
            } catch (Exception ex) {
                capacityLbl.setText("  ✘  Could not read image");
                capacityLbl.setForeground(ERROR);
            }
        });

        // Message area
        JTextArea msgArea = new JTextArea(6, 40);
        msgArea.setFont(F_MONO);
        msgArea.setBackground(BG_INPUT);
        msgArea.setForeground(TEXT_PRIMARY);
        msgArea.setCaretColor(ACCENT);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane msgScroll = darkScroll(msgArea);
        msgScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Output image row
        JTextField outField = placeholder("Choose where to save the output image...");
        JButton    outBtn   = smallButton("Browse");

        outBtn.addActionListener(e -> {
            File f = pickImage(true);
            if (f == null) return;
            String path = f.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png") && !path.toLowerCase().endsWith(".bmp")) {
                path += ".png";
            }
            outField.setText(path);
            outField.setForeground(TEXT_PRIMARY);
        });

        // Status + Encode button
        JLabel  statusLbl = new JLabel(" ");
        statusLbl.setFont(F_NORMAL);
        statusLbl.setAlignmentX(LEFT_ALIGNMENT);

        JButton encodeBtn = accentButton("  🔒  Encode Message  ");
        encodeBtn.addActionListener(e -> {
            String src = srcField.getText().trim();
            String msg = msgArea.getText().trim();
            String out = outField.getText().trim();

            if (src.startsWith("Select") || src.isEmpty()) {
                status(statusLbl, "⚠  Please select a source image.", ERROR); return;
            }
            if (msg.isEmpty()) {
                status(statusLbl, "⚠  Please enter a message to hide.", ERROR); return;
            }
            if (out.startsWith("Choose") || out.isEmpty()) {
                status(statusLbl, "⚠  Please choose an output path.", ERROR); return;
            }
            try {
                processor.encode(src, out, msg);
                status(statusLbl, "✔  Encoded successfully → " + out, SUCCESS);
            } catch (Exception ex) {
                status(statusLbl, "✘  " + ex.getMessage(), ERROR);
            }
        });

        // Assemble
        p.add(sectionLabel("Source Image"));
        p.add(gap(6));
        p.add(row(srcField, srcBtn));
        p.add(capacityLbl);
        p.add(gap(18));
        p.add(sectionLabel("Secret Message"));
        p.add(gap(6));
        p.add(msgScroll);
        p.add(gap(18));
        p.add(sectionLabel("Output Image  (PNG / BMP)"));
        p.add(gap(6));
        p.add(row(outField, outBtn));
        p.add(gap(22));
        p.add(encodeBtn);
        p.add(gap(12));
        p.add(statusLbl);
        return p;
    }

    // ── Decode Tab ────────────────────────────────────────────────────────────

    private JPanel buildDecodeTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Stego image row
        JTextField imgField = placeholder("Select the stego image (PNG or BMP)...");
        JButton    imgBtn   = smallButton("Browse");

        imgBtn.addActionListener(e -> {
            File f = pickImage(false);
            if (f == null) return;
            imgField.setText(f.getAbsolutePath());
            imgField.setForeground(TEXT_PRIMARY);
        });

        // Result area
        JTextArea resultArea = new JTextArea(9, 40);
        resultArea.setFont(F_MONO);
        resultArea.setBackground(BG_INPUT);
        resultArea.setForeground(TEXT_MUTED);
        resultArea.setCaretColor(ACCENT);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        resultArea.setText("Extracted message will appear here...");
        JScrollPane resultScroll = darkScroll(resultArea);
        resultScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        // Status
        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(F_NORMAL);
        statusLbl.setAlignmentX(LEFT_ALIGNMENT);

        // Buttons
        JButton decodeBtn = accentButton("  🔓  Decode Message  ");
        JButton copyBtn   = smallButton("📋  Copy");

        decodeBtn.addActionListener(e -> {
            String img = imgField.getText().trim();
            if (img.startsWith("Select") || img.isEmpty()) {
                status(statusLbl, "⚠  Please select a stego image.", ERROR); return;
            }
            try {
                String msg = processor.decode(img);
                resultArea.setForeground(SUCCESS);
                resultArea.setText(msg);
                status(statusLbl, "✔  Message extracted successfully!", SUCCESS);
            } catch (Exception ex) {
                resultArea.setForeground(ERROR);
                resultArea.setText(ex.getMessage());
                status(statusLbl, "✘  Decode failed.", ERROR);
            }
        });

        copyBtn.addActionListener(e -> {
            String txt = resultArea.getText();
            if (!txt.isEmpty() && !txt.startsWith("Extracted")) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(txt), null);
                status(statusLbl, "✔  Copied to clipboard!", SUCCESS);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.add(decodeBtn);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(copyBtn);

        // Assemble
        p.add(sectionLabel("Stego Image"));
        p.add(gap(6));
        p.add(row(imgField, imgBtn));
        p.add(gap(18));
        p.add(sectionLabel("Extracted Message"));
        p.add(gap(6));
        p.add(resultScroll);
        p.add(gap(12));
        p.add(btnRow);
        p.add(gap(12));
        p.add(statusLbl);
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(BG_PANEL);
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        JLabel note = new JLabel(
                "⚠  Always save output as PNG or BMP — JPEG compression destroys hidden data."
        );
        note.setFont(F_SMALL);
        note.setForeground(TEXT_MUTED);
        p.add(note);
        return p;
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField placeholder(String text) {
        JTextField f = new JTextField(text);
        f.setFont(F_NORMAL);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_MUTED);
        f.setCaretColor(ACCENT);
        f.setEditable(false);
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return f;
    }

    private JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setFont(F_NORMAL);
        b.setBackground(BG_INPUT);
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(ACCENT); }
            public void mouseExited(MouseEvent e)  { b.setForeground(TEXT_PRIMARY); }
        });
        return b;
    }

    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(ACCENT);
        b.setForeground(BG_DARK);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { b.setBackground(ACCENT); }
        });
        return b;
    }

    private JPanel row(JTextField field, JButton btn) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.add(field, BorderLayout.CENTER);
        p.add(btn,   BorderLayout.EAST);
        return p;
    }

    private JScrollPane darkScroll(JTextArea area) {
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.setBackground(BG_INPUT);
        sp.getViewport().setBackground(BG_INPUT);
        sp.setAlignmentX(LEFT_ALIGNMENT);
        return sp;
    }

    private Component gap(int size) {
        return Box.createVerticalStrut(size);
    }

    private void status(JLabel label, String msg, Color color) {
        label.setText(msg);
        label.setForeground(color);
    }

    private File pickImage(boolean save) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image Files (PNG, BMP)", "png", "bmp"));
        int result = save ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
        return result == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null;
    }
}