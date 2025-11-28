package live.lingting.minecraft.launch

import com.mojang.serialization.MapCodec
import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.ClassUtils.isAbstract
import live.lingting.framework.util.ClassUtils.isSuper
import live.lingting.framework.util.Slf4jUtils.logger
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.App
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.block.BlockSource
import live.lingting.minecraft.block.IBlockEntity
import live.lingting.minecraft.command.BasicCommand
import live.lingting.minecraft.data.BasicDataProvider
import live.lingting.minecraft.data.RegisterData
import live.lingting.minecraft.item.ItemSource
import live.lingting.minecraft.loot.BasicNumberProvider
import live.lingting.minecraft.world.IWorld
import net.minecraft.data.DataProvider
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import java.util.function.Supplier

/**
 * @author lingting 2025/11/15 14:33
 */
@Suppress("UNCHECKED_CAST")
abstract class Launch<I : Supplier<Item>, B : Supplier<Block>, BI : Any> {

    val log = logger()

    abstract val isClient: Boolean

    open val isServer: Boolean
        get() = !isClient

    open val packages: List<String> = listOf(javaClass.packageName.split(".").dropLast(2).joinToString("."))

    var registerItems = listOf<I>()
        private set
    var registerBlocks = listOf<B>()
        private set
    var registerBlockItems = listOf<BI>()
        private set
    var dataProviderClasses = listOf<Class<out Any>>()
        private set
    var commandClasses = listOf<Class<out BasicCommand>>()
        private set

    protected val registerDataValue = WaitValue.of<RegisterData>()

    val registerData: RegisterData
        get() = registerDataValue.notNull()

    protected open fun isWorld(cls: Class<*>): Boolean {
        return isSuper(cls, IWorld::class.java)
    }

    protected open fun isItem(cls: Class<*>): Boolean {
        return isSuper(cls, ItemSource::class.java)
    }

    protected open fun isBlock(cls: Class<*>): Boolean {
        return isSuper(cls, BlockSource::class.java)
    }

    protected open fun isBlockEntity(cls: Class<*>): Boolean {
        return isSuper(cls, IBlockEntity::class.java)
    }

    protected open fun isDataProvider(cls: Class<*>): Boolean {
        return isSuper(cls, DataProvider::class.java) ||
                isSuper(cls, BasicDataProvider::class.java) ||
                isSuper(cls, BasicNumberProvider::class.java)
    }

    protected open fun isNumberProvider(cls: Class<*>): Boolean {
        return isSuper(cls, BasicNumberProvider::class.java)
    }

    protected open fun isCommand(cls: Class<*>) = isSuper(cls, BasicCommand::class.java)

    protected open fun onInitializer() {
        log.debug("[{}] onInitializer", modId)
        val loaders = ClassUtils.classLoaders(javaClass.classLoader)
        val classes = packages.flatMap { p ->
            ClassUtils.scan<Any>(p, {
                if (it.isAbstract || it.isInterface) {
                    false
                } else if (it.packageName.startsWith(javaClass.packageName)) {
                    false
                } else {
                    isWorld(it) ||
                            isBlockEntity(it) ||
                            isDataProvider(it) ||
                            isCommand(it) ||
                            isNumberProvider(it)
                }
            }, loaders)
        }.sortedBy { it.name.reversed() }

        log.debug("[{}] 扫描到待加载类数量: {}", modId, classes.size)
        val items = mutableListOf<I>()
        val blocks = mutableListOf<B>()
        val blockItems = mutableListOf<BI>()
        val blockEntityTypeMap = mutableMapOf<Class<out BlockSource>, Class<out IBlockEntity>>()
        val blockEntityMap = mutableMapOf<Class<out IBlockEntity>, MutableList<B>>()
        val dataProviderClasses = mutableListOf<Class<out Any>>()
        val commandClasses = mutableListOf<Class<out BasicCommand>>()

        classes.forEach { c ->
            if (isBlockEntity(c)) {
                val cls = c as Class<IBlockEntity>
                val types = IBlockEntity.types(cls).filter { !it.isAbstract && !it.isInterface }
                types.forEach {
                    blockEntityTypeMap[it] = cls
                }
            }
            if (isDataProvider(c)) {
                dataProviderClasses.add(c)
            }
            if (isCommand(c)) {
                commandClasses.add(c as Class<out BasicCommand>)
            }
            if (isNumberProvider(c)) {
                val cls = c as Class<out NumberProvider>
                val name = BasicNumberProvider.name(cls)
                val codec = BasicNumberProvider.codes(cls)
                log.debug("[{}] 注册战利品数据类型: {}", modId, name)
                val supplier = registerNumberProvider(name, codec)
                BasicNumberProvider.upsert(cls, supplier)
            }
        }

        classes.forEach { c ->
            if (!isWorld(c)) {
                return@forEach
            }
            val id = IWorld.id(c as Class<IWorld>, log)
            if (id.isNullOrBlank()) {
                return@forEach
            }

            log.debug("[{}] 类[{}]读取到id: {}", modId, c.name, id)
            if (isItem(c)) {
                log.debug("[{}] 注册物品: {}", modId, id)
                val p = registerItem(id, c as Class<ItemSource>)
                if (p != null) {
                    items.add(p)
                }
            }

            if (isBlock(c)) {
                log.debug("[{}] 注册方块: {}", modId, id)
                val cls = c as Class<BlockSource>
                val p = registerBlock(id, cls)
                if (p != null) {
                    val (block, item) = p
                    blocks.add(block)
                    blockItems.add(item)
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
        val data = RegisterData.from(registerItems, registerBlocks)
        registerDataValue.update(data)
        App.registerData = data
        this.dataProviderClasses = dataProviderClasses
        this.commandClasses = commandClasses
        registerBlockEntityMapping(blockEntityTypeMap)
        registerBlockEntity(blockEntityMap)
    }

    protected abstract fun registerItem(id: String, c: Class<out ItemSource>): I?

    protected abstract fun registerBlock(id: String, c: Class<out BlockSource>): Pair<B, BI>?

    protected abstract fun registerBlockEntityMapping(map: Map<Class<out BlockSource>, Class<out IBlockEntity>>)

    protected abstract fun registerBlockEntity(map: Map<Class<out IBlockEntity>, List<B>>)

    protected abstract fun registerNumberProvider(
        name: String,
        codec: MapCodec<out NumberProvider>
    ): Supplier<LootNumberProviderType>

}