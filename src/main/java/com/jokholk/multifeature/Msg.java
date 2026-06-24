package com.jokholk.multifeature;

import org.bukkit.entity.Player;

/**
 * All player-facing messages with English and Vietnamese translations.
 * Usage:
 *   Msg.ONLY_PLAYERS.get(p)
 *   Msg.TRAVEL_SAVED.fmt(p, "name", checkpointName)
 */
public enum Msg {

    // ──────────────────────────────────────────────────────────
    //  Common
    // ──────────────────────────────────────────────────────────

    ONLY_PLAYERS(
        "§cThis command can only be used by players.",
        "§cLệnh này chỉ dành cho người chơi."
    ),
    NO_PERMISSION(
        "§cYou don't have permission to use this command.",
        "§cBạn không có quyền dùng lệnh này."
    ),

    // ──────────────────────────────────────────────────────────
    //  Language command
    // ──────────────────────────────────────────────────────────

    LANG_USAGE(
        "§cUsage: /language <english|vietnamese>",
        "§cCách dùng: /language <english|vietnamese>"
    ),
    LANG_SET_ENGLISH(
        "§aLanguage set to §fEnglish§a.",
        "§aĐã đổi ngôn ngữ sang §fTiếng Anh§a."
    ),
    LANG_SET_VIETNAMESE(
        "§aLanguage set to §fVietnamese§a.",
        "§aĐã đổi ngôn ngữ sang §fTiếng Việt§a."
    ),
    LANG_ALREADY_ENGLISH(
        "§7Your language is already set to §fEnglish§7.",
        "§7Ngôn ngữ của bạn đã là §fTiếng Anh§7."
    ),
    LANG_ALREADY_VIETNAMESE(
        "§7Your language is already set to §fVietnamese§7.",
        "§7Ngôn ngữ của bạn đã là §fTiếng Việt§7."
    ),

    // ──────────────────────────────────────────────────────────
    //  Rank command
    // ──────────────────────────────────────────────────────────

    RANK_USAGE(
        "§cUsage: /rank <player> <rank>",
        "§cCách dùng: /rank <người chơi> <cấp bậc>"
    ),
    RANK_NO_PERM(
        "§cYou do not have permission to manage ranks.",
        "§cBạn không có quyền quản lý cấp bậc."
    ),
    RANK_INVALID(
        "§cInvalid rank!",
        "§cCấp bậc không hợp lệ!"
    ),
    RANK_VALID_LIST(
        "§7Valid ranks: §f%ranks%",
        "§7Cấp bậc hợp lệ: §f%ranks%"
    ),
    RANK_SET_TO_SENDER(
        "§aSet rank §e%rank% §afor §e%name%",
        "§aĐã đặt cấp §e%rank% §acho §e%name%"
    ),
    RANK_SET_NOTIFY(
        "§aYour rank has been changed to §e%rank%",
        "§aCấp bậc của bạn đã đổi thành §e%rank%"
    ),
    RANK_NOT_FOUND(
        "§cPlayer §e%name% §chas never joined this server.",
        "§cNgười chơi §e%name% §cchưa từng vào máy chủ này."
    ),
    RANK_OFFLINE_SET(
        "§aSet rank §e%rank% §afor offline player §e%name%",
        "§aĐã đặt cấp §e%rank% §acho người chơi offline §e%name%"
    ),
    RANK_OFFLINE_WILL_APPLY(
        "§7Rank will apply when they next join.",
        "§7Cấp bậc sẽ áp dụng khi họ đăng nhập lần sau."
    ),

    // ──────────────────────────────────────────────────────────
    //  Travel command
    // ──────────────────────────────────────────────────────────

