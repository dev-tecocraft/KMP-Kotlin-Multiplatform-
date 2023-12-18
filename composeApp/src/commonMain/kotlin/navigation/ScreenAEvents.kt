package navigation

/**
 * @Author: Jay Bakre
 * @Date: 18/12/23
 * @Time: 12:56 pm
 */
sealed interface ScreenAEvents {
    data object ClickNextButton : ScreenAEvents
    data class UpdatedUserText(val text: String): ScreenAEvents
}