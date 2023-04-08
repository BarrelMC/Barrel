package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.data.PlayerActionType;
import com.nukkitx.protocol.bedrock.data.PlayerAuthInputData;
import com.nukkitx.protocol.bedrock.data.PlayerBlockActionData;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.data.inventory.ItemUseTransaction;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientPlayerActionPacket implements JavaPacketTranslator {

    private static final int ACTION_CLICK_BLOCK = 0;
    private static final int ACTION_CLICK_AIR = 1;
    private static final int ACTION_BREAK_BLOCK = 2;

    @Override
    public void translate(Packet pk, Player player) {
        com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket playerActionPacket = (com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket) pk;

        PlayerBlockActionData blockActionData = new PlayerBlockActionData();

        Vector3i blockPos = Vector3i.from(playerActionPacket.getPosition().getX(), playerActionPacket.getPosition().getY(), playerActionPacket.getPosition().getZ());

        switch (playerActionPacket.getAction()) {
            case START_DIGGING:
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    // TODO: implement for client authoritative
                } else {
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.PERFORM_BLOCK_ACTIONS);

                    blockActionData = new PlayerBlockActionData();
                    blockActionData.setAction(PlayerActionType.START_BREAK);
                    blockActionData.setBlockPosition(blockPos);
                    blockActionData.setFace(playerActionPacket.getFace().ordinal());
                    player.getPlayerAuthInputActions().add(blockActionData);

                    player.setDiggingPosition(blockPos);
                    player.setDiggingFace(playerActionPacket.getFace());
                    player.setDiggingStatus(PlayerActionType.START_BREAK);
                }
                break;
            case CANCEL_DIGGING:
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    // TODO: implement for client authoritative
                } else {
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.PERFORM_BLOCK_ACTIONS);
                    player.setDiggingStatus(PlayerActionType.ABORT_BREAK);

                    blockActionData = new PlayerBlockActionData();
                    blockActionData.setAction(PlayerActionType.ABORT_BREAK);
                    blockActionData.setBlockPosition(blockPos);
                    blockActionData.setFace(playerActionPacket.getFace().ordinal());

                    player.getPlayerAuthInputActions().add(blockActionData);
                }
                break;
            case FINISH_DIGGING:
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    // TODO: implement for client authoritative
                } else {
                    // perform break block action
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.PERFORM_ITEM_INTERACTION);

                    ItemUseTransaction itemUseTransaction = new ItemUseTransaction();
                    itemUseTransaction.setActionType(ACTION_BREAK_BLOCK);
                    itemUseTransaction.setBlockPosition(blockPos);
                    itemUseTransaction.setItemInHand(ItemData.AIR); // TODO: implement inventory
                    itemUseTransaction.setBlockFace(playerActionPacket.getFace().ordinal());
                    itemUseTransaction.setPlayerPosition(player.getVector3f());
                    itemUseTransaction.setBlockRuntimeId(0);
                    itemUseTransaction.setClickPosition(Vector3f.ZERO);
                    itemUseTransaction.setHotbarSlot(player.getHotbarSlot());
                    itemUseTransaction.setLegacyRequestId(0);
                    itemUseTransaction.setUsingNetIds(false);
                    player.setPlayerAuthInputItemUseTransaction(itemUseTransaction);

                    // perform stop break block action
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.PERFORM_BLOCK_ACTIONS);
                    player.setDiggingStatus(PlayerActionType.STOP_BREAK);

                    blockActionData = new PlayerBlockActionData();
                    blockActionData.setAction(PlayerActionType.STOP_BREAK);
                    blockActionData.setBlockPosition(blockPos);
                    blockActionData.setFace(playerActionPacket.getFace().ordinal());
                    player.getPlayerAuthInputActions().add(blockActionData);
                }
                break;
        }
    }
}
