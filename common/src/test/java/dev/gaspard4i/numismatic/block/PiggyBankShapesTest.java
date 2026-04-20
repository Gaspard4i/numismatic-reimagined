package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PiggyBankShapesTest {

    @BeforeAll
    static void setup() {
        McBootstrap.ensure();
    }

    @Test
    void northReturnsInputUnchanged() {
        VoxelShape in = Block.box(7, 7, 4, 9, 8, 6);
        VoxelShape out = PiggyBankShapes.rotateHorizontal(in, Direction.NORTH);
        assertEquals(bounds(in), bounds(out));
    }

    @Test
    void rotate90YMapsNorthSlotToEast() {
        // North slot at x=[7,9], z=[4,6] → East → x=[10,12] / 16 → clamp,
        // y unchanged.
        VoxelShape north = Block.box(7, 7, 4, 9, 8, 6);
        VoxelShape east = PiggyBankShapes.rotateHorizontal(north, Direction.EAST);
        AABB b = east.bounds();
        assertEquals(7.0 / 16, b.minY);
        assertEquals(8.0 / 16, b.maxY);
        // 90° Y : new x = 1 - oldMaxZ .. 1 - oldMinZ = 10..12 /16
        assertEquals(10.0 / 16, b.minX, 1e-6);
        assertEquals(12.0 / 16, b.maxX, 1e-6);
    }

    @Test
    void rotate180MapsNorthSlotToSouth() {
        VoxelShape north = Block.box(7, 7, 4, 9, 8, 6);
        VoxelShape south = PiggyBankShapes.rotateHorizontal(north, Direction.SOUTH);
        AABB b = south.bounds();
        assertEquals(7.0 / 16, b.minX, 1e-6);
        assertEquals(9.0 / 16, b.maxX, 1e-6);
        // z mirrored: 1 - 6/16 = 10/16 .. 1 - 4/16 = 12/16
        assertEquals(10.0 / 16, b.minZ, 1e-6);
        assertEquals(12.0 / 16, b.maxZ, 1e-6);
    }

    @Test
    void rotate270MapsNorthSlotToWest() {
        VoxelShape north = Block.box(7, 7, 4, 9, 8, 6);
        VoxelShape west = PiggyBankShapes.rotateHorizontal(north, Direction.WEST);
        AABB b = west.bounds();
        // 270° Y: new x = minZ .. maxZ = 4/16 .. 6/16
        assertEquals(4.0 / 16, b.minX, 1e-6);
        assertEquals(6.0 / 16, b.maxX, 1e-6);
    }

    @Test
    void rotationIsInvariantOnYHeight() {
        VoxelShape in = Block.box(5, 2, 5, 11, 6, 11);
        for (Direction d : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            VoxelShape out = PiggyBankShapes.rotateHorizontal(in, d);
            AABB b = out.bounds();
            assertEquals(2.0 / 16, b.minY, 1e-6, "minY changed for " + d);
            assertEquals(6.0 / 16, b.maxY, 1e-6, "maxY changed for " + d);
        }
    }

    @Test
    void verticalDirectionFallsBackToNorth() {
        VoxelShape in = Block.box(7, 7, 4, 9, 8, 6);
        VoxelShape up = PiggyBankShapes.rotateHorizontal(in, Direction.UP);
        assertEquals(bounds(in), bounds(up));
    }

    private static String bounds(VoxelShape s) {
        AABB b = s.bounds();
        return b.minX + "," + b.minY + "," + b.minZ + "->"
                + b.maxX + "," + b.maxY + "," + b.maxZ;
    }
}
