# EasyPan (EasyDisk)

Une plateforme complète de **stockage cloud / partage de fichiers** construite avec **Spring Boot** et **Vue 3**. Elle permet aux utilisateurs de téléverser, télécharger et partager des fichiers via une interface web claire et rapide.

> 🌐 Langue — 语言: **[English (EN)](./README.md) · [Français (FR)](#français)**

---

## 🇫🇷 Français <a id="français"></a>

<table><tr><td><b>Backend</b></td><td>Java 8 · Spring Boot · MyBatis · Spring Data Redis · Spring Mail · FFmpeg</td></tr>
<tr><td><b>Frontend</b></td><td>Vue 3 · Vue Router · Vuex · Axios · Element Plus · Highlight.js · DPlayer / APlayer</td></tr>
<tr><td><b>Base de données</b></td><td>MySQL</td></tr>
<tr><td><b>Authentification</b></td><td>Connexion tierce via QQ Connect (QQ 互联)</td></tr>
<tr><td><b>Déploiement</b></td><td>Node.js · Nginx · Sass / Webpack / Babel</td></tr></table>

### ✨ Fonctionnalités
- **Téléversement / téléchargement / partage de fichiers** — stocker et accéder à ses fichiers à tout moment.
- **Lecture multimédia** — vidéo & audio en ligne via FFmpeg, DPlayer et APlayer.
- **Connexion QQ** — s'inscrire et se connecter avec un compte QQ.
- **Sécurité des données** — authentification Spring Security + cache Redis.
- **Extensible** — structure claire Spring Boot + Vue 3, facile à étendre.

### 📁 Organisation du dépôt
| Branche | Objectif |
|---------|----------|
| `master` | Code consolidé (backend + frontend + Nginx) avec README multilingue |
| `dev-back` | Backend uniquement — Spring Boot / Java |
| `dev-front` | Frontend uniquement — Vue 3 / Vite |

### 🚀 Pour commencer
**Prérequis :** JDK 8+, Node.js (v16+), MySQL, Redis, FFmpeg, Nginx.

**Backend** (`dev-back` ou `easypan-back/` sur `master`) :
```bash
# créer d'abord la base de données "easypan" (voir easypan.sql)
# configurer les identifiants MySQL + Redis dans src/main/resources/application.properties
mvn spring-boot:run
```

**Frontend** (`dev-front` ou `easypan-front/` sur `master`) :
```bash
npm install
npm run serve        # développement
npm run build        # production
```
Déployer le `dist/` généré dans Nginx.

> **Note sur la configuration :** Les identifiants réels (code SMTP, mots de passe DB & Redis, QQ AppKey) ont été masqués dans ce référentiel public. Remplacez-les par vos propres valeurs avant de lancer.

### 🤝 Contact
Email : ruimuqin@gmail.com

---

### Dépôt — vues
[English (top)](./README.md#english) · [Français](#français)