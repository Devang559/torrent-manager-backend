# 🚀 Torrent Manager Backend

## 📌 Overview

A Spring Boot-based backend system that integrates with aria2 using JSON-RPC to manage torrent downloads programmatically. This project demonstrates real-world backend concepts like REST API design, external service integration, and asynchronous processing.

---

## ⚙️ Tech Stack

* Java
* Spring Boot
* Spring WebFlux (WebClient)
* aria2 (Download Engine)
* JSON-RPC

---

## 🔧 Features

* 📥 Add torrent via magnet link or URL
* ⏸️ Pause downloads
* ▶️ Resume downloads
* ❌ Remove downloads
* 📊 Fetch download status
* 🔌 Integration with external service (aria2)

---

## 🔌 How It Works

Client → Spring Boot API → aria2 (JSON-RPC) → Download Engine

---

## 🚀 API Endpoints

| Method | Endpoint | Description         |
| ------ | -------- | ------------------- |
| POST   | /add     | Add torrent         |
| POST   | /pause   | Pause download      |
| POST   | /resume  | Resume download     |
| GET    | /status  | Get download status |

---

## ▶️ Run Locally

### 1. Start aria2 RPC server

```bash
aria2c --enable-rpc --rpc-listen-all=true --rpc-allow-origin-all
```

### 2. Configure application

Rename:

```bash
application-example.properties → application.properties
```

Update values:

```properties
aria2.url=http://localhost:6800/jsonrpc
aria2.secret=your-secret
```

### 3. Run Spring Boot app

```bash
mvn spring-boot:run
```

---

## 📂 Project Structure

```
src/
 ├── config/
 ├── controller/
 ├── service/
 └── resources/
```

---

## ⚠️ Disclaimer

This project is intended for educational and technical purposes only. It does not promote or support piracy or illegal content distribution.

---

## 🔮 Future Improvements

* 🔐 JWT Authentication
* 📱 React Native frontend integration
* 📦 Download queue management
* 📊 Dashboard / UI

---

## 👨‍💻 Author

Devang Sharma
