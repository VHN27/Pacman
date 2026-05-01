# 🕹️ Pacman Remastered

[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Gradle-blue.svg)](https://gradle.org/)
[![UI](https://img.shields.io/badge/UI-JavaFX-red.svg)](https://openjfx.io/)

> A high-performance, modular recreation of the classic Pacman, featuring procedural map generation and local multiplayer. Developed by a cross-functional Agile team of 5 focusing on modern software design patterns.

[📺 **Watch the Video Demo (FR)**](https://www.youtube.com/watch?v=546oMnV9g8k)

---

## 🏗️ Architecture & Design
The core engine is built on the **Model-View-Controller (MVC)** design pattern, ensuring a strict separation of concerns for high code maintainability and scalability.

*   **Model:** Manages game state, entity coordinates, and collision physics independently of the UI.
*   **View:** An event-driven GUI built with **JavaFX**, handling high-frame-rate rendering and custom animations.
*   **Controller:** Bridges user input with game state updates and logic validation.

---

## 🚀 Key Technical Features

### ♾️ Infinity Mode & Procedural Generation
Replaced static layouts with a custom **procedural generation algorithm** to create random, navigable game maps in real-time, significantly increasing algorithmic complexity and replayability.

### 👥 Local Multiplayer Engine
Engineered a local multiplayer framework that handles concurrent entity states, synchronized movement, and optimized collision detection between multiple players and AI ghosts.

### 🛠️ Build Automation
Leveraged **Gradle** for efficient dependency management and lifecycle automation, ensuring consistent compilation environments across the 5-person development team.

---

## 💻 Tech Stack & Tools
*   **Language:** Java (OOP)
*   **GUI Framework:** JavaFX
*   **Build Tool:** Gradle
*   **Methodology:** Agile / Scrum
*   **Version Control:** Git

---

## ⚙️ Installation & Usage

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/pacman-java.git](https://github.com/yourusername/pacman-java.git)
2. **Build with Gradle**
   ```bash
   ./gradlew build
4. **Run the Application:**
   ```bash
   ./gradlew run
