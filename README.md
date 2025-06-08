# 🎯 CommunityGoals

**CommunityGoals** is a powerful Minecraft plugin (in active development) that enables server-wide goals for players to work toward collectively. Server owners can define custom milestones that, once met, unlock special rewards or features. It's highly configurable, supports future plugin integrations, and comes with a user-friendly GUI and in-game goal editor for admins.

---

## 🔮 Planned Features

* 🧩 **Dynamic Goal Types** – From killing mobs to mining blocks, participating in events, and more
* 🧑‍🤝‍🧑 **Community-Based Progression** – Everyone contributes to global progress
* 🖼️ **Graphical Goal Menu** – Players can view goals and track progress through an intuitive GUI
* 🛠️ **In-Game Goal Editor** – Admins can create, edit, and delete goals without leaving the game
* 📚 **Support for External Plugins** – Future integration with plugins like Citizens, Quests, etc.
* 🧵 **Flexible Config** – Define everything via `goals.yml` with support for various goal types

---

## 🧪 Commands *(WIP)*

| Command         | Description                     | Permission              |
| --------------- | ------------------------------- | ----------------------- |
| `/goals`        | Opens the community goals GUI   | `communitygoals.view`   |
| `/goals admin`  | Opens the admin goal editor     | `communitygoals.admin`  |
| `/goals reload` | Reloads plugin config and goals | `communitygoals.reload` |

> More commands will be added as features are developed.

---

## 📄 Configuration (`goals.yml`)

*The format and examples for `goals.yml` will be documented here once the schema is finalized.*

Example preview:

```yaml
goals:
  kill_zombies:
    type: MOB_KILL
    entity: ZOMBIE
    amount: 1000
    reward:
      - "broadcast: &aGoal completed! 1,000 zombies have been defeated!"
      - "giveall: diamond 1"
```

---

## 🧩 Goal Types

CommunityGoals supports a wide variety of goal types, including:

* `BLOCK_BREAK`
* `BLOCK_PLACE`
* `MOB_KILL`
* `PLAYER_KILL`
* `IRON_GOLEM_REPAIR`
* `VILLAGER_TRADE`
* `ITEM_CRAFT`
* `ITEM_SMELT`
* `FISH_CAUGHT`
* `DISTANCE_TRAVELED`
* `POTION_BREW`
* `ITEM_ENCHANT`
* `ANIMAL_BREED`
* `CROP_HARVEST`
* `EXPERIENCE_GAINED`
* `DAMAGE_DEALT`
* `DAMAGE_TAKEN`
* `STRUCTURE_DISCOVERY`
* `RAID_WIN`
* `ADVANCEMENT_COMPLETE`
* `CHAT_MESSAGE`
* `TIME_PLAYED`
* `CUSTOM_EVENT`
* `MONEY_SPENT`
* `MONEY_EARNED`

---

More goal types may be added over time depending on community needs and plugin integrations.

## 🛠️ Requirements

* Minecraft 1.20+ (pre 1.20 will likely not be supported)
* Java 17+
* Vault (if using economy-related goals) OR CoinsEngine for custom economies.
* PlaceholderAPI (if using placeholders)

---

> ✅ *Still in development. Check back soon for releases and full documentation.*
