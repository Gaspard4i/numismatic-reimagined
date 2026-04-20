package dev.gaspard4i.numismatic.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Helpers to rotate a horizontal-facing voxel shape around the Y axis.
 * Extracted from {@link PiggyBankBlock} so the rotation math stays pure
 * and testable (no MC world or block state required).
 */
public final class PiggyBankShapes {

    private PiggyBankShapes() {}

    /**
     * Rotates a shape authored as if FACING=NORTH so that it matches the
     * given {@code target} direction. Returns the input unchanged when
     * {@code target == NORTH}.
     */
    public static VoxelShape rotateHorizontal(VoxelShape northShape, Direction target) {
        return switch (target) {
            case NORTH -> northShape;
            case SOUTH -> rotate180Y(northShape);
            case EAST -> rotate90Y(northShape);
            case WEST -> rotate270Y(northShape);
            default -> northShape;
        };
    }

    private static VoxelShape rotate90Y(VoxelShape shape) {
        VoxelShape[] out = new VoxelShape[]{ Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
            out[0] = Shapes.or(out[0],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
        );
        return out[0];
    }

    private static VoxelShape rotate180Y(VoxelShape shape) {
        VoxelShape[] out = new VoxelShape[]{ Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
            out[0] = Shapes.or(out[0],
                    Shapes.box(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ))
        );
        return out[0];
    }

    private static VoxelShape rotate270Y(VoxelShape shape) {
        VoxelShape[] out = new VoxelShape[]{ Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
            out[0] = Shapes.or(out[0],
                    Shapes.box(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX))
        );
        return out[0];
    }
}
