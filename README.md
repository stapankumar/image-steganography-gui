# 🔒 image-steganography-gui

> A Java-based image steganography tool with a GUI for hiding and extracting secret messages inside images.

---

## 🔧 What is Steganography?

Steganography is the practice of hiding secret information inside an ordinary file — in this case, an image. Unlike encryption (which makes data unreadable), steganography hides the fact that a secret even exists.

This tool uses the **LSB (Least Significant Bit)** technique — it replaces the last bit of each RGB channel in every pixel with bits of your secret message. The visual change is completely invisible to the human eye.

```
Original pixel R:  1 1 0 0 1 0 1 0
Message bit:                       1
New pixel R:       1 1 0 0 1 0 1 1  ← only last bit changed, colour barely changes
```

---

## ✨ Features

- 📥 **Encode** — hide any text message inside a PNG or BMP image
- 📤 **Decode** — extract the hidden message from a stego image
- 📋 **Copy to clipboard** — one click copy of extracted message
- 📊 **Capacity indicator** — shows how many characters the selected image can hold
- 🎨 **Dark themed GUI** — clean, professional Swing interface
- 🌐 **UTF-8 support** — works with emojis and special characters

---

## 🏗️ Project Structure

```
src/main/java/com/tapan/stegano/
├── Main.java                        ← Entry point
├── algorithms/
│   └── LSBSteganography.java        ← Core LSB encode/decode logic
├── core/
│   └── ImageProcessor.java          ← Image I/O, bridges GUI ↔ algorithm
└── gui/
    └── MainWindow.java              ← Swing GUI (two tabs: Encode / Decode)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK — [Download here](https://adoptium.net/)
- IntelliJ IDEA (recommended) — [Download here](https://www.jetbrains.com/idea/)

### Run

```bash
git clone https://github.com/stapankumar/image-steganography-gui.git
```

1. Open the project in IntelliJ IDEA
2. Navigate to `src/main/java/com/tapan/stegano/Main.java`
3. Click the green ▶ button to run

---

## 🖥️ How to Use

### Encode (Hide a message)
1. Open the **Encode** tab
2. Click **Browse** and select a PNG or BMP image
3. Check the capacity indicator — it shows how many characters the image can hold
4. Type your secret message in the text area
5. Click **Browse** under Output Image and choose where to save (give it a new name)
6. Click **🔒 Encode Message**

### Decode (Extract a message)
1. Open the **Decode** tab
2. Click **Browse** and select the stego image (the output from encoding)
3. Click **🔓 Decode Message**
4. Your secret message appears — click **📋 Copy** to copy it

---

## ⚠️ Important

- Always save the output as **PNG or BMP**
- **Never use JPEG** — JPEG compression modifies pixel values and destroys the hidden data
- The output image looks visually identical to the original

---

## 🛠️ Tech Stack

| Tool | Purpose |
|------|---------|
| **Java 21** | Core language |
| **Java Swing** | GUI framework |
| **ImageIO** | Image read/write (built-in Java) |
| **LSB Algorithm** | Steganography technique |

---

## ✅ Test Results

Encode and decode tested end-to-end with PNG images.
Hidden messages including emojis extracted correctly. ✔

---

## 🗺️ Roadmap

- [x] LSB encode / decode
- [x] PNG and BMP support
- [x] Dark themed GUI
- [ ] Password / key protection
- [ ] Drag and drop image support
- [ ] Runnable JAR release

---

## 👨‍💻 Author

**Tapan** — [@stapankumar](https://github.com/stapankumar)

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.