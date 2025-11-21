package live.lingting.minecraft.component.kt

import live.lingting.framework.util.ClassUtils
import kotlin.reflect.KClass

/**
 * @author lingting 2025/11/21 15:36
 */

fun <T : Any> T?.isSuper(superClass: KClass<*>) = isSuper(superClass.java)

fun <T : Any> T?.isSuper(superClass: Class<*>) = ClassUtils.isSuper(this, superClass)
