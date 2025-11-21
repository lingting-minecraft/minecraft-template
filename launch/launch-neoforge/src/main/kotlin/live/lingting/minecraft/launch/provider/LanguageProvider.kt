package live.lingting.minecraft.launch.provider

import live.lingting.framework.util.ClassUtils
import live.lingting.framework.util.FieldUtils.isFinal
import live.lingting.framework.util.FieldUtils.isPublic
import live.lingting.framework.util.FieldUtils.isStatic
import live.lingting.minecraft.App.modId
import live.lingting.minecraft.i18n.I18n
import live.lingting.minecraft.i18n.I18nLocale
import live.lingting.minecraft.world.IWorld
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

/**
 * @author lingting 2025/9/16 17:20
 */
abstract class LanguageProvider(
    output: PackOutput,
    locale: String,
    val source: Map<String, String>
) : net.neoforged.neoforge.common.data.LanguageProvider(output, modId, locale) {

    companion object {

        @JvmStatic
        fun I18nLocale.translatable(): MutableComponent {
            return Component.translatable(key)
        }

        fun register(e: GatherDataEvent, items: List<DeferredItem<Item>>, blocks: List<DeferredBlock<Block>>) {
            // {locale: {key:value}}
            val map = mutableMapOf<String, MutableMap<String, String>>()
            // 填充国际化key
            fillI18n(I18n, map)
            // 为物品和方块注册默认的名字
            fillItem(map, items)
            fillBlock(map, blocks)
            map.forEach { (k, m) ->
                when (k) {
                    "zh" -> {
                        register(e, "zh_cn", m)
                        register(e, "zh_tw", m)
                    }

                    "en" -> {
                        register(e, "en_us", m)
                    }

                    else -> {
                        register(e, k, m)
                    }
                }
            }
        }

        fun fillI18n(obj: Any, map: MutableMap<String, MutableMap<String, String>>) {
            if (obj is I18nLocale) {
                obj.source.forEach { (l, v) ->
                    val absent = map.computeIfAbsent(l) { mutableMapOf() }
                    absent.put(obj.key, v)
                }
                return
            }

            val fields = ClassUtils.fields(obj.javaClass)
            for (field in fields) {
                if (!field.isPublic || !field.isStatic || !field.isFinal
                    || field.type == obj.javaClass
                ) {
                    continue
                }

                val any = field.get(obj)
                if (any != null) {
                    fillI18n(any, map)
                }
            }
        }

        fun fillItem(map: MutableMap<String, MutableMap<String, String>>, items: List<DeferredItem<Item>>) {
            fillWorld("item", map, items.map { it.get() as IWorld })
        }

        fun fillBlock(map: MutableMap<String, MutableMap<String, String>>, blocks: List<DeferredBlock<Block>>) {
            fillWorld("block", map, blocks.map { it.get() as IWorld })
        }

        private fun fillWorld(prefix: String, map: MutableMap<String, MutableMap<String, String>>, list: List<IWorld>) {
            map.values.forEach { m ->
                list.forEach {
                    val key = "$prefix.$modId.${it.id}"
                    val descKey = "$key.desc"

                    val name = m[it.i18nNameKey()]
                    val desc = m[it.i18nDescKey()]

                    if (!name.isNullOrBlank()) {
                        m[key] = name
                    }
                    if (!desc.isNullOrBlank()) {
                        m[descKey] = desc
                    }
                }

            }
        }

        fun register(e: GatherDataEvent, locale: String, source: Map<String, String>) {
            val output = e.generator.packOutput
            val provider = object : LanguageProvider(output, locale, source) {}
            e.generator.addProvider(true, provider)
        }

    }

    override fun addTranslations() {
        source.forEach { (k, v) -> add(k, v) }
    }
}