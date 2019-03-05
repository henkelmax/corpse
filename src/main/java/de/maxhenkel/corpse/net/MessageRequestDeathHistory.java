package de.maxhenkel.corpse.net;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.UUID;

public class MessageRequestDeathHistory implements Message {

    public MessageRequestDeathHistory() {

    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        List<Death> deaths = DeathManager.getDeaths(context.getSender());
        Main.SIMPLE_CHANNEL.sendTo(new MessageOpenHistory(deaths), context.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageRequestDeathHistory fromBytes(PacketBuffer buf) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