    TRAVEL_NO_PERM(
        "§cYou don't have permission to use fast travel.",
        "§cBạn không có quyền dùng di chuyển nhanh."
    ),
    TRAVEL_NO_PERM_SAVE(
        "§cYou don't have permission to save checkpoints.",
        "§cBạn không có quyền lưu điểm kiểm tra."
    ),
    TRAVEL_NO_PERM_LOAD(
        "§cYou don't have permission to load checkpoints.",
        "§cBạn không có quyền tải điểm kiểm tra."
    ),
    TRAVEL_NO_PERM_DELETE(
        "§cYou don't have permission to delete checkpoints.",
        "§cBạn không có quyền xóa điểm kiểm tra."
    ),
    TRAVEL_NO_PERM_ICON(
        "§cYou don't have permission to customize checkpoint icons.",
        "§cBạn không có quyền tùy chỉnh biểu tượng điểm kiểm tra."
    ),
    TRAVEL_NO_PERM_RENAME(
        "§cYou don't have permission to rename checkpoints.",
        "§cBạn không có quyền đổi tên điểm kiểm tra."
    ),
    TRAVEL_SAVE_USAGE(
        "§cUsage: /travel save <checkpoint1-%max%> [name]",
        "§cCách dùng: /travel save <checkpoint1-%max%> [tên]"
    ),
    TRAVEL_LOAD_USAGE(
        "§cUsage: /travel load <name or checkpoint>",
        "§cCách dùng: /travel load <tên hoặc checkpoint>"
    ),
    TRAVEL_DELETE_USAGE(
        "§cUsage: /travel delete <checkpoint1-%max%>",
        "§cCách dùng: /travel delete <checkpoint1-%max%>"
    ),
    TRAVEL_NAME_USAGE(
        "§cUsage: /travel name <checkpoint1-%max%> <new name>",
        "§cCách dùng: /travel name <checkpoint1-%max%> <tên mới>"
    ),
    TRAVEL_ICON_USAGE(
        "§cUsage: /travel icon <checkpoint1-%max%> <material|reset>",
        "§cCách dùng: /travel icon <checkpoint1-%max%> <vật liệu|reset>"
    ),
    TRAVEL_ICON_EXAMPLE(
        "§7Example: §f/travel icon checkpoint1 minecraft:diamond_block",
        "§7Ví dụ: §f/travel icon checkpoint1 minecraft:diamond_block"
    ),
    TRAVEL_INVALID_ID(
        "§cInvalid checkpoint ID. Use checkpoint1 → checkpoint%max%",
        "§cID điểm không hợp lệ. Dùng checkpoint1 → checkpoint%max%"
    ),
    TRAVEL_INVALID_ID_RENAME(
        "§cYou can only rename using checkpoint ID (checkpoint1-%max%)!",
        "§cChỉ đổi tên bằng ID điểm (checkpoint1-%max%)!"
    ),
    TRAVEL_INVALID_ID_DELETE(
        "§cYou must use a valid checkpoint ID (checkpoint1-%max%)!",
        "§cPhải dùng ID điểm hợp lệ (checkpoint1-%max%)!"
    ),
    TRAVEL_INVALID_ID_ICON(
        "§cInvalid checkpoint ID (checkpoint1–checkpoint%max%)",
        "§cID điểm không hợp lệ (checkpoint1–checkpoint%max%)"
    ),
    TRAVEL_CHECKPOINT_NOT_EXIST(
        "§cCheckpoint does not exist!",
        "§cĐiểm kiểm tra không tồn tại!"
    ),
    TRAVEL_CHECKPOINT_NOT_SET(
        "§cThis checkpoint has not been set yet!",
        "§cĐiểm kiểm tra này chưa được đặt!"
    ),
    TRAVEL_CHECKPOINT_EMPTY(
        "§cThis checkpoint is already empty!",
        "§cĐiểm kiểm tra này đã trống rồi!"
    ),
    TRAVEL_STARTING(
        "§eFast travel starting... do not move",
        "§eDịch chuyển nhanh đang bắt đầu... đừng di chuyển"
    ),
    TRAVEL_COUNTDOWN(
        "§7%timer%...",
        "§7%timer%..."
    ),
    TRAVEL_CANCELLED_MOVED(
        "§cCanceled: you moved!",
        "§cĐã hủy: bạn đã di chuyển!"
    ),
    TRAVEL_TELEPORTED(
        "§aTeleported to §e%name%",
        "§aĐã dịch chuyển đến §e%name%"
    ),
    TRAVEL_SAVED(
        "§aSaved checkpoint §e%name%",
        "§aĐã lưu điểm kiểm tra §e%name%"
    ),
    TRAVEL_DELETE_CONFIRM_PROMPT(
        "§eAre you sure you want to delete §c%id%§e?",
        "§eBạn chắc chắn muốn xóa §c%id%§e?"
    ),
    TRAVEL_DELETE_CONFIRM_CMD(
        "§7Type: §f/travel delete %id% confirm",
        "§7Gõ: §f/travel delete %id% confirm"
    ),
    TRAVEL_DELETED(
        "§aCheckpoint §e%id% §ahas been permanently deleted.",
        "§aĐiểm §e%id% §ađã bị xóa vĩnh viễn."
    ),
    TRAVEL_RENAMED(
        "§aCheckpoint §e%id% §arenamed to §e%name%",
        "§aĐiểm §e%id% §ađã đổi tên thành §e%name%"
    ),
    TRAVEL_SLOTS_INFO(
        "§7Your travel slots: §e%current% §7(max 54)",
        "§7Số ô di chuyển của bạn: §e%current% §7(tối đa 54)"
    ),
    TRAVEL_SLOTS_USAGE_HINT(
        "§7Usage: §f/travel slots <1-54>",
        "§7Cách dùng: §f/travel slots <1-54>"
    ),
    TRAVEL_SLOTS_INVALID(
        "§cInvalid number. Usage: /travel slots <1-54>",
        "§cSố không hợp lệ. Cách dùng: /travel slots <1-54>"
    ),
    TRAVEL_SLOTS_RANGE(
        "§cSlot count must be between §e1 §cand §e54§c.",
        "§cSố ô phải từ §e1 §cđến §e54§c."
    ),
    TRAVEL_SLOTS_CONFLICT(
        "§cCannot reduce to §e%new%§c: you have a checkpoint at slot §e%highest%§c.",
        "§cKhông thể giảm xuống §e%new%§c: bạn có điểm tại ô §e%highest%§c."
    ),
    TRAVEL_SLOTS_CONFLICT_HINT(
        "§7Delete §fcheckpoint%highest% §7first: §f/travel delete checkpoint%highest%",
        "§7Hãy xóa §fcheckpoint%highest% §7trước: §f/travel delete checkpoint%highest%"
    ),
    TRAVEL_SLOTS_SET(
        "§aTravel slots set to §e%new%§a. Reopen §f/travel §ato see changes.",
        "§aĐã đặt số ô thành §e%new%§a. Mở lại §f/travel §ađể xem thay đổi."
    ),
    TRAVEL_ICON_RESET(
        "§aIcon for §e%id% §areset to default.",
        "§aBiểu tượng của §e%id% §ađã về mặc định."
    ),
    TRAVEL_ICON_UNKNOWN(
        "§cUnknown material: §f%mat%",
        "§cVật liệu không tồn tại: §f%mat%"
    ),
    TRAVEL_ICON_UNKNOWN_HINT(
        "§7Try: §fminecraft:grass_block §7or §fDIAMOND_BLOCK",
        "§7Thử: §fminecraft:grass_block §7hoặc §fDIAMOND_BLOCK"
    ),
    TRAVEL_ICON_SET(
        "§aIcon for §e%id% §aset to §e%mat%§a.",
        "§aBiểu tượng của §e%id% §ađã đổi thành §e%mat%§a."
    ),
    TRAVEL_FALLBACK(
        "§cUnknown subcommand. Usage:",
        "§cLệnh phụ không hợp lệ. Cách dùng:"
    ),
    TRAVEL_FALLBACK_MENU(
        "§7/travel §8— open fast travel menu",
        "§7/travel §8— mở menu di chuyển nhanh"
    ),
    TRAVEL_FALLBACK_SAVE(
        "§7/travel save <checkpoint1-N> [name]",
        "§7/travel save <checkpoint1-N> [tên]"
    ),
    TRAVEL_FALLBACK_LOAD(
        "§7/travel load <name or id>",
        "§7/travel load <tên hoặc id>"
    ),
    TRAVEL_FALLBACK_NAME(
        "§7/travel name <checkpoint1-N> <new name>",
        "§7/travel name <checkpoint1-N> <tên mới>"
    ),
    TRAVEL_FALLBACK_DELETE(
        "§7/travel delete <checkpoint1-N>",
        "§7/travel delete <checkpoint1-N>"
    ),
    TRAVEL_FALLBACK_SLOTS(
        "§7/travel slots <1-54> §8— set your slot count",
        "§7/travel slots <1-54> §8— đặt số ô di chuyển"
    ),
    TRAVEL_FALLBACK_ICON(
        "§7/travel icon <checkpoint> <material|reset> §8— set slot icon",
        "§7/travel icon <checkpoint> <vật liệu|reset> §8— đặt biểu tượng ô"
    ),

