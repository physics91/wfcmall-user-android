package com.theone.busandbt.extension

import com.theone.busandbt.dto.cost.DeliveryCost
import com.theone.busandbt.dto.menu.MenuGroup
import com.theone.busandbt.dto.order.OrderMenuOption
import com.theone.busandbt.instance.EMPTY_MENU_IMAGE_RESOURCES
import com.theone.busandbt.item.basket.BasketMenu
import com.theone.busandbt.item.basket.BasketMenuOption

val List<*>.querySetParam: String
    get() {
        val str = toString()
        return str.substring(1 until str.lastIndex).replace(" ", "").replace("\"","\'")
    }

fun List<BasketMenu>.calculateMenuCost(): Int = sumOf { menu ->
    if (menu.isSelected) menu.count * (menu.saleCost + menu.optionList.sumOf { option -> option.cost }) else 0
}

@JvmName("a")
fun List<BasketMenuOption>.calculateOptionCost(): Int = sumOf { it.cost }
fun List<OrderMenuOption>.calculateOptionCost(): Int = sumOf { it.cost }

val List<BasketMenu>.simpleDescription: String
    get() = buildString {
        if (this@simpleDescription.isEmpty()) return@buildString
        append(this@simpleDescription.first().name)
        val s = this@simpleDescription.size
        if (s > 1) append(" 외 ${s - 1}건")
    }

fun List<DeliveryCost>.findDeliveryCost(menuCost: Int): Int {
    var deliveryCost = 0
    kotlin.run {
        val testList = sortedByDescending { it.testOrderCost }
        testList.forEachIndexed { index, it ->
            if (index == 0) {
                if (menuCost >= it.testOrderCost) {
                    deliveryCost = it.cost
                    return@run
                }
            } else {
                val prev = testList.getOrNull(index - 1) ?: return@forEachIndexed
                if (menuCost in it.testOrderCost..prev.testOrderCost) {
                    deliveryCost = it.cost
                    return@run
                }
            }
        }
    }
    return deliveryCost
}


fun List<MenuGroup>.getEmptyMenuImageResourceMap(): Map<Int, Map<Int, Int>> {
    val result = HashMap<Int, Map<Int, Int>>()
    var imagePosition = 0
    forEach { mg ->
        val index = indexOf(mg)
        val innerResult = HashMap<Int, Int>()
        mg.menuList.forEachIndexed { i, menu ->
            if (menu.imageUrl.isNotEmpty()) return@forEachIndexed
            innerResult[i] = imagePosition
            if (imagePosition >= EMPTY_MENU_IMAGE_RESOURCES.size - 1) imagePosition =
                0 else imagePosition++
        }
        result[index] = innerResult
    }
    return result
}

/**
 * 리스트가 연속적인지 판단한다.
 * @return true인 경우 연속적인 리스트라는 뜻이다.
 */
fun List<Int>.isConsecutive(): Boolean {
    if (isEmpty() || size == 1) return true

    val isAscending = this[0] < this[1]

    for (i in 0 until size - 1) {
        if (isAscending && this[i] + 1 != this[i + 1]) {
            return false
        } else if (!isAscending && this[i] - 1 != this[i + 1]) {
            return false
        }
    }
    return true
}