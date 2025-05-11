package ink.ptms.adyeshach.impl.entity.controller;

import com.google.gson.annotations.Expose;
import ink.ptms.adyeshach.core.entity.EntityInstance;
import ink.ptms.adyeshach.core.entity.StandardTags;
import ink.ptms.adyeshach.core.entity.controller.Controller;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static ink.ptms.adyeshach.core.Adyeshach.*;

/**
 * 让 NPC 通过数据包看向 "所有玩家"
 */
public class ControllerLookAtPlayerWithPacket extends Controller {

    @Expose
    protected final double lookDistance;

    @Expose
    protected final boolean onlyHorizontal;

    public ControllerLookAtPlayerWithPacket(EntityInstance entity) {
        this(entity, 8f);
    }

    public ControllerLookAtPlayerWithPacket(EntityInstance entity, double lookDistance) {
        this(entity, lookDistance, false);
    }

    public ControllerLookAtPlayerWithPacket(EntityInstance entity, double lookDistance, boolean onlyHorizontal) {
        super(entity);
        this.lookDistance = lookDistance;
        this.onlyHorizontal = onlyHorizontal;
    }

    @NotNull
    @Override
    public String id() {
        return "LOOK_AT_PLAYER_WITH_PACKET";
    }

    @NotNull
    @Override
    public String group() {
        return "LOOK";
    }

    @Override
    public int priority() {
        return 8;
    }

    @Override
    public boolean shouldExecute() {
        EntityInstance entity = getEntity();
        if (entity == null) {
            return false;
        }
        // 检查是否有任何可见玩家在范围内
        for (Player player : entity.getVisiblePlayers()) {
            if (player.isValid() && player.getWorld().equals(entity.getWorld())) {
                double distance = player.getLocation().distanceSquared(entity.getLocation());
                if (distance <= lookDistance * lookDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean continueExecute() {
        EntityInstance entity = getEntity();
        if (entity == null) {
            return false;
        }
        return !entity.getVisiblePlayers().isEmpty();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        // 不需要清理任何状态
    }

    @Override
    public void tick() {
        EntityInstance entity = getEntity();
        if (entity == null || entity.hasTag(StandardTags.IS_MOVING, StandardTags.IS_MOVING_START, StandardTags.IS_PATHFINDING)) {
            return;
        }
        // 向所有可见玩家发送朝向更新
        for (Player player : entity.getVisiblePlayers()) {
            if (player.isValid() && player.getWorld().equals(entity.getWorld())) {
                double distance = player.getLocation().distanceSquared(entity.getLocation());
                if (distance <= lookDistance * lookDistance) {
                    Location entityLoc = entity.getEyeLocation();
                    Location targetLoc = player.getEyeLocation();

                    // 计算朝向向量
                    Vector direction = targetLoc.toVector().subtract(entityLoc.toVector());

                    // 计算 yaw 和 pitch
                    double x = direction.getX();
                    double y = direction.getY();
                    double z = direction.getZ();

                    // 计算 yaw (水平角度)
                    double yaw = Math.toDegrees(Math.atan2(-x, z));
                    // 计算 pitch (垂直角度)
                    double pitch = Math.toDegrees(Math.asin(-y / direction.length()));

                    // 如果只需要水平视角，将 pitch 设为 0
                    if (onlyHorizontal) {
                        pitch = 0;
                    }

                    // 发送朝向更新包
                    INSTANCE.api().getMinecraftAPI().getEntityOperator().updateEntityLook(
                            player,
                            entity.getIndex(),
                            (float) yaw,
                            (float) pitch,
                            true
                    );
                }
            }
        }
    }

    @Nullable
    @Override
    public String status() {
        return "Looking at all player";
    }

    @Override
    @NotNull
    public String toString() {
        return id() + ":" + String.format("%.2f", lookDistance) + "," + onlyHorizontal;
    }
} 