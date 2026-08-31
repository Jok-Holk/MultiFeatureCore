# MultiFeatureCore — Setup máy mới

File này để mang sang máy khác khi cần tiếp tục dev/test plugin. Không chứa
bí mật gì, chỉ là hướng dẫn cài môi trường.

---

## 1. Java cần cài

- **JDK 25** bắt buộc (Paper 26.1.2 build 70 compile ra class file version 69 =
  Java 25). Bản đang dùng trên máy hiện tại: **JetBrains Runtime (JBR) 25.0.3**,
  đi kèm IntelliJ IDEA tại `C:\Users\Admin\.jdks\jbr-25.0.3`.
- Máy mới có thể dùng:
  - JBR 25 đi kèm IntelliJ (Settings → Build Tools → gọi "Download JDK" chọn
    JetBrains Runtime 25), hoặc
  - Bất kỳ OpenJDK 25 nào (Temurin/Adoptium 25, Amazon Corretto 25...) — miễn
    là đúng Java 25, không quan trọng vendor.
- Set biến môi trường trỏ đúng JDK trước khi build (xem lệnh build ở mục 3).

## 2. Công cụ compile Java → plugin .jar

- **Build tool: Maven** (không dùng Gradle). Project dùng `pom.xml` ở root.
- **QUAN TRỌNG: không dùng `maven-shade-plugin`** — shade 3.5.3 tạo jar lỗi
  `ZipException: zip END header not found` trên Paper 26.1.2/JDK 25. Project
  đã chuyển sang `maven-jar-plugin` (3.4.1) — giữ nguyên, không đổi lại shade.
- Maven có thể dùng:
  - Maven bundled theo IntelliJ IDEA (không cần cài Maven riêng), hoặc
  - Cài Maven 3.9+ độc lập nếu không dùng IntelliJ.
- IDE khuyến nghị: **IntelliJ IDEA** (project set up sẵn cho IntelliJ, thư mục
  `.idea/` bị gitignore nên máy mới mở lại từ `pom.xml` là IntelliJ tự tạo lại).

### Lệnh build (PowerShell, IntelliJ-bundled Maven)

```powershell
$env:JAVA_HOME = "<đường dẫn JDK 25 trên máy mới>"
& "<đường dẫn IntelliJ>\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -f "<đường dẫn project>\pom.xml" "-Dmaven.compiler.executable=$env:JAVA_HOME\bin\javac"
```

Output: `target/multifeaturecore-<version>.jar`

Nếu dùng Maven cài riêng thay vì bundled, chỉ cần thay đường dẫn `mvn.cmd`
bằng `mvn` trong PATH.

## 3. Server để tự host test plugin + resource pack

Server gốc chạy **Purpur 26.1.2** trong Docker (dùng image kiểu
`itzg/docker-minecraft-server`, có `mc-image-helper` copy `/plugins` vào
`/data/plugins` lúc container start). Để test trên máy mới:

### 3a. Cần cài
- **Docker Desktop** (Windows, bật WSL2 backend) — cách nhanh nhất để có server
  giống hệt production.
- Hoặc đơn giản hơn nếu không muốn Docker: tải thẳng **Purpur 26.1.2** jar từ
  https://purpurmc.org, chạy bằng JDK 25 (`java -jar purpur-26.1.2.jar --nogui`)
  trong 1 thư mục server riêng (accept eula.txt).

### 3b. Setup nhanh bằng Docker (khuyến nghị, giống prod nhất)
```yaml
# docker-compose.yml mẫu
services:
  mc:
    image: itzg/minecraft-server
    environment:
      EULA: "TRUE"
      TYPE: "PURPUR"
      VERSION: "26.1.2"
    ports:
      - "25565:25565"
    volumes:
      - ./data:/data
```
- Copy jar build ra `./data/plugins/multifeaturecore-<version>.jar`.
- Copy `config.yml` cũ (nếu có) vào `./data/plugins/MultiFeatureCore/` — nhớ:
  Bukkit KHÔNG tự update config.yml khi thay jar, phải sửa tay khi config
  thay đổi.
- Restart container để load plugin mới: `docker compose restart mc`.

### 3c. Test resource pack cụ thể
Resource pack hiện được serve qua GitHub Releases (URL hardcode trong
`MainPlugin.java`, override được qua `config.yml` → `resource-pack.url` /
`resource-pack.sha1`). Để test pack MỚI (chưa upload release) trên máy mới
mà không cần push lên GitHub mỗi lần sửa:

1. Zip nội dung `src/main/resources/pack-contents/` thành 1 file zip
   (root của zip phải là `pack.mcmeta` + `assets/`, không phải thư mục
   `pack-contents` lồng bên trong).
2. Serve zip đó bằng HTTP server local, ví dụ:
   ```bash
   cd path/to/zip-folder
   python -m http.server 8080
   ```
3. Nếu server Minecraft và client test **cùng máy/LAN**: set
   `resource-pack.url: "http://<LAN-IP>:8080/multifeature-pack.zip"` trong
   config.yml của plugin (client Minecraft cần cùng mạng để tải được).
4. Nếu cần test từ ngoài LAN (điện thoại, bạn bè): dùng `ngrok http 8080`
   hoặc `cloudflared tunnel --url http://localhost:8080` để có URL public
   tạm thời, rồi set `resource-pack.url` trỏ vào URL đó.
5. Tính SHA1 của zip để set `resource-pack.sha1` (bắt buộc nếu muốn Minecraft
   không hỏi lại mỗi lần):
   ```powershell
   Get-FileHash multifeature-pack.zip -Algorithm SHA1
   ```
6. Khi pack final, upload lên GitHub Releases (tag kiểu `resourcepack-v3`)
   và cập nhật URL/SHA1 hardcode trong `MainPlugin.java` như các lần trước.

## 4. Third-party plugin cần cài trên server test (nếu muốn test đầy đủ)
Chỉ cần nếu test các tính năng liên quan (worldedit/voxelsniper permissions,
daylight cycle...). Không cần cho test resource pack đơn thuần.
- FAWE (FastAsyncWorldEdit) 2.15.2
- FAVS (FastAsyncVoxelSniper) 3.2.4 — cần FAWE
- CustomDaytime 2.1.2 — có thể conflict với `/daylength` của plugin
- WorldGuard 7.0.17, ProtocolLib 5.4.0, Essentials 2.22.0, ViaVersion 5.10.0

## 5. Git
- Remote: https://github.com/Jok-Holk/MultiFeatureCore.git
- Branch: master
- `CLAUDE.md` (context file cho Claude Code) KHÔNG có trong repo (gitignored) —
  nếu muốn Claude Code trên máy mới có cùng context, copy tay file
  `CLAUDE.md` từ máy hiện tại sang.
