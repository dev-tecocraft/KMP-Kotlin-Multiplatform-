package navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

/**
 * @Author: Jay Bakre
 * @Date: 18/12/23
 * @Time: 12:41 pm
 */
class ScreenAComponent(
    componentContext: ComponentContext,
    val onNextButtonClick: (String) -> Unit
) : ComponentContext by componentContext {
    private var _userText = MutableValue("")
    val userText: Value<String> = _userText

    fun onEvent(screenAEvents: ScreenAEvents) {
        when (screenAEvents) {
            ScreenAEvents.ClickNextButton -> onNextButtonClick(userText.value)
            is ScreenAEvents.UpdatedUserText -> {
                _userText.value = screenAEvents.text
            }
        }
    }
}