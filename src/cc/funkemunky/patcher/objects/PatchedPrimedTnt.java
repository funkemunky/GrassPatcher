package cc.funkemunky.patcher.objects;

import cc.funkemunky.patcher.GrassPatcher;
import cc.funkemunky.patcher.utils.Config;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Explosion;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Iterator;

public class PatchedPrimedTnt extends EntityTNTPrimed {

    public int fuseTicks;
    public float yield = 4; // CraftBukkit - add field
    public boolean isIncendiary = false; // CraftBukkit - add field
    private EntityLiving source;
    private int primed = 0;

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

    public void t_() {
        GrassPatcher.INSTANCE.getExecutor().submit(() -> {
            try {

            } catch (Exception e) {
                new BukkitRunnable() {
                    public void run() {
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
                    }
                }.runTask(GrassPatcher.INSTANCE);
            }
        });
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

    private Explosion createExplosion(Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
        Explosion explosion = new Explosion(world, entity, d0, d1, d2, f, flag, flag1);
        explosion.a();
        explosion.a(true);
        return explosion;
    }

    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Fuse", (byte) this.fuseTicks);
    }

    protected void a(NBTTagCompound nbttagcompound) {
        this.fuseTicks = nbttagcompound.getByte("Fuse");
    }

    public boolean W() {
        GrassPatcher.INSTANCE.getExecutor().submit(() -> {
            if (!Config.fixcannons) {
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
        });
        if (!Config.fixcannons) {
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

    public EntityLiving getSource() {
        return this.source;
    }

    public float getHeadHeight() {
        return 0.0F;
    }
}
