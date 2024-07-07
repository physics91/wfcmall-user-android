package com.theone.busandbt.item.shop

import com.theone.busandbt.item.TitleDesc
import com.theone.busandbt.type.OperViewType

data class OperViewMetadata(
    val type: OperViewType,
    override val desc: String
) : TitleDesc(type.title, desc)
