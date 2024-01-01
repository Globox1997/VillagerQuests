// package net.villagerquests.ftb;

// import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
// import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
// import net.minecraft.nbt.NbtCompound;
// import net.minecraft.network.PacketByteBuf;

// public class VillagerPerPlayerQuestData {
//     // private boolean canEdit;
//     // private boolean autoPin;
//     // private boolean chapterPinned;
//     // private final LongSet pinnedQuests;

//     private final Long2LongOpenHashMap villagerTaskProgress;
//     private final Long2LongOpenHashMap villagerStarted;
//     private final Long2LongOpenHashMap villagerCompleted;

//     // public VillagerPerPlayerQuestData() {
//     //     // this.canEdit = this.autoPin = this.chapterPinned = false;
//     //     // this.pinnedQuests = new LongOpenHashSet();
//     // }

//     public VillagerPerPlayerQuestData(boolean canEdit, boolean autoPin, boolean chapterPinned, LongSet pinnedQuests) {

//         this.villagerTaskProgress = new Long2LongOpenHashMap();
//         this.villagerStarted = new Long2LongOpenHashMap();
//         this.villagerCompleted = new Long2LongOpenHashMap();

//         // this.canEdit = canEdit;
//         // this.autoPin = autoPin;
//         // this.chapterPinned = chapterPinned;
//         // this.pinnedQuests = pinnedQuests;
//     }

//     public static VillagerPerPlayerQuestData fromNBT(NbtCompound nbt, BaseQuestFile file) {
//         boolean canEdit = nbt.getBoolean("can_edit");
//         boolean autoPin = nbt.getBoolean("auto_pin");
//         boolean chapterPinned = nbt.getBoolean("chapter_pinned");
//         LongSet pq = (LongSet) nbt.getList("pinned_quests", 8).stream().map((tag) -> {
//             return file.getID(tag.asString());
//         }).collect(Collectors.toCollection(LongOpenHashSet::new));
//         return new VillagerPerPlayerQuestData(canEdit, autoPin, chapterPinned, pq);
//     }

//     public static VillagerPerPlayerQuestData fromNet(PacketByteBuf buffer) {
//         TeamData.PerPlayerData ppd = new TeamData.PerPlayerData();
//         ppd.canEdit = buffer.readBoolean();
//         ppd.autoPin = buffer.readBoolean();
//         ppd.chapterPinned = buffer.readBoolean();
//         int pinnedCount = buffer.readVarInt();

//         for (int i = 0; i < pinnedCount; ++i) {
//             ppd.pinnedQuests.add(buffer.readLong());
//         }

//         return ppd;
//     }

//     public NbtCompound writeNBT() {
//         NbtCompound nbt = new NbtCompound();
//         if (this.canEdit) {
//             nbt.putBoolean("can_edit", true);
//         }

//         if (this.autoPin) {
//             nbt.putBoolean("auto_pin", true);
//         }

//         if (this.chapterPinned) {
//             nbt.putBoolean("chapter_pinned", true);
//         }

//         if (!this.pinnedQuests.isEmpty()) {
//             long[] pinnedQuestsArray = this.pinnedQuests.toLongArray();
//             Arrays.sort(pinnedQuestsArray);
//             NbtList pinnedQuestsNBT = new NbtList();
//             long[] var4 = pinnedQuestsArray;
//             int var5 = pinnedQuestsArray.length;

//             for (int var6 = 0; var6 < var5; ++var6) {
//                 long l = var4[var6];
//                 pinnedQuestsNBT.add(NbtString.of(QuestObjectBase.getCodeString(l)));
//             }

//             nbt.put("pinned_quests", pinnedQuestsNBT);
//         }

//         return nbt;
//     }

//     public void writeNet(PacketByteBuf buffer) {
//         buffer.writeBoolean(this.canEdit);
//         buffer.writeBoolean(this.autoPin);
//         buffer.writeBoolean(this.chapterPinned);
//         buffer.writeVarInt(this.pinnedQuests.size());
//         LongIterator var2 = this.pinnedQuests.iterator();

//         while (var2.hasNext()) {
//             long reward = (Long) var2.next();
//             buffer.writeLong(reward);
//         }

//     }
// }
