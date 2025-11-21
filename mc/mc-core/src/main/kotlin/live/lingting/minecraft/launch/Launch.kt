package live.lingting.minecraft.launch

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isAbstract
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.minecraft.App
import live.lingting.minecraft.block.IBlock
import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.item.IItem
import live.lingting.minecraft.world.IWorld
import net.minecraft.data.DataProvider

/**
 * @author lingting 2025/11/15 14:33
 */
@Suppress("UNCHECKED_CAST")
abstract class Launch<I : Any, B : Any, BI : Any> {

    val log = logger()

    abstract val isClient: Boolean

    open val isServer: Boolean
        get() = !isClient

    open val packages: List<String> = listOf(javaClass.packageName.split(".").dropLast(2).joinToString("."))

    lateinit var baseItem: I
    lateinit var baseBlock: B

    abstract val baseItemId: String
    abstract val baseBlockId: String

    var registerItems = listOf<I>()
        private set
    var registerBlocks = listOf<B>()
        private set
    var registerBlockItems = listOf<BI>()
        private set
    var dataProviderClasses = mutableListOf<Class<out Any>>()
        private set

    protected open fun isIWorld(cls: Class<*>): Boolean {
        if (cls.isAbstract || cls.isInterface) return false
        return isSuper(cls, IWorld::class.java)
    }

    protected open fun isIItem(cls: Class<*>): Boolean {
        if (cls.isAbstract || cls.isInterface) return false
        return isSuper(cls, IItem::class.java)
    }

    protected open fun isIBlock(cls: Class<*>): Boolean {
        if (cls.isAbstract || cls.isInterface) return false
        return isSuper(cls, IBlock::class.java)
    }

    protected open fun isIBlockEntity(cls: Class<*>): Boolean {
        if (cls.isAbstract || cls.isInterface) return false
        return isSuper(cls, IBlockEntity::class.java)
    }

    protected open fun isDataProvider(cls: Class<*>): Boolean {
        if (cls.isAbstract || cls.isInterface) return false
        return isSuper(cls, DataProvider::class.java) || isSuper(cls, BasicDataProvider::class.java)
    }

    protected open fun onInitializer() {
        log.debug("[{}] onInitializer", App.modId)
        val loaders = ClassUtils.classLoaders(javaClass.classLoader)
        val classes = packages.flatMap { p ->
            ClassUtils.scan<Any>(p, { isIWorld(it) || isIBlockEntity(it) || isDataProvider(it) }, loaders)
        }.sortedBy { it.name.reversed() }

        log.debug("[{}] 扫描到待加载类数量: {}", App.modId, classes.size)
        val items = mutableListOf<I>()
        val blocks = mutableListOf<B>()
        val blockItems = mutableListOf<BI>()
        val blockEntityTypeMap = mutableMapOf<Class<out IBlock>, Class<out IBlockEntity>>()
        val blockEntityMap = mutableMapOf<Class<out IBlockEntity>, MutableList<B>>()
        val dataProviderClasses = mutableListOf<Class<out Any>>()

        classes.forEach { c ->
            if (isIBlockEntity(c)) {
                val cls = c as Class<IBlockEntity>
                val types = IBlockEntity.types(cls).filter { !it.isAbstract && !it.isInterface }
                types.forEach {
                    blockEntityTypeMap[it] = cls
                }
            }
            if (isDataProvider(c)) {
                dataProviderClasses.add(c)
            }
        }

        classes.forEach { c ->
            if (!isIWorld(c)) {
                return@forEach
            }
            val id = IWorld.id(c as Class<IWorld>, log)
            if (id.isNullOrBlank()) {
                return@forEach
            }

            log.debug("[{}] 类[{}]读取到id: {}", App.modId, c.name, id)
            if (isIItem(c)) {
                registerItem(id, c, items)
            }

            if (isIBlock(c)) {
                log.debug("[{}] 注册方块: {}", App.modId, id)
                val cls = c as Class<IBlock>
                val p = registerBlock(id, cls)
                if (p != null) {
                    val (block, item) = p
                    blocks.add(block)
                    blockItems.add(item)
                    if (id == baseBlockId && !::baseBlock.isInitialized) {
                        baseBlock = block
                    }
                    val clz = blockEntityTypeMap[cls]
                    if (clz != null) {
                        blockEntityMap.computeIfAbsent(clz) { mutableListOf() }
                            .add(block)
                    }
                }
            }
        }
        registerItems = items
        registerBlocks = blocks
        registerBlockItems = blockItems
        this.dataProviderClasses = dataProviderClasses
        registerBlockEntityMapping(blockEntityTypeMap)
        registerBlockEntity(blockEntityMap)
    }

    private fun registerItem(id: String, cls: Class<IWorld>, items: MutableList<I>) {
        log.debug("[{}] 注册物品: {}", App.modId, id)
        if (!id.startsWith(IItem.PREFIX)) {
            throw IllegalArgumentException("物品id必须以[${IItem.PREFIX}]作为前缀")
        }
        val item = registerItem(id, cls as Class<IItem>)
        if (item != null) {
            items.add(item)
            if (id == baseItemId && !::baseItem.isInitialized) {
                baseItem = item
            }
        }
    }

    protected abstract fun registerItem(id: String, c: Class<out IItem>): I?

    protected abstract fun registerBlock(id: String, c: Class<out IBlock>): Pair<B, BI>?

    protected abstract fun registerBlockEntityMapping(map: Map<Class<out IBlock>, Class<out IBlockEntity>>)

    protected abstract fun registerBlockEntity(map: Map<Class<out IBlockEntity>, List<B>>)

}