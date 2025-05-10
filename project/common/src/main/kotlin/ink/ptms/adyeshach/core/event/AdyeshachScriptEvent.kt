package ink.ptms.adyeshach.core.event

import ink.ptms.adyeshach.core.entity.EntityInstance
import taboolib.platform.type.BukkitProxyEvent

/**
 * 脚本控制相关事件
 * 父类不会作为事件触发
 */
sealed class AdyeshachScriptEvent(val entity: List<EntityInstance>): BukkitProxyEvent() {

    /**
     * 看向坐标
     * 可以取消来阻止脚本的执行
     */
    class Look(entity: List<EntityInstance>, val smooth: Boolean, val x: Double?, val y: Double?, val z: Double?) : AdyeshachScriptEvent(entity)
}