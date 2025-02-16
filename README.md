# Face-X

<p align="center">
  <img src="https://github.com/user-attachments/assets/49d53184-b7dc-4d7d-8f16-db07578605f9" alt="Face-X Logo" width="100%" ">
</p>


## Overview


Face-X is a **modular and high-performance face recognition system** built for Android. It supports multiple **face detection and recognition models**, allowing seamless switching between **OpenCV, MediaPipe, ML Kit, and LiteRT**.  

The app is **optimized for real-time inference**, featuring **low-latency processing, dynamic model selection, performance analytics, and interactive UI effects**.

---

## 🚀 Features

### 🔹 Face Detection & Recognition  
✅ Supports **OpenCV, MediaPipe, ML Kit, and LiteRT** for detection and recognition.  
✅ **Dynamic model switching** for benchmarking performance.  
✅ Achieves **real-time inference** with GPU acceleration (~10ms recognition, ~15ms detection).  

#### 🎥 Example: Face Detection Lateny in Action  
https://github.com/user-attachments/assets/dd1ee8fc-962c-4d3f-b97d-a3d8ab169ace

[![Example Video]](https://www.facebook.com/reel/1137305521170770)

**👉 My detection (orange box) used in the test video above.**  

#### 🎥 Face Similarity Example  
[![Will Ferrell vs Chad Smith](https://github.com/user-attachments/assets/b274bd9d-6f4c-48c1-99e8-3b717429d97f)](https://github.com/user-attachments/assets/b274bd9d-6f4c-48c1-99e8-3b717429d97f)  

"Chad Smith looks **82% similar** to Will Ferrell before increasing the threshold!"  

[![Hussein Fahmy vs Mustafa Fahmy](https://github.com/user-attachments/assets/fd4971be-863a-450f-ac14-b8f73060fe7d)](https://github.com/user-attachments/assets/fd4971be-863a-450f-ac14-b8f73060fe7d)  

"Hussein Fahmy and his brother Mustafa Fahmy, Egyptian actors, **famously resemble each other.**"

---

### 📊 Real-Time Performance Metrics  
✅ Displays **live processing times** for face detection & recognition.  
✅ **Dynamic line chart updates** in real-time.  
✅ **Skip, pinpoint, and data recording** (up to **5 minutes** of tracking).  

#### 🎥 Performance Monitoring in Action  
[![Performance Metrics](https://github.com/user-attachments/assets/04769d13-a567-420d-a235-b2b6fff27502)](https://github.com/user-attachments/assets/04769d13-a567-420d-a235-b2b6fff27502)

---

### 🎆 Particlize Effect for UI  
✅ Unique **disintegration & assembly effects** for any Jetpack Compose UI component.  
✅ Smooth **animation transitions** for interactive UI elements.  

#### 🎥 Particlize Effect Demo  
[![Particlize Effect](https://github.com/user-attachments/assets/d5dbfbc0-9a48-4436-9f13-7b32ca6f112a)](https://github.com/user-attachments/assets/d5dbfbc0-9a48-4436-9f13-7b32ca6f112a)

---

### 📌 Pinned Footer in Bottom Sheet  
✅ Enables a **fixed footer in the bottom sheet** for persistent controls.  
✅ Enhances **usability and quick access** to settings or actions.  

#### 🎥 Pinned Footer UI Demo  
[![Pinned Footer](https://github.com/user-attachments/assets/a23ef584-8683-4015-b594-aa7f12b7f85c)](https://github.com/user-attachments/assets/a23ef584-8683-4015-b594-aa7f12b7f85c)


---

### 📌 Animated Framework Icons in Compose    

#### 🎥 Animated Icons Demo  
[![Animated Icons](https://github.com/user-attachments/assets/88db49ad-d400-4f65-9d41-2570f7ee163b)](https://github.com/user-attachments/assets/88db49ad-d400-4f65-9d41-2570f7ee163b)

---

### 🎨 Logo Design Thoughts  
> _"I chose the old TensorFlow logo because I got the idea to extract the **T-F-L** letters as it works on TensorFlow Lite. This was the hardest logo to design in Compose."_  

> _"The OpenCV animation is not very good. I had another idea, but it would take more time, and I have a lot of things to finish in the app."_

---

## 🔥 Upcoming Features  

We're continuously improving Face-X to provide more flexibility, control, and performance optimizations. Here’s what’s coming next:  

### 🔹 Advanced Model Customization  
✅ Ability to **add and configure your own face detection & recognition models**.  
✅ Support for **more model architectures**, including high-performance quantized models.  
✅ **Automatic model benchmarking** to suggest the best model for your device.  

### 🔹 Hardware & Performance Optimization  
✅ More **hardware control**, including **manual selection of CPU, GPU, and NPU**.  
✅ Advanced **device analytics**, including **memory usage, battery impact, and thermal throttling insights**.  
✅ Optimized **low-power mode** for extended usage on mobile devices.  

### 🔹 Expanded Device Control  
✅ Ability to **prioritize performance or efficiency** based on user preferences.  
✅ **Adaptive processing** to dynamically adjust processing power based on workload.  
✅ **Background processing mode** to allow passive face recognition while running other tasks.  

Stay tuned for these updates! 🚀  

## 🚀 Project Status: Experimental & Evolving  

Face-X is an **ongoing experimental project** where I continuously challenge myself to explore and refine ** Machine Learning integration and performance optimization techniques**.  

### 🔹 What This Project Represents  
✅ **A platform for innovation** – Experimenting with **cutting-edge ML models, hardware acceleration, and real-time image processing**.  
✅ **A continuous learning journey** – Exploring new architectures, optimizing inference performance, and refining system efficiency.  
✅ **Future-focused** – Expanding capabilities with **custom model support, enhanced hardware controls, and advanced system monitoring (battery, memory, CPU usage)**.  

This is not a finalized product but rather a **dynamic and evolving initiative**. If you're interested in collaboration, discussion, or feedback, feel free to connect! 🚀  

---

## ⚠️ Disclaimer  

Face-X is currently a **personal demo project** developed as a practice-driven exploration of modern face recognition technologies.  

🔸 **Not production-ready** – This project is a sandbox for testing models, architectures, and optimizations.  
🔸 **Actively evolving** – Features, performance metrics, and system integrations are subject to change as development progresses.  
🔸 **Independently developed** – Built as a solo initiative for technical growth and experimentation.  

I'm always open to discussions, ideas, and collaborations. Feel free to reach out if you're interested! 🚀  


## 🛠 Tech Stack  
- **Languages:** Kotlin  
- **UI:** Jetpack Compose
- **Camera & Image Processing**: CameraX
- **Machine Learning Integration with Android:** TensorFlow Lite (LiteRT), OpenCV, MediaPipe, ML Kit  
- **Architecture:** Clean Architecture,MVVM  
- **Concurrency:** Coroutines, Flow



