package com.theone.busandbt.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.theone.busandbt.databinding.IncludeKeyboardBinding
import com.theone.busandbt.utils.OnClickNumberButtonListener

class CustomKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val editTextList = mutableListOf<EditText>()
    private val numbers =
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, null, null).shuffled().toMutableList()
    private var _binding: IncludeKeyboardBinding?
    private val binding: IncludeKeyboardBinding get() = _binding!!
    private val buttonList: List<TextView>

    init {
        _binding = IncludeKeyboardBinding.inflate(LayoutInflater.from(context), this, true)
        with(binding) {
            buttonList = listOf(
                button1,
                button2,
                button3,
                button4,
                button5,
                button6,
                button7,
                button8,
                button9,
                button10,
                button11,
                button12
            )
            updateNumberBoard()

            deleteButton.setOnClickListener {
                deleteNumber()
            }

            resetButton.setOnClickListener {
                rearrangeNumbers()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    fun setOnClickBackButton(op: OnClickListener?) {
        binding.deleteButton.setOnClickListener(op)
    }

    fun setOnClickNumberButton(op: OnClickNumberButtonListener?) {
        buttonList.forEach {
            it.setOnClickListener { unknown ->
                val button = unknown as? TextView ?: return@setOnClickListener
                val number = button.tag?.toString()?.toIntOrNull()
                op?.invoke(button, number)
            }
        }
    }

    fun associateEditTexts(vararg editTexts: EditText) {
        editTextList.clear()
        editTextList.addAll(editTexts)
        setOnClickNumberButton { _, number ->
            editTextList.firstOrNull { it.hasFocus() }?.let { et ->
                et.append("$number")
            }
        }
    }

    fun rearrangeNumbers() {
        numbers.shuffle()
        updateNumberBoard()
    }

    private fun deleteNumber() {
        editTextList.firstOrNull { it.hasFocus() }?.let { editText ->
            val currentText = editText.text.toString()
            if (currentText.isNotEmpty()) {
                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd
                val newText = StringBuilder(currentText).apply {
                    delete(selectionStart - 1, selectionEnd)
                }.toString()
                editText.setText(newText)
                editText.setSelection(selectionStart - 1)
            }
        }
    }

    private fun updateNumberBoard() {
        buttonList.forEachIndexed { index, button ->
            val number = numbers[index]
            button.text = number?.toString() ?: ""
            button.tag = number
        }
    }
}