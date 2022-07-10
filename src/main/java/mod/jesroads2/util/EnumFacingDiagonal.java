package mod.jesroads2.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public enum EnumFacingDiagonal {
    SOUTH(0, EnumFacing.SOUTH, false),
    WEST(1, EnumFacing.WEST, false),
    NORTH(2, EnumFacing.NORTH, false),
    EAST(3, EnumFacing.EAST, false),

    D_SOUTH(4, EnumFacing.SOUTH, true),
    D_WEST(5, EnumFacing.WEST, true),
    D_NORTH(6, EnumFacing.NORTH, true),
    D_EAST(7, EnumFacing.EAST, true);

    private final EnumFacing facing;
    private final boolean diagonal;
    private final int id;

    private static final EnumFacingDiagonal[] list = new EnumFacingDiagonal[values().length];

    static {
        for (EnumFacingDiagonal f : values())
            list[f.id] = f;
    }

    EnumFacingDiagonal(int id, EnumFacing face, boolean diagonal) {
        this.id = id;
        this.facing = face;
        this.diagonal = diagonal;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public boolean isDiagonal() {
        return diagonal;
    }

    public static EnumFacingDiagonal fromFacing(EnumFacing face) {
        return fromID(face.getHorizontalIndex());
    }

    @Deprecated
    public static EnumFacingDiagonal fromEntity(EntityLivingBase entity) {
        return fromAngle(getAngle(entity));
    }

    public static EnumFacingDiagonal fromEntityF(EntityLivingBase entity) {
        return fromAngleF(getAngleF(entity));
    }

    @Deprecated
    public static EnumFacingDiagonal fromAngle(double angle) {
        EnumFacing face = correctedFacing(angle);
        return fromID(isDiagonalAngle(face, angle) ? face.getHorizontalIndex() + 4 : face.getHorizontalIndex());
    }

    public static EnumFacingDiagonal fromAngleF(float angle) {
        if (angle < 25) return SOUTH;
        else if (angle < 75) return D_SOUTH;
        else if (angle < 125) return WEST;
        else if (angle < 150) return D_WEST;
        else if (angle < 220) return NORTH;
        else if (angle < 250) return D_NORTH;
        else if (angle < 300) return EAST;
        else if (angle < 340) return D_EAST;
        else return SOUTH;
    }

    public static EnumFacingDiagonal fromID(int id) {
        if (id > 0 && id < list.length) return list[id];
        else return list[0];
    }

    public boolean isEqual(EnumFacing other) {
        return isEqual(fromFacing(other));
    }

    public boolean isEqual(EnumFacingDiagonal other) {
        if (this == other) return true;
        else if (this.isDiagonal() && !other.isDiagonal()) return this.getFacing() == other.getFacing().rotateYCCW();
        else if (!this.isDiagonal() && other.isDiagonal()) return this.getFacing() == other.getFacing().rotateY();
        else return false;
    }

    public int getIndex() {
        return id;
    }

    public String getName() {
        return diagonal ? "diagonal_" + facing.getName() : facing.getName();
    }

    public EnumFacingDiagonal getRight() {
        int newID = id + 1;
        if (!diagonal && newID > 3) newID = 0;
        else if (newID > 7) newID = 4;
        return fromID(newID);
    }

    public EnumFacingDiagonal getLeft() {
        int newID = id - 1;
        if (newID < 0) newID = 3;
        else if (diagonal && newID < 4) newID = 7;
        return fromID(newID);
    }

    @Deprecated
    private static double getAngle(EntityLivingBase entity) {
        return entity.rotationYaw * 4.0F / 360.0F;
    }

    private static float getAngleF(EntityLivingBase entity) {
        float res = MathHelper.wrapDegrees(entity.rotationYaw);
        if (res < 0) res = 360 + res;
        return res;
    }

    @Deprecated
    private static EnumFacing correctedFacing(Double angle) {
        angle = Math.abs(angle);
        String s = angle.toString();
        int dec = s.indexOf(".");
        if (dec > -1 && s.charAt(dec + 1) >= '8') return EnumFacing.fromAngle(Math.ceil(angle) * 90);
        else return EnumFacing.fromAngle(Math.floor(angle) * 90);
    }

    @Deprecated
    private static boolean isDiagonalAngle(EnumFacing face, double angle) {
        double normal = face != null ? angle - face.getHorizontalIndex() : angle;
        return normal > 0.2D && normal < 0.8D;
    }
}