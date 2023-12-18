This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

## Below is the steps to create Kotlin multiplatform project and added screen navigation using decompose
* Generate Project using [Kotlin multiplatform wizard]( https://kmp.jetbrains.com/)
* Update and add below files to add support for navigation using compose.
   - **libs.version.toml**
   ```
    [versions]
    ...
    decompose = "2.2.1"
    kotlinx-serialization-json = "1.6.1"
    extensions-compose-jetbrains = "2.1.4-compose-experimental"
    
    [libraries]
    ...
    decompose = {module = "com.arkivanov.decompose:decompose", version.ref = "decompose"}
    kotlinx-serialization = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization-json" }
    decompose-jetbrains = { module = "com.arkivanov.decompose:extensions-compose-jetbrains", version.ref = "extensions-compose-jetbrains" }
    ```
    - **build.gradle.kts(:composeApp)**
    
    ```
    plugins {
       alias(libs.plugins.kotlinMultiplatform)
       alias(libs.plugins.androidApplication)
       alias(libs.plugins.jetbrainsCompose)
       alias(libs.plugins.kotlinSerialization)
    }


    sourceSets {
        androidMain.dependencies {
            ...
           implementation(libs.decompose)
        }
        commonMain.dependencies {
    	    ...
           implementation(libs.decompose)
           implementation(libs.decompose.jetbrains)
           implementation(libs.kotlinx.serialization)
        }
    }
    ```
* Prefer below official link of decompose as reference. [**Decompose**](https://arkivanov.github.io/Decompose/getting-started/quick-start/) 
* **Steps to add Navigation logic in KMP project:**
    - Add a package in commonMain named **navigation**.
    - Create kotlin class named **RootComponent**
        - This class contains all the compose components which will be shared with android and iOS, and compose components can be a small UI part or whole screen. 
        - **RootComponent** class contains screen navigation stack, screen configuration and list of screens
    ```
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
    ```
    - Create screen wise components which are responsible for events and lifecycle for particular composable and screen.
    ```
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
    ```
    - Add a package in commonMain named **screens**, which contains actual composable components and screens.
    ```
    @Composable
    fun ScreenA(component: ScreenAComponent) {
       val userText by component.userText.subscribeAsState()
       Column(
           modifier = Modifier.fillMaxSize(),
           verticalArrangement = Arrangement.Center,
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
           Text("Screen A")
           OutlinedTextField(
               userText,
               onValueChange = { component.onEvent(ScreenAEvents.UpdatedUserText(it)) },
               modifier = Modifier.fillMaxWidth().padding(16.dp)
           )
           Button(
               onClick = { component.onEvent(ScreenAEvents.ClickNextButton) }
           ){
               Text("Next Screen")
           }
       }
    }
    ```
    - In **App.kt** file, get a childStack by using rootComponent which is passed in argument of App (RootComponent will create in particular module like android and iOS)
    - Using **Children** composable and **childStack** define which screen composable will render as per child instance.
    ```
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
    ```
    - Last step to create root component with specific module android and iOS main,
        - In androidMain, in **MainActivity.kt** create rootComponent using **retainedComponent** method and pass it to **App** composable.
        ```
        class MainActivity : ComponentActivity() {
           override fun onCreate(savedInstanceState: Bundle?) {
               super.onCreate(savedInstanceState)
               val rootComponent = retainedComponent {
                   RootComponent(it)
               }
               setContent {
                   App(rootComponent)
               }
           }
        }
        ```
        - In iOSMain, in **MainController.kt** create rootComponent using **DefaultComponentContext** and pass it to **App** composable.
        ```
        fun MainViewController() = ComposeUIViewController {
           val rootComponent = remember {
               RootComponent(DefaultComponentContext(LifecycleRegistry()))
           }
           App(rootComponent)
        }
        ```


## Results:
![kmp_android](https://github.com/dev-tecocraft/KMP-Kotlin-Multiplatform-/assets/120561602/4e7139eb-c885-4c54-b57f-da1da3feebd2) ![kmp_iOS](https://github.com/dev-tecocraft/KMP-Kotlin-Multiplatform-/assets/120561602/33a4055a-4cf6-4917-9332-16862d63a11e)
