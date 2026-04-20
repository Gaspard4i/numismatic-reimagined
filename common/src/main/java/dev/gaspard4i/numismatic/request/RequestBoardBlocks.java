package dev.gaspard4i.numismatic.request;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RequestBoardBlocks {

    private RequestBoardBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(NumismaticConstants.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> REQUEST_BOARD = BLOCKS.register(
            "request_board",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0f))
    );

    static {
        NumismaticItems.ITEMS.register("request_board",
                () -> new BlockItem(REQUEST_BOARD.get(), new Item.Properties()));
    }

    public static void register() {
        BLOCKS.register();
    }
}
