
▬▬▬▬▬▬▬▬▬▬▬▬ MSG 1 / EN — Rank + Travel (~2000 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# 📖 Server Plugin Guide
-# English · 1 of 3 · Ranks, Fast Travel

## 🏅 Ranks

Five ranks, lowest to highest: **GUEST → BUILDER → ADMIN → OWNER → DEVELOPER**

Your rank shows up in chat, above your head, and on the scoreboard.

**GUEST** — Stuck in Adventure mode. Can use `/travel` and checkpoints, but can't place or break anything.
**BUILDER** — Full build access. Also unlocks WorldEdit, VoxelSniper, kits, war horse, speed fly, heightlock, and the measure tool.
**ADMIN** — Full access to everything on the server.
**OWNER** — Same as ADMIN. When joining, everyone hears thunder and sees *"GOD HAS COME"*.
**DEVELOPER** — Same as ADMIN.

Only ADMIN+ can change ranks: `/rank <player> <rank>`
> Rank is saved permanently and survives server restarts.

## ✈️ Fast Travel — `/travel`

Save spots around the world and teleport back through a GUI.

```
/travel                              open the teleport GUI
/travel save <name>                  save your current location
/travel load <name>                  teleport there (short countdown)
/travel delete <name>                remove a saved spot
/travel name <checkpoint> <new>      rename a saved spot
```

Moving during the countdown cancels the teleport.

**Customizing your menu:**
```
/travel slots <1–54>                 set how many slots your GUI has
/travel icon <checkpoint> <item>     change a slot's display icon
/travel icon <checkpoint> reset      restore the default icon
```

Both `grass_block` and `minecraft:grass_block` work as item names.

> You can set an icon for a slot even before saving a location there.
> Slot count can't go below your highest-numbered used slot.
> *Example: checkpoint7 is saved → you can't set slots below 7.*


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 2 / EN — Kits + Horse (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 2 of 3 · PvP Kits, War Horse

## ⚔️ PvP Kits — `/kits`

Get a full combat loadout with one command.

```
/kits                  open the kit selection GUI
/kits <name>           grab a kit directly by name
/kits confirm          confirm you want to clear your inventory and get the kit
/kits cancel           cancel, keep your stuff
```

**Available kits:**
warrior · juggernaut · spear · archer · survivor · berserker · ghost · alchemist · pantheon

Each kit gives a full loadout: weapon, armor, potions, food, and utility items.

If your inventory isn't empty when you run `/kits`, a confirm prompt appears in chat. It expires after **30 seconds** — after that just run `/kits` again.
Running `/kits warrior` while already waiting on a confirm switches the pending kit to warrior automatically.

## 🐴 War Horse — `/horse`

Spawn a personal horse that only you can ride. Always has max stats. Armor is cosmetic only.

```
/horse <breed>                   spawn a horse
/horse <breed> <armor>           spawn with specific armor appearance
/horse <breed> <armor> <name>    spawn with a custom name
/horse dismiss                   send your horse away
/horse confirm                   replace your current horse with the new one
/horse cancel                    keep your current horse
```

**Breeds:** white · creamy · chestnut · brown · black · gray · dark_brown · skeleton · zombie · donkey · mule
**Armor looks:** leather · iron · gold · diamond · netherite *(all give identical protection)*

> Other players can't ride your horse — they get pushed off automatically.
> Your horse stays in the world when you log off. It won't despawn on its own.
> If you already have an active horse, you'll get a confirm/cancel prompt before summoning a new one.
> Skeleton, zombie, donkey, and mule can't wear armor. Only standard horses can.
> If your horse dies while you're offline, it's gone. Just use `/horse` again to get a new one.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 3 / EN — Builder Tools + Special Weapons (~2400 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 3 of 3 · Builder Tools, Special Weapons

## 🚀 Speed Fly — `/speedfly`

```
/speedfly             toggle speed fly on or off
/speedfly <1–10>      set fly speed (1 = normal, 10 = maximum)
/speedfly tool        get a Speed Wing feather
```

Right-clicking the **Speed Wing feather** toggles speed fly without typing a command. Works from any hotbar slot.
> The feather is owner-locked. Anyone else who picks it up gets kicked from the server.

## 📐 Height Lock — `/heightlock`

Locks your Y level so you can build perfectly flat horizontal lines.

```
/heightlock           lock to current Y (or unlock if already on)
/heightlock <y>       lock to a specific Y coordinate
/heightlock on / off
```

Only blocks movement — placing and breaking works normally. Doesn't apply in Spectator mode.

## 📏 Measure Tool — `/measure`

```
/measure distance     get a compass that measures the gap between two clicks
/measure center       get a compass that finds the center point between two clicks
```

Distance shows X/Y/Z + 2D and 3D totals in chat.
Center result includes a **clickable [TP] button** in chat to teleport directly there.
> The compass is also owner-locked.

## 🪟 Glass — `/glass`

Places a glass block instantly at your feet. Quick scaffolding while building.

## 🌞 Day Length — `/daylength`

```
/daylength            check the current day cycle length
/daylength <minutes>  set it (e.g. /daylength 45 for 45-min days)
/daylength reset      back to vanilla default (20 minutes)
```

## ⚡ God Mace — `/godmace` *(ADMIN+ only)*

Right-click to launch yourself upward. Landing on a player below executes them instantly.
The mace is owner-locked — anyone else who picks it up gets kicked.

## 🔱 Abyssal Sovereign — `/trident` *(ADMIN+ only)*

A trident that throws at 3× speed with massive damage.
- Hit a **block** → it automatically flies back to your hand.
- Hit a **player in Survival** → instant execution (kicked from server).
- Owner-locked.

**Wet bonus** (target is in water OR raining with no roof above them):
- Direct damage doubles
- Splash AoE expands to twice the area
- Explosion twice as powerful


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 4 / VN — Rank + Travel (~2000 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# 📖 Hướng Dẫn Plugin Server
-# Tiếng Việt · 1 / 3 · Rank, Dịch Chuyển Nhanh

## 🏅 Rank

Năm rank từ thấp đến cao: **GUEST → BUILDER → ADMIN → OWNER → DEVELOPER**

Rank hiện trong chat, trên đầu nhân vật, và trên bảng điểm.

**GUEST** — Bị khoá Adventure mode. Chỉ dùng `/travel` và checkpoint, không xây/phá được.
**BUILDER** — Toàn quyền xây dựng. Mở khoá thêm WorldEdit, VoxelSniper, kits, ngựa chiến, bay nhanh, khoá chiều cao, công cụ đo.
**ADMIN** — Toàn quyền mọi thứ trên server.
**OWNER** — Giống ADMIN. Khi vào server, mọi người nghe tiếng sét và thấy *"GOD HAS COME"*.
**DEVELOPER** — Giống ADMIN.

Chỉ ADMIN+ mới đổi rank được: `/rank <player> <rank>`
> Rank lưu vĩnh viễn, không mất khi khởi động lại hay kết nối lại.

## ✈️ Dịch Chuyển Nhanh — `/travel`

Lưu vị trí và quay lại bất cứ lúc nào qua giao diện GUI.

```
/travel                              mở GUI dịch chuyển
/travel save <tên>                   lưu vị trí hiện tại
/travel load <tên>                   dịch chuyển đến đó (có đếm ngược)
/travel delete <tên>                 xoá vị trí đã lưu
/travel name <checkpoint> <tên_mới>  đổi tên checkpoint
```

Di chuyển trong lúc đếm ngược sẽ huỷ dịch chuyển.

**Tuỳ chỉnh menu:**
```
/travel slots <1–54>                 đặt số lượng ô trong GUI
/travel icon <checkpoint> <vật_liệu> đổi icon hiển thị cho ô đó
/travel icon <checkpoint> reset      quay về icon mặc định
```

Gõ `grass_block` hay `minecraft:grass_block` đều được, kết quả như nhau.

> Có thể đặt icon cho ô dù chưa lưu vị trí nào vào đó.
> Không thể giảm số ô xuống dưới số thứ tự ô cao nhất đang dùng.
> *Ví dụ: checkpoint7 đang có dữ liệu → không set dưới 7 ô được.*


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 5 / VN — Kits + Horse (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 2 / 3 · Kit PvP, Ngựa Chiến

## ⚔️ Kit PvP — `/kits`

Lấy ngay bộ trang bị chiến đấu hoàn chỉnh.

```
/kits                  mở GUI chọn kit
/kits <tên>            lấy kit trực tiếp bằng tên
/kits confirm          xác nhận xoá đồ cũ và nhận kit
/kits cancel           huỷ, giữ nguyên đồ cũ
```

**Các kit có sẵn:**
warrior · juggernaut · spear · archer · survivor · berserker · ghost · alchemist · pantheon

Mỗi kit cho đủ: vũ khí, giáp, thuốc, đồ ăn, đồ tiện ích.

Nếu túi đồ chưa trống khi dùng `/kits`, sẽ có prompt xác nhận trong chat. Prompt hết hạn sau **30 giây** — hết thì chạy `/kits` lại là được.
Đang chờ xác nhận kit A mà gõ `/kits warrior` → tự động đổi sang warrior, không cần cancel trước.

## 🐴 Ngựa Chiến — `/horse`

Triệu ngựa cá nhân, chỉ mình bạn cưỡi được. Luôn có stats tối đa. Giáp chỉ là ngoại hình.

```
/horse <giống>                    triệu ngựa
/horse <giống> <giáp>             triệu với ngoại hình giáp cụ thể
/horse <giống> <giáp> <tên>       triệu với tên tuỳ chỉnh
/horse dismiss                    giải tán ngựa
/horse confirm                    thay ngựa cũ bằng ngựa mới
/horse cancel                     giữ ngựa cũ
```

**Giống ngựa:** white · creamy · chestnut · brown · black · gray · dark_brown · skeleton · zombie · donkey · mule
**Giáp:** leather · iron · gold · diamond · netherite *(bảo vệ như nhau hoàn toàn)*

> Người chơi khác không cưỡi ngựa của bạn được — tự bị đẩy ra.
> Ngựa vẫn ở trong thế giới khi bạn offline, không tự biến mất.
> Nếu đã có ngựa đang sống, triệu thêm sẽ xuất hiện prompt confirm/cancel.
> Skeleton, zombie, donkey, mule không đeo giáp được.
> Ngựa bị giết khi bạn offline → mất luôn. Dùng `/horse` để triệu con mới.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 6 / VN — Builder Tools + Special Weapons (~2400 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 3 / 3 · Công Cụ Builder, Vũ Khí Đặc Biệt

## 🚀 Bay Nhanh — `/speedfly`

```
/speedfly             bật/tắt bay nhanh
/speedfly <1–10>      đặt tốc độ bay (1 = bình thường, 10 = cực nhanh)
/speedfly tool        nhận lông vũ Speed Wing
```

**Lông vũ Speed Wing** — nhấp chuột phải để bật/tắt bay nhanh không cần gõ lệnh. Dùng được từ bất kỳ ô hotbar nào.
> Lông vũ gắn với bạn. Người khác nhặt lên bị kick khỏi server.

## 📐 Khoá Chiều Cao — `/heightlock`

Khoá trục Y để xây ngang hoàn toàn thẳng hàng.

```
/heightlock           khoá vào Y hiện tại (hoặc mở khoá nếu đang bật)
/heightlock <y>       khoá vào Y cụ thể
/heightlock on / off
```

Chỉ chặn di chuyển — đặt và phá block vẫn bình thường. Không áp dụng trong Spectator mode.

## 📏 Công Cụ Đo — `/measure`

```
/measure distance     compass đo khoảng cách giữa hai điểm bạn click
/measure center       compass tìm điểm trung tâm giữa hai lần click
```

Kết quả đo hiện X/Y/Z + khoảng 2D và 3D trong chat.
Kết quả trung tâm có **nút [TP] trong chat** — click để dịch chuyển thẳng đến điểm đó.
> Compass cũng gắn với bạn.

## 🪟 Đặt Kính — `/glass`

Đặt ngay một khối kính dưới chân. Tiện cho giàn giáo tạm khi xây.

## 🌞 Độ Dài Ngày — `/daylength`

```
/daylength            xem cài đặt hiện tại
/daylength <phút>     đặt thời lượng (ví dụ: /daylength 45)
/daylength reset      về mặc định vanilla (20 phút)
```

## ⚡ Thần Chùy — `/godmace` *(chỉ ADMIN+)*

Nhấp chuột phải để phóng bản thân lên cao. Rơi trúng người chơi bên dưới → xử tử ngay.
Chùy gắn với bạn — người khác nhặt lên bị kick.

## 🔱 Abyssal Sovereign — `/trident` *(chỉ ADMIN+)*

Đinh ba phóng nhanh gấp 3 lần, sát thương cực cao.
- Trúng **block** → tự bay về tay bạn.
- Trúng **người chơi Survival** → xử tử ngay (bị kick).
- Gắn với bạn.

**Bonus ướt** (mục tiêu trong nước HOẶC trời mưa không có mái che):
- Sát thương trực tiếp tăng gấp đôi
- AoE văng mở rộng gấp đôi
- Vụ nổ mạnh gấp đôi