    // ──────────────────────────────────────────────────────────
    //  HeightLock
    // ──────────────────────────────────────────────────────────

    HL_NO_PERM(
        "§cYou don't have permission to use height lock.",
        "§cBạn không có quyền dùng khóa độ cao."
    ),
    HL_ALREADY_OFF(
        "§7[HeightLock] §eHeight lock is already off.",
        "§7[HeightLock] §eKhóa độ cao đã tắt rồi."
    ),
    HL_ALREADY_ON(
        "§7[HeightLock] §eAlready locked at Y=%y%.",
        "§7[HeightLock] §eĐã khóa tại Y=%y% rồi."
    ),
    HL_OUT_OF_RANGE(
        "§cY value out of range (must be between -64 and 320).",
        "§cGiá trị Y ngoài phạm vi (phải từ -64 đến 320)."
    ),
    HL_USAGE(
        "§cUsage: /heightlock [<y>|on|off]",
        "§cCách dùng: /heightlock [<y>|on|off]"
    ),
    HL_LOCKED(
        "§7[HeightLock] §aLocked at Y=%y% — elevation is frozen.",
        "§7[HeightLock] §aĐã khóa tại Y=%y% — độ cao bị cố định."
    ),
    HL_REACTIVATED(
        "§7[HeightLock] §aReactivated at Y=%y%",
        "§7[HeightLock] §aĐã kích hoạt lại tại Y=%y%"
    ),
    HL_UNLOCKED(
        "§7[HeightLock] §cUnlocked.",
        "§7[HeightLock] §cĐã mở khóa."
    ),

    // ──────────────────────────────────────────────────────────
    //  Measure
    // ──────────────────────────────────────────────────────────

    MEASURE_NO_PERM(
        "§cYou don't have permission to use measure tools.",
        "§cBạn không có quyền dùng công cụ đo lường."
    ),
    MEASURE_USAGE(
        "§cUsage: /measure <distance|center>",
        "§cCách dùng: /measure <distance|center>"
    ),
    MEASURE_PREV_CANCELLED(
        "§7[Measure] Previous session cancelled.",
        "§7[Measure] Phiên đo trước đã bị hủy."
    ),
    MEASURE_START_DISTANCE(
        "§7[Measure] §bDistance §7mode started — §fleft-click §7a block for §aPoint 1§7.",
        "§7[Measure] Chế độ §bkhoảng cách §7đã bắt đầu — §fnhấp trái §7vào block để đặt §aĐiểm 1§7."
    ),
    MEASURE_START_CENTER(
        "§7[Measure] §dCenter §7mode started — §fleft-click §7a block for §aPoint 1§7.",
        "§7[Measure] Chế độ §dtâm §7đã bắt đầu — §fnhấp trái §7vào block để đặt §aĐiểm 1§7."
    ),
    MEASURE_P1_SET(
        "§7[Measure] §aPoint 1 §7set at §f%coords% §7— §fright-click §7a block for §aPoint 2§7.",
        "§7[Measure] §aĐiểm 1 §7đã đặt tại §f%coords% §7— §fnhấp phải §7vào block để đặt §aĐiểm 2§7."
    ),
    MEASURE_P1_FIRST(
        "§c[Measure] Set Point 1 first (left-click a block).",
        "§c[Measure] Hãy đặt Điểm 1 trước (nhấp trái vào block)."
    ),
    MEASURE_DIFF_WORLD(
        "§c[Measure] Both points must be in the same world.",
        "§c[Measure] Cả hai điểm phải ở cùng thế giới."
    ),
    MEASURE_CANCEL_DROP(
        "§7[Measure] §cSession cancelled.",
        "§7[Measure] §cPhiên đo đã bị hủy."
    ),
    MEASURE_HINT_P1(
        "§7[Measure] §fLeft-click §7on a §fblock §7to set §aPoint 1§7.",
        "§7[Measure] §fNhấp trái §7vào §fblock §7để đặt §aĐiểm 1§7."
    ),
    MEASURE_HINT_P2(
        "§7[Measure] §fRight-click §7on a §fblock §7to set §aPoint 2§7.",
        "§7[Measure] §fNhấp phải §7vào §fblock §7để đặt §aĐiểm 2§7."
    ),
    MEASURE_HINT_P2_FIRST(
        "§c[Measure] Set Point 1 first (left-click a block).",
        "§c[Measure] Hãy đặt Điểm 1 trước (nhấp trái vào block)."
    ),
    // Distance output lines
    MEASURE_DIST_TITLE(
        "§6§l  MEASURE — DISTANCE",
        "§6§l  ĐO LƯỜNG — KHOẢNG CÁCH"
    ),
    MEASURE_POINT1(
        "§7  Point 1: §f%coords%",
        "§7  Điểm 1: §f%coords%"
    ),
    MEASURE_POINT2(
        "§7  Point 2: §f%coords%",
        "§7  Điểm 2: §f%coords%"
    ),
    MEASURE_WIDTH(
        "§f  Width  (X): §e%val%",
        "§f  Chiều rộng (X): §e%val%"
    ),
    MEASURE_HEIGHT_Y(
        "§f  Height (Y): §e%val%",
        "§f  Chiều cao (Y): §e%val%"
    ),
    MEASURE_LENGTH(
        "§f  Length (Z): §e%val%",
        "§f  Chiều dài (Z): §e%val%"
    ),
    MEASURE_FLAT_DIST(
        "§f  Flat distance  (2D): §a%val%",
        "§f  Khoảng cách phẳng (2D): §a%val%"
    ),
    MEASURE_TOTAL_DIST(
        "§f  Total distance (3D): §a%val%",
        "§f  Tổng khoảng cách (3D): §a%val%"
    ),
    MEASURE_BLOCK_UNIT(
        "%n% block",
        "%n% khối"
    ),
    MEASURE_BLOCK_UNIT_PLURAL(
        "%n% blocks",
        "%n% khối"
    ),
    // Center output lines
    MEASURE_CENTER_TITLE(
        "§d§l  MEASURE — CENTER",
        "§d§l  ĐO LƯỜNG — TÂM ĐIỂM"
    ),
    MEASURE_AREA(
        "§f  Area: §e%dx% × %dz% blocks",
        "§f  Vùng: §e%dx% × %dz% khối"
    ),
    MEASURE_CENTER_XZ(
        "§f  Center X: §e%cx%  §fZ: §e%cz%",
        "§f  Tâm X: §e%cx%  §fZ: §e%cz%"
    ),
    MEASURE_EXACT_ODD(
        "§aExact center (odd × odd area)",
        "§aTâm chính xác (vùng lẻ × lẻ)"
    ),
    MEASURE_EVEN_4(
        "§eCenter between 4 blocks (even × even)",
        "§eTâm giữa 4 khối (vùng chẵn × chẵn)"
    ),
    MEASURE_EVEN_X(
        "§eCenter between 2 blocks on X axis",
        "§eTâm giữa 2 khối theo trục X"
    ),
    MEASURE_EVEN_Z(
        "§eCenter between 2 blocks on Z axis",
        "§eTâm giữa 2 khối theo trục Z"
    ),
    MEASURE_TP_LABEL(
        "[✦ Teleport to Center]",
        "[✦ Dịch Chuyển đến Tâm]"
    ),
    MEASURE_TP_HOVER_LINE1(
        "Click to teleport to:",
        "Nhấp để dịch chuyển đến:"
    ),
    MEASURE_TP_HOVER_LINE2(
        "X=%cx%  Z=%cz%  Y=<current>",
        "X=%cx%  Z=%cz%  Y=<hiện tại>"
    ),
    MEASURE_COMPASS_MODE_DISTANCE(
        "§bMode: Distance",
        "§bChế độ: Khoảng cách"
    ),
    MEASURE_COMPASS_MODE_CENTER(
        "§dMode: Center",
        "§dChế độ: Tâm điểm"
    ),
    MEASURE_COMPASS_LORE_CANCEL(
        "§7Drop compass to cancel.",
        "§7Thả la bàn để hủy đo lường."
    ),
    MEASURE_COMPASS_P1(
        "§fLeft-click  §7→ Point 1 ",
        "§fNhấp trái  §7→ Điểm 1 "
    ),
    MEASURE_COMPASS_P2(
        "§fRight-click §7→ Point 2 §8○",
        "§fNhấp phải §7→ Điểm 2 §8○"
    ),

