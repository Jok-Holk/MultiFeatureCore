
▬▬▬▬▬▬▬▬▬▬▬▬ MSG 1 / EN — How It Works + Erosion (~2400 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# 🏔️ VoxelSniper (FAVS) Guide
-# English · 1 of 3 · Core Concept, Setup, Erosion Brush
-# Full docs: https://intellectualsites.gitbook.io/fastasyncvoxelsniper

## 🔧 How It Works

VoxelSniper lets you sculpt terrain by **pointing and clicking** at long range — no selection box needed.

You bind a brush to any item in your hand, then use that item to paint the ground.

**Arrow** → apply the brush normally
**Gunpowder** → apply the brush **in reverse**

```
/vs              show all current settings
/vs brushes      list available brushes
/d               reset everything back to defaults
/b [name] info   in-game help for a specific brush
```

## 📏 Setting Up Before You Click

Three things to set before brushing:

**1. Choose a brush:**
```
/b e       erosion brush
/b bb      blend brush
/b over    overlay brush
```

**2. Set the brush size (radius):**
```
/b 3       small — for detail work
/b 5       medium — good starting point
/b 10      large — broad terrain shaping
```

**3. Set the material (for brushes that place blocks):**
```
/v grass_block
/v stone
/v dirt
```

Check everything at once with `/vs`.

## 🏔️ Erosion Brush — `/b e`

The main terrain tool. Simulates erosion to reshape the ground.

```
/b e melt         smooth down bumps and sharp peaks  ← most used
/b e fill         raise low spots, fill dips
/b e smooth       blend terrain without much height change
/b e lift         gently push terrain upward
/b e float_clean  remove floating blocks
```

**Arrow + melt** → rounds off peaks and edges
**Gunpowder + melt** → builds up instead (acts like fill)

**Arrow + fill** → raises hollows and flat ground
**Gunpowder + fill** → smooths down instead (acts like melt)

The arrow/gunpowder swap applies to all presets the same way.

> Start with `/b 5` (radius 5). Try melt a few times to feel how it moves, then switch to fill to build up.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 2 / EN — More Brushes + Workflow (~2800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 2 of 3 · More Brushes, Terrain Workflow

## 🎲 Random Erode — `/b re`

Same idea as erosion but uses randomness, so the result looks more natural. Less "processed" than `/b e smooth`.

Arrow = erode · Gunpowder = fill

Use this when you want the terrain to feel organic and imperfect rather than cleanly sculpted.

## 🌿 Overlay — `/b over`

Paints a block type onto the top surface of the terrain. Use after sculpting to fix the surface.

```
/v grass_block
/b over
```

Arrow-click the ground → grass_block gets painted on every exposed top block in the radius.

```
/b over d2     go 2 blocks deep instead of just 1
```

Gunpowder + overlay → **removes** the top layer instead of adding one.

> Only affects the very topmost block of each column. Won't paint under overhangs or inside caves.

## 🎨 Splatter Overlay — `/b sover`

Like overlay but leaves random gaps — great for natural-looking mixed surfaces (grass patches, dirt spots, stone peeking through).

Arrow = paint patches · Gunpowder = remove patches

## 🧊 Blend Brushes

Don't add or remove blocks. They blend the transition between different materials — use wherever two block types meet awkwardly.

```
/b bb      Blend Ball (sphere)    ← most common
/b bd      Blend Disc (flat circle)
/b bv      Blend Voxel (cube)
/b bvd     Blend Voxel Disc
```

No `/v` material needed — it works with what's already there.

## ⚽ Ball Brush — `/b b`

Place or remove a solid sphere of your chosen material.
Arrow = place it · Gunpowder = dig it out

## 🌊 Drain — `/b drain`

Removes water and lava from where you click.
Arrow = drain · Gunpowder = fill with liquid (set `/v water` or `/v lava` first)

## 🏗️ Raising Flatland Into Hills — Full Workflow

```
Step 1 — Build the base shape:
/b e fill
/b 8
```
Arrow-click the flat area many times. Use a larger radius for the broad shape first, smaller for the slopes.

```
Step 2 — Soften the raw bumps:
/b e melt
/b 5
```
Click around the mound a few times to round off the peaks you just made.

```
Step 3 — Light final smooth:
/b e smooth
/b 4
```
A few gentle passes around edges and the base.

```
Step 4 — Repaint the surface:
/v grass_block
/b over
```
Paint grass back on everything you sculpted.

```
Step 5 — Blend material seams:
/b bb
/b 3
```
Click anywhere two materials meet to blend them together.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 3 / EN — Other Brushes + Watch Out (~1800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 3 of 3 · Extra Brushes, Common Mistakes

## 🌿 Other Brushes

**Flatten — `/b f`**
Flattens terrain to the same height as the block you click. Good for creating plateaus.
*(Listed as under development — try it and see if it works)*

**Snow Cone — `/b snow`**
Creates a snowy mountain cone shape at the click point.

**Stencil — `/b st`**
Pastes a saved terrain stencil pattern onto the ground. Advanced feature.
```
/b st fill <name>     add blocks
/b st full <name>     full replace
/b st replace <name>  replace existing blocks only
```

## ⚠️ Things to Watch Out For

**Arrow vs. Gunpowder confusion**
They always do opposite things. Before clicking, check which one you're holding.

**`/d` resets everything**
If your brush stops behaving as expected, `/d` and reconfigure from scratch.

**Erosion on air does nothing**
The erosion brush needs existing terrain. Aiming at empty air won't do anything.

**`float_clean` can surprise you**
It removes floating blocks aggressively. Test on a small area near caves or overhangs first — it might remove things you didn't intend.

**Too many passes in the same spot**
Over-eroding removes all natural detail and makes terrain look flat and uniform. Alternate between melt and fill passes for layered, natural-looking results.

**Gunpowder + overlay by accident**
This scrapes the top layer off. Easy to grab gunpowder instead of an arrow without noticing.

**Always check `/vs` before starting**
Easy to forget what size and material are currently set from a previous session.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 4 / VN — Cách hoạt động + Xói mòn (~2400 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# 🏔️ Hướng Dẫn VoxelSniper (FAVS)
-# Tiếng Việt · 1 / 3 · Nguyên Lý, Cài Đặt, Brush Xói Mòn
-# Tài liệu đầy đủ: https://intellectualsites.gitbook.io/fastasyncvoxelsniper

## 🔧 Cách Hoạt Động

VoxelSniper cho phép điêu khắc địa hình bằng cách **chỉ và click** tầm xa — không cần chọn hộp vùng.

Gắn brush vào item đang cầm, rồi dùng item đó như súng sơn.

**Mũi tên** → áp dụng brush bình thường
**Thuốc nổ** → áp dụng brush **ngược lại**

```
/vs              xem tất cả cài đặt hiện tại
/vs brushes      danh sách brush khả dụng
/d               reset tất cả về mặc định
/b [tên] info    hướng dẫn in-game của brush cụ thể
```

## 📏 Cài Đặt Trước Khi Click

Ba thứ cần cài trước:

**1. Chọn loại brush:**
```
/b e       brush xói mòn
/b bb      brush hoà trộn
/b over    brush phủ bề mặt
```

**2. Đặt kích thước (bán kính):**
```
/b 3       nhỏ — chi tiết tinh
/b 5       vừa — điểm bắt đầu tốt
/b 10      lớn — tạo hình địa hình rộng
```

**3. Đặt vật liệu (cho brush có đặt block):**
```
/v grass_block
/v stone
/v dirt
```

Kiểm tra tất cả cùng lúc với `/vs`.

## 🏔️ Brush Xói Mòn — `/b e`

Công cụ địa hình chính. Mô phỏng xói mòn để định hình lại địa hình.

```
/b e melt         làm mượt bướu và đỉnh nhọn  ← dùng nhiều nhất
/b e fill         nâng chỗ thấp, lấp khoảng trống
/b e smooth       hoà trộn mà không thay đổi chiều cao nhiều
/b e lift         nâng nhẹ địa hình lên
/b e float_clean  xoá block lơ lửng
```

**Mũi tên + melt** → bào mòn đỉnh và cạnh sắc
**Thuốc nổ + melt** → đắp lên thay vì bào (hoạt động như fill)

**Mũi tên + fill** → nâng chỗ thấp và đồng bằng
**Thuốc nổ + fill** → bào xuống thay vì đắp (hoạt động như melt)

Cách swap mũi tên/thuốc nổ này áp dụng cho tất cả preset như nhau.

> Bắt đầu với `/b 5`. Thử melt vài lần để cảm nhận, sau đó đổi sang fill để đắp lên.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 5 / VN — Brush khác + Quy trình (~2800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 2 / 3 · Brush Khác, Quy Trình Làm Địa Hình

## 🎲 Xói Mòn Ngẫu Nhiên — `/b re`

Giống xói mòn nhưng dùng nhiều yếu tố ngẫu nhiên, kết quả trông tự nhiên hơn. Ít "nhân tạo" hơn `/b e smooth`.

Mũi tên = xói mòn · Thuốc nổ = đắp lên

Dùng khi muốn địa hình trông hữu cơ và không hoàn hảo thay vì được tạo hình gọn gàng.

## 🌿 Phủ Bề Mặt — `/b over`

Sơn block lên mặt trên cùng của địa hình. Dùng sau khi điêu khắc để sửa bề mặt.

```
/v grass_block
/b over
```

Mũi tên click vào đất → grass_block được sơn lên mọi block trên cùng lộ thiên trong bán kính.

```
/b over d2     sâu 2 block thay vì chỉ 1
```

Thuốc nổ + overlay → **xoá** lớp trên thay vì thêm.

> Chỉ ảnh hưởng block trên cùng mỗi cột. Không sơn dưới mái nhô hay trong hang.

## 🎨 Phủ Bề Mặt Loang Lổ — `/b sover`

Giống overlay nhưng để lại chỗ trống ngẫu nhiên — tốt cho bề mặt trông tự nhiên (mảng cỏ, mảng đất, đá lộ ra).

Mũi tên = sơn mảng · Thuốc nổ = xoá mảng

## 🧊 Brush Hoà Trộn

Không thêm/xoá block. Hoà trộn ranh giới giữa các vật liệu — dùng nơi hai loại block tiếp giáp nhau trông kỳ.

```
/b bb      Blend Ball (hình cầu)    ← phổ biến nhất
/b bd      Blend Disc (đĩa phẳng)
/b bv      Blend Voxel (hình khối)
/b bvd     Blend Voxel Disc
```

Không cần `/v` — tự làm việc với block đang có sẵn.

## ⚽ Brush Hình Cầu — `/b b`

Đặt hoặc xoá cầu đặc bằng vật liệu đã chọn.
Mũi tên = đặt cầu · Thuốc nổ = khoét cầu ra

## 🌊 Rút Chất Lỏng — `/b drain`

Xoá nước/lava tại nơi click.
Mũi tên = rút · Thuốc nổ = bơm đầy (đặt `/v water` hoặc `/v lava` trước)

## 🏗️ Biến Đồng Bằng Thành Đồi — Quy Trình Đầy Đủ

```
Bước 1 — Tạo hình cơ bản:
/b e fill
/b 8
```
Mũi tên click nhiều lần vào vùng phẳng. Bán kính lớn trước cho hình tổng thể, nhỏ hơn cho phần rìa.

```
Bước 2 — Làm mềm bướu thô:
/b e melt
/b 5
```
Click vài lần quanh mô đất để bào bớt đỉnh nhọn vừa tạo ra.

```
Bước 3 — Làm mượt lần cuối:
/b e smooth
/b 4
```
Vài lần nhẹ quanh rìa và chân đồi.

```
Bước 4 — Sửa bề mặt:
/v grass_block
/b over
```
Sơn cỏ lại lên tất cả những gì vừa điêu khắc.

```
Bước 5 — Hoà trộn ranh giới:
/b bb
/b 3
```
Click vào nơi hai vật liệu khác nhau gặp nhau.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 6 / VN — Brush khác + Lưu ý (~1800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 3 / 3 · Brush Khác, Những Lỗi Hay Gặp

## 🌿 Brush Đáng Biết Thêm

**Flatten — `/b f`**
Làm phẳng địa hình về cùng chiều cao với block bạn click. Tốt để tạo cao nguyên.
*(Đang phát triển — thử xem có dùng được không)*

**Snow Cone — `/b snow`**
Tạo hình nón núi tuyết tại điểm click.

**Stencil — `/b st`**
Dán pattern stencil đã lưu lên địa hình. Tính năng nâng cao.
```
/b st fill <tên>     thêm block
/b st full <tên>     thay thế toàn bộ
/b st replace <tên>  chỉ thay block đang có
```

## ⚠️ Những Lỗi Hay Gặp

**Nhầm mũi tên và thuốc nổ**
Chúng luôn làm ngược nhau. Trước khi click, nhớ kiểm tra đang cầm cái nào.

**`/d` reset hết**
Nếu brush không hoạt động như mong đợi, `/d` rồi cài lại từ đầu.

**Brush xói mòn không làm gì trên air**
Cần có địa hình để xói. Nhắm vào không khí trống sẽ không có hiệu quả.

**`float_clean` có thể bất ngờ**
Xoá block lơ lửng khá hung hăng. Test trên vùng nhỏ gần hang hoặc mái nhô trước — có thể xoá thứ bạn không muốn xoá.

**Xói mòn quá nhiều cùng một chỗ**
Xói quá nhiều lần xoá hết chi tiết tự nhiên, địa hình trở nên phẳng và đồng đều. Xen kẽ melt và fill để có kết quả có chiều sâu hơn.

**Thuốc nổ + overlay ngoài ý muốn**
Cái này cạo mất lớp bề mặt. Dễ cầm nhầm thuốc nổ thay vì mũi tên mà không để ý.

**Luôn kiểm tra `/vs` trước khi bắt đầu**
Dễ quên kích thước và vật liệu đang cài từ phiên trước.
