package cc.funkemunky.patcher.objects;

import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;

// $FF: synthetic class
public class PatchedPrimedTnt extends Entity {
    public PatchedPrimedTnt(Location loc, World world) {
        super(world);
        this.yield = 4.0F;
        this.isIncendiary = false;
        this.sourceLoc = loc;
        this.k = true;
        this.setSize(0.98F, 0.98F);
        this.loadChunks = world.paperSpigotConfig.loadUnloadedTNTEntities;
        this.multiplier = 1;
    }

    public PatchedPrimedTnt(Location loc, World world, double d0, double d1, double d2, EntityLiving entityliving) {
        this(loc, world);
        this.setPosition(d0, d1, d2);
        float f = (float)(Math.random() * 3.1415927410125732D * 2.0D);
        this.motX = (double)(-((float)Math.sin((double)f)) * 0.02F);
        this.motY = 0.20000000298023224D;
        this.motZ = (double)(-((float)Math.cos((double)f)) * 0.02F);
        this.fuseTicks = 80;
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
        this.source = entityliving;
        if (world.paperSpigotConfig.fixCannons) {
            this.motX = this.motZ = 0.0D;
        }

        this.multiplier = 1;
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
        if (this.world.spigotConfig.currentPrimedTnt++ <= this.world.spigotConfig.maxTntTicksPerTick) {
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            this.motY -= 0.03999999910593033D;
            this.move(this.motX, this.motY, this.motZ);
            this.motX *= 0.9800000190734863D;
            this.motY *= 0.9800000190734863D;
            this.motZ *= 0.9800000190734863D;
            if (this.onGround) {
                this.motX *= 0.699999988079071D;
                this.motZ *= 0.699999988079071D;
                this.motY *= -0.5D;
            }

            if (this.fuseTicks-- <= 0) {
                if (!this.world.isClientSide) {
                    this.explode();
                }

                this.die();
            } else {
                this.W();
                this.world.addParticle(EnumParticle.SMOKE_NORMAL, this.locX, this.locY + 0.5D, this.locZ, 0.0D, 0.0D, 0.0D, new int[0]);
            }

        }
    }

    private void explode() {
        CraftServer server = this.world.getServer();
        ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive) CraftEntity.getEntity(server, this));
        server.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.world.createExplosion(this, this.locX, this.locY + (double)(this.length / 2.0F), this.locZ, event.getRadius(), event.getFire(), true);
        }

    }

    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Fuse", (byte)this.fuseTicks);
    }

    protected void a(NBTTagCompound nbttagcompound) {
        this.fuseTicks = nbttagcompound.getByte("Fuse");
    }

    public EntityLiving getSource() {
        return this.source;
    }

    public float getHeadHeight() {
        return 0.0F;
    }
}
