# VillagerQuests

VillagerQuests is a mod which adds skillables to the player which can get skilled to unlock blocks and strengthen passive
skills.

### Installation

VillagerQuests is a mod built for the [Fabric Loader](https://fabricmc.net/). It
requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately; all other
dependencies are installed with the mod.

### License

VillagerQuests is licensed under MIT.

### Datapacks

This mod doesn't add quests by itself so you or your modpack creator has to create them via datapacks.\
If you don't know how to create a datapack check out [Data Pack Wiki](https://minecraft.fandom.com/wiki/Data_Pack)
website and try to create your first one for the vanilla game.\
If you know how to create one, the folder path has to be ```data\villagerquests\quests\YOURFILE.json```

```
{
    "id": 3,
    "title": "Fourth Test Quest",
    "level": 0,
    "type": "fight",
    "profession": "toolsmith",
    "task": [
        "kill",
        "minecraft:zombie",
        5,
        "kill",
        "minecraft:skeleton",
        3,
        "farm",
        "minecraft:wheat",
        20,
        "submit",
        "minecraft:creeper_head",
        1
    ],
    "description": "This is a test description for the first quest",
    "experience": 10,
    "reward": [
        "minecraft:stick",
        10,
        "minecraft:bucket",
        1
    ]
}
```

### Necessary Content

"id": has to be a unique quest id.\
"title": is the title of the quest shown in the quest screen.\
"type": is the type of the quest which indicates the quest with an icon, can be empty. Types: "fight", "farm", "mine".\
"profession": is the villager profession which can get this quest. Check here for all availables: [Professions](https://minecraft.fandom.com/wiki/Villager#Professions).  
"task": is the quest tasks which the player has to complete to finish the quest. It has to be the task type, type object and object count.

* "kill" indicates which mobs have to get killed : "modid:entityname"
* "farm" indicates the items which have to get brought to the quest giver : "modid:itemname"
* "submit" indicates the items which have to get brought to the quest giver : "modid:itemname"
* "mine" indicates the items which have to get brought to the quest giver : "modid:itemname"
* "explore" or "travel" indicates the biome or structure where the player has to find : "modid:structurename" or "modid:biomename"

"description": is the quest description which will show up in the quest screen.\
"reward": is the item rewards when finishing the quest. It has to be the item identifier like "modid:itemname" and the item count.

### Additions

"level": is the villager profession level at which it can obtain the quest. If non set, the quest will be obtainable at level 0.\
"experience": is the amount of experience the player will receive on finishing the quest.\
"refresh": is the time in ticks at which the quest is available again from the same villager when it was finished, otherwise it won't show up for the specific player anymore.\
"timer": is the time in ticks in which the quest has to get done. Non-compliance leads to failure of the quest\
Info: 20 ticks in minecraft are 1 second in real life.

### Limitations

Titles shouldn't be longer than 20 characters.\
Descriptions shouldn't be longer than 220 characters.\
Tasks are limited to 4 different tasks.\
Rewards are limited to 3 different reward items.

### Info

Find itemnames here: https://minecraft.fandom.com/wiki/Java_Edition_data_values#Items\
Find entitynames here: https://minecraft.fandom.com/wiki/Java_Edition_data_values#Entities\
Find biomenames here: https://minecraft.fandom.com/wiki/Biome#Biome_IDs\
Find structurenames here:\
[minecraft:desert_pyramid, minecraft:village_taiga, minecraft:ruined_portal_swamp, minecraft:nether_fossil, minecraft:bastion_remnant, minecraft:pillager_outpost, minecraft:shipwreck, minecraft:jungle_pyramid, minecraft:ruined_portal_jungle, minecraft:mineshaft_mesa, minecraft:swamp_hut, minecraft:ocean_ruin_warm, minecraft:village_plains, minecraft:ruined_portal, minecraft:buried_treasure, minecraft:mansion, minecraft:ruined_portal_desert, minecraft:end_city, minecraft:mineshaft, minecraft:monument, minecraft:igloo, minecraft:ruined_portal_ocean, minecraft:ruined_portal_mountain, minecraft:fortress, minecraft:village_savanna, minecraft:ruined_portal_nether, minecraft:stronghold, minecraft:shipwreck_beached, minecraft:village_desert, minecraft:ocean_ruin_cold, minecraft:village_snowy]