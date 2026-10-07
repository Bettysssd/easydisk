# EasyPan (EasyDisk)

A full-featured **cloud drive / file-sharing platform** built with **Spring Boot** and **Vue 3**. It lets users upload, download and share files through a clean, fast web interface.

> 🌐 Language — 语言: **[English (EN)](#english) · [Français (FR)](./README.fr.md)**

---

## 🇬🇧 English <a id="english"></a>

<table><tr><td><b>Backend</b></td><td>Java 8 · Spring Boot · MyBatis · Spring Data Redis · Spring Mail · FFmpeg</td></tr>
<tr><td><b>Frontend</b></td><td>Vue 3 · Vue Router · Vuex · Axios · Element Plus · Highlight.js · DPlayer / APlayer</td></tr>
<tr><td><b>Database</b></td><td>MySQL</td></tr>
<tr><td><b>Auth</b></td><td>QQ Connect (QQ 互联) third-party login</td></tr>
<tr><td><b>Deploy</b></td><td>Node.js · Nginx · Sass / Webpack / Babel</td></tr></table>

### ✨ Features
- **File upload / download / share** — store and access files anytime.
- **Media playback** — online video & audio via FFmpeg, DPlayer and APlayer.
- **QQ Connect login** — register & sign in with a QQ account.
- **Data security** — Spring Security auth/authorization + Redis caching.
- **Extensible** — clean Spring Boot + Vue 3 structure, easy to extend.

### 📁 Repository layout
| Branch | Purpose |
|--------|---------|
| `master` | Consolidated codebase (backend + frontend + Nginx) with multi-language README |
| `dev-back` | Backend only — Spring Boot / Java |
| `dev-front` | Frontend only — Vue 3 / Vite |

### 🚀 Getting started
**Requirements:** JDK 8+, Node.js (v16+), MySQL, Redis, FFmpeg, Nginx.

**Backend** (`dev-back` or `easypan-back/` on `master`):
```bash
# create database "easypan" first (see easypan.sql)
# configure MySQL + Redis credentials in src/main/resources/application.properties
mvn spring-boot:run
```

**Frontend** (`dev-front` or `easypan-front/` on `master`):
```bash
npm install
npm run serve        # development
npm run build        # production
```
Deploy the built `dist/` to Nginx.

> **Note on config:** Real credentials (SMTP auth code, DB & Redis passwords, QQ AppKey) have been redacted in this public repository. Replace them with your own values before running.

### 🤝 Contact
Email: ruimuqin@gmail.com

---

### Views — this repo
[English (top)](#english) · [Français](./README.fr.md#français)