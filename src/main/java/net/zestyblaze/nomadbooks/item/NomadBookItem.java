package net.zestyblaze.nomadbooks.item;

import joptsimple.internal.Strings;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
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
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.ModTags;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;
import net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig;
import net.zestyblaze.nomadbooks.util.NomadInkComponent;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.zestyblaze.nomadbooks.util.Helper.convertAABBtoBoundingBox;
import static net.zestyblaze.nomadbooks.util.Helper.getOrElse;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookHeight;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookWidth;

public class NomadBookItem extends Item {
    public static final int CAMP_RETRIEVAL_RADIUS = 32;

    public static final String DEFAULT_STRUCTURE_PATH = Constants.MODID + ":campfire3x1x3";
    public static final String NETHER_DEFAULT_STRUCTURE_PATH = Constants.MODID + ":nethercampfire7x3x7";
    public static final String CREATIVE_DEFAULT_STRUCTURE_PATH = Constants.MODID + ":nethercampfire15x15x15";

    public NomadBookItem(Settings properties) {
        super(properties);
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
        NomadBooksComponent tags = getOrElse(context.getStack().get(NomadBooks.NOMAD_BOOK_DATA), new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of()));
        boolean isDeployed = tags.isDeployed(); // desired value is false
        World world = context.getWorld();
        PlayerEntity player = Objects.requireNonNull(context.getPlayer());
        String structurePath = tags.structure();
        int height = tags.height();
        int width = tags.width();

        //---------------------------

        // Setup Logic / Checks
        if (isDeployed) {
            this.use(world, player, context.getHand()); // note calls use()
            return ActionResult.FAIL;
        }
        // checks just the clicked position. continues down to a valid place
        BlockPos pos = context.getBlockPos(); // Oh [neat](https://media1.tenor.com/m/UchYBXaC-1cAAAAC/futurama-bender.gif
        pos = findTheGround(pos, tags, world);
        // get dimension tag
        RegistryKey<World> dimension = world.getRegistryKey();
        // center position on camp center
        pos = pos.add(new BlockPos(-width / 2, 1, -width / 2));
        //-----------------------------

        // Upgrades Flags
        boolean hasAquaticMembrane = tags.upgrades().contains(Constants.AQUATIC_MEMBRANE); // 🟦
        boolean hasFungiSupport = tags.upgrades().contains(Constants.FUNGI_SUPPORT); // 🟪
        boolean hasSpacialDisplacer = tags.upgrades().contains(Constants.SPACIAL_DISPLACER); // 🟫
        boolean hasDefaultStructure = structurePath.equals(DEFAULT_STRUCTURE_PATH) || structurePath.equals(NETHER_DEFAULT_STRUCTURE_PATH) || structurePath.equals(CREATIVE_DEFAULT_STRUCTURE_PATH);
        hasSpacialDisplacer = hasSpacialDisplacer && !hasDefaultStructure; // If book has Displacer Page, but still uses the Default structure. Then pretend It doesn't have the Displacer Page.
        // -----------------------------------

