## Shimmer Layout

[![android library](https://img.shields.io/badge/version-v1.0.0-orange)](https://github.com/UdaraWanasinghe/android-shimmer-layout)

Shimmer loading animation for android views.

## Screen Records
<img src="https://github.com/UdaraWanasinghe/android-shimmer-layout/blob/main/resources/screen-records/screen-record.gif?raw=true" width=360>

## Building

1. Clone this repository.
    ```shell
    git clone https://github.com/UdaraWanasinghe/android-shimmer-layout
    ```
2. Run `gradle` task `publishToMavenLocal`.
    ```shell
    sh gradlew publishToMavenLocal
    ```

## Using

1. Add the `mavenLocal` repository to your project.
    ```groovy
    // settings.gradle.kts
    dependencyResolutionManagement {
        repositories {
            mavenLocal()
        }
    }
    ```
2. Import the library to your project.
    ```groovy
    // module level build.gradle.kts
    dependencies {
        implementation("com.aureusapps.android:shimmer-layout:1.0.0")
    }
    ```
3. Wrap your views with `ShimmerLayout`.
    ```xml
    <com.aureusapps.android.shimmerlayout.ShimmerLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:shimmerEnabled="true"
        app:shimmerBaseColor="@color/teal_200"
        app:shimmerHighlightColor="@color/purple_500"
        app:shimmerGradientStart="0.33"
        app:shimmerGradientEnd="0.66"
        app:shimmerTilt="0.9"
        app:shimmerXfermode="src_in">

        <TextView
            style="@style/TextAppearance.MaterialComponents.Headline3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/hello_world"
            android:textColor="@color/black" />

    </com.aureusapps.android.shimmerlayout.ShimmerLayout>
   ```

## Appreciate my work!

<a href="https://www.buymeacoffee.com/udarawanasinghe" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>
