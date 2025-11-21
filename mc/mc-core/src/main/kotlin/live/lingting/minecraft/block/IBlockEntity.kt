package live.lingting.minecraft.block

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.framework.util.ClassUtils.newInstance
import live.lingting.framework.util.CollectionUtils
import live.lingting.framework.util.FieldUtils.isStatic
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.App
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * @author lingting 2025/11/6 19:25
 */
@Suppress("UNCHECKED_CAST")
abstract class IBlockEntity : BlockEntity {

    companion object {

        @JvmStatic
        fun types(cls: Class<IBlockEntity>, log: Logger = logger(cls)): List<Class<out IBlock>> {
            val field = ClassUtils.classField(cls, "TYPES")
            if (field == null || field.field == null || field.field?.isStatic != true) {
                log.debug("[{}] 类[{}]无法加载, 未获取到TYPES字段!", App.modId, cls.name)
                return emptyList()
            }
            val any = field.field?.get(null)
            val list = CollectionUtils.multiToList(any)

            return list.mapNotNull {
                if (it == null) {
                    null
                } else if (isSuper(it.javaClass, Class::class.java)) {
                    it as Class<*>
                } else if (isSuper(it.javaClass, KClass::class.java)) {
                    (it as KClass<*>).java
                } else {
                    null
                }
            }.filter {
                isSuper(it, IBlock::class.java)
            }.map {
                it as Class<out IBlock>
            }
        }

        /**
         * 方块实体持有缓存, key: 方块实体名称, value: 方块实体持有者
         */
        @JvmField
        val CACHE = ConcurrentHashMap<String, Holder<*>>()

        /**
         * 方块和方块实体类映射, key: 方块全类名, value: 方块实体名称
         */
        @JvmField
        val MAPPING = ConcurrentHashMap<String, String>()

        @JvmStatic
        fun entityName(cls: Class<out IBlockEntity>) = cls.name.lowercase().replace(".", "_")

        @JvmStatic
        fun registerMapping(map: Map<Class<out IBlock>, Class<out IBlockEntity>>) {
            map.forEach { (b, e) ->
                registerMapping(b, e)
            }
        }

        @JvmStatic
        fun registerMapping(b: Class<out IBlock>, e: Class<out IBlockEntity>) {
            MAPPING[b.name] = entityName(e)
        }

        @JvmStatic
        fun register(h: Holder<*>) {
            CACHE[h.name] = h
        }

        @JvmStatic
        fun register(e: Class<out IBlockEntity>, h: Holder<*>) {
            val name = entityName(e)
            CACHE[name] = h
        }

        @JvmStatic
        fun findByBlock(cls: Class<out IBlock>): Holder<*>? {
            val name = MAPPING[cls.name]
            if (name.isNullOrBlank()) {
                return null
            }
            return CACHE[name]
        }

        @JvmStatic
        fun find(cls: Class<out IBlockEntity>): Holder<*>? {
            val name = entityName(cls)
            return CACHE[name]
        }
    }

    protected val log = logger()

    constructor(cls: Class<out IBlockEntity>, pos: BlockPos, blockState: BlockState) :
            super(find(cls)!!.type, pos, blockState)

    abstract class Holder<E : IBlockEntity>(val entityClass: Class<E>) {

        val name = entityName(entityClass)

        abstract val type: BlockEntityType<*>

        abstract fun createType(key: ResourceLocation, supplier: Supplier<Collection<Block>>): BlockEntityType<E>

        fun create(pos: BlockPos, state: BlockState): E {
            return newInstance(entityClass, false) {
                if (isSuper(pos.javaClass, it)) {
                    pos
                } else if (isSuper(state.javaClass, it)) {
                    state
                } else if (isSuper(Class::class.java, it)) {
                    entityClass
                } else {
                    null
                }
            }
        }

    }

    override fun saveAdditional(tag: CompoundTag, provider: HolderLookup.Provider) {
        super.saveAdditional(tag, provider)
    }

    override fun loadAdditional(tag: CompoundTag, provider: HolderLookup.Provider) {
        super.loadAdditional(tag, provider)
    }

}