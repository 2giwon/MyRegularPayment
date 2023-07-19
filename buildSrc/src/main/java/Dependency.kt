object Version {
    const val KOTLIN = "1.7.10"
    const val HILT = "2.43.2"
    const val COROUTINE = "1.6.4"
    const val BILLING_CLIENT = "4.1.0"
}

object TestDependency {
    private const val JUNIT_VERSION = "5.8.2"

    const val JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:$JUNIT_VERSION"
    const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:$JUNIT_VERSION"
    const val JUNIT_JUPITER_PARAMS = "org.junit.jupiter:junit-jupiter-params:$JUNIT_VERSION"
    const val ASSERTJ_CORE = "org.assertj:assertj-core:3.21.0"
    const val MOCKK = "io.mockk:mockk:1.12.1"
}

object AndroidXDependency {
    const val CORE_KTX = "androidx.core:core-ktx:1.7.0"
    const val CONSTRAINTLAYOUT = "androidx.constraintlayout:constraintlayout:2.1.2"
    const val ACTIVITY_KTX = "androidx.activity:activity-ktx:1.5.1"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:1.5.2"
    const val LIFECYCLE_LIVEDATA_KTX = "androidx.lifecycle:lifecycle-livedata-ktx:2.5.1"
    const val RECYCLERVIEW = "androidx.recyclerview:recyclerview:1.2.0"
}

object BasicDependency {
    const val MATERIAL = "com.google.android.material:material:1.2.1"
}

object HiltDependency {
    const val ANDROID = "com.google.dagger:hilt-android:${Version.HILT}"
    const val COMPILER = "com.google.dagger:hilt-compiler:${Version.HILT}"
}

object CoroutineDependency {
    const val CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINE}"
}

object PaymentDependency {
    const val BILLING_CLIENT = "com.android.billingclient:billing:${Version.BILLING_CLIENT}"
    const val BILLING_CLIENT_KTX = "com.android.billingclient:billing-ktx:${Version.BILLING_CLIENT}"
}

object EtcDependency {
    const val TIMBER = "com.jakewharton.timber:timber:5.0.1"
}
