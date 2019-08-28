# Corpse Mod

This mod brings a corpse into the game.
It will spawn upon your death containing all items that you had in your inventory.


# Features

The corpse will appear at the players location upon death.
Your player skin will be applied to the corpse.
It contains all the items you had when you died.
It can hold an infinite amount of items.

You can access the items by right-clicking the corpse.

When you take out all items, the corpse will disappear.
The corpse can't fall into the void or burn in lava.

![](https://i.imgur.com/WfIoIXE.png)

![](https://i.imgur.com/p574CdX.png)

![](https://i.imgur.com/ioFPSdL.png)

After 1 hour, the corpse will turn into a skeleton, indicating that it existed for a longer time.
This won't change the functionality of the corpse.

![](https://i.imgur.com/pzChrfC.png)


## Death History

You can view all your past deaths by pressing the 'U' key.
You can retrieve lost items if you are in creative mode.

![](https://i.imgur.com/mg68xFT.png)


## Viewing the Death Hisory of other Players

By typing `/deathhistory <player>` as an operator, you can view the death history of another player.

![](https://i.imgur.com/RzYuMFX.png)

![](https://i.imgur.com/OaPMXl6.png)


## Recovering Lost Items

You can retrieve lost items by pressing the "Items" button in the death history GUI.
Note that you can only take out items if you are in creative mode.


## Teleporting to the Death Location

By clicking on the "Location" area in the death history GUI, the command to teleport you to the death location will be displayed in chat.
It includes the coordinates and the dimension.

![](https://i.imgur.com/EvRsWwp.png)

![](https://i.imgur.com/e7xZeen.png)


## Optional Features

Only the owner of the corpse can access the inventory (Config option)

## Config

### Server

``` toml
#The time passed after a corpse despawns (only if empty)
corpse_despawn_time = 600
#If only the owner of the corpse can access the inventory
only_owner_access = false
#The time passed after a corpse turns into a skeleton
corpse_skeleton_time = 72000
#If the corpse should spawn on its face
spawn_corpse_on_face = true
#The time passed after a corpse despawns even if its not empty (-1 = never)
corpse_force_despawn_time = -1
```


## Credits

Russian translation [ghost_screa_m](https://minecraft.curseforge.com/members/ghost_screa_m)

Portuguese translation [srbedrock](https://minecraft.curseforge.com/members/srbedrock)

Polish translation [DarkKnightComes](https://www.curseforge.com/members/darkknightcomes)

---

[Gallery](https://imgur.com/a/H1ltydQ)