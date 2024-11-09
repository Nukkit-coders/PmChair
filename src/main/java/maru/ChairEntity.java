package maru;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityMinecartEmpty;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.ArrayList;

public class ChairEntity extends Entity implements EntityRideable {

    public ChairEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true, false);
    }

    @Override
    public int getNetworkId() {
        return EntityMinecartEmpty.NETWORK_ID;
    }

    @Override
    public boolean onUpdate(int tick) {
        return false;
    }

    @Override
    public boolean canSaveToStorage() {
        return false;
    }

    @Override
    public boolean dismountEntity(Entity entity, boolean sendLinks) {
        if (super.dismountEntity(entity, sendLinks)) {
            this.close();
            return true;
        }
        return false;
    }

    @Override
    public Vector3f getMountedOffset(Entity entity) {
        return new Vector3f(0.0f, 1.5f, 0.0f);
    }

    @Override
    public void close() {
        super.close();
        if (!this.passengers.isEmpty()) {
            for (Entity passenger : new ArrayList<>(this.passengers)) {
                this.dismountEntity(passenger);
            }
        }
    }
}
