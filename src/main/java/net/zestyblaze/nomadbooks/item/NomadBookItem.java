package net.zestyblaze.nomadbooks.item;

import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.ModTags;
import net.zestyblaze.nomadbooks.util.NomadBooksConfig;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.zestyblaze.nomadbooks.util.Helper.convertAABBtoBoundingBox;

public class NomadBookItem extends Item implements DyeableLeatherItem {
    public static final int CAMP_RETRIEVAL_RADIUS = 20;

    public static final String DEFAULT_STRUCTURE_PATH = Constants.MODID + ":campfire3x1x3";
    public static final String NETHER_DEFAULT_STRUCTURE_PATH = Constants.MODID + ":nethercampfire3x1x3";

    public NomadBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        super.getDefaultInstance();
        ItemStack itemStack = new ItemStack(this);
        CompoundTag tags = itemStack.getOrCreateTagElement(Constants.MODID);
        tags.putInt(Constants.HEIGHT, 1);
        tags.putInt(Constants.WIDTH, 3);
        tags.putString(Constants.STRUCTURE, DEFAULT_STRUCTURE_PATH);
        return itemStack;
    }

    /**
     * Do things regarding a currently <b>NOT</b> deployed camp. click the item on a block
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // TODO Refactor to reduce cognitive complexity

        //                        | 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08
        // 🟦 AQUATIC_MEMBRANE    |  0 |  1 |  0 |  1 |  0 |  1 |  0 |  1
        // 🟪 FUNGI_SUPPORT       |  0 |  0 |  1 |  1 |  0 |  0 |  1 |  1
        // 🟫 SPACIAL_DISPLACER   |  0 |  0 |  0 |  0 |  1 |  1 |  1 |  1
        // 01: Stock/un-upgraded
        // 02: AM // 03: FS // 05: SD
        // 04: AM&FS // 06: AM&SD // 07: FS&SD
        // 08: AM&FS&SD: <determine-order> of checks and func
        // ------------------------------------------------------------------
        // I am seeing many missed edge cases. will have to REFACTOR the whole F***ng thing to fully address

        // Stock Flags
        CompoundTag tags = context.getItemInHand().getOrCreateTagElement(Constants.MODID);
        boolean isDeployed = context.getItemInHand().getOrCreateTag().getFloat(Constants.DEPLOYED) == 1f; // is deployed
        Level world = context.getLevel();
        Player player = Objects.requireNonNull(context.getPlayer());
        String structurePath = tags.getString(Constants.STRUCTURE);
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        //---------------------------

        // Setup Logic / Checks
        if (isDeployed) {
            this.use(world, player, context.getHand()); // note calls use()
            return InteractionResult.FAIL;
        }
        // checks just the clicked position. continues down to a valid place
        BlockPos pos = context.getClickedPos(); // Oh [neat](https://media1.tenor.com/m/UchYBXaC-1cAAAAC/futurama-bender.gif
        pos = findTheGround(pos, tags, world);
        // set dimension tag
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, world.dimension()).result().ifPresent(tag -> tags.put(Constants.DIMENSION, tag));
        // center position on camp center
        pos = pos.offset(new BlockPos(-width / 2, 1, -width / 2));
        //-----------------------------

        // Upgrades Flags
        boolean hasAquaticMembrane = tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.AQUATIC_MEMBRANE)); // 🟦
        boolean hasFungiSupport = tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.FUNGI_SUPPORT)); // 🟪
        boolean hasSpacialDisplacer = tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.SPACIAL_DISPLACER)); // 🟫
        // -----------------------------------

        // Upgrades, Checks and Func
        // check if there's enough space
        BoundingBox campVolume = BoundingBox.fromCorners(pos, pos.offset(width - 1, height - 1, width - 1));
        int spaceY;
        int maxChecks = NomadBooksConfig.checksAboveOnDeploy;
        for (spaceY=0; spaceY <= maxChecks; spaceY++) {
            if (hasEnoughSpace(world, campVolume.moved(0,spaceY,0),tags)) { // 🟦🟫 check
                pos = pos.above(spaceY);
                campVolume = campVolume.moved(0,spaceY,0);
                break;
            }
        }
        if (spaceY > maxChecks && !hasSpacialDisplacer) { // here if SPACIAL_DISPLACER, ignore this fail // 🟫 check
            player.displayClientMessage(Component.translatable("error.nomadbooks.no_space"), true);
            return InteractionResult.FAIL;
        }
        // mushroom platform upgrade FUNGI_SUPPORT
        if (hasFungiSupport) { // 🟪 check
            buildMushroomPlatform(world, pos, width); // 🟪 do
        }
        if (!isSurfaceValid(world, pos, width)) {
            boolean foundValid = false;
            if (!hasFungiSupport && hasSpacialDisplacer) {
                for (int i = 0; i < width/2; i++) {
                    if (isSurfaceValid(world, pos.below(i+1), width)) {
                        pos = pos.below(i+1);
                        campVolume = campVolume.moved(0, -i-1, 0);
                        foundValid = true;
                        break;
                    }
                }
            }
            if ( !hasEnoughSpace(world, campVolume,tags) ) {
                player.displayClientMessage(Component.translatable("error.nomadbooks.no_space"), true);
                return InteractionResult.FAIL;
            }
            if (!foundValid) {
                player.displayClientMessage(Component.translatable("error.nomadbooks.invalid_surface"), true);
                return InteractionResult.FAIL;
            }
        }
        // Move Player up with mushroom upgrade (no need to check the upgrade because it's not possible to place without it and satisfy the TP)
        BoundingBox fungiCapMovementCheck = new BoundingBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
        Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
        Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
        BoundingBox fungiStem = BoundingBox.fromCorners(minPos, maxPos);
        if (fungiCapMovementCheck.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))
        || fungiStem.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))) {
            player.teleportTo(player.getX(), pos.getY(), player.getZ());
        }
        // if membrane upgrade, replace water and underwater plants with membrane
        if (hasAquaticMembrane) { // 🟦 check + do
            BoundingBox membraneVolume = new BoundingBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+width, pos.getY()+height, pos.getZ()+width);
            BoundingBox membraneMinX = new BoundingBox(membraneVolume.minX(), membraneVolume.minY()+1, membraneVolume.minZ()+1, membraneVolume.minX() /*🟩*/, membraneVolume.maxY()-1, membraneVolume.maxZ()-1);
            BoundingBox membraneMaxX = new BoundingBox(membraneVolume.maxX() /*🟩*/, membraneVolume.minY()+1, membraneVolume.minZ()+1, membraneVolume.maxX(), membraneVolume.maxY()-1, membraneVolume.maxZ()-1);
            BoundingBox membraneMinZ = new BoundingBox(membraneVolume.minX()+1, membraneVolume.minY()+1, membraneVolume.minZ(), membraneVolume.maxX()-1, membraneVolume.maxY()-1, membraneVolume.minZ() /*🟩*/);
            BoundingBox membraneMaxZ = new BoundingBox(membraneVolume.minX()+1, membraneVolume.minY()+1, membraneVolume.maxZ() /*🟩*/, membraneVolume.maxX()-1, membraneVolume.maxY()-1, membraneVolume.maxZ());
            BoundingBox membraneMaxY = new BoundingBox(membraneVolume.minX()+1, membraneVolume.maxY() /*🟩*/, membraneVolume.minZ()+1, membraneVolume.maxX()-1, membraneVolume.maxY(), membraneVolume.maxZ()-1);
            List<BoundingBox> membranePanels = Arrays.asList(membraneMinX, membraneMaxX, membraneMinZ, membraneMaxZ, membraneMaxY);
            Streams.stream(membranePanels).forEach(panel -> BlockPos.betweenClosedStream(panel)
                .filter(bp -> isBlockUnderwaterReplaceable(world.getBlockState(bp)))
                .forEach(bp -> {
                    world.destroyBlock(bp, true);
                    world.setBlock(bp, NomadBooks.MEMBRANE.defaultBlockState(), 2);
                }));
        }
        // Save the Terrain as a structure using SPACIAL_DISPLACER
        if (hasSpacialDisplacer && !world.isClientSide()) { // 🟫 check + do --V
            ServerLevel serverLevel = (ServerLevel) world;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getOrCreate(new ResourceLocation(structurePath + Constants.DISPLACED));
            } catch (ResourceLocationException e) {
                NomadBooks.LOGGER.error("Error creating or retrieving structure: {}", e.getMessage());
                return InteractionResult.FAIL;
            }
            structure.fillFromWorld(world, pos.offset(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID);
            structure.setAuthor(player.getScoreboardName());
            structureTemplateManager.save(new ResourceLocation(structurePath + Constants.DISPLACED)); // added DISPLACED
        }
        // destroy destroyable blocks in the way
        BlockPos.betweenClosedStream(campVolume).forEach(bp -> world.setBlock(bp, Blocks.AIR.defaultBlockState(), 2));
        // Place the structure
        if (!world.isClientSide()) {
            placeStructure(world, structurePath, pos, width);
        }
        // set deployed, register nbt
        context.getItemInHand().getOrCreateTag().putFloat(Constants.DEPLOYED, 1F); // set is deployed
        tags.put(Constants.CAMP_POS, NbtUtils.writeBlockPos(pos));

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.SUCCESS;
    }

    /**
     * checks just the clicked position. If the position is replaceable, then continue checking down until something isn't replaceable
     * AKA: Find the ground
     */
    private BlockPos findTheGround(BlockPos pos, CompoundTag tags, Level level) {
//        BlockPos pos = context.getClickedPos(); // Oh [neat](https://media1.tenor.com/m/UchYBXaC-1cAAAAC/futurama-bender.gif
        while (isBlockReplaceable(level.getBlockState(pos))
            || isBlockUnderwaterReplaceable(level.getBlockState(pos))
            && tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.AQUATIC_MEMBRANE))) { // 🟦 unrelated check to AQUATIC_MEMBRANE
            pos = pos.below();
        }
        return pos;
    }

    /**
     * Basically: false if surface has any replaceable. true if a complete "Solid" surface
     */
    private boolean isSurfaceValid(Level world, BlockPos pos, int width) {
        BoundingBox surface = new BoundingBox(pos.getX(), pos.getY()-1, pos.getZ(), pos.getX()+width-1, pos.getY()-1, pos.getZ()+width-1);
        return BlockPos.betweenClosedStream(surface).noneMatch(bp -> isBlockReplaceable(world.getBlockState(bp)));
    }

    private boolean hasEnoughSpace(Level world, BoundingBox campVolume, CompoundTag tags) {
        return BlockPos.betweenClosedStream(campVolume).allMatch(bp -> {
            BlockState bs = world.getBlockState(bp);
            return isBlockReplaceable(bs)
                || isBlockUnderwaterReplaceable(bs) && tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.AQUATIC_MEMBRANE))
                || isBlockDisplaceable(bs) && tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.SPACIAL_DISPLACER));
        });
    }

    private void buildMushroomPlatform(Level level, BlockPos pos, int width) {
        BoundingBox fungiCap = new BoundingBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
        BlockPos.betweenClosedStream(fungiCap).forEach(bp -> {
            BlockState bs = level.getBlockState(bp);
            if (isBlockReplaceable(bs) || isBlockUnderwaterReplaceable(bs)) {
                level.destroyBlock(bp, true);
                level.setBlock(bp, NomadBooks.NOMAD_MUSHROOM_BLOCK.defaultBlockState(), 2);
            }
        });
        Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
        Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
        BoundingBox fungiStem = BoundingBox.fromCorners(minPos, maxPos);
        BlockPos.betweenClosedStream(fungiStem).forEach(bp -> {
            if (isBlockReplaceable(level.getBlockState(bp)) || isBlockUnderwaterReplaceable(level.getBlockState(bp))) {
                level.destroyBlock(bp, true);
                level.setBlock(bp, NomadBooks.NOMAD_MUSHROOM_STEM.defaultBlockState(), 2);
            }
        });
    }

    private void placeStructure(Level level, String structurePath, BlockPos pos, int width) {
        ServerLevel serverLevel = (ServerLevel) level;
        Optional<StructureTemplate> structure = serverLevel.getStructureManager().get(new ResourceLocation(structurePath));

        if (structure.isPresent()) {
            int offsetWidth = (width - structure.get().getSize().getX()) / 2;
            StructurePlaceSettings placementData = new StructurePlaceSettings().setIgnoreEntities(true);
            structure.get().placeInWorld(serverLevel, pos.offset(offsetWidth, 0, offsetWidth), pos.offset(offsetWidth, 0, offsetWidth), placementData, serverLevel.getRandom(), 2); // Bingo
        }
    }

    // begin methods related to use()

    /**
     * Do things regarding a currently deployed camp. (click the item on "nothing")
     * Such as: toggle display boundaries when shift-right-click, teleport to it using offhand ender-pearls, first-time use save as a new structure file, retrieve the camp if within normal distance
     */
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        CompoundTag tags = itemStack.getOrCreateTagElement(Constants.MODID);
        boolean isDeployed = itemStack.getOrCreateTag().getFloat(Constants.DEPLOYED) == 1f;

        if (!isDeployed) { // if camp isn't deployed, this use() method is not to be used. so use() is meant to retrieve a camp + other things
            return InteractionResultHolder.fail(itemStack);
        }

        BlockPos pos = NbtUtils.readBlockPos(tags.getCompound(Constants.CAMP_POS));
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        String structurePath = tags.getString(Constants.STRUCTURE);

        if (user.isShiftKeyDown()) {
            toggleBoundaries(user, tags);
            return InteractionResultHolder.pass(itemStack);
        }
        if (teleportToCampHandler(tags, world, user, pos, width)) { // teleport to camp. not shifting. and either tp will ender-pearls or too far message. else try retrieve below.
            return InteractionResultHolder.success(itemStack);
        }
        if (createStructureIfDefaultOrRetrieve(tags, structurePath, user, world, pos, width, height)) { // Create a new structure or retrieve the camp. only false on error
            // set undeployed
            itemStack.getOrCreateTag().putFloat(Constants.DEPLOYED, 0F);
                removeBlocks(tags, world, pos, width, height);
            placeTerrainWithSpacialDisplacer(tags, world, pos, width);
            // Remove Boundaries and play sound
            tags.putBoolean(Constants.DISPLAY_BOUNDARIES, false);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, 0.9f);
            return InteractionResultHolder.success(itemStack);
        }
        // Probably wont ever be hit
        return InteractionResultHolder.fail(itemStack);
    }

    private void placeTerrainWithSpacialDisplacer(CompoundTag tags, Level world, BlockPos pos, int width) {

        String structurePath = tags.getString(Constants.STRUCTURE) + Constants.DISPLACED;

        if (tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.SPACIAL_DISPLACER))
        && !world.isClientSide()) {
            // Place the structure
            placeStructure(world, structurePath, pos, width); // should I add checks for if the player upgraded the camp size?
        }
    }

    private void toggleBoundaries(Player user, CompoundTag tags) {
        boolean displayBoundaries = tags.getBoolean(Constants.DISPLAY_BOUNDARIES);
        displayBoundaries = !displayBoundaries;
        user.displayClientMessage(Component.translatable(displayBoundaries ? "info.nomadbooks.display_boundaries_on" : "info.nomadbooks.display_boundaries_off"), true);
        tags.putBoolean(Constants.DISPLAY_BOUNDARIES, displayBoundaries);
    }

    /**
     * handler should return true if an action is performed within
     */
    private boolean teleportToCampHandler(CompoundTag tags, Level world, Player user, BlockPos pos, int width) {
        // handler should return true if an action is performed within
        Optional<ResourceKey<Level>> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tags.get(Constants.DIMENSION)).result();
        double centerX = pos.getX() + (width / 2.0) + 0.5;
        double centerZ = pos.getZ() + (width / 2.0) + 0.5;
        double distanceSquared = user.distanceToSqr(centerX, pos.getY(), centerZ);

        if (dimension.isEmpty() || dimension.get() != world.dimension()) {
            user.displayClientMessage(Component.translatable("error.nomadbooks.different_dimension"), true);
            return true; // Dimension mismatch
        }
        if (distanceSquared > CAMP_RETRIEVAL_RADIUS * CAMP_RETRIEVAL_RADIUS) {
            int enderPrice = (int) Math.ceil(Math.sqrt(distanceSquared) / 100);

            // Insufficient ender pearls
            if (user.getOffhandItem().getItem() == Items.ENDER_PEARL && user.getOffhandItem().getCount() >= enderPrice) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
                user.teleportTo(centerX, pos.getY(), centerZ);
                if (!user.isCreative()) {
                    user.getOffhandItem().shrink(enderPrice);
                }
                world.playSound(null, centerX, pos.getY(), centerZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
            } else {
                user.displayClientMessage(Component.translatable("error.nomadbooks.camp_too_far"), true);
            }
            return true; // Teleported
        }
        return false; // No action needed
    }

    /**
     * Create a new structure or retrieve the camp. only false on error
     */
    private boolean createStructureIfDefaultOrRetrieve(CompoundTag tags, String structurePath, Player user, Level world, BlockPos pos, int width, int height) {
        // Check if the structure path needs to be generated
        if (structurePath.equals(DEFAULT_STRUCTURE_PATH) || structurePath.equals(NETHER_DEFAULT_STRUCTURE_PATH)) {
            List<String> path = Arrays.asList(user.getUUID().toString(), String.valueOf(System.currentTimeMillis()));
            structurePath = new ResourceLocation(Constants.MODID, Strings.join(path, "/")).toString();
            NomadBooks.LOGGER.info("Creating Structure (Server thread): " + structurePath);
            tags.putString(Constants.STRUCTURE, structurePath);
        }
        // Server-side logic
        if (!world.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) world;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
            // Free beds from being occupied
            BoundingBox campVolume = BoundingBox.fromCorners(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
            BlockPos.betweenClosedStream(campVolume).forEach(bp -> {
                BlockState blockState = world.getBlockState(bp);
                if (blockState.getBlock() instanceof BedBlock) {
                    world.setBlock(bp, blockState.setValue(BedBlock.OCCUPIED, false), 2);
                }
            });
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getOrCreate(new ResourceLocation(structurePath));
            } catch (ResourceLocationException e) {
                NomadBooks.LOGGER.error("Error creating or retrieving structure: {}", e.getMessage());
                return false; // 🟧  Return false on error
            }
            structure.fillFromWorld(world, pos.offset(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID);
            structure.setAuthor(user.getScoreboardName());
            structureTemplateManager.save(new ResourceLocation(structurePath));
        }
        return true; // Return true indicating success
    }

    /**
     * Remove the blocks left behind after the structure/camp is saved.
     * includes the camp, and blocks from upgrades.
     */
    private void removeBlocks(CompoundTag tags, Level world, BlockPos pos, int width, int height) {
        // clear block entities && remove blocks using BoundingBox
        BoundingBox campVolume = BoundingBox.fromCorners(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
        BlockPos.betweenClosedStream(campVolume).forEach(bp -> { // NOTE: this is what the fill command does
            // clear block entities && remove blocks
            world.removeBlockEntity(bp);
            world.setBlock(bp, Blocks.AIR.defaultBlockState(), 255);
            world.blockUpdated(bp, Blocks.AIR);
        });
        // if membrane upgrade, remove membrane
        if (tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.AQUATIC_MEMBRANE))) {
            BoundingBox membraneVolume = new BoundingBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+width, pos.getY()+height, pos.getZ()+width);
            BlockPos.betweenClosedStream(membraneVolume).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.MEMBRANE)) {
                    removeBlock(world, bp);
                }
            });
        }
        // if mushroom upgrade, remove mushroom blocks
        if (tags.getList(Constants.UPGRADES, Tag.TAG_STRING).contains(StringTag.valueOf(Constants.FUNGI_SUPPORT))) {
            BoundingBox fungiCap = new BoundingBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
            BlockPos.betweenClosedStream(fungiCap).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.NOMAD_MUSHROOM_BLOCK)) {
                    removeBlock(world, bp);
                }
            });
            Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
            Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
            BoundingBox fungiStem = BoundingBox.fromCorners(minPos, maxPos);
            BlockPos.betweenClosedStream(fungiStem).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.NOMAD_MUSHROOM_STEM)) {
                    removeBlock(world, bp);
                }
            });
        }
    }

    // End of use()

    /**
     * Get blocks from the config
     */
    private static List<Block> getBlocksFromStrings(List<String> resources) {
        List<Block> blocks = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (String resource : resources) {
            ResourceLocation location = ResourceLocation.tryParse(resource);
            if (location == null) {
                notFound.add(resource);
            } else {
                NomadBooks.LOGGER.debug("resource: " + location);
                blocks.add(BuiltInRegistries.BLOCK.get(location));
            }
        }
        if (!notFound.isEmpty()) {
            NomadBooks.LOGGER.debug(String.format(
                "ResourceLocation(s) not found from config: %s. ",
                String.join(", ", notFound)
            ));
        }

        return blocks;
    }

    // 3 Util methods
    /**
     * Is the block replaceable in general
     */
    public static boolean isBlockReplaceable(BlockState blockState) {
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksConfig.airReplaceable); // tmp
        return blockState.is(ModTags.Blocks.IS_AIR_REPLACEABLE) || configBlocks.contains(blockState.getBlock());
    }

    /**
     * Is the block replaceable for use underwater
     */
    public static boolean isBlockUnderwaterReplaceable(BlockState blockState) {
        return blockState.is(ModTags.Blocks.IS_WATER_REPLACEABLE);
    }


    /**
     * Is the block displaceable
     */
    public static boolean isBlockDisplaceable(BlockState blockState) {
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksConfig.notSpacialDisplaceable); // tmp
        return !blockState.is(ModTags.Blocks.IS_NOT_DISPLACABLE) && !configBlocks.contains(blockState.getBlock());
    }

    /**
     * Removes a block at a given position
     * Actually sets it to Air
     */
    public void removeBlock(Level world, BlockPos blockPos) {
        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
    }

    // End of 3 Util methods

    // All the below methods work with appendHoverText()

    /**
     * Adds all the cool toolTip info
     */
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, @NotNull TooltipFlag context) {
        CompoundTag tags = stack.getOrCreateTagElement(Constants.MODID);

        // Display height and width
        displayHeightAndWidth(tags, tooltip);

        // Display upgrades
        displayUpgrades(tags, tooltip);

        // Display fireproof
        if (stack.getItem().isFireResistant()) {
            tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.fireproof").setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.RED)));
        }

        // Display ink progress if inked
        displayInkProgress(tags, tooltip);

        // Display camp coordinates if deployed
        displayCampCoordinates(tags, world, tooltip);

        // Display boundaries if necessary
        displayBoundaries(tags, stack, tooltip);
    }

    private void displayHeightAndWidth(CompoundTag tags, List<Component> tooltip) {
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.height", height).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.width", width).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
    }

    private void displayUpgrades(CompoundTag tags, List<Component> tooltip) {
        ListTag upgrades = tags.getList(Constants.UPGRADES, Tag.TAG_STRING);
        upgrades.forEach(tag -> tooltip.add(Component.translatable("upgrade.nomadbooks." + tag.getAsString()).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA))));
    }

    private void displayInkProgress(CompoundTag tags, List<Component> tooltip) {
        if (tags.getBoolean(Constants.INKED)) {
            int inkProgress = tags.getInt(Constants.INK_PROGRESS);
            int inkGoal = tags.getInt(Constants.INK_GOAL);
            tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.itinerant_ink", inkProgress, inkGoal).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));
        }
    }

    private void displayCampCoordinates(CompoundTag tags, Level world, List<Component> tooltip) {
        if (tags.getFloat(Constants.DEPLOYED) == 1.0f) { // is deployed
            BlockPos pos = NbtUtils.readBlockPos(tags.getCompound(Constants.CAMP_POS));
            Optional<ResourceKey<Level>> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tags.get(Constants.DIMENSION)).result();

            ChatFormatting color = ChatFormatting.DARK_GRAY;
            Style style = Style.EMPTY.withColor(color);
            if (dimension.isPresent() && dimension.get() != world.dimension()) {
                style = style.applyFormats(ChatFormatting.OBFUSCATED);
            }
            tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).setStyle(style));
        }
    }

    private void displayBoundaries(CompoundTag tags, ItemStack stack, List<Component> tooltip) {
        if (stack.getItem() instanceof NomadBookItem && tags.getBoolean(Constants.DISPLAY_BOUNDARIES)) {
            tooltip.add(Component.translatable("item.nomadbooks.nomad_book.tooltip.boundaries_display").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(true)));
        }
    }

}