    // ──────────────────────────────────────────────────────────
    //  DayLength
    // ──────────────────────────────────────────────────────────

    DL_NO_PERM(
        "§cYou don't have permission to use this command.",
        "§cBạn không có quyền dùng lệnh này."
    ),
    DL_CURRENT(
        "§6[DayLength] §7Current day length: §e%min% minutes %status%",
        "§6[DayLength] §7Độ dài ngày hiện tại: §e%min% phút %status%"
    ),
    DL_VANILLA_TAG(
        "§7(vanilla default)",
        "§7(mặc định vanilla)"
    ),
    DL_CUSTOM_TAG(
        "§e(custom)",
        "§e(tùy chỉnh)"
    ),
    DL_USAGE(
        "§7Usage: §f/daylength <minutes> §7(1–720) §8| §f/daylength reset",
        "§7Cách dùng: §f/daylength <phút> §7(1–720) §8| §f/daylength reset"
    ),
    DL_RESET(
        "§6[DayLength] §aReset to vanilla default §7(20 minutes per day).",
        "§6[DayLength] §aĐã đặt lại về mặc định vanilla §7(20 phút mỗi ngày)."
    ),
    DL_SET_VANILLA(
        "§6[DayLength] §aSet to vanilla default §7(20 minutes).",
        "§6[DayLength] §aĐặt về mặc định vanilla §7(20 phút)."
    ),
    DL_SET(
        "§6[DayLength] §aDay length set to §e%min% minutes§a per full cycle.",
        "§6[DayLength] §aĐã đặt độ dài ngày thành §e%min% phút§a mỗi chu kỳ."
    ),
    DL_INVALID(
        "§cInvalid number. Usage: §f/daylength <minutes> §cor §f/daylength reset",
        "§cSố không hợp lệ. Cách dùng: §f/daylength <phút> §choặc §f/daylength reset"
    ),
    DL_RANGE(
        "§cDay length must be between §e1 §cand §e720 §cminutes.",
        "§cĐộ dài ngày phải từ §e1 §cđến §e720 §cphút."
    ),

    // ──────────────────────────────────────────────────────────
    //  Scoreboard
    // ──────────────────────────────────────────────────────────

    SB_USAGE(
        "§cUsage: /scoreboard on|off",
        "§cCách dùng: /scoreboard on|off"
    ),
    SB_OFF(
        "§eScoreboard disabled",
        "§eĐã tắt bảng điểm"
    ),
    SB_ON(
        "§aScoreboard enabled",
        "§aĐã bật bảng điểm"
    ),
    // Scoreboard body lines (buildLines)
    SB_LINE_PING(
        "§fPing: §e%ping%ms §7| §f%world%",
        "§fPing: §e%ping%ms §7| §f%world%"
    ),
    SB_LINE_ONLINE(
        "§fOnline: §a%online% §7| §fStaff: §a%staff%",
        "§fOnline: §a%online% §7| §fNhân viên: §a%staff%"
    ),

    // ──────────────────────────────────────────────────────────
    //  Glass
    // ──────────────────────────────────────────────────────────

    GLASS_NO_PERM(
        "§cYou don't have permission to use this command.",
        "§cBạn không có quyền dùng lệnh này."
    ),
    GLASS_PLACED(
        "§7Placed §fglass §7at §e%x% %y% %z%",
        "§7Đã đặt §fkính §7tại §e%x% %y% %z%"
    ),

