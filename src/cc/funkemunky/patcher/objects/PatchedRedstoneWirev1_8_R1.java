package cc.funkemunky.patcher.objects;

import cc.funkemunky.patcher.utils.ReflectUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_8_R1.BaseBlockPosition;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockDiodeAbstract;
import net.minecraft.server.v1_8_R1.BlockPiston;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.BlockRedstoneComparator;
import net.minecraft.server.v1_8_R1.BlockRedstoneTorch;
import net.minecraft.server.v1_8_R1.BlockRedstoneWire;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.EnumDirection;
import net.minecraft.server.v1_8_R1.EnumDirectionLimit;
import net.minecraft.server.v1_8_R1.IBlockAccess;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.World;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PatchedRedstoneWirev1_8_R1 extends BlockRedstoneWire implements RedstoneWire {
    private static final EnumDirection[] facingsHorizontal;
    private static final EnumDirection[] facingsVertical;
    private static final EnumDirection[] facings;
    private static final BaseBlockPosition[] surroundingBlocksOffset;

    static {
        facingsHorizontal = new EnumDirection[]{EnumDirection.WEST, EnumDirection.EAST, EnumDirection.NORTH, EnumDirection.SOUTH};
        facingsVertical = new EnumDirection[]{EnumDirection.DOWN, EnumDirection.UP};
        facings = (EnumDirection[]) ArrayUtils.addAll(PatchedRedstoneWirev1_8_R1.facingsVertical, (Object[]) PatchedRedstoneWirev1_8_R1.facingsHorizontal);
        final Set<BaseBlockPosition> set = Sets.newLinkedHashSet();
        for (final EnumDirection facing : PatchedRedstoneWirev1_8_R1.facings) {
            set.add( ReflectUtil.getOfT(facing, BaseBlockPosition.class));
        }
        for (final EnumDirection facing2 : PatchedRedstoneWirev1_8_R1.facings) {
            final BaseBlockPosition v1 =  ReflectUtil.getOfT(facing2, BaseBlockPosition.class);
            for (final EnumDirection facing3 : PatchedRedstoneWirev1_8_R1.facings) {
                final BaseBlockPosition v2 =  ReflectUtil.getOfT(facing3, BaseBlockPosition.class);
                set.add( new BlockPosition(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ()));
            }
        }
        set.remove(BlockPosition.ZERO);
        surroundingBlocksOffset = set.toArray(new BaseBlockPosition[set.size()]);
    }

    private final Set<BlockPosition> updatedRedstoneWire;
    private List<BlockPosition> turnOff;
    private List<BlockPosition> turnOn;
    private boolean g;

    public PatchedRedstoneWirev1_8_R1() {
        this.turnOff = Lists.newArrayList();
        this.turnOn = Lists.newArrayList();
        this.updatedRedstoneWire = Sets.newLinkedHashSet();
        this.g = true;
        this.c(0.0f);
        this.a(Block.e);
        this.c("redstoneDust");
        this.J();
    }

    private void e(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
        this.calculateCurrentChanges(world, blockposition);
        final Set<BlockPosition> blocksNeedingUpdate = Sets.newLinkedHashSet();
        for (final BlockPosition posi : this.updatedRedstoneWire) {
            this.addBlocksNeedingUpdate(world, posi, blocksNeedingUpdate);
        }
        final Iterator<BlockPosition> it = Lists.newLinkedList(this.updatedRedstoneWire).descendingIterator();
        while (it.hasNext()) {
            this.addAllSurroundingBlocks(it.next(), blocksNeedingUpdate);
        }
        blocksNeedingUpdate.removeAll(this.updatedRedstoneWire);
        this.updatedRedstoneWire.clear();
        for (final BlockPosition posi2 : blocksNeedingUpdate) {
            world.d(posi2,  this);
        }
    }

    private void calculateCurrentChanges(final World world, final BlockPosition blockposition) {
        if (world.getType(blockposition).getBlock() == this) {
            this.turnOff.add(blockposition);
        } else {
            this.checkSurroundingWires(world, blockposition);
        }
        while (!this.turnOff.isEmpty()) {
            final BlockPosition pos = this.turnOff.remove(0);
            IBlockData state = world.getType(pos);
            final int oldPower = (int) state.get(PatchedRedstoneWirev1_8_R1.POWER);
            this.g = false;
            final int blockPower = world.A(pos);
            this.g = true;
            int wirePower = this.getSurroundingWirePower(world, pos);
            --wirePower;
            final int newPower = Math.max(blockPower, wirePower);
            if (newPower < oldPower) {
                if (blockPower > 0 && !this.turnOn.contains(pos)) {
                    this.turnOn.add(pos);
                }
                state = this.setWireState(world, pos, state, 0);
            } else if (newPower > oldPower) {
                state = this.setWireState(world, pos, state, newPower);
            }
            this.checkSurroundingWires(world, pos);
        }
        while (!this.turnOn.isEmpty()) {
            final BlockPosition pos = this.turnOn.remove(0);
            IBlockData state = world.getType(pos);
            final int oldPower = (int) state.get(PatchedRedstoneWirev1_8_R1.POWER);
            this.g = false;
            final int blockPower = world.A(pos);
            this.g = true;
            int wirePower = this.getSurroundingWirePower(world, pos);
            --wirePower;
            int newPower = Math.max(blockPower, wirePower);
            if (oldPower != newPower) {
                final BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), oldPower, newPower);
                world.getServer().getPluginManager().callEvent(event);
                newPower = event.getNewCurrent();
            }
            if (newPower > oldPower) {
                state = this.setWireState(world, pos, state, newPower);
            } else if (newPower < oldPower) {
            }
            this.checkSurroundingWires(world, pos);
        }
        this.turnOff.clear();
        this.turnOn.clear();
    }

    private void addWireToList(final World worldIn, final BlockPosition pos, final int otherPower) {
        final IBlockData state = worldIn.getType(pos);
        if (state.getBlock() == this) {
            final int power = (int) state.get(PatchedRedstoneWirev1_8_R1.POWER);
            if (power < otherPower - 1 && !this.turnOn.contains(pos)) {
                this.turnOn.add(pos);
            }
            if (power > otherPower && !this.turnOff.contains(pos)) {
                this.turnOff.add(pos);
            }
        }
    }

    private void checkSurroundingWires(final World worldIn, final BlockPosition pos) {
        final IBlockData state = worldIn.getType(pos);
        int ownPower = 0;
        if (state.getBlock() == this) {
            ownPower = (int) state.get(PatchedRedstoneWirev1_8_R1.POWER);
        }
        for (final EnumDirection facing : PatchedRedstoneWirev1_8_R1.facingsHorizontal) {
            final BlockPosition offsetPos = pos.shift(facing);
            if (facing.k().c()) {
                this.addWireToList(worldIn, offsetPos, ownPower);
            }
        }
        for (final EnumDirection facingVertical : PatchedRedstoneWirev1_8_R1.facingsVertical) {
            final BlockPosition offsetPos = pos.shift(facingVertical);
            final boolean solidBlock = worldIn.getType(offsetPos).getBlock().u();
            for (final EnumDirection facingHorizontal : PatchedRedstoneWirev1_8_R1.facingsHorizontal) {
                if ((facingVertical == EnumDirection.UP && !solidBlock) || (facingVertical == EnumDirection.DOWN && solidBlock && !worldIn.getType(offsetPos.shift(facingHorizontal)).getBlock().isOccluding())) {
                    this.addWireToList(worldIn, offsetPos.shift(facingHorizontal), ownPower);
                }
            }
        }
    }

    private int getSurroundingWirePower(final World worldIn, final BlockPosition pos) {
        int wirePower = 0;
        for (final Object enumfacing : EnumDirectionLimit.HORIZONTAL) {
            final BlockPosition offsetPos = pos.shift((EnumDirection) enumfacing);
            wirePower = this.getPower(worldIn, offsetPos, wirePower);
            if (worldIn.getType(offsetPos).getBlock().isOccluding() && !worldIn.getType(pos.up()).getBlock().isOccluding()) {
                wirePower = this.getPower(worldIn, offsetPos.up(), wirePower);
            } else {
                if (worldIn.getType(offsetPos).getBlock().isOccluding()) {
                    continue;
                }
                wirePower = this.getPower(worldIn, offsetPos.down(), wirePower);
            }
        }
        return wirePower;
    }

    private void addBlocksNeedingUpdate(final World worldIn, final BlockPosition pos, final Set<BlockPosition> set) {
        final List<EnumDirection> connectedSides = this.getSidesToPower(worldIn, pos);
        for (final EnumDirection facing : PatchedRedstoneWirev1_8_R1.facings) {
            final BlockPosition offsetPos = pos.shift(facing);
            if ((connectedSides.contains(facing.opposite()) || facing == EnumDirection.DOWN || (facing.k().c() && a(worldIn.getType(offsetPos), facing))) && this.canBlockBePoweredFromSide(worldIn.getType(offsetPos), facing, true)) {
                set.add(offsetPos);
            }
        }
        for (final EnumDirection facing : PatchedRedstoneWirev1_8_R1.facings) {
            final BlockPosition offsetPos = pos.shift(facing);
            if ((connectedSides.contains(facing.opposite()) || facing == EnumDirection.DOWN) && worldIn.getType(offsetPos).getBlock().isOccluding()) {
                for (final EnumDirection facing2 : PatchedRedstoneWirev1_8_R1.facings) {
                    if (this.canBlockBePoweredFromSide(worldIn.getType(offsetPos.shift(facing2)), facing2, false)) {
                        set.add(offsetPos.shift(facing2));
                    }
                }
            }
        }
    }

    private boolean canBlockBePoweredFromSide(final IBlockData state, final EnumDirection side, final boolean isWire) {
        if (state.getBlock() instanceof BlockPiston && state.get(BlockPiston.FACING) == side.opposite()) {
            return false;
        }
        if (state.getBlock() instanceof BlockDiodeAbstract && state.get(BlockDiodeAbstract.FACING) != side.opposite()) {
            return isWire && state.getBlock() instanceof BlockRedstoneComparator && (state.get(BlockRedstoneComparator.FACING)) != side.k() && side.k().c();
        }
        return !(state.getBlock() instanceof BlockRedstoneTorch) || (!isWire && state.get(BlockRedstoneTorch.FACING) == side);
    }

    private List<EnumDirection> getSidesToPower(final World worldIn, final BlockPosition pos) {
        final List retval = Lists.newArrayList();
        for (final EnumDirection facing : PatchedRedstoneWirev1_8_R1.facingsHorizontal) {
            if (this.d(worldIn, pos, facing)) {
                retval.add(facing);
            }
        }
        if (retval.isEmpty()) {
            return Lists.newArrayList(PatchedRedstoneWirev1_8_R1.facingsHorizontal);
        }
        final boolean northsouth = retval.contains(EnumDirection.NORTH) || retval.contains(EnumDirection.SOUTH);
        final boolean eastwest = retval.contains(EnumDirection.EAST) || retval.contains(EnumDirection.WEST);
        if (northsouth) {
            retval.remove(EnumDirection.EAST);
            retval.remove(EnumDirection.WEST);
        }
        if (eastwest) {
            retval.remove(EnumDirection.NORTH);
            retval.remove(EnumDirection.SOUTH);
        }
        return retval;
    }

    private void addAllSurroundingBlocks(final BlockPosition pos, final Set<BlockPosition> set) {
        for (final BaseBlockPosition vect : PatchedRedstoneWirev1_8_R1.surroundingBlocksOffset) {
            set.add(pos.a(vect));
        }
    }

    private IBlockData setWireState(final World worldIn, final BlockPosition pos, IBlockData state, final int power) {
        state = state.set(PatchedRedstoneWirev1_8_R1.POWER,  power);
        worldIn.setTypeAndData(pos, state, 2);
        this.updatedRedstoneWire.add(pos);
        return state;
    }

    public int a(final IBlockAccess iblockaccess, final BlockPosition blockposition, final IBlockData iblockdata, final EnumDirection enumdirection) {
        if (!this.g) {
            return 0;
        }
        final int i = (int) iblockdata.get(BlockRedstoneWire.POWER);
        if (i == 0) {
            return 0;
        }
        if (enumdirection == EnumDirection.UP) {
            return i;
        }
        if (this.getSidesToPower((World) iblockaccess, blockposition).contains(enumdirection)) {
            return i;
        }
        return 0;
    }

    private boolean d(final IBlockAccess iblockaccess, final BlockPosition blockposition, final EnumDirection enumdirection) {
        final BlockPosition blockposition2 = blockposition.shift(enumdirection);
        final IBlockData iblockdata = iblockaccess.getType(blockposition2);
        final Block block = iblockdata.getBlock();
        final boolean flag = block.isOccluding();
        final boolean flag2 = iblockaccess.getType(blockposition.up()).getBlock().isOccluding();
        return (!flag2 && flag && e(iblockaccess, blockposition2.up())) || a(iblockdata, enumdirection) || (block == Blocks.POWERED_REPEATER && iblockdata.get(BlockDiodeAbstract.FACING) == enumdirection) || (!flag && e(iblockaccess, blockposition2.down()));
    }
}
