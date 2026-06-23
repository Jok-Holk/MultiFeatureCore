
▬▬▬▬▬▬▬▬▬▬▬▬ MSG 1 / EN — Wand + Selection (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# ⛏️ WorldEdit (FAWE) Guide
-# English · 1 of 5 · Wand, Selection, Expanding
-# Full docs: https://intellectualsites.gitbook.io/fastasyncworldedit

## 🪓 Getting Your Wand

Type `//wand` to get a **wooden axe**.

- **Left-click** a block → Position 1 (first corner)
- **Right-click** a block → Position 2 (second corner)

The box between those two corners is your **selection** — where all commands apply.

No block to click? Set positions at your feet:
```
//pos1      mark Position 1 where you're standing
//pos2      mark Position 2 where you're standing
//size      see how many blocks are in your selection
```

## 📦 Selection Shape

Default is a box. You can switch to other shapes:
```
//sel cuboid    default rectangular box
//sel poly      click corners freely, click same point twice to close
//sel sphere    spherical selection
//sel cyl       cylinder selection
//sel convex    click multiple points, selection wraps around all of them
```

## 📐 Resizing Your Selection

```
//expand 10 up       grow selection 10 blocks upward
//expand 10 north    grow 10 blocks north
//expand vert        stretch from bedrock to build limit
//contract 5 down    shrink 5 blocks from the bottom
//shift 5 east       slide the whole selection box 5 blocks east
```

Direction shortcuts: `up` `down` `north` `south` `east` `west` `me` *(the way you're facing)*

## 🧭 Navigation

```
/jumpto     teleport to wherever your crosshair points
/thru       pass through the wall you're facing
/unstuck    escape from being stuck inside blocks
/ascend     go up to the next floor above you
/descend    go down to the next floor below you
/up 5       float up 5 blocks
```


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 2 / EN — Filling + Mixing Blocks (~2500 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 2 of 5 · Filling, Replacing, Mixing Blocks

## 🏗️ Filling and Changing Blocks

**Fill the whole selection with one block:**
```
//set stone
//set grass_block
//set air              clears everything in the selection
```

**Replace one block type with another:**
```
//replace stone dirt               replace all stone with dirt
//replace stone,sand dirt          replace stone OR sand with dirt
```

No from-block specified → replaces everything that isn't air.

**Build just the shell:**
```
//walls stone      place stone on the 4 sides only
//faces stone      place stone on all 6 sides (walls + floor + ceiling)
//hollow           empty the inside, keep the outer layer
//hollow 2         keep a 2-block thick shell
```

**Other useful commands:**
```
//smooth           smooth terrain elevation
//smooth 4         smooth 4 times (more passes = smoother result)
//naturalize       top block = grass, next 3 = dirt, rest = stone
//overlay grass_block    place grass on top of every exposed surface block
//line stone       draw a stone line from pos1 to pos2
```

## 🎨 Mixing Blocks

Mix blocks using percentages — numbers must add up to 100:
```
//set 80%stone,20%dirt
//set 50%oak_planks,50%air      half planks, half empty space
//replace stone 60%dirt,40%gravel
```

**Special block keywords:**
```
#existing        keep whatever block is already there (don't overwrite it)
#clipboard       use whatever is currently in your clipboard
```

Mix example: `//set 70%stone,30%#existing`
→ Fills 70% with stone, leaves the other 30% of blocks completely untouched.

**Noise-based mixing (organic blends):**
```
//set #simplex[5][stone,dirt]          natural-looking stone/dirt blend
//set #simplex[5][stone,dirt,gravel]   three-material organic mix
```

Lower number = bigger blobs. Higher number = finer grain.

## 🏔️ Creating Shapes

```
//sphere stone 10        solid stone ball, radius 10
//hsphere stone 10       hollow stone ball (shell only)
//cyl stone 8 15         stone cylinder, radius 8, height 15
//hcyl stone 8 15        hollow cylinder
//pyramid stone 6        solid pyramid
//generate stone y<50    custom shape using a math condition
```


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 3 / EN — Masks + Copy/Paste (~2600 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 3 of 5 · Masks, Copy, Paste, Schematics

## 🎭 Masks — Control Where Edits Land

A mask tells WorldEdit *"only change blocks where this condition is true."*

**Global mask** — applies to everything you do until cleared:
```
//gmask stone       only affect stone blocks
//gmask             clear the global mask (back to normal)
```

**Useful masks:**
```
stone               only where stone is
!stone              everywhere except stone
#existing           only where there's already a block (not air)
#surface            only the topmost block of each column exposed to air
>grass_block        only blocks sitting directly above grass
<stone              only blocks directly below stone
~stone              any block adjacent to stone (any side)
%50                 randomly skip 50% of eligible blocks
$plains             only inside the plains biome
\15\50              only on slopes between 15° and 50°
```

Combine with `&` (AND):
```
#surface&%50        surface blocks, but only half of them randomly
!stone&#existing    not stone AND not air
```

> `//gmask` stays active until you clear it. If your edits aren't landing where expected, this is often why.

## 📋 Copy, Cut, Paste

```
//copy              copy selection (your feet mark the paste origin)
//cut               copy and delete (leaves air)
//paste             paste at your current position
//paste -a          paste but skip air blocks (won't overwrite terrain with air)
//paste -o          paste at the exact original location it was copied from
```

**Rotate and flip before pasting:**
```
//rotate 90         rotate 90° horizontally
//rotate 180        flip direction
//flip up           flip upside down
//flip north        mirror facing north
```

> `//paste -a` is almost always what you want when pasting builds over terrain.
> Paste position depends on where you were standing when you copied. If something lands in the wrong spot, that's usually why — try `//paste -o` or move closer to where you copied from.

**Schematics (saved clipboards):**
```
/schematic save myhouse     save clipboard to file "myhouse"
/schematic load myhouse     load "myhouse" into clipboard
/schematic list             list your saved schematics
```

Schematics persist between sessions — they don't disappear when you relog.

## ⏪ Undo and Redo

```
//undo          undo last action
//undo 5        undo last 5 actions
//redo          redo
```

> Undo history clears when you disconnect.
> Commands run with `//fast` mode cannot be undone.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 4 / EN — Brushes (~2800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 4 of 5 · Brushes

## 🖌️ Brushes

Brushes let you sculpt and place blocks by clicking at long range — no selection box needed.

Hold the item you want as your brush tool, then run one of these:

```
/br sphere stone 5        sphere of stone, radius 5
/br cylinder stone 4 3    stone cylinder, radius 4, height 3
/br smooth 4 3            smoothing brush, size 4, 3 passes per click
/br erode 5               erosion brush — organic terrain smoothing
/br gravity 5             gravity brush — blocks fall downward
/br clipboard [-a]        paste your clipboard on each click (-a = skip air)
/br splatter stone 5 3    random stone splatter, size 5, 3 seeds
```

**After binding, change settings with:**
```
/size 7         change brush radius to 7
/mat dirt       change the material the brush places
/mask #surface  restrict brush to surface blocks only
/none           remove the brush from this item
```

**Targeting mode — controls where the brush lands:**
```
/br target 0    aim at the block face you're looking at (default)
/br target 1    aim at a point in the air ahead of you
/br target 2    aim based on terrain height below your cursor
```

**Preview before committing:**
```
/br vis 0       off
/br vis 1       show a dot where the brush center will land
/br vis 2       show all blocks the brush would affect
```

> The brush binds to the specific item in your hand. Hold a different item to have multiple brushes ready.
> `/none` removes the brush from just that item.
> Scroll wheel can adjust brush size on the fly: `/scroll size`


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 5 / EN — Other Commands + Tips (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# English · 5 of 5 · More Commands, Common Mistakes

## 🌲 More Useful Commands

```
//stack 3 north          copy-stack your selection 3 times northward
//move 5 up              move contents 5 blocks up (removes the original)
//regen                  reset selection back to original world generation
//fill water 10          fill a 10-block radius hole with water
//drain 10               drain water/lava in a 10-block radius
//snow                   simulate snow fall in your selection
/butcher 20              kill all mobs within 20 blocks
/remove entities 30      remove all entities within 30 blocks
```

## ⚠️ Things People Get Wrong

**`//paste` lands in the wrong place**
When you `//copy`, the game marks where your feet are. When pasting, it uses that as a reference. If the build lands off, try `//paste -o` to paste at the exact original spot.

**Forgot `//paste -a`**
Without `-a`, pasting replaces terrain blocks with air from inside the clipboard. Almost always use `-a` when pasting structures into the open world.

**`//gmask` still set from before**
If your edits aren't affecting the right blocks, check `//gmask`. Clear it with `//gmask` (no argument).

**`//rotate` only rotates sideways**
`//rotate 90` turns the clipboard left/right. To tilt on another axis: `//rotate 0 90 0`

**`#surface` vs `>air`**
`#surface` = only the topmost block per column. `>air` = any block with air directly above it (includes overhangs and cave ceilings).

**`//stack` overwrites terrain**
It doesn't check for collisions — stacking into existing ground will overwrite it.

**`//smooth` on flat ground does nothing**
It needs existing height variation to smooth out. Works best on gently rolling terrain.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 6 / VN — Rìu + Chọn Vùng (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

# ⛏️ Hướng Dẫn WorldEdit (FAWE)
-# Tiếng Việt · 1 / 5 · Rìu, Chọn Vùng, Mở Rộng
-# Tài liệu đầy đủ: https://intellectualsites.gitbook.io/fastasyncworldedit

## 🪓 Lấy Cây Rìu

Gõ `//wand` để nhận **rìu gỗ**.

- **Click trái** vào block → Vị trí 1 (góc đầu)
- **Click phải** vào block → Vị trí 2 (góc cuối)

Hộp giữa hai góc đó là **vùng chọn** — mọi lệnh sẽ ảnh hưởng vào đây.

Không có block để click? Đặt vị trí tại chỗ đứng:
```
//pos1      đánh dấu Vị trí 1 tại chỗ đứng
//pos2      đánh dấu Vị trí 2 tại chỗ đứng
//size      xem bao nhiêu block trong vùng chọn
```

## 📦 Hình Dạng Vùng Chọn

Mặc định là hộp. Có thể đổi sang hình khác:
```
//sel cuboid    hộp chữ nhật mặc định
//sel poly      click nhiều góc tuỳ ý, click lại điểm đầu để đóng
//sel sphere    hình cầu
//sel cyl       hình trụ
//sel convex    click nhiều điểm, vùng chọn bao quanh tất cả
```

## 📐 Mở Rộng Vùng Chọn

```
//expand 10 up       mở rộng lên 10 block
//expand 10 north    mở rộng về phía bắc 10 block
//expand vert        kéo từ bedrock đến giới hạn xây dựng
//contract 5 down    thu hẹp 5 block từ phía dưới
//shift 5 east       dịch cả hộp vùng chọn sang đông 5 block
```

Từ chỉ hướng: `up` `down` `north` `south` `east` `west` `me` *(hướng bạn đang nhìn)*

## 🧭 Di Chuyển Nhanh

```
/jumpto     dịch chuyển đến nơi crosshair chỉ
/thru       xuyên qua bức tường đang nhìn vào
/unstuck    thoát khỏi kẹt trong block
/ascend     lên tầng trên
/descend    xuống tầng dưới
/up 5       nâng lên 5 block
```


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 7 / VN — Điền Block + Trộn (~2500 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 2 / 5 · Điền Block, Thay Block, Trộn Block

## 🏗️ Điền và Thay Block

**Điền cả vùng chọn bằng một block:**
```
//set stone
//set grass_block
//set air              xoá hết mọi thứ trong vùng chọn
```

**Thay một loại block bằng loại khác:**
```
//replace stone dirt               thay toàn bộ stone bằng dirt
//replace stone,sand dirt          thay stone HOẶC sand bằng dirt
```

Không ghi block cần thay → thay hết tất cả trừ air.

**Chỉ xây vỏ ngoài:**
```
//walls stone      đặt stone ở 4 mặt bên
//faces stone      đặt stone ở cả 6 mặt (bên + sàn + trần)
//hollow           xoá hết bên trong, giữ lớp ngoài
//hollow 2         giữ vỏ dày 2 block
```

**Các lệnh hữu ích khác:**
```
//smooth           làm mượt địa hình
//smooth 4         làm mượt 4 lần (nhiều lần = mượt hơn)
//naturalize       trên cùng = cỏ, 3 lớp tiếp = đất, còn lại = đá
//overlay grass_block    đặt cỏ lên trên mọi block bề mặt lộ thiên
//line stone       vẽ đường đá từ pos1 đến pos2
```

## 🎨 Trộn Block

Trộn nhiều loại block bằng phần trăm — tổng phải bằng 100:
```
//set 80%stone,20%dirt
//set 50%oak_planks,50%air
//replace stone 60%dirt,40%gravel
```

**Từ khoá block đặc biệt:**
```
#existing        giữ nguyên block đang có (không ghi đè)
#clipboard       dùng nội dung clipboard hiện tại
```

Ví dụ: `//set 70%stone,30%#existing`
→ Điền 70% bằng đá, giữ nguyên 30% block không động đến.

**Trộn dựa trên noise (trông tự nhiên hơn):**
```
//set #simplex[5][stone,dirt]          trộn đá/đất tự nhiên
//set #simplex[5][stone,dirt,gravel]   trộn 3 vật liệu
```

Số nhỏ = mảng lớn hơn. Số lớn = hạt mịn hơn.

## 🏔️ Tạo Hình

```
//sphere stone 10        cầu đá đặc, bán kính 10
//hsphere stone 10       cầu rỗng (chỉ vỏ ngoài)
//cyl stone 8 15         trụ đá, bán kính 8, cao 15
//hcyl stone 8 15        trụ rỗng
//pyramid stone 6        kim tự tháp
```


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 8 / VN — Mask + Copy/Paste (~2600 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 3 / 5 · Mask, Copy, Paste, Schematic

## 🎭 Mask — Giới Hạn Nơi Edit Có Hiệu Lực

Mask nói cho WorldEdit biết *"chỉ thay đổi nơi điều kiện này đúng."*

**Mask toàn cục** — áp dụng cho mọi thứ cho đến khi xoá:
```
//gmask stone       chỉ ảnh hưởng block đá
//gmask             xoá mask toàn cục (về bình thường)
```

**Các mask hữu ích:**
```
stone               chỉ nơi có đá
!stone              mọi nơi trừ đá
#existing           chỉ nơi đang có block (không phải air)
#surface            block trên cùng lộ thiên của mỗi cột
>grass_block        chỉ block ngay trên cỏ
<stone              chỉ block ngay dưới đá
~stone              kề cạnh stone ở bất kỳ mặt nào
%50                 bỏ qua ngẫu nhiên 50% block đủ điều kiện
$plains             chỉ trong biome đồng bằng
\15\50              chỉ trên dốc nghiêng 15°–50°
```

Kết hợp bằng `&` (VÀ):
```
#surface&%50        block bề mặt, nhưng chỉ phân nửa ngẫu nhiên
!stone&#existing    không phải đá VÀ không phải air
```

> `//gmask` vẫn hiệu lực cho đến khi bạn xoá bằng `//gmask` không tham số. Nếu edit không ảnh hưởng đúng chỗ, kiểm tra cái này trước.

## 📋 Copy, Cắt, Dán

```
//copy              sao chép vùng chọn (chân bạn = điểm gốc khi dán)
//cut               sao chép và xoá (để lại air)
//paste             dán tại vị trí hiện tại
//paste -a          dán nhưng bỏ qua air (không ghi đè terrain bằng air)
//paste -o          dán đúng tại vị trí gốc đã copy
```

**Xoay và lật trước khi dán:**
```
//rotate 90         xoay 90° theo chiều ngang
//rotate 180        đổi chiều
//flip up           lật ngược chiều dọc
//flip north        gương hướng bắc
```

> Gần như luôn dùng `//paste -a` khi dán công trình vào địa hình — tránh ghi đè terrain bằng air.
> Vị trí dán phụ thuộc vào chỗ bạn đứng khi copy. Dán nhầm chỗ thì thường là vì vậy — thử `//paste -o`.

**Schematic (clipboard được lưu lại):**
```
/schematic save nhatoitui     lưu clipboard thành file "nhatoitui"
/schematic load nhatoitui     tải vào clipboard
/schematic list               xem tất cả file đã lưu
```

Schematic vẫn còn sau khi thoát game.

## ⏪ Hoàn Tác

```
//undo          hoàn tác thao tác vừa rồi
//undo 5        hoàn tác 5 thao tác gần nhất
//redo          làm lại
```

> Lịch sử hoàn tác mất khi bạn ngắt kết nối.
> Lệnh chạy với `//fast` không thể hoàn tác.


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 9 / VN — Brush (~2800 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 4 / 5 · Brush

## 🖌️ Brush

Brush cho phép điêu khắc và đặt block bằng cách click tầm xa — không cần chọn hộp vùng.

Cầm item muốn dùng làm brush, rồi gắn:

```
/br sphere stone 5        cầu đá, bán kính 5
/br cylinder stone 4 3    trụ đá, bán kính 4, cao 3
/br smooth 4 3            brush làm mượt, kích thước 4, 3 lần mỗi click
/br erode 5               brush xói mòn — làm mượt địa hình tự nhiên
/br gravity 5             brush trọng lực — block rơi xuống
/br clipboard [-a]        dán clipboard mỗi lần click (-a = bỏ qua air)
/br splatter stone 5 3    ngẫu nhiên loang lổ đá, kích thước 5, 3 nguồn
```

**Sau khi gắn brush, đổi cài đặt:**
```
/size 7         đổi bán kính thành 7
/mat dirt       đổi vật liệu brush đặt
/mask #surface  chỉ cho brush ảnh hưởng block bề mặt
/none           tháo brush khỏi item này
```

**Chế độ nhắm — brush xác định vị trí thế nào:**
```
/br target 0    nhắm vào mặt block đang nhìn vào (mặc định)
/br target 1    nhắm vào điểm trong không khí phía trước
/br target 2    nhắm theo chiều cao địa hình bên dưới con trỏ
```

**Xem trước:**
```
/br vis 0       tắt
/br vis 1       hiện chấm tại tâm brush
/br vis 2       hiện tất cả block brush sẽ ảnh hưởng
```

> Brush gắn vào item cụ thể đang cầm. Cầm item khác để có nhiều brush sẵn sàng.
> `/none` chỉ tháo brush của item đó thôi.
> Scroll chuột thay đổi kích thước brush ngay lập tức: `/scroll size`


▬▬▬▬▬▬▬▬▬▬▬▬ MSG 10 / VN — Lệnh khác + Lưu ý (~2200 chars) ▬▬▬▬▬▬▬▬▬▬▬▬

-# Tiếng Việt · 5 / 5 · Lệnh Khác, Những Lỗi Hay Gặp

## 🌲 Lệnh Hữu Ích Khác

```
//stack 3 north          nhân bản vùng chọn 3 lần về phía bắc
//move 5 up              di chuyển nội dung lên 5 block (xoá bản gốc)
//regen                  đặt lại vùng chọn về địa hình gen gốc
//fill water 10          điền nước vào hố trong bán kính 10
//drain 10               rút nước/lava trong bán kính 10
/butcher 20              giết tất cả mob trong 20 block
/remove entities 30      xoá tất cả entity trong 30 block
```

## ⚠️ Những Lỗi Hay Gặp

**`//paste` dán nhầm chỗ**
Khi `//copy`, game đánh dấu chỗ chân bạn đứng. Khi dán, dùng đó làm tham chiếu. Dán nhầm chỗ thì thử `//paste -o` để dán đúng vị trí gốc.

**Quên `//paste -a`**
Không có `-a`, dán sẽ ghi đè terrain bằng air từ bên trong clipboard. Gần như luôn phải dùng `-a` khi dán công trình vào thế giới.

**`//gmask` đang bật từ lúc trước**
Nếu edit không ảnh hưởng đúng chỗ, xoá mask toàn cục bằng `//gmask` không tham số.

**`//rotate` chỉ xoay ngang**
`//rotate 90` quay trái/phải. Nghiêng theo trục khác dùng `//rotate 0 90 0`.

**`#surface` vs `>air`**
`#surface` = block trên cùng mỗi cột. `>air` = bất kỳ block nào có air phía trên (gồm cả trong hang và mái nhô).

**`//stack` ghi đè terrain**
Không kiểm tra va chạm — nhân bản vào đất sẽ ghi đè hết.

**`//smooth` trên đất phẳng không làm gì**
Cần địa hình có độ cao biến đổi để làm mượt. Hiệu quả nhất trên đồi nhấp nhô nhẹ.
