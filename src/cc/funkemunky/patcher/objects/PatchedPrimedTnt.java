package cc.funkemunky.patcher.objects;

import cc.funkemunky.patcher.GrassPatcher;
import cc.funkemunky.patcher.utils.Config;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.*;
import cc.funkemunky.patcher.objects.Explosion;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PatchedPrimedTnt extends EntityTNTPrimed {

    public int fuseTicks;
    public float yield = 4; // CraftBukkit - add field
    public boolean isIncendiary = false; // CraftBukkit - add field
    private EntityLiving source;
    private int primed = 0;
    private int h;

    public PatchedPrimedTnt(World world) {
        super(world);
        this.k = true;
        this.setSize(0.98F, 0.98F);
    }

    public PatchedPrimedTnt(World world, double d0, double d1, double d2, EntityLiving entityliving) {
        this(world);
        this.setPosition(d0, d1, d2);
        float f = (float) (Math.random() * 3.1415927410125732D * 2.0D);

        this.motX = (double) (-((float) Math.sin((double) f)) * 0.02F);
        this.motY = 0.20000000298023224D;
        this.motZ = (double) (-((float) Math.cos((double) f)) * 0.02F);
        this.fuseTicks = Config.max_tnt_per_tick;
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
        this.source = entityliving;
    }

    protected void h() {
    }

    protected boolean s_() {
        return false;
    }

    public boolean ad() {
        return !this.dead;
    }

    public synchronized void move(double d0, double d1, double d2) {
        if (this.noclip) {
            this.a(this.getBoundingBox().c(d0, d1, d2));
            this.recalcPosition();
        } else {
            try {
                this.checkBlockCollisions();
            } catch (Throwable var84) {
                CrashReport crashreport = CrashReport.a(var84, "Checking entity block collision");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being checked for collision");
                this.appendEntityCrashDetails(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }

            if (d0 == 0.0D && d1 == 0.0D && d2 == 0.0D && this.vehicle == null && this.passenger == null) {
                return;
            }

            this.world.methodProfiler.a("move");
            double d3 = this.locX;
            double d4 = this.locY;
            double d5 = this.locZ;
            if (this.H) {
                this.H = false;
                d0 *= 0.25D;
                d1 *= 0.05000000074505806D;
                d2 *= 0.25D;
                this.motX = 0.0D;
                this.motY = 0.0D;
                this.motZ = 0.0D;
            }

            double d6 = d0;
            double d7 = d1;
            double d8 = d2;

            List<AxisAlignedBB> list = Lists.newArrayList();
            List<AxisAlignedBB> cubes = this.world.getCubes(this, this.getBoundingBox().a(d0, d1, d2));
            synchronized (cubes) {
                cubes.forEach(cube -> list.add(cube));
            }
            AxisAlignedBB axisalignedbb = this.getBoundingBox();

            AxisAlignedBB axisalignedbb1;
            for(Iterator iterator = list.iterator(); iterator.hasNext(); d1 = axisalignedbb1.b(this.getBoundingBox(), d1)) {
                axisalignedbb1 = (AxisAlignedBB)iterator.next();
            }

            this.a(this.getBoundingBox().c(0.0D, d1, 0.0D));
            boolean flag1 = this.onGround || d7 != d1 && d7 < 0.0D;

            Iterator iterator1;
            AxisAlignedBB axisalignedbb2;
            for(iterator1 = list.iterator(); iterator1.hasNext(); d0 = axisalignedbb2.a(this.getBoundingBox(), d0)) {
                axisalignedbb2 = (AxisAlignedBB)iterator1.next();
            }

            this.a(this.getBoundingBox().c(d0, 0.0D, 0.0D));

            for(iterator1 = list.iterator(); iterator1.hasNext(); d2 = axisalignedbb2.c(this.getBoundingBox(), d2)) {
                axisalignedbb2 = (AxisAlignedBB)iterator1.next();
            }

            this.a(this.getBoundingBox().c(0.0D, 0.0D, d2));
            if (this.S > 0.0F && flag1 && (d6 != d0 || d8 != d2)) {
                double d10 = d0;
                double d11 = d1;
                double d12 = d2;
                AxisAlignedBB axisalignedbb3 = this.getBoundingBox();
                this.a(axisalignedbb);
                d1 = (double)this.S;
                List list1 = this.world.getCubes(this, this.getBoundingBox().a(d6, d1, d8));
                AxisAlignedBB axisalignedbb4 = this.getBoundingBox();
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.a(d6, 0.0D, d8);
                double d13 = d1;

                AxisAlignedBB axisalignedbb6;
                for(Iterator iterator2 = list1.iterator(); iterator2.hasNext(); d13 = axisalignedbb6.b(axisalignedbb5, d13)) {
                    axisalignedbb6 = (AxisAlignedBB)iterator2.next();
                }

                axisalignedbb4 = axisalignedbb4.c(0.0D, d13, 0.0D);
                double d14 = d6;

                AxisAlignedBB axisalignedbb7;
                for(Iterator iterator3 = list1.iterator(); iterator3.hasNext(); d14 = axisalignedbb7.a(axisalignedbb4, d14)) {
                    axisalignedbb7 = (AxisAlignedBB)iterator3.next();
                }

                axisalignedbb4 = axisalignedbb4.c(d14, 0.0D, 0.0D);
                double d15 = d8;

                AxisAlignedBB axisalignedbb8;
                for(Iterator iterator4 = list1.iterator(); iterator4.hasNext(); d15 = axisalignedbb8.c(axisalignedbb4, d15)) {
                    axisalignedbb8 = (AxisAlignedBB)iterator4.next();
                }

                axisalignedbb4 = axisalignedbb4.c(0.0D, 0.0D, d15);
                AxisAlignedBB axisalignedbb9 = this.getBoundingBox();
                double d16 = d1;

                AxisAlignedBB axisalignedbb10;
                for(Iterator iterator5 = list1.iterator(); iterator5.hasNext(); d16 = axisalignedbb10.b(axisalignedbb9, d16)) {
                    axisalignedbb10 = (AxisAlignedBB)iterator5.next();
                }

                axisalignedbb9 = axisalignedbb9.c(0.0D, d16, 0.0D);
                double d17 = d6;

                AxisAlignedBB axisalignedbb11;
                for(Iterator iterator6 = list1.iterator(); iterator6.hasNext(); d17 = axisalignedbb11.a(axisalignedbb9, d17)) {
                    axisalignedbb11 = (AxisAlignedBB)iterator6.next();
                }

                axisalignedbb9 = axisalignedbb9.c(d17, 0.0D, 0.0D);
                double d18 = d8;

                AxisAlignedBB axisalignedbb12;
                for(Iterator iterator7 = list1.iterator(); iterator7.hasNext(); d18 = axisalignedbb12.c(axisalignedbb9, d18)) {
                    axisalignedbb12 = (AxisAlignedBB)iterator7.next();
                }

                axisalignedbb9 = axisalignedbb9.c(0.0D, 0.0D, d18);
                double d19 = d14 * d14 + d15 * d15;
                double d20 = d17 * d17 + d18 * d18;
                if (d19 > d20) {
                    d0 = d14;
                    d2 = d15;
                    d1 = -d13;
                    this.a(axisalignedbb4);
                } else {
                    d0 = d17;
                    d2 = d18;
                    d1 = -d16;
                    this.a(axisalignedbb9);
                }

                AxisAlignedBB axisalignedbb13;
                for(Iterator iterator8 = list1.iterator(); iterator8.hasNext(); d1 = axisalignedbb13.b(this.getBoundingBox(), d1)) {
                    axisalignedbb13 = (AxisAlignedBB)iterator8.next();
                }

                this.a(this.getBoundingBox().c(0.0D, d1, 0.0D));
                if (d10 * d10 + d12 * d12 >= d0 * d0 + d2 * d2) {
                    d0 = d10;
                    d1 = d11;
                    d2 = d12;
                    this.a(axisalignedbb3);
                }
            }

            this.world.methodProfiler.b();
            this.world.methodProfiler.a("rest");
            this.recalcPosition();
            this.positionChanged = d6 != d0 || d8 != d2;
            this.E = d7 != d1;
            this.onGround = this.E && d7 < 0.0D;
            this.F = this.positionChanged || this.E;
            int i = MathHelper.floor(this.locX);
            int j = MathHelper.floor(this.locY - 0.20000000298023224D);
            int k = MathHelper.floor(this.locZ);
            BlockPosition blockposition = new BlockPosition(i, j, k);
            net.minecraft.server.v1_8_R3.Block block = this.world.getType(blockposition).getBlock();
            if (block.getMaterial() == Material.AIR) {
                net.minecraft.server.v1_8_R3.Block block1 = this.world.getType(blockposition.down()).getBlock();
                if (block1 instanceof BlockFence || block1 instanceof BlockCobbleWall || block1 instanceof BlockFenceGate) {
                    block = block1;
                    blockposition = blockposition.down();
                }
            }

            this.a(d1, this.onGround, block, blockposition);
            if (d6 != d0) {
                this.motX = 0.0D;
            }

            if (d8 != d2) {
                this.motZ = 0.0D;
            }

            if (d7 != d1) {
                block.a(this.world, this);
            }

            if (this.positionChanged && this.getBukkitEntity() instanceof Vehicle) {
                Vehicle vehicle = (Vehicle)this.getBukkitEntity();
                org.bukkit.block.Block bl = this.world.getWorld().getBlockAt(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ));
                if (d6 > d0) {
                    bl = bl.getRelative(BlockFace.EAST);
                } else if (d6 < d0) {
                    bl = bl.getRelative(BlockFace.WEST);
                } else if (d8 > d2) {
                    bl = bl.getRelative(BlockFace.SOUTH);
                } else if (d8 < d2) {
                    bl = bl.getRelative(BlockFace.NORTH);
                }

                VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, bl);
                this.world.getServer().getPluginManager().callEvent(event);
            }

            if (this.s_() && this.vehicle == null) {
                double d21 = this.locX - d3;
                double d22 = this.locY - d4;
                double d23 = this.locZ - d5;
                if (block != Blocks.LADDER) {
                    d22 = 0.0D;
                }

                this.M = (float)((double)this.M + (double)MathHelper.sqrt(d21 * d21 + d23 * d23) * 0.6D);
                this.N = (float)((double)this.N + (double)MathHelper.sqrt(d21 * d21 + d22 * d22 + d23 * d23) * 0.6D);
                if (this.N > (float)this.h && block.getMaterial() != Material.AIR) {
                    this.h = (int)this.N + 1;
                    if (this.V()) {
                        float f = MathHelper.sqrt(this.motX * this.motX * 0.20000000298023224D + this.motY * this.motY + this.motZ * this.motZ * 0.20000000298023224D) * 0.35F;
                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        this.makeSound(this.P(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                    }

                    this.a(blockposition, block);
                    block.a(this.world, blockposition, this);
                }
            }

            boolean flag2 = this.U();
            if (this.world.e(this.getBoundingBox().shrink(0.001D, 0.001D, 0.001D))) {
                this.burn(1.0F);
                if (!flag2) {
                    ++this.fireTicks;
                    if (this.fireTicks <= 0) {
                        EntityCombustEvent event = new EntityCombustEvent(this.getBukkitEntity(), 8);
                        this.world.getServer().getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            this.setOnFire(event.getDuration());
                        }
                    } else {
                        this.setOnFire(8);
                    }
                }
            } else if (this.fireTicks <= 0) {
                this.fireTicks = -this.maxFireTicks;
            }

            if (flag2 && this.fireTicks > 0) {
                this.makeSound("random.fizz", 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                this.fireTicks = -this.maxFireTicks;
            }

            this.world.methodProfiler.b();
        }
    }

    private void recalcPosition() {
        this.locX = (this.getBoundingBox().a + this.getBoundingBox().d) / 2.0D;
        this.locY = this.getBoundingBox().b;
        this.locZ = (this.getBoundingBox().c + this.getBoundingBox().f) / 2.0D;
    }
    public void t_() {
        try {
            if (primed++ > Config.max_tnt_per_tick) {
                return;
            } // Spigot
            PatchedPrimedTnt.this.lastX = PatchedPrimedTnt.this.locX;
            PatchedPrimedTnt.this.lastY = PatchedPrimedTnt.this.locY;
            PatchedPrimedTnt.this.lastZ = PatchedPrimedTnt.this.locZ;
            PatchedPrimedTnt.this.motY -= 0.03999999910593033D;
            PatchedPrimedTnt.this.move(PatchedPrimedTnt.this.motX, PatchedPrimedTnt.this.motY, PatchedPrimedTnt.this.motZ);
            PatchedPrimedTnt.this.motX *= 0.9800000190734863D;
            PatchedPrimedTnt.this.motY *= 0.9800000190734863D;
            PatchedPrimedTnt.this.motZ *= 0.9800000190734863D;
            if (PatchedPrimedTnt.this.onGround) {
                PatchedPrimedTnt.this.motX *= 0.699999988079071D;
                PatchedPrimedTnt.this.motZ *= 0.699999988079071D;
                PatchedPrimedTnt.this.motY *= -0.5D;
            }

            if (PatchedPrimedTnt.this.fuseTicks-- <= 0) {
                // CraftBukkit start - Need to reverse the order of the explosion and the entity death so we have a location for the event
                // PatchedPrimedTnt.this.die();
                if (!PatchedPrimedTnt.this.world.isClientSide) {
                    PatchedPrimedTnt.this.explode();
                }
                PatchedPrimedTnt.this.die();
                // CraftBukkit end
            } else {
                PatchedPrimedTnt.this.W();
                PatchedPrimedTnt.this.world.addParticle(EnumParticle.SMOKE_NORMAL, PatchedPrimedTnt.this.locX, PatchedPrimedTnt.this.locY + 0.5D, PatchedPrimedTnt.this.locZ, 0.0D, 0.0D, 0.0D, new int[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void explode() {
        // CraftBukkit start
        // float f = 4.0F;

        CraftServer server = this.world.getServer();

        ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) CraftEntity.getEntity(server, this));
        server.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            createExplosion(this, this.locX, this.locY + (double) (this.length / 2.0F), this.locZ, event.getRadius(), event.getFire(), true);
        }
        // CraftBukkit end
    }

    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Fuse", (byte) this.fuseTicks);
    }

    protected void a(NBTTagCompound nbttagcompound) {
        this.fuseTicks = nbttagcompound.getByte("Fuse");
    }

    public boolean W() {
        if (!Config.fix_cannons) {
            return super.W();
        } else {
            double oldMotX = this.motX;
            double oldMotY = this.motY;
            double oldMotZ = this.motZ;
            super.W();
            this.motX = oldMotX;
            this.motY = oldMotY;
            this.motZ = oldMotZ;
            if (this.inWater) {
                EntityTrackerEntry ete = ((WorldServer)this.getWorld()).getTracker().trackedEntities.get(this.getId());
                if (ete != null) {
                    PacketPlayOutEntityVelocity velocityPacket = new PacketPlayOutEntityVelocity(this);
                    PacketPlayOutEntityTeleport positionPacket = new PacketPlayOutEntityTeleport(this);
                    Iterator var10 = Collections.synchronizedCollection(ete.trackedPlayers).iterator();

                    while(var10.hasNext()) {
                        EntityPlayer viewer = (EntityPlayer)var10.next();
                        if ((viewer.locX - this.locX) * (viewer.locY - this.locY) * (viewer.locZ - this.locZ) < 256.0D) {
                            viewer.playerConnection.sendPacket(velocityPacket);
                            viewer.playerConnection.sendPacket(positionPacket);
                        }
                    }
                }
            }

            return this.inWater;
        }
    }

    public Explosion explode(Entity entity, double d0, double d1, double d2, float f, boolean flag) {
        return this.createExplosion(entity, d0, d1, d2, f, false, flag);
    }

    public Explosion createExplosion(Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        Explosion explosion = new Explosion(world, entity, d0, d1, d2, f, flag, flag1);
        explosion.a();
        explosion.a(true);
        return explosion;
    }
    public EntityLiving getSource() {
        return this.source;
    }

    public float getHeadHeight() {
        return 0.0F;
    }
}
