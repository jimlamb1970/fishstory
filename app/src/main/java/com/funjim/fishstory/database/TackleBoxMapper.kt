package com.funjim.fishstory.database

import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxWithLures

// Extension: Database Entity -> Domain Model
fun TackleBoxEntity.toDomain(): TackleBox {
    return TackleBox(
        id = id,
        fishermanId = fishermanId,
        name = name
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<TackleBoxEntity>.toTackleBoxDomainList(): List<TackleBox> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun TackleBox.toEntity(): TackleBoxEntity {
    return TackleBoxEntity(
        id = id,
        fishermanId = fishermanId,
        name = name
    )
}

fun TackleBoxEntityWithLures.toDomain(): TackleBoxWithLures {
    return TackleBoxWithLures(
        tackleBox = tackleBox.toDomain(),
        lures = lures.map { it.toDomain() }
    )
}