    // ──────────────────────────────────────────────────────────
    //  SpeedFly
    // ──────────────────────────────────────────────────────────

    SF_NO_PERM(
        "§cYou don't have permission to use speedfly.",
        "§cBạn không có quyền dùng speedfly."
    ),
    SF_ALREADY_HAS_WING(
        "§7You already have a §eSpeed Wing§7. Check your inventory.",
        "§7Bạn đã có §eSpeed Wing§7 rồi. Kiểm tra túi đồ."
    ),
    SF_WING_ADDED(
        "§a⚡ Speed Wing §2added to your inventory. §7Right-click to toggle.",
        "§a⚡ Speed Wing §2đã thêm vào túi đồ. §7Nhấp phải để bật/tắt."
    ),
    SF_SPEED_RANGE(
        "§cSpeed must be between §e1 §cand §e10§c.",
        "§cTốc độ phải từ §e1 §cđến §e10§c."
    ),
    SF_ON(
        "§aSpeedFly §2ON §7— speed: §e%speed%§7x",
        "§aSpeedFly §2BẬT §7— tốc độ: §e%speed%§7x"
    ),
    SF_STATUS(
        "§7Speed Wing: §e%speed%x §7— right-click to toggle | §f/speedfly <1-10> §7to change speed",
        "§7Speed Wing: §e%speed%x §7— nhấp phải để bật/tắt | §f/speedfly <1-10> §7để đổi tốc độ"
    ),
    SF_OFF(
        "§cSpeedFly §4OFF",
        "§cSpeedFly §4TẮT"
    ),
    SF_USAGE(
        "§cUsage: /speedfly [1-10|tool]",
        "§cCách dùng: /speedfly [1-10|tool]"
    ),

    // ──────────────────────────────────────────────────────────
    //  Kits
    // ──────────────────────────────────────────────────────────

    KITS_NO_PERM(
        "§cYou don't have permission to use kits.",
        "§cBạn không có quyền dùng bộ trang bị."
    ),
    KIT_NO_PENDING(
        "§cNo pending kit.",
        "§cKhông có bộ trang bị nào đang chờ."
    ),
    KIT_EXPIRED(
        "§cKit selection expired (30s).",
        "§cBộ trang bị đã hết hạn chọn (30 giây)."
    ),
    KIT_CANCELLED(
        "§cKit selection cancelled.",
        "§cĐã hủy chọn bộ trang bị."
    ),
    KIT_APPLIED_SEP(
        "§7──────────────────────────────",
        "§7──────────────────────────────"
    ),
    KIT_APPLIED(
        "§6  Kit applied: §e%name%",
        "§6  Đã trang bị: §e%name%"
    ),
    KIT_CONFIRM_MSG(
        "§eApply kit §6%name%§e?",
        "§eTrang bị bộ §6%name%§e?"
    ),
    KIT_CONFIRM_YES(
        "§a[Confirm]",
        "§a[Xác nhận]"
    ),
    KIT_CONFIRM_NO(
        "§c[Cancel]",
        "§c[Hủy]"
    ),
    KIT_HOVER_CONFIRM(
        "Click to confirm",
        "Nhấp để xác nhận"
    ),
    KIT_HOVER_CANCEL(
        "Click to cancel",
        "Nhấp để hủy"
    ),

    // ──────────────────────────────────────────────────────────
    //  Horse
    // ──────────────────────────────────────────────────────────

    HORSE_NO_PERM(
        "§cYou don't have permission to summon a horse.",
        "§cBạn không có quyền triệu hồi ngựa chiến."
    ),
    HORSE_USAGE(
        "§cUsage: /horse <breed> [armor] [name]\n§7Breeds: §fphantom §8| §fdeath §8| §fghost §8| §fstormbringer §8| §fgoldenking\n§7Armor: §fnone §8| §firon §8| §fgold §8| §fdiamond §8| §fnetherite\n§7Other: §f/horse dismiss",
        "§cCách dùng: /horse <giống> [giáp] [tên]\n§7Giống: §fphantom §8| §fdeath §8| §fghost §8| §fstormbringer §8| §fgoldenking\n§7Giáp: §fnone §8| §firon §8| §fgold §8| §fdiamond §8| §fnetherite\n§7Khác: §f/horse dismiss"
    ),
    HORSE_UNKNOWN_BREED(
        "§cUnknown breed: §f%breed%",
        "§cGiống ngựa không tồn tại: §f%breed%"
    ),
    HORSE_NO_ARMOR_NOTE(
        "§7Note: §f%breed% §7cannot wear armor — armor ignored.",
        "§7Lưu ý: §f%breed% §7không thể mang giáp — giáp bị bỏ qua."
    ),
    HORSE_NO_PENDING(
        "§cNo pending horse spawn.",
        "§cKhông có ngựa nào đang chờ triệu hồi."
    ),
    HORSE_EXPIRED(
        "§cHorse spawn request expired.",
        "§cYêu cầu triệu hồi ngựa đã hết hạn."
    ),
    HORSE_SPAWN_CANCELLED(
        "§cHorse spawn cancelled.",
        "§cĐã hủy triệu hồi ngựa."
    ),
    HORSE_NO_ACTIVE(
        "§cYou don't have an active horse.",
        "§cBạn không có ngựa chiến đang hoạt động."
    ),
    HORSE_DISMISSED(
        "§aYour horse has been dismissed.",
        "§aNgựa chiến của bạn đã được cho về."
    ),
    HORSE_SUMMONED(
        "§a%label% summoned!",
        "§a%label% đã được triệu hồi!"
    ),
    HORSE_CONFIRM_MSG(
        "§eSummon §6%label%§e?",
        "§eTriệu hồi §6%label%§e?"
    ),
    HORSE_CONFIRM_YES(
        "§a[Confirm]",
        "§a[Xác nhận]"
    ),
    HORSE_CONFIRM_NO(
        "§c[Cancel]",
        "§c[Hủy]"
    ),
    HORSE_HOVER_CONFIRM(
        "Click to confirm",
        "Nhấp để xác nhận"
    ),
    HORSE_HOVER_CANCEL(
        "Click to cancel",
        "Nhấp để hủy"
    ),
    HORSE_NOT_YOURS(
        "§cThis is not your horse.",
        "§cĐây không phải ngựa của bạn."
    ),

