package net.zorby.prc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.zorby.prc.database.WorldEntry;
import net.zorby.prc.server.PRCServer;

import java.io.IOException;
import java.util.List;

public class Finder {

    private final PRCServer server;
    private ColumnPos elytraCol;
    private int elytraY;
    private boolean exact;

    public Finder() {
        this.server = new PRCServer();
    }

    public static BlockPos getPlayerBlockPos() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return null;

        return player.getBlockPos();
    }

    public ColumnPos getElytraCol() {
        return this.elytraCol;
    }

    public int getElytraY() {
        return this.elytraY;
    }

    public PRCServer getServer() {
        return this.server;
    }

    public boolean foundExact() {
        return this.exact;
    }

    public void findApproximate() {
        BlockPos pos = getPlayerBlockPos();

        assert pos != null;

        this.exact = false;

        try {
            this.elytraCol = this.server.request(pos.getX(), pos.getZ());
            System.out.println(elytraCol);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void attemptFindExact() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        World world = player.getWorld();

        ColumnPos col = this.elytraCol;
        if (col == null) return;

        ChunkPos chunkPos = col.toChunkPos();

        if (!world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) return;

        Vec3i pos = new Vec3i(col.x(), 0, col.z());
        Vec3d centerPos = Vec3d.ofCenter(pos);
        Box boundingBox = Box.of(centerPos, 1, 1, 1).withMaxY(world.getTopY());

        List<ItemFrameEntity> itemFrames = world.getEntitiesByType(EntityType.ITEM_FRAME, boundingBox, i -> true);

        if (itemFrames.isEmpty()) return;

        ItemFrameEntity itemFrameWithElytra = null;
        for (ItemFrameEntity itemFrame : itemFrames) {
            if (itemFrame.getHeldItemStack().isOf(Items.ELYTRA)) {
                itemFrameWithElytra = itemFrame;
                break;
            }
        }

        if (itemFrameWithElytra == null) {
            if (exact) {
                PRC.getInstance().getDatabase().addEntry(pos, WorldEntry.Style.Grabbed);
            } else {
                PRC.getInstance().getDatabase().addEntry(pos, WorldEntry.Style.Found);
            }

            this.findApproximate();

            return;
        }

        this.exact = true;
        this.elytraY = itemFrameWithElytra.getBlockY();
    }
}
