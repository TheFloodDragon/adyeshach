package ink.ptms.adyeshach.impl.nms

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftMeta
import ink.ptms.adyeshach.core.MinecraftPacketHandler
import org.bukkit.entity.Player
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.PacketSender
import taboolib.module.nms.sendBundlePacket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.nms.DefaultMinecraftPacketHandler
 *
 * @author 坏黑
 * @since 2022/6/28 00:11
 */
class DefaultMinecraftPacketHandler : MinecraftPacketHandler {

    val buffer = ConcurrentHashMap<Player, ConcurrentLinkedQueue<Any>>()
    val metaBuffer = ConcurrentHashMap<Player, ConcurrentLinkedQueue<BufferPacket>>()
    val operator by unsafeLazy { Adyeshach.api().getMinecraftAPI().getEntityOperator() }

    init {
        PacketSender.useMinecraftMethod()
    }

    override fun sendPacket(player: List<Player>, packet: Any) {
        player.forEach {
            buffer.getOrPut(it) { ConcurrentLinkedQueue() }.offer(packet)
        }
    }

    override fun bufferMetadataPacket(player: List<Player>, id: Int, packet: MinecraftMeta) {
        player.forEach {
            metaBuffer.getOrPut(it) { ConcurrentLinkedQueue() }.offer(BufferPacket(id, packet))
        }
    }

    override fun flush(player: List<Player>) {
        player.forEach { p ->
            // 处理普通数据包缓存
            buffer.remove(p)?.also { queue ->
                if (queue.isNotEmpty()) {
                    p.sendBundlePacket(queue.toList())
                }
            }
            // 处理元数据缓存
            metaBuffer.remove(p)?.also { queue ->
                if (queue.isNotEmpty()) {
                    // 在替换队列前处理当前队列内容
                    queue.groupBy { it.id }.forEach { (id, packets) ->
                        operator.updateEntityMetadata(p, id, packets.map { it.packet })
                    }
                }
            }
        }
    }

    /** 缓存数据包 */
    class BufferPacket(val id: Int, val packet: MinecraftMeta)
}