    // ──────────────────────────────────────────────────────────
    //  Divine Weapon base
    // ──────────────────────────────────────────────────────────

    DIVINE_COOLDOWN(
        "§cCooldown: §e%.1fs",
        "§cThời gian chờ: §e%.1f giây"
    ),
    DIVINE_CHARGING(
        "§6Charging... right-click again to release.",
        "§6Đang nạp lực... nhấp phải lần nữa để phóng."
    ),

    // ──────────────────────────────────────────────────────────
    //  Excalibur
    // ──────────────────────────────────────────────────────────

    EXCALIBUR_NO_PERM(
        "§8Darkness §4does not §8choose the unworthy.",
        "§8Bóng tối §4không §8chọn kẻ không xứng."
    ),
    EXCALIBUR_ALREADY_HAS(
        "§8The blade is already with you. It has not forgotten.",
        "§8Lưỡi kiếm đã ở bên bạn. Nó chưa quên đâu."
    ),
    EXCALIBUR_GIVEN(
        "§4⚔ §8The blade stirs. §4It already knows your name.",
        "§4⚔ §8Lưỡi kiếm thức tỉnh. §4Nó đã biết tên bạn."
    ),
    EXCALIBUR_KICK_THEFT(
        "§8The darkness devours the unworthy.\n§4It does not spit them back out.",
        "§8Bóng tối nuốt chửng kẻ không xứng.\n§4Và không nhả ra."
    ),
    EXCALIBUR_KICK_HIT(
        "§8The darkness swallowed you whole.\n§4Dark Excalibur §8allows no survivors.",
        "§8Bóng tối đã nuốt bạn trọn vẹn.\n§4Dark Excalibur §8không để ai sống sót."
    ),
    EXCALIBUR_CAST(
        "§8§lThe darkness §4flows §8§lthrough the void.",
        "§8§lBóng tối §4chảy §8§lxuyên qua hư không."
    ),
    EXCALIBUR_BROADCAST(
        "§0§l⚔ §4§l≪ DARK EXCALIBUR ≫ §0§l⚔ §r§8has been drawn — §4darkness §8follows.",
        "§0§l⚔ §4§l≪ DARK EXCALIBUR ≫ §0§l⚔ §r§8đã được tuốt ra — §4bóng tối §8theo sau."
    ),

    // ──────────────────────────────────────────────────────────
    //  Ragnarok
    // ──────────────────────────────────────────────────────────

    RAGNAROK_NO_PERM(
        "§7Ragnarok §ccannot §7be summoned. §cIt chooses §7its wielder.",
        "§7Ragnarok §ckhông thể §7được triệu hồi. §cNó chọn §7người cầm nó."
    ),
    RAGNAROK_ALREADY_HAS(
        "§cThe storm already answers to you. Check your inventory.",
        "§cCơn bão đã phục tùng bạn rồi. Kiểm tra túi đồ."
    ),
    RAGNAROK_GIVEN(
        "§c§l⚡ §4Ragnarok §c§l⚡ §7acknowledges its new master.",
        "§c§l⚡ §4Ragnarok §c§l⚡ §7thừa nhận chủ nhân mới."
    ),
    RAGNAROK_KICK_THEFT(
        "§cThe storm does not serve thieves.\n§7Only the chosen may wield Ragnarok.",
        "§cCơn bão không phục vụ kẻ trộm.\n§7Chỉ người được chọn mới cầm được Ragnarok."
    ),
    RAGNAROK_KICK_HIT(
        "§c⚡ RAGNAROK — STORM VERDICT ⚡\n§7The thunder has spoken.\n§cYou were not worthy of its mercy.",
        "§c⚡ RAGNAROK — PHÁN QUYẾT BÃO §7Sấm sét đã lên tiếng.\n§cBạn không xứng được tha."
    ),
    RAGNAROK_BROADCAST(
        "§c§l⚡ §4RAGNAROK §c§l⚡ §r§7has been unleashed — §cthe storm arrives.",
        "§c§l⚡ §4RAGNAROK §c§l⚡ §r§7đã được giải phóng — §ccơn bão ập đến."
    ),

    // ──────────────────────────────────────────────────────────
    //  Ignis
    // ──────────────────────────────────────────────────────────

    IGNIS_NO_PERM(
        "§cThe core rejects you. You are not its vessel.",
        "§cLõi lửa từ chối bạn. Bạn không phải vật chứa của nó."
    ),
    IGNIS_ALREADY_HAS(
        "§6The Ignis Core already burns within you.",
        "§6Ignis Core đã cháy bên trong bạn rồi."
    ),
    IGNIS_GIVEN(
        "§6§l✦ §eThe Ignis Core §6ignites. §7You can feel it breathing.",
        "§6§l✦ §eIgnis Core §6bùng cháy. §7Bạn có thể cảm nhận nó đang thở."
    ),
    IGNIS_KICK_THEFT(
        "§6Fire does not burn for thieves.\n§eThe Ignis Core returns to its master.",
        "§6Lửa không bùng cháy cho kẻ trộm.\n§eIgnis Core trở về với chủ nhân."
    ),
    IGNIS_KICK_HIT(
        "§6§l✦ IGNIS CORE — INFERNO VERDICT ✦\n§eYou burned.\n§6Nothing remains but ash.",
        "§6§l✦ IGNIS CORE — PHÁN QUYẾT HỎA NGỤC ✦\n§eBạn đã bị thiêu rụi.\n§6Chỉ còn tro tàn."
    ),
    IGNIS_BROADCAST(
        "§6§l✦ §eIGNIS CORE §6§l✦ §r§7has awakened — §6the inferno begins.",
        "§6§l✦ §eIGNIS CORE §6§l✦ §r§7đã thức tỉnh — §6địa ngục lửa bắt đầu."
    ),

