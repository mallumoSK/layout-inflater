# layout-inflater

![https://mallumo.jfrog.io/artifactory/gradle-dev-local/tk/mallumo/layout-inflater/](https://img.shields.io/maven-metadata/v?color=%234caf50&metadataUrl=https%3A%2F%2Fmallumo.jfrog.io%2Fartifactory%2Fgradle-dev-local%2Ftk%2Fmallumo%2Flayout-inflater%2Fmaven-metadata.xml&style=for-the-badge "Version")

## About
This library / simbolic processor is alternative to android ![https://developer.android.com/topic/libraries/view-binding]("viewBinding").

## Example of usage
#### XML LAYOUT simple_layout.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/veverka"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!" />

    <include
        android:id="@+id/textTemplate0"
        layout="@layout/simple_layout_2" />
    
    <include
        android:id="@+id/textTemplate1"
        layout="@layout/simple_layout_2" />
</LinearLayout>
```
#### XML LAYOUT simple_layout_2.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/textView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="template" />
```

#### Usage in fragment
* value **layout** is automatic attached to lifecycle
* whole content of **layout** is automatic released after fragment destroyed


```kotlin

class SimpleVM : ViewModel() {
    val flowText = MutableStateFlow("initValue")
}

class SimpleFragment : Fragment() {

    val viewModel by viewModels<SimpleVM>()

    val layout by lazyLayout<LayoutSimpleLayout> { 
        veverka.text = "Replaced text"
        textTemplate0.textView.text = "Included layout"
        flow(viewModel.flowText) { textTemplate1.textView.text = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            repeat(4) {
                delay(2000)
                viewModel.flowText.value = "Text update ($it) ${Date()}"
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = layout.root
    
}

```
P.S: I do not like LiveData because of default value is null, 
in example obowe is used kotlinx.coroutines.flow.MutableStateFlow

## WARNING
This library/processor is ussing [Kotlin Symbol Processor](https://github.com/android/kotlin/tree/ksp/libraries/tools/kotlin-symbol-processing-api).

This is in testing unstable stage.

Usage in production in on your risk.

## Reason
* Android databinding is great but compilation time in multimodular app is enorm.
* Android viewbinding is faster then databinding but still slow.
* Dependency injection is nice but it is messy and app runtime speed fall down.
* Newer ending problems with sources linking (generated sources vs source root).

## What library do
* extract layout resource xml`s.
* generate similiar object as android ViewBinding
* generate extension function for attach object to activity/fragment lifecycle
* generated files are stored in source root
* when layouts are generated, and you currently not expanging or creating new layouts, you cen just disable plugin and generated sources stay untouched. It meand speed up by 100% :D BUT do not forget enable when you make changes in layouts
* after layout changes sometimes compiller throw exception, do not wory, run it again and everithing will be fixed automaticvaly :)

## When are sources generated:
* you can manually ./gradlew :app:generateLayoutInflater
* automatic when project is building


## How to implement

1. define library version (**build.gradle**)
```groovy
buildscript {
    ext.layout_inflater_version = 'x.y.z'
}
```
2. add plugin (**build.gradle**)
```groovy
plugins {
    id("kotlin-ksp") version "1.4.0-dev-experimental-20200914"
}
```

3. apply dependency (**build.gradle**)
```groovy
apply from: 'https://raw.githubusercontent.com/mallumoSK/layout-inflater/main/ksp-config.gradle'
apply from: 'https://raw.githubusercontent.com/mallumoSK/layout-inflater/main/ksp-layout-inflater.gradle'
dependencies {
    ksp implementation("tk.mallumo:layout-inflater:x.y.z")
}
```

4. add pluginResolutionStrategy On top of file **settings.gradle** add this:
```groovy
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if ("kotlin-ksp".equals(requested.id.id)) {
                useModule("org.jetbrains.kotlin:kotlin-ksp:${requested.version}")
            }
            if ("org.jetbrains.kotlin.kotlin-ksp".equals(requested.id.id)) {
                useModule("org.jetbrains.kotlin:kotlin-ksp:${requested.version}")
            }
            if ("org.jetbrains.kotlin.ksp".equals(requested.id.id)) {
                useModule("org.jetbrains.kotlin:kotlin-ksp:${requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        maven {
            url = "https://dl.bintray.com/kotlin/kotlin-eap"
        }
        google()
    }
}
```

5. JOB DONE :)

# More info will bee soon currently is library in development


