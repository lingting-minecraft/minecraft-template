package live.lingting.minecraft.eunums

/**
 * @author lingting 2025/11/21 15:13
 */
enum class ClickBlockResult {

    /**
     * 不处理, 继续传递
     */
    PASS,

    /**
     * 阻止任何操作
     */
    REJECT,

    /**
     * 仅进行交互行为
     */
    INTERACT,

    /**
     * 攻击方块 - 触发以下方法. 仅在使用主手物品时返回才生效
     * @see net.minecraft.world.level.block.Block.attack
     */
    ATTACK,

    /**
     * 直接移除方块, 会走正常的破坏逻辑
     */
    REMOVE,

    ;

}