    // ──────────────────────────────────────────────────────────
    //  Grave Sovereign
    // ──────────────────────────────────────────────────────────

    GRAVE_NO_PERM(
        "§8The grave does not open for the unworthy.",
        "§8Nấm mộ không mở ra cho kẻ không xứng."
    ),
    GRAVE_ALREADY_HAS(
        "§8The Grave Sovereign is already bound to your soul.",
        "§8Grave Sovereign đã gắn với linh hồn bạn rồi."
    ),
    GRAVE_GIVEN(
        "§8§l† §7The Grave Sovereign §8rises from the earth. §8§l†",
        "§8§l† §7Grave Sovereign §8trỗi dậy từ lòng đất. §8§l†"
    ),
    GRAVE_KICK_THEFT(
        "§8The grave claimed what you tried to steal.\n§7It keeps what it takes.",
        "§8Nấm mộ đã lấy lại những gì bạn cố đánh cắp.\n§7Nó giữ những gì nó lấy."
    ),
    GRAVE_KICK_HIT(
        "§8§l† GRAVE SOVEREIGN — DEATH VERDICT †\n§7You have been claimed.\n§8The grave does not return its dead.",
        "§8§l† GRAVE SOVEREIGN — PHÁN QUYẾT TỬ THẦN †\n§7Bạn đã bị mang đi.\n§8Nấm mộ không trả lại người chết."
    ),
    GRAVE_BROADCAST(
        "§8§l† §7GRAVE SOVEREIGN §8§l† §r§8has awakened — §7death walks among you.",
        "§8§l† §7GRAVE SOVEREIGN §8§l† §r§8đã thức tỉnh — §7thần chết đang đi giữa các bạn."
    ),

    // ──────────────────────────────────────────────────────────
    //  Verdant Cipher
    // ──────────────────────────────────────────────────────────

    VERDANT_NO_PERM(
        "§2The forest does not recognize you.",
        "§2Khu rừng không nhận ra bạn."
    ),
    VERDANT_ALREADY_HAS(
        "§aThe Verdant Cipher is already woven into your hands.",
        "§aVerdant Cipher đã dệt vào đôi tay bạn rồi."
    ),
    VERDANT_GIVEN(
        "§2§l✿ §aThe Verdant Cipher §2grows toward you. §2§l✿",
        "§2§l✿ §aVerdant Cipher §2lớn về phía bạn. §2§l✿"
    ),
    VERDANT_KICK_THEFT(
        "§2This cipher was written for one hand only.\n§aThe land rejects you.",
        "§2Mật mã này chỉ được viết cho một bàn tay.\n§aMặt đất từ chối bạn."
    ),
    VERDANT_MODE(
        "§2§l🌿 §aArea: §2%area% §a— right-click to till and ripen.",
        "§2§l🌿 §aVùng: §2%area% §a— click chuột phải để cày và làm chín."
    ),

    // ──────────────────────────────────────────────────────────
    //  Void Constellation (VoidBow)
    // ──────────────────────────────────────────────────────────

    VOID_NO_PERM(
        "§9The void does not acknowledge you.",
        "§9Hư không không thừa nhận bạn."
    ),
    VOID_ALREADY_HAS(
        "§9The Void Constellation is already yours. The stars remember.",
        "§9Void Constellation đã là của bạn rồi. Các vì sao còn nhớ."
    ),
    VOID_GIVEN(
        "§9§l✦ §3The Void Constellation §9stirs. §9§l✦",
        "§9§l✦ §3Void Constellation §9lay động. §9§l✦"
    ),
    VOID_KICK_THEFT(
        "§9The void found something to claim after all.\n§8It was you.",
        "§9Hư không cuối cùng đã tìm thấy thứ để đòi lại.\n§8Đó là bạn."
    ),

    // ──────────────────────────────────────────────────────────
    //  Spear of Justice
    // ──────────────────────────────────────────────────────────

    SPEAR_NO_PERM(
        "§eJustice §7does not arm the guilty.",
        "§eSự công bằng §7không trao vũ khí cho kẻ có tội."
    ),
    SPEAR_ALREADY_HAS(
        "§eThe Spear of Justice is already bound to your oath.",
        "§eSpear of Justice đã gắn với lời thề của bạn rồi."
    ),
    SPEAR_GIVEN(
        "§e§l⚖ §7Justice §erecognizes you. §7The spear §eleaps to your hand. §e§l⚖",
        "§e§l⚖ §7Công lý §ennhận ra bạn. §7Ngọn giáo §enbật vào tay bạn. §e§l⚖"
    ),
    SPEAR_KICK_THEFT(
        "§eJustice §7saw what you tried to do.\n§eIt remembers everything.",
        "§eCông lý §7đã thấy những gì bạn làm.\n§eNó nhớ tất cả."
    ),
    SPEAR_KICK_HIT(
        "§e⚖ SPEAR OF JUSTICE ⚖\n§7You were found guilty.\n§eThe verdict is final.",
        "§e⚖ SPEAR OF JUSTICE ⚖\n§7Bạn đã bị kết tội.\n§ePhán quyết là cuối cùng."
    ),

    // ──────────────────────────────────────────────────────────
    //  Divine Crossbow (Nothan)
    // ──────────────────────────────────────────────────────────

    NOTHAN_NO_PERM(
        "§6Heaven's arm was not meant for you.",
        "§6Cánh tay thiên đình không dành cho bạn."
    ),
    NOTHAN_ALREADY_HAS(
        "§6The Divine Crossbow is already in your keeping.",
        "§6Nỏ Thần đã ở trong tay bạn rồi."
    ),
    NOTHAN_GIVEN(
        "§6§l✦ §eThe Divine Crossbow §6descends. §6§l✦",
        "§6§l✦ §eNỏ Thần §6giáng xuống. §6§l✦"
    ),
    NOTHAN_KICK_THEFT(
        "§6Heaven's arm was drawn against you.\n§7You touched what was never meant for your hands.",
        "§6Cánh tay thiên đình đã chĩa vào bạn.\n§7Bạn đã chạm vào thứ không dành cho bạn."
    ),
    NOTHAN_CAST(
        "§6§l✦ §eHeavenly decree issued. §6§l✦ §7%count% judged.",
        "§6§l✦ §eThiên lệnh đã ban ra. §6§l✦ §7%count% người bị phán xét."
    ),

