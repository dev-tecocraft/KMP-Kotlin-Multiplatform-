import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import navigation.RootComponent
import screens.ScreenA
import screens.ScreenB

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        val childStack by rootComponent.screenStack.subscribeAsState()
        Children(
            stack = childStack,
            animation = stackAnimation(slide())
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Screens.ScreenA -> ScreenA(instance.screenAComponent)
                is RootComponent.Screens.ScreenB -> ScreenB(instance.screenBComponent)
            }
        }
    }
}