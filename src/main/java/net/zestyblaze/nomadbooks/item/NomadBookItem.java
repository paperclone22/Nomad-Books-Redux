package net.zestyblaze.nomadbooks.item;

import joptsimple.internal.Strings;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.ModTags;
import net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.zestyblaze.nomadbooks.util.Helper.convertAABBtoBoundingBox;

public class NomadBookItem extends Item implements DyeableItem {
    public static final int CAMP_RETRIEVAL_RADIUS = 20;

    public static final String DEFAULT_STRUCTURE_PATH = Constants.MODID + ":campfire3x1x3";
    public static final String NETHER_DEFAULT_STRUCTURE_PATH = Constants.MODID + ":nethercampfire7x3x7";
    public static final String CREATIVE_DEFAULT_STRUCTURE_PATH = Constants.MODID + ":nethercampfire15x15x15";

    public NomadBookItem(Settings properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultStack() {
        super.getDefaultStack();
        ItemStack itemStack = new ItemStack(this);
        NbtCompound tags = itemStack.getOrCreateSubNbt(Constants.MODID);
        tags.putInt(Constants.HEIGHT, 1);
        tags.putInt(Constants.WIDTH, 3);
        tags.putString(Constants.STRUCTURE, DEFAULT_STRUCTURE_PATH);
        return itemStack;
    }

    /**
     * Do things regarding a currently <b>NOT</b> deployed camp. click the item on a block
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
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
        NbtCompound tags = context.getStack().getOrCreateSubNbt(Constants.MODID);
        boolean isDeployed = context.getStack().getOrCreateNbt().getFloat(Constants.DEPLOYED) == 1f; // is deployed
        World world = context.getWorld();
        PlayerEntity player = Objects.requireNonNull(context.getPlayer());
        String structurePath = tags.getString(Constants.STRUCTURE);
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        //---------------------------

        // Setup Logic / Checks
        if (isDeployed) {
            this.use(world, player, context.getHand()); // note calls use()
            return ActionResult.FAIL;
        }
        // checks just the clicked position. continues down to a valid place
        BlockPos pos = context.getBlockPos(); // Oh [neat](https://media1.tenor.com/m/UchYBXaC-1cAAAAC/futurama-bender.gif
        pos = findTheGround(pos, tags, world);
        // set dimension tag
        World.CODEC.encodeStart(NbtOps.INSTANCE, world.getRegistryKey()).result().ifPresent(tag -> tags.put(Constants.DIMENSION, tag));
        // center position on camp center
        pos = pos.add(new BlockPos(-width / 2, 1, -width / 2));
        //-----------------------------

        // Upgrades Flags
        boolean hasAquaticMembrane = tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.AQUATIC_MEMBRANE)); // 🟦
        boolean hasFungiSupport = tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.FUNGI_SUPPORT)); // 🟪
        boolean hasSpacialDisplacer = tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.SPACIAL_DISPLACER)); // 🟫
        boolean hasDefaultStructure = tags.getString(Constants.STRUCTURE).equals(DEFAULT_STRUCTURE_PATH) || tags.getString(Constants.STRUCTURE).equals(NETHER_DEFAULT_STRUCTURE_PATH) || tags.getString(Constants.STRUCTURE).equals(CREATIVE_DEFAULT_STRUCTURE_PATH);
        hasSpacialDisplacer = hasSpacialDisplacer && !hasDefaultStructure; // If book has Displacer Page, but still uses the Default structure. Then pretend It doesn't have the Displacer Page.
        // -----------------------------------

        // Upgrades, Checks and Func
        // check if there's enough space
        BlockBox campVolume = BlockBox.create(pos, pos.add(width - 1, height - 1, width - 1));
        int spaceY;
        int maxChecks = NomadBooksYACLConfig.checksAboveOnDeploy;
        for (spaceY=0; spaceY <= maxChecks; spaceY++) {
            if (hasEnoughSpace(world, campVolume.offset(0,spaceY,0), hasAquaticMembrane, hasSpacialDisplacer)) { // 🟦🟫 check
                pos = pos.up(spaceY);
                campVolume = campVolume.offset(0,spaceY,0);
                break;
            }
        }
        if (spaceY > maxChecks && !hasSpacialDisplacer) { // here if SPACIAL_DISPLACER, ignore this fail // 🟫 check
            player.sendMessage(Text.translatable("error.nomadbooks.no_space"), true);
            return ActionResult.FAIL;
        }
        // mushroom platform upgrade FUNGI_SUPPORT
        if (hasFungiSupport) { // 🟪 check
            buildMushroomPlatform(world, pos, width); // 🟪 do
        }
        if (!isSurfaceValid(world, pos, width)) {
            boolean foundValid = false;
            if (!hasFungiSupport && hasSpacialDisplacer) {
                for (int i = 0; i < width/2; i++) {
                    if (isSurfaceValid(world, pos.down(i+1), width)) {
                        pos = pos.down(i+1);
                        campVolume = campVolume.offset(0, -i-1, 0);
                        foundValid = true;
                        break;
                    }
                }
            }
            if ( !hasEnoughSpace(world, campVolume, hasAquaticMembrane, hasSpacialDisplacer) ) {
                player.sendMessage(Text.translatable("error.nomadbooks.no_space"), true);
                return ActionResult.FAIL;
            }
            if (!foundValid) {
                player.sendMessage(Text.translatable("error.nomadbooks.invalid_surface"), true);
                return ActionResult.FAIL;
            }
        }
        // Move Player up with mushroom upgrade (no need to check the upgrade because it's not possible to place without it and satisfy the TP)
        BlockBox fungiCapMovementCheck = new BlockBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
        Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
        Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
        BlockBox fungiStem = BlockBox.create(minPos, maxPos);
        if (fungiCapMovementCheck.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))
        || fungiStem.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))) {
            player.requestTeleport(player.getX(), pos.getY(), player.getZ());
        }
        // if membrane upgrade, replace water and underwater plants with membrane
        if (hasAquaticMembrane) { // 🟦 check + do
            BlockBox membraneVolume = new BlockBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+width, pos.getY()+height, pos.getZ()+width);
            BlockBox membraneMinX = new BlockBox(membraneVolume.getMinX(), membraneVolume.getMinY()+1, membraneVolume.getMinZ()+1, membraneVolume.getMinX() /*🟩*/, membraneVolume.getMaxY()-1, membraneVolume.getMaxZ()-1);
            BlockBox membraneMaxX = new BlockBox(membraneVolume.getMaxX() /*🟩*/, membraneVolume.getMinY()+1, membraneVolume.getMinZ()+1, membraneVolume.getMaxX(), membraneVolume.getMaxY()-1, membraneVolume.getMaxZ()-1);
            BlockBox membraneMinZ = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMinY()+1, membraneVolume.getMinZ(), membraneVolume.getMaxX()-1, membraneVolume.getMaxY()-1, membraneVolume.getMinZ() /*🟩*/);
            BlockBox membraneMaxZ = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMinY()+1, membraneVolume.getMaxZ() /*🟩*/, membraneVolume.getMaxX()-1, membraneVolume.getMaxY()-1, membraneVolume.getMaxZ());
            BlockBox membraneMaxY = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMaxY() /*🟩*/, membraneVolume.getMinZ()+1, membraneVolume.getMaxX()-1, membraneVolume.getMaxY(), membraneVolume.getMaxZ()-1);
            List<BlockBox> membranePanels = Arrays.asList(membraneMinX, membraneMaxX, membraneMinZ, membraneMaxZ, membraneMaxY);
            Streams.stream(membranePanels).forEach(panel -> BlockPos.stream(panel)
                .filter(bp -> isBlockUnderwaterReplaceable(world.getBlockState(bp)))
                .forEach(bp -> {
                    world.breakBlock(bp, true);
                    world.setBlockState(bp, NomadBooks.MEMBRANE.getDefaultState(), 2);
                }));
        }
        // Save the Terrain as a structure using SPACIAL_DISPLACER
        if (hasSpacialDisplacer && !world.isClient()) { // 🟫 check + do --V
            ServerWorld serverLevel = (ServerWorld) world;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureTemplateManager();
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getTemplateOrBlank(new Identifier(structurePath + Constants.DISPLACED));
            } catch (InvalidIdentifierException e) {
                NomadBooks.LOGGER.error("Error creating or retrieving structure: {}", e.getMessage());
                return ActionResult.FAIL;
            }
            structure.saveFromWorld(world, pos.add(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID);
            structure.setAuthor(player.getEntityName());
            structureTemplateManager.saveTemplate(new Identifier(structurePath + Constants.DISPLACED)); // added DISPLACED
        }
        // destroy destroyable blocks in the way
        BlockPos.stream(campVolume).forEach(bp -> world.setBlockState(bp, Blocks.AIR.getDefaultState(), 2));
        // Place the structure
        if (!world.isClient()) {
            placeStructure(world, structurePath, pos, width);
        }
        // set deployed, register nbt
        context.getStack().getOrCreateNbt().putFloat(Constants.DEPLOYED, 1F); // set is deployed
        tags.put(Constants.CAMP_POS, NbtHelper.fromBlockPos(pos));

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 1);
        return ActionResult.SUCCESS;
    }

    /**
     * checks just the clicked position. If the position is replaceable, then continue checking down until something isn't replaceable
     * AKA: Find the ground
     */
    private BlockPos findTheGround(BlockPos pos, NbtCompound tags, World level) {
//        BlockPos pos = context.getClickedPos(); // Oh [neat](https://media1.tenor.com/m/UchYBXaC-1cAAAAC/futurama-bender.gif
        while (isBlockReplaceable(level.getBlockState(pos))
            || isBlockUnderwaterReplaceable(level.getBlockState(pos))
            && tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.AQUATIC_MEMBRANE))) { // 🟦 unrelated check to AQUATIC_MEMBRANE
            pos = pos.down();
        }
        return pos;
    }

    /**
     * Basically: false if surface has any replaceable. true if a complete "Solid" surface
     */
    private boolean isSurfaceValid(World world, BlockPos pos, int width) {
        BlockBox surface = new BlockBox(pos.getX(), pos.getY()-1, pos.getZ(), pos.getX()+width-1, pos.getY()-1, pos.getZ()+width-1);
        return BlockPos.stream(surface).noneMatch(bp -> isBlockReplaceable(world.getBlockState(bp)));
    }

    private boolean hasEnoughSpace(World world, BlockBox campVolume, boolean hasAquatic, boolean hasDisplacer) {
        return BlockPos.stream(campVolume).allMatch(bp -> {
            BlockState bs = world.getBlockState(bp);
            return isBlockReplaceable(bs)
                || isBlockUnderwaterReplaceable(bs) && hasAquatic
                || isBlockDisplaceable(bs) && hasDisplacer;
        });
    }

    private void buildMushroomPlatform(World level, BlockPos pos, int width) {
        BlockBox fungiCap = new BlockBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
        BlockPos.stream(fungiCap).forEach(bp -> {
            BlockState bs = level.getBlockState(bp);
            if (isBlockReplaceable(bs) || isBlockUnderwaterReplaceable(bs)) {
                level.breakBlock(bp, true);
                level.setBlockState(bp, NomadBooks.NOMAD_MUSHROOM_BLOCK.getDefaultState(), 2);
            }
        });
        Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
        Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
        BlockBox fungiStem = BlockBox.create(minPos, maxPos);
        BlockPos.stream(fungiStem).forEach(bp -> {
            if (isBlockReplaceable(level.getBlockState(bp)) || isBlockUnderwaterReplaceable(level.getBlockState(bp))) {
                level.breakBlock(bp, true);
                level.setBlockState(bp, NomadBooks.NOMAD_MUSHROOM_STEM.getDefaultState(), 2);
            }
        });
    }

    private void placeStructure(World level, String structurePath, BlockPos pos, int width) {
        ServerWorld serverLevel = (ServerWorld) level;
        Optional<StructureTemplate> structure = serverLevel.getStructureTemplateManager().getTemplate(new Identifier(structurePath));

        if (structure.isPresent()) {
            int offsetWidth = (width - structure.get().getSize().getX()) / 2;
            StructurePlacementData placementData = new StructurePlacementData().setIgnoreEntities(true);
            structure.get().place(serverLevel, pos.add(offsetWidth, 0, offsetWidth), pos.add(offsetWidth, 0, offsetWidth), placementData, serverLevel.getRandom(), 2); // Bingo
        }
    }

    // begin methods related to use()

    /**
     * Do things regarding a currently deployed camp. (click the item on "nothing")
     * Such as: toggle display boundaries when shift-right-click, teleport to it using offhand ender-pearls, first-time use save as a new structure file, retrieve the camp if within normal distance
     */
    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, PlayerEntity user, @NotNull Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound tags = itemStack.getOrCreateSubNbt(Constants.MODID);
        boolean isDeployed = itemStack.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 1f;

        if (!isDeployed) { // if camp isn't deployed, this use() method is not to be used. so use() is meant to retrieve a camp + other things
            return TypedActionResult.fail(itemStack);
        }

        BlockPos pos = NbtHelper.toBlockPos(tags.getCompound(Constants.CAMP_POS));
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        String structurePath = tags.getString(Constants.STRUCTURE);

        if (user.isSneaking()) {
            toggleBoundaries(user, tags);
            return TypedActionResult.pass(itemStack);
        }
        if (teleportToCampHandler(tags, world, user, pos, width)) { // teleport to camp. not shifting. and either tp will ender-pearls or too far message. else try retrieve below.
            return TypedActionResult.success(itemStack);
        }
        if (createStructureIfDefaultOrRetrieve(tags, structurePath, user, world, pos, width, height)) { // Create a new structure or retrieve the camp. only false on error
            // set undeployed
            itemStack.getOrCreateNbt().putFloat(Constants.DEPLOYED, 0F);
                removeBlocks(tags, world, pos, width, height);
            placeTerrainWithSpacialDisplacer(tags, world, pos, width);
            // Remove Boundaries and play sound
            tags.putBoolean(Constants.DISPLAY_BOUNDARIES, false);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 0.9f);
            return TypedActionResult.success(itemStack);
        }
        // Probably wont ever be hit
        return TypedActionResult.fail(itemStack);
    }

    private void placeTerrainWithSpacialDisplacer(NbtCompound tags, World world, BlockPos pos, int width) {

        String structurePath = tags.getString(Constants.STRUCTURE) + Constants.DISPLACED;

        if (tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.SPACIAL_DISPLACER))
        && !world.isClient()) {
            // Place the structure
            placeStructure(world, structurePath, pos, width); // TODO should I add checks for if the player upgraded the camp size?
        }
    }

    private void toggleBoundaries(PlayerEntity user, NbtCompound tags) {
        boolean displayBoundaries = tags.getBoolean(Constants.DISPLAY_BOUNDARIES);
        displayBoundaries = !displayBoundaries;
        user.sendMessage(Text.translatable(displayBoundaries ? "info.nomadbooks.display_boundaries_on" : "info.nomadbooks.display_boundaries_off"), true);
        tags.putBoolean(Constants.DISPLAY_BOUNDARIES, displayBoundaries);
    }

    /**
     * handler should return true if an action is performed within
     */
    private boolean teleportToCampHandler(NbtCompound tags, World world, PlayerEntity user, BlockPos pos, int width) {
        // handler should return true if an action is performed within
        Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, tags.get(Constants.DIMENSION)).result();
        double centerX = pos.getX() + (width / 2.0) + 0.5;
        double centerZ = pos.getZ() + (width / 2.0) + 0.5;
        double distanceSquared = user.squaredDistanceTo(centerX, pos.getY(), centerZ);

        if (dimension.isEmpty() || dimension.get() != world.getRegistryKey()) {
            user.sendMessage(Text.translatable("error.nomadbooks.different_dimension"), true);
            return true; // Dimension mismatch
        }
        if (distanceSquared > CAMP_RETRIEVAL_RADIUS * CAMP_RETRIEVAL_RADIUS) {
            int enderPrice = (int) Math.ceil(Math.sqrt(distanceSquared) / 100);

            // Insufficient ender pearls
            if (user.getOffHandStack().getItem() == Items.ENDER_PEARL && user.getOffHandStack().getCount() >= enderPrice) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
                user.requestTeleport(centerX, pos.getY(), centerZ);
                if (!user.isCreative()) {
                    user.getOffHandStack().decrement(enderPrice);
                }
                world.playSound(null, centerX, pos.getY(), centerZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
            } else {
                user.sendMessage(Text.translatable("error.nomadbooks.camp_too_far"), true);
            }
            return true; // Teleported
        }
        return false; // No action needed
    }

    /**
     * Create a new structure or retrieve the camp. only false on error
     */
    private boolean createStructureIfDefaultOrRetrieve(NbtCompound tags, String structurePath, PlayerEntity user, World world, BlockPos pos, int width, int height) {
        // Check if the structure path needs to be generated
        if (structurePath.equals(DEFAULT_STRUCTURE_PATH) || structurePath.equals(NETHER_DEFAULT_STRUCTURE_PATH) || structurePath.equals(CREATIVE_DEFAULT_STRUCTURE_PATH)) {
            List<String> path = Arrays.asList(user.getUuid().toString(), String.valueOf(System.currentTimeMillis()));
            structurePath = new Identifier(Constants.MODID, Strings.join(path, "/")).toString();
            NomadBooks.LOGGER.info("Creating Structure (Server thread): " + structurePath);
            tags.putString(Constants.STRUCTURE, structurePath);
        }
        // Server-side logic
        if (!world.isClient) {
            ServerWorld serverLevel = (ServerWorld) world;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureTemplateManager();
            // Free beds from being occupied
            BlockBox campVolume = BlockBox.create(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
            BlockPos.stream(campVolume).forEach(bp -> {
                BlockState blockState = world.getBlockState(bp);
                if (blockState.getBlock() instanceof BedBlock) {
                    world.setBlockState(bp, blockState.with(BedBlock.OCCUPIED, false), 2);
                }
            });
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getTemplateOrBlank(new Identifier(structurePath));
            } catch (InvalidIdentifierException e) {
                NomadBooks.LOGGER.error("Error creating or retrieving structure: {}", e.getMessage());
                return false; // 🟧  Return false on error
            }
            structure.saveFromWorld(world, pos.add(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID);
            structure.setAuthor(user.getEntityName());
            structureTemplateManager.saveTemplate(new Identifier(structurePath));
        }
        return true; // Return true indicating success
    }

    /**
     * Remove the blocks left behind after the structure/camp is saved.
     * includes the camp, and blocks from upgrades.
     */
    private void removeBlocks(NbtCompound tags, World world, BlockPos pos, int width, int height) {
        // clear block entities && remove blocks using BoundingBox
        BlockBox campVolume = BlockBox.create(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
        BlockPos.stream(campVolume).forEach(bp -> { // NOTE: this is what the fill command does
            // clear block entities && remove blocks
            world.removeBlockEntity(bp);
            world.setBlockState(bp, Blocks.AIR.getDefaultState(), 255);
            world.updateNeighbors(bp, Blocks.AIR);
        });
        // if membrane upgrade, remove membrane
        if (tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.AQUATIC_MEMBRANE))) {
            BlockBox membraneVolume = new BlockBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+width, pos.getY()+height, pos.getZ()+width);
            BlockPos.stream(membraneVolume).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.MEMBRANE)) {
                    removeBlock(world, bp);
                }
            });
        }
        // if mushroom upgrade, remove mushroom blocks
        if (tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE).contains(NbtString.of(Constants.FUNGI_SUPPORT))) {
            BlockBox fungiCap = new BlockBox(pos.getX(), pos.getY() -1, pos.getZ(), pos.getX() +width -1, pos.getY() -1, pos.getZ() +width -1);
            BlockPos.stream(fungiCap).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.NOMAD_MUSHROOM_BLOCK)) {
                    removeBlock(world, bp);
                }
            });
            Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
            Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
            BlockBox fungiStem = BlockBox.create(minPos, maxPos);
            BlockPos.stream(fungiStem).forEach(bp -> {
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
            Identifier location = Identifier.tryParse(resource);
            if (location == null) {
                notFound.add(resource);
            } else {
                NomadBooks.LOGGER.debug("resource: " + location);
                blocks.add(Registries.BLOCK.get(location));
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
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksYACLConfig.airReplaceable); // tmp
        return blockState.isIn(ModTags.Blocks.IS_AIR_REPLACEABLE) || configBlocks.contains(blockState.getBlock());
    }

    /**
     * Is the block replaceable for use underwater
     */
    public static boolean isBlockUnderwaterReplaceable(BlockState blockState) {
        return blockState.isIn(ModTags.Blocks.IS_WATER_REPLACEABLE);
    }


    /**
     * Is the block displaceable
     */
    public static boolean isBlockDisplaceable(BlockState blockState) {
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksYACLConfig.notSpacialDisplaceable); // tmp
        return !blockState.isIn(ModTags.Blocks.IS_NOT_DISPLACABLE) && !configBlocks.contains(blockState.getBlock());
    }

    /**
     * Removes a block at a given position
     * Actually sets it to Air
     */
    public void removeBlock(World world, BlockPos blockPos) {
        world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);
    }

    // End of 3 Util methods

    // All the below methods work with appendHoverText()

    /**
     * Adds all the cool toolTip info
     */
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, @NotNull TooltipContext context) {
        NbtCompound tags = stack.getOrCreateSubNbt(Constants.MODID);

        // Display height and width
        displayHeightAndWidth(tags, tooltip);

        // Display upgrades
        displayUpgrades(tags, tooltip);

        // Display fireproof
        if (stack.getItem().isFireproof()) {
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.fireproof").setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.RED)));
        }

        // Display ink progress if inked
        displayInkProgress(tags, tooltip);

        // Display camp coordinates if deployed
        displayCampCoordinates(tags, world, tooltip);

        // Display boundaries if necessary
        displayBoundaries(tags, stack, tooltip);
    }

    private void displayHeightAndWidth(NbtCompound tags, List<Text> tooltip) {
        int height = tags.getInt(Constants.HEIGHT);
        int width = tags.getInt(Constants.WIDTH);
        tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.height", height).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.width", width).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
    }

    private void displayUpgrades(NbtCompound tags, List<Text> tooltip) {
        NbtList upgrades = tags.getList(Constants.UPGRADES, NbtElement.STRING_TYPE);
        upgrades.forEach(tag -> tooltip.add(Text.translatable("upgrade.nomadbooks." + tag.asString()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA))));
    }

    private void displayInkProgress(NbtCompound tags, List<Text> tooltip) {
        if (tags.getBoolean(Constants.INKED)) {
            int inkProgress = tags.getInt(Constants.INK_PROGRESS);
            int inkGoal = tags.getInt(Constants.INK_GOAL);
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.itinerant_ink", inkProgress, inkGoal).setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        }
    }

    private void displayCampCoordinates(NbtCompound tags, World world, List<Text> tooltip) {
        if (tags.getFloat(Constants.DEPLOYED) == 1.0f) { // is deployed
            BlockPos pos = NbtHelper.toBlockPos(tags.getCompound(Constants.CAMP_POS));
            Optional<RegistryKey<World>> dimension = World.CODEC.parse(NbtOps.INSTANCE, tags.get(Constants.DIMENSION)).result();

            Formatting color = Formatting.DARK_GRAY;
            Style style = Style.EMPTY.withColor(color);
            if (dimension.isPresent() && dimension.get() != world.getRegistryKey()) {
                style = style.withFormatting(Formatting.OBFUSCATED);
            }
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).setStyle(style));
        }
    }

    private void displayBoundaries(NbtCompound tags, ItemStack stack, List<Text> tooltip) {
        if (stack.getItem() instanceof NomadBookItem && tags.getBoolean(Constants.DISPLAY_BOUNDARIES)) {
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.boundaries_display").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withItalic(true)));
        }
    }

}
