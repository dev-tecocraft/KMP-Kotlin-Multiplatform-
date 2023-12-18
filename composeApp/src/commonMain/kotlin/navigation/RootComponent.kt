@file:OptIn(ExperimentalDecomposeApi::class)

package navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.serialization.Serializable

/**
 * @Author: Jay Bakre
 * @Date: 18/12/23
 * @Time: 12:31 pm
 */
class RootComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<ScreenConfiguration>()
    val screenStack = childStack(
        source = navigation,
        serializer = ScreenConfiguration.serializer(),
        initialConfiguration = ScreenConfiguration.ScreenA,
        handleBackButton = true,
        childFactory = ::createScreens
    )

    private fun createScreens(
        configuration: ScreenConfiguration,
        context: ComponentContext
    ): Screens {
        return when (configuration) {
            ScreenConfiguration.ScreenA -> Screens.ScreenA(
                ScreenAComponent(context) { userText ->
                    navigation.pushNew(
                        ScreenConfiguration.ScreenB(userText)
                    )
                }
            )

            is ScreenConfiguration.ScreenB -> Screens.ScreenB(
                ScreenBComponent(
                    text = configuration.text,
                    componentContext = context,
                    onBackPressed = {
                        navigation.pop()
                    }
                )
            )
        }
    }

    @Serializable
    sealed class ScreenConfiguration {
        @Serializable
        data object ScreenA : ScreenConfiguration()

        @Serializable
        data class ScreenB(val text: String) : ScreenConfiguration()
    }

    sealed class Screens {
        data class ScreenA(val screenAComponent: ScreenAComponent) : Screens()
        data class ScreenB(val screenBComponent: ScreenBComponent) : Screens()
    }
}