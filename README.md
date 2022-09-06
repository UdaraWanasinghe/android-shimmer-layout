## Shimmer Layout

Shimmer loading animation for android views.

## Building

1. Clone this repository.
    ```shell
    git clone https://github.com/UdaraWanasinghe/webp-android
    ```
2. Run `gradle` task `publishToMavenLocal`
    ```shell
    sh gradlew publishToMavenLocal
    ```

## Using

1. Add the `mavenLocal` repository to your project.
    ```groovy
    // settings.gradle
    dependencyResolutionManagement {
        repositories {
            mavenLocal()
        }
    }
    ```
2. Import the library to your project.
    ```groovy
    // module level build.gradle
    dependencies {
        implementation 'com.aureusapps.android:shimmer-layout:1.0.0'
    }
    ```
3. Wrap your views with `ShimmerLayout`.
    ```xml
    <com.aureusapps.android.shimmerlayout.ShimmerLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:shimmerBaseColor="@color/teal_200"
        app:shimmerEnabled="true"
        app:shimmerGradientEnd="0.33"
        app:shimmerGradientStart="0.66"
        app:shimmerHighlightColor="@color/purple_500"
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