        // Upgrades, Checks and Func
        // check for places that may have space, and moves to that available space
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
        // TODO concider experimenting with the order/priority of the upgrades. Like I feel that Spacial Displacer should act as a last resort. eg if Fungal Support can find a spot, then Funcal Support should be used without Spacial Displacer acting.
        if (!isSurfaceValid(world, pos, width)) { // if the surface is already valid then nothing in here matters // TODO Need to simplify this.
            boolean foundValid = false;
            if (!hasFungiSupport && hasSpacialDisplacer) { // under the upgrade condition !fs && sd , we check if we can move down to find a flat surface
                for (int i = 0; i < width/2; i++) { // we check down based on half the width of the camp rounded up. so wider camps can drop farther
                    if (isSurfaceValid(world, pos.down(i+1), width)) {
                        pos = pos.down(i+1);
                        campVolume = campVolume.offset(0, -i-1, 0);
                        foundValid = true;
                        break;
                    }
                }
            }
            // check if there's enough space (if we had to move)
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
        BlockBox fungiStemCheck = BlockBox.create(minPos, maxPos);
        if (fungiCapMovementCheck.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))
        || fungiStemCheck.intersects(convertAABBtoBoundingBox(player.getBoundingBox()))) {
            player.requestTeleport(player.getX(), pos.getY(), player.getZ());
        }
        // if membrane upgrade, replace water and underwater plants with membrane
        if (hasAquaticMembrane) { // 🟦 check + do
            List<BlockBox> membranePanels = getPanelsSurroundingBox(pos, width, height);
            Streams.failableStream(membranePanels).forEach(panel -> BlockPos.stream(panel)
                .filter(bp -> isBlockUnderwaterReplaceable(world.getBlockState(bp)))
                .forEach(bp -> {
                    world.breakBlock(bp, true);
                    world.setBlockState(bp, NomadBooks.MEMBRANE.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS);
                }));
        }
        // Save the Terrain as a structure using SPACIAL_DISPLACER
        if (hasSpacialDisplacer && !world.isClient()) { // 🟫 check + do --V
            StructureTemplateManager structureTemplateManager = ((ServerWorld) world).getStructureTemplateManager();
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getTemplateOrBlank(Identifier.of(structurePath + Constants.DISPLACED));
            } catch (InvalidIdentifierException e) {
                NomadBooks.LOGGER.error("Error creating or retrieving structure: {}", e.getMessage());
                return ActionResult.FAIL;
            }
            structure.saveFromWorld(world, pos.add(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID);
            structure.setAuthor(player.getNameForScoreboard());
            structureTemplateManager.saveTemplate(Identifier.of(structurePath + Constants.DISPLACED)); // added DISPLACED
        }
        // Place the structure
        if (!world.isClient()) {
            if (!hasSpacialDisplacer) {
                BlockPos.stream(campVolume)
                    .filter(bp -> isBlockReplaceable(world.getBlockState(bp))) // save drops from flowers, etc.
                    .forEach(bp -> {
                        world.breakBlock(bp, true);
                        world.setBlockState(bp, Blocks.AIR.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS);
                    });
            }
            placeStructure(world, structurePath, pos, width, hasSpacialDisplacer);
        }
        // set deployed, register nbt
        context.getStack().set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(true, false, height, width, structurePath, tags.upgrades())); // set is deployed
        context.getStack().set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of( new GlobalPos(dimension, pos)), false)); // set dimension + Position in LodestoneTracker

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 1);
        return ActionResult.SUCCESS;
    }

    private static @NotNull List<BlockBox> getPanelsSurroundingBox(BlockPos pos, int width, int height) {
        BlockBox membraneVolume = new BlockBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+ width, pos.getY()+ height, pos.getZ()+ width);
        BlockBox membraneMinX = new BlockBox(membraneVolume.getMinX(), membraneVolume.getMinY()+1, membraneVolume.getMinZ()+1, membraneVolume.getMinX() /*🟩*/, membraneVolume.getMaxY()-1, membraneVolume.getMaxZ()-1);
        BlockBox membraneMaxX = new BlockBox(membraneVolume.getMaxX() /*🟩*/, membraneVolume.getMinY()+1, membraneVolume.getMinZ()+1, membraneVolume.getMaxX(), membraneVolume.getMaxY()-1, membraneVolume.getMaxZ()-1);
        BlockBox membraneMinZ = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMinY()+1, membraneVolume.getMinZ(), membraneVolume.getMaxX()-1, membraneVolume.getMaxY()-1, membraneVolume.getMinZ() /*🟩*/);
        BlockBox membraneMaxZ = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMinY()+1, membraneVolume.getMaxZ() /*🟩*/, membraneVolume.getMaxX()-1, membraneVolume.getMaxY()-1, membraneVolume.getMaxZ());
        BlockBox membraneMaxY = new BlockBox(membraneVolume.getMinX()+1, membraneVolume.getMaxY() /*🟩*/, membraneVolume.getMinZ()+1, membraneVolume.getMaxX()-1, membraneVolume.getMaxY(), membraneVolume.getMaxZ()-1);
        return Arrays.asList(membraneMinX, membraneMaxX, membraneMinZ, membraneMaxZ, membraneMaxY);
    }

    /**
     * checks just the clicked position. If the position is replaceable, then continue checking down until something isn't replaceable
     * AKA: Find the ground
     */
    private BlockPos findTheGround(BlockPos pos, NomadBooksComponent tags, World level) {
        while (isBlockReplaceable(level.getBlockState(pos))
            || isBlockUnderwaterReplaceable(level.getBlockState(pos))
            && tags.upgrades().contains(Constants.AQUATIC_MEMBRANE)) { // 🟦 unrelated check to AQUATIC_MEMBRANE
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
                level.setBlockState(bp, NomadBooks.NOMAD_MUSHROOM_BLOCK.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS);
            }
        });
        Vec3i minPos = new Vec3i((width-1)/2 +pos.getX() -1, pos.getY() -5, (width-1)/2 +pos.getZ() -1);
        Vec3i maxPos = new Vec3i((width-1)/2 +pos.getX() +1, pos.getY() -2, (width-1)/2 +pos.getZ() +1);
        BlockBox fungiStem = BlockBox.create(minPos, maxPos);
        BlockPos.stream(fungiStem).forEach(bp -> {
            if (isBlockReplaceable(level.getBlockState(bp)) || isBlockUnderwaterReplaceable(level.getBlockState(bp))) {
                level.breakBlock(bp, true);
                level.setBlockState(bp, NomadBooks.NOMAD_MUSHROOM_STEM.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS);
            }
        });
    }

    private void placeStructure(World level, String structurePath, BlockPos pos, int width, boolean hasSpacialDisplacer) {
        ServerWorld serverLevel = (ServerWorld) level;
        Optional<StructureTemplate> structure = serverLevel.getStructureTemplateManager().getTemplate(Identifier.of(structurePath));

        if (structure.isPresent()) {
            int offsetWidth = (width - structure.get().getSize().getX()) / 2;
            StructurePlacementData placementData = new StructurePlacementData().setIgnoreEntities(true); // This would be false if when I make an entity mover upgrade. would need to kill anything in the area after saving in Use method
            if (hasSpacialDisplacer) {
                structure.get().place(serverLevel, pos.add(offsetWidth, 0, offsetWidth), pos.add(offsetWidth, 0, offsetWidth), placementData, serverLevel.getRandom(), Block.NOTIFY_LISTENERS + Block.FORCE_STATE + Block.SKIP_DROPS);
            } else {
                structure.get().place(serverLevel, pos.add(offsetWidth, 0, offsetWidth), pos.add(offsetWidth, 0, offsetWidth), placementData, serverLevel.getRandom(), Block.NOTIFY_LISTENERS + Block.FORCE_STATE); // Bingo
            }
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
        NomadBooksComponent tags = getOrElse(itemStack.get(NomadBooks.NOMAD_BOOK_DATA), new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of()));
        LodestoneTrackerComponent campTracker = getOrElse(itemStack.get(DataComponentTypes.LODESTONE_TRACKER), new LodestoneTrackerComponent(Optional.empty(), false));
        boolean isDeployed = tags.isDeployed(); // desired value is true

        if (!isDeployed) { // if camp isn't deployed, this use() method is not to be used. so use() is meant to retrieve a camp + other things
            return TypedActionResult.fail(itemStack);
        }

        BlockPos pos = campTracker.target().orElse(GlobalPos.create(world.getRegistryKey(), BlockPos.ORIGIN)).pos();
        int height = tags.height();
        int width = tags.width();
        String structurePath = tags.structure();

        if (user.isSneaking()) { // TODO if I add logic like this in the UseOn method, I could allow for rotation of the structure 90 degrees
            itemStack.set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(true, toggleBoundaries(user, tags), height, width, structurePath, tags.upgrades()));
            return TypedActionResult.pass(itemStack);
        }
        if (teleportToCampHandler(campTracker, world, user, pos, width)) { // teleport to camp. not shifting. and either tp will ender-pearls or too far message. else try retrieve below.
            return TypedActionResult.success(itemStack);
        }
        // Validate no blocks are in the blacklist
        if(!containsValidBlocks(user, world, pos, width, height)) {
            return TypedActionResult.fail(itemStack);
        }
        structurePath = createStructureIfDefaultOrRetrieve(structurePath, user, world, pos, width, height); // Create a new structure or retrieve the camp
        // set un-deployed & Remove Boundaries
        itemStack.set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, height, width, structurePath, tags.upgrades()));
        // process block removal/placement
        removeBlocks(tags, world, pos, width, height);
        placeTerrainWithSpacialDisplacer(tags, world, pos, width);
        // play sound
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1, 0.9f);
        return TypedActionResult.success(itemStack);
    }

    private boolean containsValidBlocks(PlayerEntity user, World world, BlockPos pos, int width, int height) {
        Set<Block> notifyList = new HashSet<>();
        if (!world.isClient) {
            BlockBox campVolume = BlockBox.create(pos, new Vec3i(pos.getX() + width - 1, pos.getY() + height - 1, pos.getZ() + width - 1));
            BlockPos.stream(campVolume).forEach(bp -> {
                BlockState blockState = world.getBlockState(bp);
                if ( !isBlockAllowedInCamp(blockState) ) {
                    notifyList.add(blockState.getBlock());
                }
            });
            if (!notifyList.isEmpty()) {
                user.sendMessage(Text.translatable("info.nomadbooks.notify_blacklist", notifyList.toString()), false);
            }
        }
        return  notifyList.isEmpty();
    }

    private void placeTerrainWithSpacialDisplacer(NomadBooksComponent tags, World world, BlockPos pos, int width) {

        String structurePath = tags.structure() + Constants.DISPLACED;

        if (tags.upgrades().contains(Constants.SPACIAL_DISPLACER)
        && !world.isClient()) {
            // Place the structure
            placeStructure(world, structurePath, pos, width, true); // Note need to handle change in size via ink while deployed for spacial displacer. see ServerPlayerMixin
        }
    }

    private boolean toggleBoundaries(PlayerEntity user, NomadBooksComponent tags) {
        boolean displayBoundaries = tags.doDisplayBoundaries();
        displayBoundaries = !displayBoundaries;
        user.sendMessage(Text.translatable(displayBoundaries ? "info.nomadbooks.display_boundaries_on" : "info.nomadbooks.display_boundaries_off"), true);
        return displayBoundaries;
    }

    /**
     * handler should return true if an action is performed within
     */
    private boolean teleportToCampHandler(LodestoneTrackerComponent tags, World world, PlayerEntity user, BlockPos pos, int width) {
        // handler should return true if an action is performed within
        RegistryKey<World> dimension = null;
        if (tags.target().isPresent()) {
            dimension = tags.target().get().dimension(); // NOSONAR
        } else {
            return false;
        }
        double centerX = pos.getX() + (width / 2.0) + 0.5;
        double centerZ = pos.getZ() + (width / 2.0) + 0.5;
        double distanceSquared = user.squaredDistanceTo(centerX, pos.getY(), centerZ);

        if (dimension != world.getRegistryKey()) {
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
    private String createStructureIfDefaultOrRetrieve(String structurePath, PlayerEntity user, World world, BlockPos pos, int width, int height) {
        // Check if the structure path needs to be generated
        if (structurePath.equals(DEFAULT_STRUCTURE_PATH) || structurePath.equals(NETHER_DEFAULT_STRUCTURE_PATH) || structurePath.equals(CREATIVE_DEFAULT_STRUCTURE_PATH)) {
            List<String> path = Arrays.asList(user.getUuid().toString(), String.valueOf(System.currentTimeMillis()));
            structurePath = Identifier.of(Constants.MODID, Strings.join(path, "/")).toString();
            NomadBooks.LOGGER.info("Creating Structure (Server thread): {}", structurePath);
        }
        // Server-side logic
        if (!world.isClient) {
            StructureTemplateManager structureTemplateManager = ((ServerWorld) world).getStructureTemplateManager();
            // Free beds from being occupied
            BlockBox campVolume = BlockBox.create(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
            BlockPos.stream(campVolume).forEach(bp -> {
                BlockState blockState = world.getBlockState(bp);
                if (blockState.getBlock() instanceof BedBlock) {
                    world.setBlockState(bp, blockState.with(BedBlock.OCCUPIED, false), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS);
                }
            });
            // Save the structure
            StructureTemplate structure;
            try {
                structure = structureTemplateManager.getTemplateOrBlank(Identifier.of(structurePath));
            } catch (InvalidIdentifierException e) {
                NomadBooks.LOGGER.error("(Server thread) Error creating or retrieving structure: {}", e.getMessage());
                return DEFAULT_STRUCTURE_PATH; // 🟧  Return false on error
            }
            structure.saveFromWorld(world, pos.add(new BlockPos(0, 0, 0)), new BlockPos(width, height, width), true, Blocks.STRUCTURE_VOID); // STRUCTURE_VOID is being ignored. treat it like air. No idea why I'm including entities
            structure.setAuthor(user.getNameForScoreboard());
            structureTemplateManager.saveTemplate(Identifier.of(structurePath));
        }
        return structurePath; // Return true indicating success
    }

    /**
     * Remove the blocks left behind after the structure/camp is saved.
     * includes the camp, and blocks from upgrades.
     */
    private void removeBlocks(NomadBooksComponent tags, World world, BlockPos pos, int width, int height) {
        // clear block entities && remove blocks using BoundingBox
        BlockBox campVolume = BlockBox.create(pos, new Vec3i(pos.getX()+width-1, pos.getY()+height-1, pos.getZ()+width-1));
        BlockPos.stream(campVolume).forEach(bp -> { // NOTE: this is what the fill command does
            // clear block entities && remove blocks
            world.removeBlockEntity(bp);
            world.setBlockState(bp, Blocks.AIR.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS + Block.FORCE_STATE + Block.SKIP_DROPS); // idk what 255 would do. I just guessed when I put it
            world.updateNeighbors(bp, Blocks.AIR);
        });
        // if membrane upgrade, remove membrane
        if (tags.upgrades().contains(Constants.AQUATIC_MEMBRANE)) {
            BlockBox membraneVolume = new BlockBox(pos.getX()-1, pos.getY()-1, pos.getZ()-1, pos.getX()+width, pos.getY()+height, pos.getZ()+width);
            BlockPos.stream(membraneVolume).forEach(bp -> {
                if (world.getBlockState(bp).getBlock().equals(NomadBooks.MEMBRANE)) {
                    setBlockWater(world, bp);
                }
            });
        }
        // if mushroom upgrade, remove mushroom blocks
        if (tags.upgrades().contains(Constants.FUNGI_SUPPORT)) {
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
                NomadBooks.LOGGER.debug("resource: {}", location);
                blocks.add(Registries.BLOCK.get(location));
            }
        }
        if (!notFound.isEmpty()) {
            NomadBooks.LOGGER.debug(
                "ResourceLocation(s) not found from config: %s. {}", notFound);
        }

        return blocks;
    }

    // 3 Util methods
    /**
     * Is the block replaceable in general
     */
    public static boolean isBlockReplaceable(BlockState blockState) {
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksYACLConfig.airReplaceable);
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
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksYACLConfig.notSpacialDisplaceable);
        return !blockState.isIn(ModTags.Blocks.IS_NOT_DISPLACEABLE) && isBlockAllowedInCamp(blockState) && !configBlocks.contains(blockState.getBlock());
    }

    /**
     * is the block <b>NOT</b> contained in the camp blacklist
     */
    public static boolean isBlockAllowedInCamp(BlockState blockState) {
        List<Block> configBlocks = getBlocksFromStrings(NomadBooksYACLConfig.campBlocksBlacklist);
        return !configBlocks.contains(blockState.getBlock());
    }

    /**
     * Removes a block at a given position
     * Actually sets it to Air
     */
    public void removeBlock(World world, BlockPos blockPos) {
        world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS + Block.FORCE_STATE);
    }

    /**
     * Set a block to water at a given position
     */
    public void setBlockWater(World world, BlockPos blockPos) {
        world.setBlockState(blockPos, Blocks.WATER.getDefaultState(), Block.NOTIFY_NEIGHBORS + Block.NOTIFY_LISTENERS + Block.SKIP_DROPS);
    }

    // End of 3 Util methods

    // All the below methods work with appendHoverText()

    /**
     * Adds all the cool toolTip info
     */
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options); // super

        // get stuff
        NomadBooksComponent tags = stack.getComponents().get(NomadBooks.NOMAD_BOOK_DATA);
        NomadInkComponent ink = stack.getComponents().get(NomadBooks.NOMAD_INK_DATA);
        LodestoneTrackerComponent campTracker = stack.getComponents().get(DataComponentTypes.LODESTONE_TRACKER);
        World world = MinecraftClient.getInstance().world;

        if (tags != null) {
            // Display height and width
            displayHeightAndWidth(tags, tooltip); // works

            // Display upgrades
            displayUpgrades(tags, tooltip); // works (but not in emi (also upgrades recipes don't show in emi because its special)

            // Display boundaries if necessary
            displayBoundaries(tags, tooltip); // works
        }

        // Display fireproof
        if (null != stack.getComponents().get(DataComponentTypes.FIRE_RESISTANT)) {  // works
            tooltip.add((Text.translatable("item.nomadbooks.nomad_book.tooltip.fireproof").setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.RED))));
        }

        // Display ink progress if inked
        if (ink != null) {
            displayInkProgress(ink, tooltip);
        }

        // Display camp coordinates if deployed
         if (tags != null && campTracker != null && tags.isDeployed()) { // is deployed
            displayCampCoordinates(campTracker, world, tooltip);
        }
    }

    private void displayHeightAndWidth(NomadBooksComponent tags, List<Text> tooltip) {
        int height = tags.height();
        int width = tags.width();
        tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.height", height).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.width", width).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
    }

    private void displayUpgrades(NomadBooksComponent tags, List<Text> tooltip) {
        List<String> upgrades = new ArrayList<>(tags.upgrades());
        upgrades.forEach(tag -> tooltip.add(Text.translatable("upgrade.nomadbooks." + tag).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA))));
    }

    private void displayInkProgress(NomadInkComponent tags, List<Text> tooltip) {
        if (tags.isInked()) {
            int inkProgress = tags.inkProgress();
            int inkGoal = tags.inkGoal();
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.itinerant_ink", inkProgress, inkGoal).setStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        }
    }

    private void displayCampCoordinates(LodestoneTrackerComponent tags, World world, List<Text> tooltip) {
        if (tags.target().isEmpty()) {
            return;
        }
        BlockPos pos = tags.target().get().pos(); // NOSONAR
        RegistryKey<World> dimension = tags.target().get().dimension(); // NOSONAR

        Formatting color = Formatting.DARK_GRAY;
        Style style = Style.EMPTY.withColor(color);
        if (dimension != world.getRegistryKey()) {
            style = style.withFormatting(Formatting.OBFUSCATED);
        }
        tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).setStyle(style));

    }

    private void displayBoundaries(NomadBooksComponent tags, List<Text> tooltip) {
        if (tags.doDisplayBoundaries()) {
            tooltip.add(Text.translatable("item.nomadbooks.nomad_book.tooltip.boundaries_display").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withItalic(true)));
        }
    }
}
