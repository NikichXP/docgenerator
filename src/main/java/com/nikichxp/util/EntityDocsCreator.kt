package com.nikichxp.util

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

object EntityDocsCreator {
	
	fun test(names: Set<String>, prevSize: Int = 0): Set<EntityInfo> {
		val ret = File(System.getProperty("user.dir") + "/src/main/java/com/clearcash/entity").listFiles()
			.flatMap { return@flatMap if (it.isFile) listOf(it) else it.listFiles().toList() }
			.map {
				var path = it.absolutePath.substringAfter("java").substring(1)
				path = path.replace('/', '.').replace('\\', '.')
				path.substringBeforeLast('.')
			}
			.filter {
				try {
					Class.forName(it).kotlin
					true
				} catch (e: Exception) {
					false
				}
			}
			.map { Class.forName(it).kotlin }
			.filter {
				names.any { name -> name.toLowerCase() == it.simpleName?.toLowerCase() }
			}
			.filter {
				!it.javaObjectType.isEnum
			}
			.map {
				//				it.simpleName?.print()
				val info = EntityInfo(it.simpleName!!)
				it.memberProperties
					.filter {
						it.javaField?.let {
							!it.annotations.any { it is JsonIgnore }
						} ?: true
					}
					.forEach { prop ->
						info.params[prop.name] = prop.javaGetter?.returnType?.simpleName ?: "null"
					}
				return@map info
			}.toSet()
		return if (ret.size > prevSize) {
			val newSet = ret.flatMap { it.params.values }.toMutableSet()
			newSet.addAll(names)
			test(newSet, newSet.size)
		} else {
			ret
		}
	}
	
	class EntityInfo(val name: String) {
		var params = mutableMapOf<String, String>()
		
		val paramList: List<Pair<String, String>>
			get() = params.map { it.key to it.value }
	}
	
}