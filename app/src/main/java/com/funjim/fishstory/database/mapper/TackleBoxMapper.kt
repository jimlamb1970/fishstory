package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.TackleBoxEntity
import com.funjim.fishstory.database.entity.TackleBoxEntityWithLures
import com.funjim.fishstory.database.entity.TackleBoxLureEntity
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLure
import com.funjim.fishstory.model.TackleBoxWithLures

fun TackleBoxEntity.toDomain(): TackleBox {
    return TackleBox(
        id = id,
        fishermanId = fishermanId,
        name = name
    )
}
fun List<TackleBoxEntity>.toTackleBoxDomainList(): List<TackleBox> {
    return map { it.toDomain() }
}
fun TackleBox.toEntity(): TackleBoxEntity {
    return TackleBoxEntity(
        id = id,
        fishermanId = fishermanId,
        name = name
    )
}

fun TackleBoxLureEntity.toDomain(): TackleBoxLure {
    return TackleBoxLure(
        tackleBoxId = tackleBoxId,
        lureId = lureId
    )
}
fun List<TackleBoxLureEntity>.toTackleBoxLureDomainList(): List<TackleBoxLure> {
    return map { it.toDomain() }
}
fun TackleBoxLure.toEntity(): TackleBoxLureEntity {
    return TackleBoxLureEntity(
        tackleBoxId = tackleBoxId,
        lureId = lureId
    )
}

fun TackleBoxEntityWithLures.toDomain(): TackleBoxWithLures {
    return TackleBoxWithLures(
        tackleBox = tackleBox.toDomain(),
        lures = lures.map { it.toDomain() }
    )
}
