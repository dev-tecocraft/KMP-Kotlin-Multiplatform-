package navigation

import com.arkivanov.decompose.ComponentContext

/**
 * @Author: Jay Bakre
 * @Date: 18/12/23
 * @Time: 12:41 pm
 */
class ScreenBComponent(
    val text: String,
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit
) : ComponentContext by componentContext {
    fun onBackPress() {
        onBackPressed()
    }
}