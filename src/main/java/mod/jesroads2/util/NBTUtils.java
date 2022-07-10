package mod.jesroads2.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;

public final class NBTUtils {
    private static final String positionKey = "position", sizeKey = "count";

    public static BlockPos readBlockPos(NBTTagCompound nbt) {
        if (nbt != null && nbt.hasKey(positionKey)) {
            int[] position = nbt.getIntArray(positionKey);
            return new BlockPos(position[0], position[1], position[2]);
        } else return null;
    }

    public static ArrayList<BlockPos> readBlockPosList(NBTTagCompound nbt) {
        int[] signsPos = nbt.getIntArray(positionKey);
        ArrayList<BlockPos> positions = new ArrayList<>(nbt.getInteger(sizeKey));
        for (int i = 0; i < signsPos.length; i += 3)
            positions.add(new BlockPos(signsPos[i], signsPos[i + 1], signsPos[i + 2]));
        return positions;
    }

    public static NBTTagCompound writeBlockPos(BlockPos pos) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (pos != null) nbt.setIntArray(positionKey, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        return nbt;
    }

    public static NBTTagCompound writeBlockPosList(Collection<BlockPos> positionList) {
        int[] positions = new int[positionList.size() * 3];
        int index = 0;
        for (BlockPos sign : positionList) {
            positions[index++] = sign.getX();
            positions[index++] = sign.getY();
            positions[index++] = sign.getZ();
        }

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(sizeKey, positionList.size());
        nbt.setIntArray(positionKey, positions);
        return nbt;
    }
}