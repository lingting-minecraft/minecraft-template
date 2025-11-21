package live.lingting.minecraft.launch.basic

import live.lingting.minecraft.block.IBlockEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Function
import java.util.function.Supplier

/**
 * @author lingting 2025/11/15 15:40
 */
@Suppress("UNCHECKED_CAST")
class NBlockEntityHolder<E : IBlockEntity>(
    entityClass: Class<E>,
    register: DeferredRegister<BlockEntityType<*>>,
    supplier: Supplier<Collection<Block>>
) : IBlockEntity.Holder<E>(entityClass) {

    private val supplier = register.register(name, Function { createType(it, supplier) })

    override val type: BlockEntityType<*>
        get() = supplier.get()

    override fun createType(
        key: ResourceLocation,
        supplier: Supplier<Collection<Block>>
    ): BlockEntityType<E> {
        val blocks = supplier.get().toSet()
        val factory = BlockEntityType.BlockEntitySupplier<E> { pos, state -> create(pos, state) }
        return BlockEntityType::class.java.constructors[0].newInstance(
            factory, blocks, null
        ) as BlockEntityType<E>
    }

}