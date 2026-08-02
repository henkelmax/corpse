package de.maxhenkel.corpse.gui;

import net.minecraft.server.level.ServerPlayer;

public interface ITransferrable {

    void transferItems(ServerPlayer sender);

}