    // ──────────────────────────────────────────────────────────
    //  Abyssal Sovereign (Trident)
    // ──────────────────────────────────────────────────────────

    ABYSSAL_NO_PERM(
        "§3The abyss does not answer your call.",
        "§3Vực thẳm không đáp lời bạn."
    ),
    ABYSSAL_ALREADY_HAS(
        "§3The Abyssal Sovereign is already bound to your soul.",
        "§3Abyssal Sovereign đã gắn với linh hồn bạn rồi."
    ),
    ABYSSAL_GIVEN(
        "§b⚓ §3The Abyssal Sovereign §brises from the deep. §b⚓",
        "§b⚓ §3Abyssal Sovereign §btrỗi dậy từ vực thẳm. §b⚓"
    ),
    ABYSSAL_KICK_THEFT(
        "§3The abyss does not forget\n§bThis weapon was never yours",
        "§3Vực thẳm không quên\n§bVũ khí này không bao giờ là của bạn"
    ),
    ABYSSAL_KICK_HIT(
        "§b⚓ THE ABYSS CLAIMS YOU ⚓\n\n§3The crushing deep has swallowed you whole...",
        "§b⚓ VỰC THẲM ĐÒI MẠNG BẠN ⚓\n\n§3Biển sâu đã nuốt bạn trọn vẹn..."
    ),

    // ──────────────────────────────────────────────────────────
    //  God Mace
    // ──────────────────────────────────────────────────────────

    GODMACE_NO_PERM(
        "§6The gods §chave no use §6for the undeserving.",
        "§6Thần linh §ckhông cần §6kẻ không xứng."
    ),
    GODMACE_ALREADY_HAS(
        "§6The GOD MACE is already in your hands. Heaven awaits.",
        "§6GOD MACE đã ở trong tay bạn. Thiên đàng đang chờ."
    ),
    GODMACE_GIVEN(
        "§6§l✞ §eThe GOD MACE §6descends into mortal hands. §6§l✞",
        "§6§l✞ §eGOD MACE §6giáng xuống vào đôi tay người phàm. §6§l✞"
    ),
    GODMACE_KICK_THEFT(
        "§6✞ The gods do not share their instruments. §c✞\n§eFool. Your name is already forgotten.",
        "§6✞ Thần linh không chia sẻ công cụ của mình. §c✞\n§eKẻ ngốc. Tên bạn đã bị quên rồi."
    ),
    GODMACE_KICK_VERDICT(
        "§6§l✞ GOD MACE — DIVINE VERDICT ✞\n§eYou were weighed.\n§6You were found wanting.\n§cReturn if you dare.",
        "§6§l✞ GOD MACE — PHÁN QUYẾT THẦN THÁNH ✞\n§eBạn đã được cân đong.\n§6Bạn bị xem là thiếu xứng.\n§cQuay lại nếu bạn dám."
    ),

    // ──────────────────────────────────────────────────────────
    //  MainPlugin join / quit
    // ──────────────────────────────────────────────────────────

    JOIN_OWNER(
        "%color%GOD HAS COME",
        "%color%THẦN LINH ĐÃ ĐẾN"
    ),
    JOIN_PLAYER(
        "%color%[%rank%] %name% joined the game",
        "%color%[%rank%] %name% đã vào máy chủ"
    ),
    QUIT_OWNER(
        "%color%GOD HAS LEFT",
        "%color%THẦN LINH ĐÃ RỜI ĐI"
    ),
    QUIT_PLAYER(
        "%color%[%rank%] %name% left the game",
        "%color%[%rank%] %name% đã rời máy chủ"
    ),

    // ──────────────────────────────────────────────────────────
    //  Listener cast / feedback messages
    // ──────────────────────────────────────────────────────────

    RAGNAROK_CAST(
        "§c§l⚡ §4THE END §c§l⚡ §7sweeps the field.",
        "§c§l⚡ §4TẬN THẾ §c§l⚡ §7quét sạch chiến trường."
    ),
    IGNIS_CAST(
        "§6§l🔥 §eCORE DISCHARGED §6§l🔥 §7stone yields to fire.",
        "§6§l🔥 §eLÕI ĐÃ KHAI HỎA §6§l🔥 §7đá tảng quy phục lửa."
    ),
    GRAVE_CAST(
        "§5§l💀 §8The sovereign §5commands the earth. §8It answers.",
        "§5§l💀 §8Sovereign §5ra lệnh cho đất. §8Đất đã nghe."
    ),
    VERDANT_CAST(
        "§2§l✿ §aThe cipher §2blooms §7across the land.",
        "§2§l✿ §aMật mã §2nở rộ §7khắp đất đai."
    ),
    VOID_CAST(
        "§9§l✦ §3The stars have judged. §9§l✦",
        "§9§l✦ §3Các vì sao đã phán xét. §9§l✦"
    ),
    SPEAR_CAST(
        "§e⚖ §7Justice §epierces through.",
        "§e⚖ §7Công lý §exuyên thấu."
    );

    // ──────────────────────────────────────────────────────────

    private final String en;
    private final String vi;

    Msg(String en, String vi) {
        this.en = en;
        this.vi = vi;
    }

    /** Get raw string for given language. */
    public String get(Language lang) {
        return lang == Language.VIETNAMESE ? vi : en;
    }

    /** Get raw string for the given player's language. */
    public String get(Player p) {
        return get(LanguageManager.getLang(p));
    }

    /** Get English string (for console / non-player). */
    public String get() {
        return en;
    }

    /**
     * Get formatted string for a player's language, replacing %key% placeholders.
     * Pass pairs: "key1", value1, "key2", value2, ...
     */
    public String fmt(Player p, Object... pairs) {
        return fmt(LanguageManager.getLang(p), pairs);
    }

    /** Get formatted string for a specific language, replacing %key% placeholders. */
    public String fmt(Language lang, Object... pairs) {
        String s = get(lang);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            s = s.replace("%" + pairs[i] + "%", String.valueOf(pairs[i + 1]));
        }
        return s;
    }
}
